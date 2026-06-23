package com.universalwallet.loyalty.core.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium adaptive-layout helpers. On expanded screens (tablets, unfolded
 * foldables, landscape) content is capped and centred so lines don't stretch
 * uncomfortably wide; on compact screens it fills the width. Complements
 * [rememberAdaptiveColumns].
 */
fun contentMaxWidth(widthClass: WidthClass): Dp = when (widthClass) {
    WidthClass.COMPACT -> Dp.Unspecified
    WidthClass.MEDIUM -> 640.dp
    WidthClass.EXPANDED -> 840.dp
}

/** Horizontal screen padding that grows on larger screens for breathing room. */
fun adaptiveHorizontalPadding(widthClass: WidthClass): Dp = when (widthClass) {
    WidthClass.COMPACT -> 16.dp
    WidthClass.MEDIUM -> 24.dp
    WidthClass.EXPANDED -> 32.dp
}
