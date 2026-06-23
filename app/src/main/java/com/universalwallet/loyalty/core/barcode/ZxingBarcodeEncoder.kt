package com.universalwallet.loyalty.core.barcode

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import javax.inject.Inject

/**
 * ZXing-backed [BarcodeEncoder]. Renders any supported [BarcodeSymbology] into a
 * crisp [Bitmap] at the requested pixel size — high enough quality for both
 * on-screen display and printing. Foreground/background colours are
 * configurable so a card can render a dark-mode-friendly barcode.
 */
class ZxingBarcodeEncoder @Inject constructor() : BarcodeEncoder {

    override fun encode(
        content: String,
        symbology: BarcodeSymbology,
        widthPx: Int,
        heightPx: Int,
    ): Bitmap = encode(content, symbology, widthPx, heightPx, Color.BLACK, Color.WHITE)

    /** Colour-aware encode used by the on-card renderer for dark-mode support. */
    fun encode(
        content: String,
        symbology: BarcodeSymbology,
        widthPx: Int,
        heightPx: Int,
        foreground: Int,
        background: Int,
    ): Bitmap {
        require(content.isNotEmpty()) { "Barcode content must not be empty" }
        require(widthPx > 0 && heightPx > 0) { "Barcode dimensions must be positive" }
        val hints = mapOf<EncodeHintType, Any>(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = MultiFormatWriter().encode(
            content,
            BarcodeFormats.toZxing(symbology),
            widthPx,
            heightPx,
            hints,
        )
        return matrix.toBitmap(foreground, background)
    }

    /** Non-throwing variant for UI code that should degrade gracefully. */
    fun encodeOrNull(
        content: String,
        symbology: BarcodeSymbology,
        widthPx: Int,
        heightPx: Int,
        foreground: Int = Color.BLACK,
        background: Int = Color.WHITE,
    ): Bitmap? = runCatching {
        encode(content, symbology, widthPx, heightPx, foreground, background)
    }.getOrNull()

    private fun BitMatrix.toBitmap(foreground: Int, background: Int): Bitmap {
        val w = width
        val h = height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (get(x, y)) foreground else background
            }
        }
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, w, 0, 0, w, h)
        }
    }
}
