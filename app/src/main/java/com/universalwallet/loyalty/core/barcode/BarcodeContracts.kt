package com.universalwallet.loyalty.core.barcode

import android.graphics.Bitmap

/**
 * Barcode symbologies the app supports for display and scanning. Each store
 * definition declares which of these it uses.
 */
enum class BarcodeSymbology {
    QR_CODE,
    CODE_128,
    CODE_39,
    CODE_93,
    EAN_13,
    EAN_8,
    UPC_A,
    UPC_E,
    PDF_417,
    AZTEC,
    DATA_MATRIX,
    ITF,
}

/**
 * Contract for rendering a card's value into a scannable image (ZXing-backed).
 * Implementation arrives in the barcode phase.
 */
interface BarcodeEncoder {
    fun encode(
        content: String,
        symbology: BarcodeSymbology,
        widthPx: Int,
        heightPx: Int,
    ): Bitmap
}

/** Result of a single live camera scan (ML Kit-backed). */
data class BarcodeScanResult(
    val rawValue: String,
    val symbology: BarcodeSymbology,
)
