package com.universalwallet.loyalty.core.wear

import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import kotlinx.serialization.Serializable

/*
 * Wear OS architecture (contracts + shared models). The companion `:wear`
 * Gradle module — Wear Compose UI (round/square aware), ambient mode, and the
 * Data Layer client — consumes these. They live in the shared app here so the
 * phone side and a future wear module agree on one serialization contract; no
 * separate module is created in this phase.
 */

/** Minimal, watch-friendly projection of a card (no notes/usage metadata). */
@Serializable
data class WearCardSummary(
    val id: String,
    val storeName: String,
    val nickname: String,
    val barcodeValue: String,
    val barcodeType: String,
    val colorThemeId: String,
)

/** Screen geometry the Wear UI must adapt to. */
enum class WearScreenShape { ROUND, SQUARE }

/** Ambient (always-on, low-power) vs interactive. */
enum class WearAmbientState { ACTIVE, AMBIENT }

/** Watch-side display preferences. */
@Serializable
data class WearSettings(
    val maxBrightnessOnShow: Boolean = true,
    val keepScreenOnWhileViewing: Boolean = true,
    val favoritesOnly: Boolean = true,
)

/**
 * Phone↔watch transport abstraction (over the Wearable Data Layer). The wear
 * module implements the watch side; the phone app implements the sender.
 */
interface WearSyncInterface {
    suspend fun pushCards(cards: List<WearCardSummary>): Result<Unit>
    suspend fun requestCards(): Result<List<WearCardSummary>>
    suspend fun isWatchConnected(): Boolean
}

/** Offline cache contract for cards stored on the watch. */
interface WearCacheContract {
    suspend fun cache(cards: List<WearCardSummary>)
    suspend fun cached(): List<WearCardSummary>
    suspend fun clear()
}

/** Complication data source for a favourite-card watch-face complication. */
interface WearComplicationProvider {
    fun favoriteShortText(): String
    fun favoriteCardId(): String?
}

/** Maps a domain card to its watch projection. */
fun LoyaltyCard.toWearSummary(): WearCardSummary = WearCardSummary(
    id = id,
    storeName = storeName,
    nickname = nickname,
    barcodeValue = barcodeValue,
    barcodeType = barcodeType.name,
    colorThemeId = colorThemeId,
)

/** Reconstructs a (display-only) domain card from a watch projection. */
fun WearCardSummary.toDomain(now: Long): LoyaltyCard = LoyaltyCard(
    id = id,
    storeId = "",
    storeName = storeName,
    cardNumber = barcodeValue,
    barcodeValue = barcodeValue,
    barcodeType = BarcodeType.fromName(barcodeType),
    nickname = nickname,
    createdAt = now,
    updatedAt = now,
    colorThemeId = colorThemeId,
)
