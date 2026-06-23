package com.universalwallet.loyalty.domain.model

import java.util.UUID

/**
 * The central domain model: one stored loyalty / rewards card.
 *
 * This type is framework-agnostic (no Android, no Room) so it can be used and
 * unit-tested anywhere. Timestamps are epoch milliseconds. [cardNumber] is the
 * sensitive field and is encrypted at rest in the data layer (see the security
 * design); everything else is stored in plaintext for fast indexed search.
 */
data class LoyaltyCard(
    val id: String,
    val storeId: String,
    val storeName: String,
    val cardNumber: String,
    val barcodeValue: String,
    val barcodeType: BarcodeType,
    val qrCodeValue: String? = null,
    val customerName: String? = null,
    val nickname: String = "",
    val notes: String = "",
    val category: CardCategory = CardCategory.GENERAL,
    val isFavorite: Boolean = false,
    val lastUsedTimestamp: Long = 0L,
    val createdAt: Long,
    val updatedAt: Long,
    val imagePath: String? = null,
    val colorThemeId: String = "default",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isHidden: Boolean = false,
    val usageCount: Int = 0,
    val sortIndex: Int = 0,
) {
    companion object {
        /** Generates a new unique card id. */
        fun newId(): String = UUID.randomUUID().toString()
    }
}
