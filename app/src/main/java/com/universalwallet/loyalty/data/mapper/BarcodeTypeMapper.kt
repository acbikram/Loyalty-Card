package com.universalwallet.loyalty.data.mapper

import com.universalwallet.loyalty.core.barcode.BarcodeSymbology
import com.universalwallet.loyalty.domain.model.BarcodeType

/**
 * Bridges the domain [BarcodeType] to the rendering/scanning contract enum
 * [BarcodeSymbology] used by the plugin layer.
 */
object BarcodeTypeMapper {

    fun toSymbology(type: BarcodeType): BarcodeSymbology = when (type) {
        BarcodeType.EAN13 -> BarcodeSymbology.EAN_13
        BarcodeType.EAN8 -> BarcodeSymbology.EAN_8
        BarcodeType.CODE128 -> BarcodeSymbology.CODE_128
        BarcodeType.CODE39 -> BarcodeSymbology.CODE_39
        BarcodeType.CODE93 -> BarcodeSymbology.CODE_93
        BarcodeType.UPC -> BarcodeSymbology.UPC_A
        BarcodeType.ITF -> BarcodeSymbology.ITF
        BarcodeType.PDF417 -> BarcodeSymbology.PDF_417
        BarcodeType.QR -> BarcodeSymbology.QR_CODE
        BarcodeType.AZTEC -> BarcodeSymbology.AZTEC
    }

    /**
     * Maps a (possibly scanner-detected) [BarcodeSymbology] back to the domain
     * [BarcodeType]. Symbologies the domain doesn't model distinctly collapse to
     * their closest equivalent: UPC-E to [BarcodeType.UPC] and Data Matrix to
     * [BarcodeType.QR]. The raw value is always preserved on the card.
     */
    fun fromSymbology(symbology: BarcodeSymbology): BarcodeType = when (symbology) {
        BarcodeSymbology.EAN_13 -> BarcodeType.EAN13
        BarcodeSymbology.EAN_8 -> BarcodeType.EAN8
        BarcodeSymbology.CODE_128 -> BarcodeType.CODE128
        BarcodeSymbology.CODE_39 -> BarcodeType.CODE39
        BarcodeSymbology.CODE_93 -> BarcodeType.CODE93
        BarcodeSymbology.UPC_A -> BarcodeType.UPC
        BarcodeSymbology.UPC_E -> BarcodeType.UPC
        BarcodeSymbology.ITF -> BarcodeType.ITF
        BarcodeSymbology.PDF_417 -> BarcodeType.PDF417
        BarcodeSymbology.QR_CODE -> BarcodeType.QR
        BarcodeSymbology.AZTEC -> BarcodeType.AZTEC
        BarcodeSymbology.DATA_MATRIX -> BarcodeType.QR
    }
}
