package com.universalwallet.loyalty.core.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing scale for margins and padding. Composables must reference these
 * tokens instead of hard-coding `.dp` values, so spacing stays consistent and
 * is tunable from a single place.
 */
object Spacing {
    val none = 0.dp
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 48.dp

    /** Default screen edge padding. */
    val screenHorizontal = 16.dp
    val screenVertical = 16.dp
}
