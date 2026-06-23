package com.universalwallet.loyalty.core.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.universalwallet.loyalty.core.barcode.BarcodeFormats
import com.universalwallet.loyalty.core.barcode.BarcodeGenerator
import com.universalwallet.loyalty.core.barcode.BarcodeSymbology
import com.universalwallet.loyalty.core.theme.CornerRadius
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Renders a real, scannable barcode for [value] in [symbology] using the ZXing
 * generator off the main thread. While generating — or if the value cannot be
 * encoded in the requested symbology — it falls back to the deterministic
 * [BarcodePlaceholder] / [QrPlaceholder], so the UI never shows an empty box.
 *
 * Size is taken from the incoming [modifier]'s constraints, so callers control
 * the dimensions (e.g. `Modifier.fillMaxWidth().height(80.dp)` for a 1D code, or
 * `Modifier.size(180.dp)` for a square 2D code).
 */
@Composable
fun BarcodeView(
    value: String,
    symbology: BarcodeSymbology,
    modifier: Modifier = Modifier,
) {
    val generator = remember { BarcodeGenerator() }
    val is2D = BarcodeFormats.isTwoDimensional(symbology)

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(CornerRadius.small))
            .background(Color.White)
            .semantics { contentDescription = "Barcode for $value" },
    ) {
        val widthPx = constraints.maxWidth.takeIf { it in 1..4000 } ?: 600
        val heightPx = constraints.maxHeight.takeIf { it in 1..4000 }
            ?: if (is2D) widthPx else widthPx / 3

        val bitmap by produceState<ImageBitmap?>(
            initialValue = null,
            value, symbology, widthPx, heightPx,
        ) {
            this.value = withContext(Dispatchers.Default) {
                generator.generate(value, symbology, widthPx, heightPx)?.asImageBitmap()
            }
        }

        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else if (is2D) {
            QrPlaceholder(value = value, modifier = Modifier.fillMaxSize())
        } else {
            Box(Modifier.fillMaxSize()) {
                BarcodePlaceholder(value = value)
            }
        }
    }
}
