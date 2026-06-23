package com.universalwallet.loyalty

import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard

/** Builds a LoyaltyCard with sensible defaults for engine/manager tests. */
fun card(
    id: String = LoyaltyCard.newId(),
    storeName: String = "Store",
    nickname: String = "",
    cardNumber: String = "1234567890",
    barcodeValue: String = "1234567890",
    barcodeType: BarcodeType = BarcodeType.CODE128,
    category: CardCategory = CardCategory.GENERAL,
    notes: String = "",
    isFavorite: Boolean = false,
    isPinned: Boolean = false,
    isArchived: Boolean = false,
    isHidden: Boolean = false,
    usageCount: Int = 0,
    lastUsedTimestamp: Long = 0L,
    createdAt: Long = 0L,
    imagePath: String? = null,
    sortIndex: Int = 0,
): LoyaltyCard = LoyaltyCard(
    id = id,
    storeId = "store_${storeName.lowercase()}",
    storeName = storeName,
    cardNumber = cardNumber,
    barcodeValue = barcodeValue,
    barcodeType = barcodeType,
    nickname = nickname,
    notes = notes,
    category = category,
    isFavorite = isFavorite,
    lastUsedTimestamp = lastUsedTimestamp,
    createdAt = createdAt,
    updatedAt = createdAt,
    imagePath = imagePath,
    isPinned = isPinned,
    isArchived = isArchived,
    isHidden = isHidden,
    usageCount = usageCount,
    sortIndex = sortIndex,
)
