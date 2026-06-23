package com.universalwallet.loyalty.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 shape scale. Corner-radius token values are centralised in
 * [CornerRadius]; this object maps them onto the M3 [Shapes] slots.
 */
val WalletShapes = Shapes(
    extraSmall = RoundedCornerShape(CornerRadius.extraSmall),
    small = RoundedCornerShape(CornerRadius.small),
    medium = RoundedCornerShape(CornerRadius.medium),
    large = RoundedCornerShape(CornerRadius.large),
    extraLarge = RoundedCornerShape(CornerRadius.extraLarge),
)
