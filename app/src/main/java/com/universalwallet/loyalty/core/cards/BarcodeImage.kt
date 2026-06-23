package com.universalwallet.loyalty.core.cards

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.universalwallet.loyalty.core.barcode.BarcodeFormats
import com.universalwallet.loyalty.core.barcode.BarcodeSymbology
import com.universalwallet.loyalty.core.barcode.ZxingBarcodeEncoder
import com.universalwallet.loyalty.core.theme.CornerRadius
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Renders a real, scannable barcode for [content] using the ZXing encoder.
 * Encoding runs off the main thread via [produceState] and re-runs when the
 * content, symbology, or measured size changes. Always drawn black-on-white for
 * maximum scanner reliability, regardless of app theme. Falls back to the
 * deterministic [BarcodePlaceholder] until/if encoding produces a bitmap.
 */
@Composable
fun BarcodeImage(
    content: String,
    symbology: BarcodeSymbology,
    modifier: Modifier = Modifier,
) {
    val is2d = BarcodeFormats.isTwoDimensional(symbology)
    val density = LocalDensity.current
    val fg = Color.Black.toArgb()
    val bg = Color.White.toArgb()

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(CornerRadius.small))
            .background(Color.White)
            .aspectRatio(if (is2d) 1f else 3f),
        contentAlignment = Alignment.Center,
    ) {
        val widthPx = with(density) { maxWidth.toPx() }.toInt().coerceAtLeast(1)
        val heightPx = if (is2d) widthPx else (widthPx / 3).coerceAtLeast(1)

        val bitmap by produceState<Bitmap?>(null, content, symbology, widthPx, heightPx) {
            value = withContext(Dispatchers.Default) {
                ZxingBarcodeEncoder().encodeOrNull(content, symbology, widthPx, heightPx, fg, bg)
            }
        }

        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = "Barcode for $content",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Barcode for $content" },
            )
        } else {
            BarcodePlaceholder(value = content, modifier = Modifier.fillMaxSize())
        }
    }
}
