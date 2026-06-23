package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.data.mapper.LoyaltyCardMapper
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import org.junit.Test

/** Verifies the loyalty-card Entity <-> Domain mapping is lossless. */
class LoyaltyCardMapperTest {

    private fun sampleCard() = LoyaltyCard(
        id = "id-1",
        storeId = "lulu",
        storeName = "Lulu Hypermarket",
        cardNumber = "6291000000001",
        barcodeValue = "6291000000001",
        barcodeType = BarcodeType.EAN13,
        qrCodeValue = null,
        customerName = "Bikram",
        nickname = "Lulu Card",
        notes = "main",
        category = CardCategory.SUPERMARKET,
        isFavorite = true,
        lastUsedTimestamp = 123L,
        createdAt = 100L,
        updatedAt = 110L,
        imagePath = null,
        colorThemeId = "green",
    )

    @Test
    fun domainToEntityToDomain_isLossless() {
        val original = sampleCard()
        val roundTripped = LoyaltyCardMapper.toDomain(LoyaltyCardMapper.toEntity(original))
        assertThat(roundTripped).isEqualTo(original)
    }

    @Test
    fun enumsAreStoredAsNames() {
        val entity = LoyaltyCardMapper.toEntity(sampleCard())
        assertThat(entity.barcodeType).isEqualTo("EAN13")
        assertThat(entity.category).isEqualTo("SUPERMARKET")
    }
}
