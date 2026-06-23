package com.universalwallet.loyalty.core.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.universalwallet.loyalty.core.theme.CornerRadius
import kotlin.math.abs

/**
 * A non-functional barcode rendering — a visual placeholder only. Real barcode
 * generation is intentionally out of scope for this phase; this draws
 * deterministic stripes from the [value] so the same card always looks the same.
 */
@Composable
fun BarcodePlaceholder(
    value: String,
    modifier: Modifier = Modifier,
    barColor: Color = Color.Black,
    background: Color = Color.White,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(CornerRadius.small))
            .background(background)
            .semantics { contentDescription = "Barcode for $value" },
    ) {
        val seed = value.ifEmpty { "0" }
        var x = 6f
        var i = 0
        val maxX = size.width - 6f
        while (x < maxX) {
            val code = seed[i % seed.length].code
            val barWidth = 2f + (abs(code * (i + 1)) % 6)
            val isBar = (code + i) % 2 == 0
            if (isBar) {
                drawRectCompat(barColor, x, barWidth, size.height)
            }
            x += barWidth + 1.5f
            i++
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRectCompat(
    color: Color,
    x: Float,
    width: Float,
    height: Float,
) {
    drawRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(x, height * 0.12f),
        size = androidx.compose.ui.geometry.Size(width, height * 0.76f),
    )
}

/**
 * A non-functional QR placeholder: a deterministic grid of modules derived from
 * [value]. Visual only; not a scannable code.
 */
@Composable
fun QrPlaceholder(
    value: String,
    modifier: Modifier = Modifier,
    moduleColor: Color = Color.Black,
    background: Color = Color.White,
    modules: Int = 21,
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(CornerRadius.small))
            .background(background)
            .semantics { contentDescription = "QR code for $value" }
            .fillMaxSize(),
    ) {
        val seed = value.ifEmpty { "0" }
        val cell = size.minDimension / modules
        for (row in 0 until modules) {
            for (col in 0 until modules) {
                val h = (seed[(row * modules + col) % seed.length].code * (row + 3) * (col + 7))
                val filled = h % 2 == 0
                val finder = isFinder(row, col, modules)
                if (filled || finder) {
                    drawRect(
                        color = moduleColor,
                        topLeft = androidx.compose.ui.geometry.Offset(col * cell, row * cell),
                        size = androidx.compose.ui.geometry.Size(cell, cell),
                    )
                }
            }
        }
    }
}

/** Marks the three QR finder-pattern corners so the placeholder reads as a QR. */
private fun isFinder(row: Int, col: Int, modules: Int): Boolean {
    val inTopLeft = row < 7 && col < 7
    val inTopRight = row < 7 && col >= modules - 7
    val inBottomLeft = row >= modules - 7 && col < 7
    if (!(inTopLeft || inTopRight || inBottomLeft)) return false
    val r = row % modules.coerceAtLeast(1)
    val c = col
    val lr = if (inTopRight) c - (modules - 7) else c
    val lrow = if (inBottomLeft) r - (modules - 7) else r
    val ring = lrow == 0 || lrow == 6 || lr == 0 || lr == 6
    val core = lrow in 2..4 && lr in 2..4
    return ring || core
}
