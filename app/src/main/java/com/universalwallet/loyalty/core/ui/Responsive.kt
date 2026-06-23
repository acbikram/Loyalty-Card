package com.universalwallet.loyalty.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Coarse device width buckets derived from available width (no Activity needed). */
enum class WidthClass { COMPACT, MEDIUM, EXPANDED }

/** Classifies an available width into a [WidthClass] using Material breakpoints. */
fun widthClassFor(maxWidth: Dp): WidthClass = when {
    maxWidth < 600.dp -> WidthClass.COMPACT   // phones, portrait
    maxWidth < 840.dp -> WidthClass.MEDIUM    // large phones, small foldables, split-screen
    else -> WidthClass.EXPANDED               // tablets, unfolded foldables, landscape
}

/**
 * Number of columns to use for a card grid at the given width. Scales from a
 * single column on the narrowest phones up to four on a tablet, so the same
 * grid is responsive across phones, foldables, and tablets.
 */
@Composable
fun rememberAdaptiveColumns(maxWidth: Dp): Int = remember(maxWidth) {
    when (widthClassFor(maxWidth)) {
        WidthClass.COMPACT -> if (maxWidth < 360.dp) 1 else 2
        WidthClass.MEDIUM -> 3
        WidthClass.EXPANDED -> 4
    }
}
