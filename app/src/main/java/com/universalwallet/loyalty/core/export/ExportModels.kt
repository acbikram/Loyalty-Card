package com.universalwallet.loyalty.core.export

import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import kotlinx.serialization.Serializable

/** Versioned wallet export envelope. */
@Serializable
data class WalletExport(
    val version: Int = CURRENT_VERSION,
    val exportedAt: Long,
    val cards: List<CardExport>,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

/**
 * Portable card representation. Deliberately omits the local id, timestamps, and
 * stored image path — those are device-specific and regenerated on import.
 */
@Serializable
data class CardExport(
    val storeId: String,
    val storeName: String,
    val cardNumber: String,
    val barcodeValue: String,
    val barcodeType: String,
    val qrCodeValue: String? = null,
    val customerName: String? = null,
    val nickname: String = "",
    val notes: String = "",
    val category: String = CardCategory.GENERAL.name,
    val isFavorite: Boolean = false,
    val colorThemeId: String = "default",
)

/** Maps a domain card to its portable form. */
fun LoyaltyCard.toExport(): CardExport = CardExport(
    storeId = storeId,
    storeName = storeName,
    cardNumber = cardNumber,
    barcodeValue = barcodeValue,
    barcodeType = barcodeType.name,
    qrCodeValue = qrCodeValue,
    customerName = customerName,
    nickname = nickname,
    notes = notes,
    category = category.name,
    isFavorite = isFavorite,
    colorThemeId = colorThemeId,
)

/** Rehydrates an imported card into a fresh domain card (new id + timestamps). */
fun CardExport.toDomain(now: Long): LoyaltyCard = LoyaltyCard(
    id = LoyaltyCard.newId(),
    storeId = storeId,
    storeName = storeName,
    cardNumber = cardNumber,
    barcodeValue = barcodeValue,
    barcodeType = BarcodeType.fromName(barcodeType),
    qrCodeValue = qrCodeValue,
    customerName = customerName,
    nickname = nickname,
    notes = notes,
    category = CardCategory.fromName(category),
    isFavorite = isFavorite,
    createdAt = now,
    updatedAt = now,
    colorThemeId = colorThemeId,
)
