package com.universalwallet.loyalty.core.billing

import kotlinx.coroutines.flow.Flow

/**
 * Source of truth for the user's [Entitlement]. The app reads entitlements only
 * through this interface, so swapping the local fallback for a Play-Billing-backed
 * implementation requires no feature-code changes.
 */
interface EntitlementProvider {
    fun entitlement(): Flow<Entitlement>

    /** Re-query the backing source (no-op for the local provider). */
    suspend fun refresh()
}

/** A purchasable premium product as surfaced by the store. */
data class PremiumProduct(
    val productId: String,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val billingPeriod: BillingPeriod,
)

enum class BillingPeriod { ONE_TIME, MONTHLY, YEARLY }

/** Result of a purchase / restore attempt. */
sealed interface PurchaseResult {
    data class Success(val productId: String) : PurchaseResult
    data object Cancelled : PurchaseResult
    data object Pending : PurchaseResult
    data class Failed(val reason: String) : PurchaseResult
}

/**
 * Gateway to the platform billing client. **Interfaces only — no billing logic
 * ships in this phase.** A future module implements this against Google Play
 * Billing and feeds an [EntitlementProvider].
 */
interface BillingGateway {
    suspend fun queryProducts(): List<PremiumProduct>
    suspend fun purchase(productId: String): PurchaseResult
    suspend fun restorePurchases(): List<String>
    suspend fun acknowledge(productId: String)
}
