package com.universalwallet.loyalty.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Brand colour palette and the resulting Material 3 light/dark colour schemes.
 *
 * These are the fallback schemes used when dynamic colour is unavailable or
 * disabled. Per-store accent colours (from the JSON store catalog) are layered
 * on top of this base in the card components in a later phase.
 */

// --- Brand seed colours ---
val BrandBlue = Color(0xFF0A4DA2)
val BrandBlueLight = Color(0xFF4F7FD4)
val BrandBlueDark = Color(0xFF062E63)
val BrandTeal = Color(0xFF00897B)
val BrandAmber = Color(0xFFFFB300)
val BrandRed = Color(0xFFD32F2F)
val BrandGreen = Color(0xFF2E7D32)

// --- Neutrals ---
val NeutralSurfaceLight = Color(0xFFFCFCFF)
val NeutralBackgroundLight = Color(0xFFF6F7FB)
val NeutralSurfaceDark = Color(0xFF121316)
val NeutralBackgroundDark = Color(0xFF0C0D10)

val WalletLightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E6FA),
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00251F),
    tertiary = BrandAmber,
    onTertiary = Color(0xFF3A2E00),
    error = BrandRed,
    onError = Color.White,
    background = NeutralBackgroundLight,
    onBackground = Color(0xFF1A1C1E),
    surface = NeutralSurfaceLight,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F),
)

val WalletDarkColorScheme = darkColorScheme(
    primary = BrandBlueLight,
    onPrimary = Color(0xFF00305F),
    primaryContainer = Color(0xFF0E468C),
    onPrimaryContainer = Color(0xFFD8E6FA),
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF005046),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = BrandAmber,
    onTertiary = Color(0xFF3A2E00),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = NeutralBackgroundDark,
    onBackground = Color(0xFFE3E2E6),
    surface = NeutralSurfaceDark,
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
)
