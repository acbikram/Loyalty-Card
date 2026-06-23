package com.universalwallet.loyalty.core.plugin

import com.universalwallet.loyalty.core.barcode.BarcodeSymbology
import com.universalwallet.loyalty.core.ui.UiText
import com.universalwallet.loyalty.domain.validation.ValidationResult

/**
 * The always-available fallback plugin used for "custom" cards and for any
 * store id that has no dedicated plugin registered. Its validation is
 * deliberately permissive (any non-blank value up to a sane maximum), and its
 * formatting simply trims surrounding whitespace.
 */
class GenericStorePlugin : StorePluginContract {

    override fun getStoreId(): String = STORE_ID

    override fun getStoreName(): String = "Custom Card"

    override fun getSupportedBarcodeTypes(): List<BarcodeSymbology> = listOf(
        BarcodeSymbology.CODE_128,
        BarcodeSymbology.QR_CODE,
        BarcodeSymbology.EAN_13,
        BarcodeSymbology.PDF_417,
        BarcodeSymbology.AZTEC,
        BarcodeSymbology.DATA_MATRIX,
    )

    override fun getTheme(): StoreTheme = StoreTheme(
        brandColorHex = "#5B5F97",
        onBrandColorHex = "#FFFFFF",
        logoAsset = null,
    )

    override fun getCardTemplate(): CardTemplate = CardTemplate(
        numberLabelEn = "Card Number",
        numberLabelAr = "رقم البطاقة",
        defaultSymbology = BarcodeSymbology.CODE_128,
        numberPattern = ".{1,64}",
        hasPin = false,
    )

    override fun validateCard(cardNumber: String): ValidationResult {
        val trimmed = cardNumber.trim()
        return when {
            trimmed.isEmpty() ->
                ValidationResult.Invalid(UiText.Dynamic("Card number cannot be empty"))
            trimmed.length > MAX_LENGTH ->
                ValidationResult.Invalid(UiText.Dynamic("Card number is too long"))
            else -> ValidationResult.Valid
        }
    }

    override fun formatCard(cardNumber: String): String = cardNumber.trim()

    companion object {
        const val STORE_ID: String = "custom"
        private const val MAX_LENGTH: Int = 64
    }
}
