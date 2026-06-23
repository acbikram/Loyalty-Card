package com.universalwallet.loyalty.core.barcode

import android.graphics.Bitmap
import android.graphics.Color
import javax.inject.Inject

/**
 * High-level, crash-safe barcode image generator. Wraps [BarcodeEncoderImpl] and
 * never throws: an unsupported symbology or unencodable value returns `null`, so
 * the UI can fall back to a placeholder. Generation is CPU-only (no Context),
 * so it can run off the main thread from anywhere.
 *
 * For 1D symbologies a wide, short aspect is used; 2D symbologies are square.
 */
class BarcodeGenerator @Inject constructor(
    private val encoder: BarcodeEncoderImpl = BarcodeEncoderImpl(),
) {

    /**
     * Generates a barcode bitmap, or `null` on any failure.
     *
     * @param foreground bar/module colour (default black) — kept dark on a light
     *   background for reliable scanning even in dark mode.
     */
    fun generate(
        content: String,
        symbology: BarcodeSymbology,
        widthPx: Int,
        heightPx: Int,
        foreground: Int = Color.BLACK,
        background: Int = Color.WHITE,
    ): Bitmap? {
        if (content.isBlank() || widthPx <= 0 || heightPx <= 0) return null
        return runCatching {
            encoder.encode(content.trim(), symbology, widthPx, heightPx, foreground, background)
        }.getOrNull()
    }

    /** Suggested pixel height for a 1D symbology given its rendered [widthPx]. */
    fun suggestedHeight(symbology: BarcodeSymbology, widthPx: Int): Int =
        if (BarcodeFormats.isTwoDimensional(symbology)) widthPx else (widthPx / 3).coerceAtLeast(120)
}
