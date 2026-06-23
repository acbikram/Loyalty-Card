package com.universalwallet.loyalty.core.plugin

import com.universalwallet.loyalty.core.barcode.BarcodeSymbology

/**
 * Describes how a store's card is captured and displayed. This is data, not UI:
 * the rendering layer reads it to decide which barcode to draw, what to label
 * the number field (bilingually), and whether a PIN field is shown.
 *
 * @property numberLabelEn  English label for the card-number field
 * @property numberLabelAr  Arabic label for the card-number field (RTL)
 * @property defaultSymbology default barcode symbology to render
 * @property numberPattern  regex the card number must satisfy
 * @property hasPin         whether this store's card carries a PIN
 */
data class CardTemplate(
    val numberLabelEn: String,
    val numberLabelAr: String,
    val defaultSymbology: BarcodeSymbology,
    val numberPattern: String,
    val hasPin: Boolean = false,
)
