package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.importer.CsvImporter
import com.universalwallet.loyalty.domain.model.BarcodeType
import org.junit.Test

class CsvImporterTest {

    private val importer = CsvImporter()

    @Test
    fun parsesRowsWithHeader() {
        val csv = """
            storeName,cardNumber,barcodeType,nickname,category
            Lulu,12345,EAN13,My Lulu,SUPERMARKET
            Nesto,67890,CODE128,,PHARMACY
        """.trimIndent()
        val cards = importer.parse(csv)
        assertThat(cards).hasSize(2)
        assertThat(cards[0].storeName).isEqualTo("Lulu")
        assertThat(cards[0].cardNumber).isEqualTo("12345")
        assertThat(cards[0].barcodeType).isEqualTo(BarcodeType.EAN13.name)
    }

    @Test
    fun parsesRowsWithoutHeader() {
        val csv = "Carrefour,55555,QR"
        val cards = importer.parse(csv)
        assertThat(cards).hasSize(1)
        assertThat(cards[0].storeName).isEqualTo("Carrefour")
        assertThat(cards[0].barcodeType).isEqualTo(BarcodeType.QR.name)
    }

    @Test
    fun defaultsBarcodeTypeWhenUnknownOrMissing() {
        val csv = "Prime,99999"
        val cards = importer.parse(csv)
        assertThat(cards).hasSize(1)
        assertThat(cards[0].barcodeType).isEqualTo(BarcodeType.CODE128.name)
    }

    @Test
    fun skipsRowsMissingRequiredFields() {
        val csv = "Lulu,12345\n,nope\nNesto,"
        val cards = importer.parse(csv)
        assertThat(cards).hasSize(1)
        assertThat(cards[0].storeName).isEqualTo("Lulu")
    }

    @Test
    fun handlesQuotedFields() {
        val csv = "\"Lulu, Mall\",12345,EAN13"
        val cards = importer.parse(csv)
        assertThat(cards).hasSize(1)
        assertThat(cards[0].storeName).isEqualTo("Lulu, Mall")
    }

    @Test
    fun emptyInputYieldsEmptyList() {
        assertThat(importer.parse("")).isEmpty()
        assertThat(importer.parse("   \n  ")).isEmpty()
    }
}
