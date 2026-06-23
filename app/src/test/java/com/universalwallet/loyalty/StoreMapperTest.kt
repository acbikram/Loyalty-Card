package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.data.mapper.StoreMapper
import com.universalwallet.loyalty.data.model.StoreDefinitionDto
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import org.junit.Test

/** Verifies DTO->Domain parsing and Entity<->Domain list encoding. */
class StoreMapperTest {

    private val dto = StoreDefinitionDto(
        storeId = "lulu",
        storeName = "Lulu Hypermarket",
        category = "SUPERMARKET",
        country = listOf("AE", "SA"),
        keywords = listOf("lulu", "grocery"),
        primaryColor = "#0F8A3C",
        secondaryColor = "#0B6E30",
        darkColor = "#0A4D24",
        lightColor = "#E6F4EA",
        supportedBarcodeTypes = listOf("CODE128", "EAN13", "QR"),
        cardTemplateId = "standard",
        logoAssetPath = null,
        isActive = true,
    )

    @Test
    fun fromDto_parsesEnumsAndLists() {
        val def = StoreMapper.fromDto(dto)
        assertThat(def.category).isEqualTo(CardCategory.SUPERMARKET)
        assertThat(def.supportedBarcodeTypes)
            .containsExactly(BarcodeType.CODE128, BarcodeType.EAN13, BarcodeType.QR).inOrder()
        assertThat(def.country).containsExactly("AE", "SA").inOrder()
    }

    @Test
    fun fromDto_unknownEnum_fallsBack() {
        val def = StoreMapper.fromDto(dto.copy(category = "WHATEVER", supportedBarcodeTypes = listOf("XYZ")))
        assertThat(def.category).isEqualTo(CardCategory.GENERAL)
        assertThat(def.supportedBarcodeTypes).containsExactly(BarcodeType.CODE128)
    }

    @Test
    fun entityRoundTrip_preservesLists() {
        val def = StoreMapper.fromDto(dto)
        val roundTripped = StoreMapper.toDomain(StoreMapper.toEntity(def))
        assertThat(roundTripped).isEqualTo(def)
    }
}
