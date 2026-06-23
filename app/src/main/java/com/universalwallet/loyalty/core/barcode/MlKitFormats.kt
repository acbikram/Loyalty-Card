package com.universalwallet.loyalty.core.barcode

import com.google.mlkit.vision.barcode.common.Barcode

/**
 * Maps ML Kit's integer barcode formats to the app's [BarcodeSymbology].
 * Isolated here (rather than in [BarcodeFormats]) so the plain-JVM format
 * mapping and validator stay free of any ML Kit dependency. Returns null for
 * formats the app does not model (e.g. Codabar).
 */
fun mlKitFormatToSymbology(format: Int): BarcodeSymbology? = when (format) {
    Barcode.FORMAT_QR_CODE -> BarcodeSymbology.QR_CODE
    Barcode.FORMAT_CODE_128 -> BarcodeSymbology.CODE_128
    Barcode.FORMAT_CODE_39 -> BarcodeSymbology.CODE_39
    Barcode.FORMAT_CODE_93 -> BarcodeSymbology.CODE_93
    Barcode.FORMAT_EAN_13 -> BarcodeSymbology.EAN_13
    Barcode.FORMAT_EAN_8 -> BarcodeSymbology.EAN_8
    Barcode.FORMAT_UPC_A -> BarcodeSymbology.UPC_A
    Barcode.FORMAT_UPC_E -> BarcodeSymbology.UPC_E
    Barcode.FORMAT_ITF -> BarcodeSymbology.ITF
    Barcode.FORMAT_PDF417 -> BarcodeSymbology.PDF_417
    Barcode.FORMAT_AZTEC -> BarcodeSymbology.AZTEC
    Barcode.FORMAT_DATA_MATRIX -> BarcodeSymbology.DATA_MATRIX
    else -> null
}
