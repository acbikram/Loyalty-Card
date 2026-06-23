package com.universalwallet.loyalty.core.billing

/**
 * Premium-tier model (Part 6B). This describes *what* a premium entitlement
 * grants; it deliberately contains no billing logic. A future Google Play Billing
 * integration implements [EntitlementProvider] / [BillingGateway] to populate it.
 */
enum class PremiumFeature(val id: String, val title: String) {
    UNLIMITED_CARDS("unlimited_cards", "Unlimited cards"),
    UNLIMITED_WIDGETS("unlimited_widgets", "Unlimited widgets"),
    ADVANCED_THEMES("advanced_themes", "Advanced themes"),
    CLOUD_SYNC("cloud_sync", "Cloud sync"),
    FAMILY_SHARING("family_sharing", "Family sharing"),
}

/** Where an entitlement was established. */
enum class EntitlementSource { NONE, LOCAL_OVERRIDE, PLAY_BILLING }

/**
 * The user's current entitlement snapshot. Free users get an empty feature set.
 */
data class Entitlement(
    val isPremium: Boolean = false,
    val features: Set<PremiumFeature> = emptySet(),
    val source: EntitlementSource = EntitlementSource.NONE,
) {
    fun has(feature: PremiumFeature): Boolean = isPremium && feature in features

    companion object {
        val FREE = Entitlement()
        fun fullPremium(source: EntitlementSource) =
            Entitlement(isPremium = true, features = PremiumFeature.entries.toSet(), source = source)
    }
}

/**
 * Free-tier limits. Premium removes these caps. Values are product decisions and
 * can be tuned without touching gating logic.
 */
object FreeTierLimits {
    const val MAX_CARDS = 50
    const val MAX_WIDGETS = 1
    const val MAX_CUSTOM_THEMES = 1
}
