package com.universalwallet.loyalty.core.theme

import androidx.compose.ui.unit.dp

/**
 * Fixed component dimensions and corner-radius / icon-size tokens. Centralising
 * these guarantees, for example, that every loyalty card uses the same height
 * and every touch target meets the accessibility minimum.
 */
object Dimensions {
    // Component heights
    val cardHeight = 200.dp
    val cardCompactHeight = 88.dp
    val buttonHeight = 52.dp
    val searchBarHeight = 56.dp
    val appBarHeight = 64.dp
    val navigationBarHeight = 80.dp
    val bottomSheetHandleWidth = 32.dp

    // Borders & strokes
    val borderThin = 1.dp
    val borderRegular = 2.dp

    // Minimum accessible touch target (Material / WCAG)
    val minTouchTarget = 48.dp
}

/** Corner-radius tokens, referenced by [WalletShapes] and components. */
object CornerRadius {
    val none = 0.dp
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 20.dp
    val extraLarge = 28.dp
    val full = 1000.dp
}

/** Icon-size tokens. */
object IconSize {
    val small = 16.dp
    val medium = 24.dp
    val large = 32.dp
    val extraLarge = 48.dp
}
