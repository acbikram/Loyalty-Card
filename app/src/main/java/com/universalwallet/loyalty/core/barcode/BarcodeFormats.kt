package com.universalwallet.loyalty.core.barcode

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat

/**
 * Bridges [BarcodeSymbology] to the two third-party libraries: ZXing
 * [BarcodeFormat] for generation, and ML Kit [Barcode] format constants for
 * scanning. Kept here so the third-party enums never leak into feature code.
 */
object BarcodeFormats {

    /** Maps a symbology to its ZXing writer format. */
    fun toZxing(symbology: BarcodeSymbology): BarcodeFormat = when (symbology) {
        BarcodeSymbology.QR_CODE -> BarcodeFormat.QR_CODE
        BarcodeSymbology.CODE_128 -> BarcodeFormat.CODE_128
        BarcodeSymbology.CODE_39 -> BarcodeFormat.CODE_39
        BarcodeSymbology.CODE_93 -> BarcodeFormat.CODE_93
        BarcodeSymbology.EAN_13 -> BarcodeFormat.EAN_13
        BarcodeSymbology.EAN_8 -> BarcodeFormat.EAN_8
        BarcodeSymbology.UPC_A -> BarcodeFormat.UPC_A
        BarcodeSymbology.UPC_E -> BarcodeFormat.UPC_E
        BarcodeSymbology.PDF_417 -> BarcodeFormat.PDF_417
        BarcodeSymbology.AZTEC -> BarcodeFormat.AZTEC
        BarcodeSymbology.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        BarcodeSymbology.ITF -> BarcodeFormat.ITF
    }

    /** True for 2D matrix symbologies, which render as squares. */
    fun isTwoDimensional(symbology: BarcodeSymbology): Boolean = when (symbology) {
        BarcodeSymbology.QR_CODE,
        BarcodeSymbology.PDF_417,
        BarcodeSymbology.AZTEC,
        BarcodeSymbology.DATA_MATRIX -> true
        else -> false
    }

    /** The set of ML Kit formats this app recognises, OR-ed for scanner options. */
    val mlkitFormatMask: Int =
        Barcode.FORMAT_QR_CODE or
            Barcode.FORMAT_CODE_128 or
            Barcode.FORMAT_CODE_39 or
            Barcode.FORMAT_CODE_93 or
            Barcode.FORMAT_EAN_13 or
            Barcode.FORMAT_EAN_8 or
            Barcode.FORMAT_UPC_A or
            Barcode.FORMAT_UPC_E or
            Barcode.FORMAT_PDF417 or
            Barcode.FORMAT_AZTEC or
            Barcode.FORMAT_DATA_MATRIX or
            Barcode.FORMAT_ITF

    /** Maps an ML Kit detected format to a [BarcodeSymbology], or null if unknown. */
    fun fromMlKit(format: Int): BarcodeSymbology? = when (format) {
        Barcode.FORMAT_QR_CODE -> BarcodeSymbology.QR_CODE
        Barcode.FORMAT_CODE_128 -> BarcodeSymbology.CODE_128
        Barcode.FORMAT_CODE_39 -> BarcodeSymbology.CODE_39
        Barcode.FORMAT_CODE_93 -> BarcodeSymbology.CODE_93
        Barcode.FORMAT_EAN_13 -> BarcodeSymbology.EAN_13
        Barcode.FORMAT_EAN_8 -> BarcodeSymbology.EAN_8
        Barcode.FORMAT_UPC_A -> BarcodeSymbology.UPC_A
        Barcode.FORMAT_UPC_E -> BarcodeSymbology.UPC_E
        Barcode.FORMAT_PDF417 -> BarcodeSymbology.PDF_417
        Barcode.FORMAT_AZTEC -> BarcodeSymbology.AZTEC
        Barcode.FORMAT_DATA_MATRIX -> BarcodeSymbology.DATA_MATRIX
        Barcode.FORMAT_ITF -> BarcodeSymbology.ITF
        else -> null
    }
}
