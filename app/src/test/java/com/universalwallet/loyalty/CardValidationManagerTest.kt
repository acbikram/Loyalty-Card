package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.data.model.DataError
import com.universalwallet.loyalty.data.repository.CardValidationManager
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.model.StoreDefinition
import org.junit.Test

/** Verifies required-field, format, compatibility, and duplicate rules. */
class CardValidationManagerTest {

    private val manager = CardValidationManager()

    private fun card(
        id: String = "1",
        storeId: String = "lulu",
        number: String = "6291000000001",
        type: BarcodeType = BarcodeType.EAN13,
    ) = LoyaltyCard(
        id = id,
        storeId = storeId,
        storeName = "Lulu",
        cardNumber = number,
        barcodeValue = number,
        barcodeType = type,
        category = CardCategory.SUPERMARKET,
        createdAt = 0L,
        updatedAt = 0L,
    )

    private val store = StoreDefinition(
        storeId = "lulu",
        storeName = "Lulu",
        category = CardCategory.SUPERMARKET,
        country = listOf("AE"),
        keywords = emptyList(),
        primaryColor = "#000000",
        secondaryColor = "#000000",
        darkColor = "#000000",
        lightColor = "#FFFFFF",
        supportedBarcodeTypes = listOf(BarcodeType.EAN13, BarcodeType.CODE128),
        cardTemplateId = "standard",
    )

    @Test
    fun validCard_hasNoErrors() {
        assertThat(manager.validate(card(), store, emptyList())).isEmpty()
    }

    @Test
    fun blankNumber_isRequiredError() {
        val errors = manager.validate(card(number = ""), store, emptyList())
        assertThat(errors).isNotEmpty()
        assertThat(errors.any { it is DataError.ValidationError }).isTrue()
    }

    @Test
    fun wrongEan13Length_failsFormat() {
        val errors = manager.validate(card(number = "123"), store, emptyList())
        assertThat(errors.any { it is DataError.ValidationError }).isTrue()
    }

    @Test
    fun unsupportedType_failsCompatibility() {
        val errors = manager.validate(card(type = BarcodeType.PDF417, number = "anything"), store, emptyList())
        assertThat(errors.any { it is DataError.ValidationError }).isTrue()
    }

    @Test
    fun duplicateNumber_failsWithDuplicateError() {
        val existing = listOf(card(id = "other"))
        val errors = manager.validate(card(id = "new"), store, existing)
        assertThat(errors.any { it is DataError.DuplicateError }).isTrue()
    }
}
