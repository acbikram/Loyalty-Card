package com.universalwallet.loyalty.core.barcode

import com.universalwallet.loyalty.domain.model.BarcodeType

/**
 * Pure, framework-free barcode validation and auto-detection. No Android, ZXing,
 * or ML Kit types appear here, so it is fully unit-testable on the JVM.
 *
 * Validation covers character set, length, and — for the numeric symbologies
 * that define one — the mod-10 check digit. The 2D symbologies accept any
 * non-empty payload.
 */
object BarcodeValidator {

    /** Outcome of validating a value against a symbology. */
    sealed interface Result {
        data object Valid : Result
        data class Invalid(val reason: String) : Result

        val isValid: Boolean get() = this is Valid
    }

    /** Trims surrounding whitespace; callers should store the trimmed value. */
    fun normalize(value: String): String = value.trim()

    /**
     * Validates [rawValue] against [type], including checksum where applicable.
     */
    fun validate(type: BarcodeType, rawValue: String): Result {
        val value = normalize(rawValue)
        if (value.isEmpty()) return Result.Invalid("Value is empty")

        return when (type) {
            BarcodeType.EAN13 -> requireDigits(value, 13) ?: checkMod10(value, firstWeight = 1)
            BarcodeType.EAN8 -> requireDigits(value, 8) ?: checkMod10(value, firstWeight = 3)
            BarcodeType.UPC -> requireDigits(value, 12) ?: checkMod10(value, firstWeight = 3)
            BarcodeType.ITF -> when {
                !value.all { it.isDigit() } -> Result.Invalid("ITF accepts digits only")
                value.length % 2 != 0 -> Result.Invalid("ITF requires an even number of digits")
                else -> Result.Valid
            }
            BarcodeType.CODE39 ->
                if (value.all { it in CODE39_CHARSET }) Result.Valid
                else Result.Invalid("Code 39 contains unsupported characters")
            BarcodeType.CODE93 ->
                if (value.all { it.code in 0..127 }) Result.Valid
                else Result.Invalid("Code 93 contains unsupported characters")
            BarcodeType.CODE128 ->
                if (value.all { it.code in 0..127 }) Result.Valid
                else Result.Invalid("Code 128 supports ASCII characters only")
            // 2D symbologies accept arbitrary non-empty payloads.
            BarcodeType.QR, BarcodeType.PDF417, BarcodeType.AZTEC -> Result.Valid
        }
    }

    /**
     * Best-effort detection of the most likely [BarcodeType] for a raw value,
     * used when the user types a number manually (a live scan already knows its
     * symbology). Falls back to [BarcodeType.CODE128], which encodes any ASCII.
     */
    fun detectType(rawValue: String): BarcodeType {
        val value = normalize(rawValue)
        val allDigits = value.isNotEmpty() && value.all { it.isDigit() }
        return when {
            allDigits && value.length == 13 && validate(BarcodeType.EAN13, value).isValid -> BarcodeType.EAN13
            allDigits && value.length == 12 && validate(BarcodeType.UPC, value).isValid -> BarcodeType.UPC
            allDigits && value.length == 8 && validate(BarcodeType.EAN8, value).isValid -> BarcodeType.EAN8
            allDigits && value.length % 2 == 0 -> BarcodeType.ITF
            value.all { it in CODE39_CHARSET } -> BarcodeType.CODE39
            else -> BarcodeType.CODE128
        }
    }

    // --- helpers ---

    private val CODE39_CHARSET: Set<Char> =
        ("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. \$/+%").toSet()

    private fun requireDigits(value: String, length: Int): Result.Invalid? = when {
        !value.all { it.isDigit() } -> Result.Invalid("Expected digits only")
        value.length != length -> Result.Invalid("Expected $length digits, got ${value.length}")
        else -> null
    }

    /**
     * Validates a trailing mod-10 check digit. Weights alternate [firstWeight]
     * and the complementary weight (1↔3) across the leading digits.
     */
    private fun checkMod10(value: String, firstWeight: Int): Result {
        val digits = value.map { it - '0' }
        val body = digits.dropLast(1)
        val expected = digits.last()
        var sum = 0
        body.forEachIndexed { index, d ->
            val weight = if (index % 2 == 0) firstWeight else (4 - firstWeight)
            sum += d * weight
        }
        val computed = (10 - (sum % 10)) % 10
        return if (computed == expected) Result.Valid
        else Result.Invalid("Checksum mismatch (expected $computed)")
    }
}
