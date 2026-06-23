package com.universalwallet.loyalty.core.ui

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Blurs content only where the platform supports it (API 31+, RenderEffect).
 * On older devices it is a no-op, so callers get a graceful fallback rather than
 * a broken effect. Used for premium "glass" overlays behind dialogs/sheets.
 */
fun Modifier.glassBlur(radius: Dp = 16.dp): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) this.blur(radius) else this

/**
 * A frosted-glass surface: a translucent, softly elevated rounded panel. True
 * backdrop blur (blurring what's behind) requires API 31+ RenderEffect on the
 * parent and is layered in via [glassBlur]; this surface itself stays crisp and
 * legible on every API level.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        tonalElevation = 3.dp,
    ) {
        Box(content = { content() })
    }
}
