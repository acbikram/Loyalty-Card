package com.universalwallet.loyalty.core.barcode

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.util.EnumMap
import javax.inject.Inject

/**
 * ZXing-backed implementation of [BarcodeEncoder]. Renders a value into a
 * scannable [Bitmap]. Throws [IllegalArgumentException] for an unsupported
 * symbology and propagates ZXing's `WriterException` when the content is not
 * encodable in the requested symbology (e.g. an EAN-13 with a bad check digit);
 * higher-level callers ([BarcodeGenerator]) convert those into a safe fallback.
 */
class BarcodeEncoderImpl @Inject constructor() : BarcodeEncoder {

    override fun encode(
        content: String,
        symbology: BarcodeSymbology,
        widthPx: Int,
        heightPx: Int,
    ): Bitmap = encode(content, symbology, widthPx, heightPx, Color.BLACK, Color.WHITE)

    /** Colour-aware encode used by the high-level generator. */
    fun encode(
        content: String,
        symbology: BarcodeSymbology,
        widthPx: Int,
        heightPx: Int,
        foreground: Int,
        background: Int,
    ): Bitmap {
        val format = BarcodeFormats.toZxing(symbology)
            ?: throw IllegalArgumentException("Unsupported symbology: $symbology")

        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, if (BarcodeFormats.isTwoDimensional(symbology)) 1 else 8)
        }

        val matrix = MultiFormatWriter().encode(content, format, widthPx, heightPx, hints)
        return matrix.toBitmap(foreground, background)
    }

    private fun BitMatrix.toBitmap(foreground: Int, background: Int): Bitmap {
        val w = width
        val h = height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (this[x, y]) foreground else background
            }
        }
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, w, 0, 0, w, h)
        }
    }
}
