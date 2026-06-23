package com.universalwallet.loyalty.data.mapper

import com.universalwallet.loyalty.data.database.LoyaltyCardEntity
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard

/**
 * Pure Entity ↔ Domain mapping for loyalty cards. No Android or Room logic
 * leaks through here; the domain layer never sees an entity.
 */
object LoyaltyCardMapper {

    fun toDomain(entity: LoyaltyCardEntity): LoyaltyCard = LoyaltyCard(
        id = entity.id,
        storeId = entity.storeId,
        storeName = entity.storeName,
        cardNumber = entity.cardNumber,
        barcodeValue = entity.barcodeValue,
        barcodeType = BarcodeType.fromName(entity.barcodeType),
        qrCodeValue = entity.qrCodeValue,
        customerName = entity.customerName,
        nickname = entity.nickname,
        notes = entity.notes,
        category = CardCategory.fromName(entity.category),
        isFavorite = entity.isFavorite,
        lastUsedTimestamp = entity.lastUsedTimestamp,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
        imagePath = entity.imagePath,
        colorThemeId = entity.colorThemeId,
        isPinned = entity.isPinned,
        isArchived = entity.isArchived,
        isHidden = entity.isHidden,
        usageCount = entity.usageCount,
        sortIndex = entity.sortIndex,
    )

    fun toEntity(card: LoyaltyCard): LoyaltyCardEntity = LoyaltyCardEntity(
        id = card.id,
        storeId = card.storeId,
        storeName = card.storeName,
        cardNumber = card.cardNumber,
        barcodeValue = card.barcodeValue,
        barcodeType = card.barcodeType.name,
        qrCodeValue = card.qrCodeValue,
        customerName = card.customerName,
        nickname = card.nickname,
        notes = card.notes,
        category = card.category.name,
        isFavorite = card.isFavorite,
        lastUsedTimestamp = card.lastUsedTimestamp,
        createdAt = card.createdAt,
        updatedAt = card.updatedAt,
        imagePath = card.imagePath,
        colorThemeId = card.colorThemeId,
        isPinned = card.isPinned,
        isArchived = card.isArchived,
        isHidden = card.isHidden,
        usageCount = card.usageCount,
        sortIndex = card.sortIndex,
    )

    fun toDomainList(entities: List<LoyaltyCardEntity>): List<LoyaltyCard> =
        entities.map(::toDomain)
}
