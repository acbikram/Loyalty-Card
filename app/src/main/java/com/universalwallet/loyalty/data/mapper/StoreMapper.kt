package com.universalwallet.loyalty.data.mapper

import com.universalwallet.loyalty.data.database.StoreEntity
import com.universalwallet.loyalty.data.model.StoreDefinitionDto
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.StoreDefinition

/**
 * Pure mapping for store definitions: JSON DTO → Domain, and Entity ↔ Domain.
 * List fields are delimited via [ListCodec] for storage and split back here.
 */
object StoreMapper {

    fun fromDto(dto: StoreDefinitionDto): StoreDefinition = StoreDefinition(
        storeId = dto.storeId,
        storeName = dto.storeName,
        category = CardCategory.fromName(dto.category),
        country = dto.country,
        keywords = dto.keywords,
        primaryColor = dto.primaryColor,
        secondaryColor = dto.secondaryColor,
        darkColor = dto.darkColor,
        lightColor = dto.lightColor,
        supportedBarcodeTypes = dto.supportedBarcodeTypes.map { BarcodeType.fromName(it) },
        cardTemplateId = dto.cardTemplateId,
        logoAssetPath = dto.logoAssetPath,
        isActive = dto.isActive,
    )

    fun toDomain(entity: StoreEntity): StoreDefinition = StoreDefinition(
        storeId = entity.storeId,
        storeName = entity.storeName,
        category = CardCategory.fromName(entity.category),
        country = ListCodec.decode(entity.country),
        keywords = ListCodec.decode(entity.keywords),
        primaryColor = entity.primaryColor,
        secondaryColor = entity.secondaryColor,
        darkColor = entity.darkColor,
        lightColor = entity.lightColor,
        supportedBarcodeTypes = ListCodec.decode(entity.supportedBarcodeTypes)
            .map { BarcodeType.fromName(it) },
        cardTemplateId = entity.cardTemplateId,
        logoAssetPath = entity.logoAssetPath,
        isActive = entity.isActive,
    )

    fun toEntity(def: StoreDefinition): StoreEntity = StoreEntity(
        storeId = def.storeId,
        storeName = def.storeName,
        category = def.category.name,
        country = ListCodec.encode(def.country),
        keywords = ListCodec.encode(def.keywords),
        primaryColor = def.primaryColor,
        secondaryColor = def.secondaryColor,
        darkColor = def.darkColor,
        lightColor = def.lightColor,
        supportedBarcodeTypes = ListCodec.encode(def.supportedBarcodeTypes.map { it.name }),
        cardTemplateId = def.cardTemplateId,
        logoAssetPath = def.logoAssetPath,
        isActive = def.isActive,
    )

    fun toDomainList(entities: List<StoreEntity>): List<StoreDefinition> =
        entities.map(::toDomain)

    fun toEntityList(defs: List<StoreDefinition>): List<StoreEntity> =
        defs.map(::toEntity)
}
