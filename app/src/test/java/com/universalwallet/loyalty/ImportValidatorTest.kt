package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.backup.ImportValidator
import com.universalwallet.loyalty.core.export.CardExport
import com.universalwallet.loyalty.core.export.WalletExport
import org.junit.Test

class ImportValidatorTest {

    private val validator = ImportValidator()

    private fun export(cards: List<CardExport>, version: Int = WalletExport.CURRENT_VERSION) =
        WalletExport(version = version, exportedAt = 0L, cards = cards)

    @Test
    fun validBackupPasses() {
        val result = validator.validate(
            export(listOf(CardExport(storeId = "lulu", storeName = "Lulu", cardNumber = "123", barcodeValue = "123", barcodeType = "EAN13"))),
        )
        assertThat(result.isValid).isTrue()
        assertThat(result.validCards).isEqualTo(1)
    }

    @Test
    fun missingNumberIsReported() {
        val result = validator.validate(
            export(listOf(CardExport(storeId = "x", storeName = "X", cardNumber = "", barcodeValue = "", barcodeType = "EAN13"))),
        )
        assertThat(result.validCards).isEqualTo(0)
        assertThat(result.issues).isNotEmpty()
    }

    @Test
    fun unknownBarcodeTypeIsReported() {
        val result = validator.validate(
            export(listOf(CardExport(storeId = "x", storeName = "X", cardNumber = "1", barcodeValue = "1", barcodeType = "NOPE"))),
        )
        assertThat(result.validCards).isEqualTo(0)
    }

    @Test
    fun newerVersionIsInvalid() {
        val result = validator.validate(
            export(
                listOf(CardExport(storeId = "x", storeName = "X", cardNumber = "1", barcodeValue = "1", barcodeType = "EAN13")),
                version = WalletExport.CURRENT_VERSION + 1,
            ),
        )
        assertThat(result.isValid).isFalse()
    }

    @Test
    fun emptyBackupIsInvalid() {
        val result = validator.validate(export(emptyList()))
        assertThat(result.isValid).isFalse()
    }
}
