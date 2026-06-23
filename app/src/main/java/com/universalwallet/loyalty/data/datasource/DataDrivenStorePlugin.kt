package com.universalwallet.loyalty.data.datasource

import com.universalwallet.loyalty.core.barcode.BarcodeSymbology
import com.universalwallet.loyalty.core.plugin.CardTemplate
import com.universalwallet.loyalty.core.plugin.StorePluginContract
import com.universalwallet.loyalty.core.plugin.StoreTheme
import com.universalwallet.loyalty.core.ui.UiText
import com.universalwallet.loyalty.data.mapper.BarcodeTypeMapper
import com.universalwallet.loyalty.domain.model.StoreDefinition
import com.universalwallet.loyalty.domain.validation.ValidationResult

/**
 * Adapts a JSON-sourced [StoreDefinition] to the [StorePluginContract] the
 * registry consumes. This is the bridge that turns the data-driven catalogue
 * into first-class plugins — fulfilling the loader seam left open in Part 1C
 * without any hardcoded per-store classes.
 */
class DataDrivenStorePlugin(
    private val definition: StoreDefinition,
) : StorePluginContract {

    override fun getStoreId(): String = definition.storeId

    override fun getStoreName(): String = definition.storeName

    override fun getSupportedBarcodeTypes(): List<BarcodeSymbology> =
        definition.supportedBarcodeTypes.map(BarcodeTypeMapper::toSymbology)

    override fun getTheme(): StoreTheme = StoreTheme(
        brandColorHex = definition.primaryColor,
        onBrandColorHex = "#FFFFFF",
        logoAsset = definition.logoAssetPath,
    )

    override fun getCardTemplate(): CardTemplate = CardTemplate(
        numberLabelEn = "Card Number",
        numberLabelAr = "رقم البطاقة",
        defaultSymbology = getSupportedBarcodeTypes().firstOrNull() ?: BarcodeSymbology.CODE_128,
        numberPattern = ".{1,64}",
        hasPin = false,
    )

    override fun validateCard(cardNumber: String): ValidationResult {
        val trimmed = cardNumber.trim()
        return if (trimmed.isEmpty()) {
            ValidationResult.Invalid(UiText.Dynamic("Card number cannot be empty"))
        } else {
            ValidationResult.Valid
        }
    }

    override fun formatCard(cardNumber: String): String = cardNumber.trim()
}
