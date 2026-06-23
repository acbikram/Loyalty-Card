package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.barcode.BarcodeValidator
import com.universalwallet.loyalty.domain.model.BarcodeType
import org.junit.Test

/** Pure checksum/format/detection tests for [BarcodeValidator]. */
class BarcodeValidatorTest {

    @Test
    fun validEan13_passesChecksum() {
        assertThat(BarcodeValidator.validate(BarcodeType.EAN13, "6291041500213").isValid).isTrue()
    }

    @Test
    fun invalidEan13Checksum_fails() {
        assertThat(BarcodeValidator.validate(BarcodeType.EAN13, "6291041500214").isValid).isFalse()
    }

    @Test
    fun wrongEan13Length_fails() {
        assertThat(BarcodeValidator.validate(BarcodeType.EAN13, "629104150021").isValid).isFalse()
    }

    @Test
    fun validEan8_passesChecksum() {
        assertThat(BarcodeValidator.validate(BarcodeType.EAN8, "96385074").isValid).isTrue()
    }

    @Test
    fun validUpcA_passesChecksum() {
        assertThat(BarcodeValidator.validate(BarcodeType.UPC, "036000291452").isValid).isTrue()
    }

    @Test
    fun itf_requiresEvenDigitCount() {
        assertThat(BarcodeValidator.validate(BarcodeType.ITF, "1234").isValid).isTrue()
        assertThat(BarcodeValidator.validate(BarcodeType.ITF, "123").isValid).isFalse()
    }

    @Test
    fun code39_rejectsLowercase() {
        assertThat(BarcodeValidator.validate(BarcodeType.CODE39, "ABC-123").isValid).isTrue()
        assertThat(BarcodeValidator.validate(BarcodeType.CODE39, "abc").isValid).isFalse()
    }

    @Test
    fun twoDimensional_acceptsAnyNonEmpty() {
        assertThat(BarcodeValidator.validate(BarcodeType.QR, "https://example.com/x").isValid).isTrue()
        assertThat(BarcodeValidator.validate(BarcodeType.QR, "").isValid).isFalse()
    }

    @Test
    fun normalize_trimsWhitespace() {
        assertThat(BarcodeValidator.normalize("  123  ")).isEqualTo("123")
    }

    @Test
    fun detectType_recognisesNumericSymbologies() {
        assertThat(BarcodeValidator.detectType("6291041500213")).isEqualTo(BarcodeType.EAN13)
        assertThat(BarcodeValidator.detectType("036000291452")).isEqualTo(BarcodeType.UPC)
        assertThat(BarcodeValidator.detectType("96385074")).isEqualTo(BarcodeType.EAN8)
    }

    @Test
    fun detectType_fallsBackToCode39ThenCode128() {
        assertThat(BarcodeValidator.detectType("ABC-123")).isEqualTo(BarcodeType.CODE39)
        assertThat(BarcodeValidator.detectType("hello world!")).isEqualTo(BarcodeType.CODE128)
    }
}
