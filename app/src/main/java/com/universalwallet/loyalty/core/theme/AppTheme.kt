package com.universalwallet.loyalty.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * The single Material 3 theme wrapper for the whole application.
 *
 * Every screen is hosted inside this composable, which applies the resolved
 * colour scheme together with the app's [WalletTypography] and [WalletShapes].
 * Spacing, dimension, elevation, motion and icon tokens are plain objects and
 * are read directly by components.
 *
 * @param themeMode    user-selected light/dark/system preference
 * @param dynamicColor whether to use Material You dynamic colour where available
 */
@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = resolveColorScheme(
        themeMode = themeMode,
        dynamicColor = dynamicColor,
        systemInDarkTheme = isSystemInDarkTheme(),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WalletTypography,
        shapes = WalletShapes,
        content = content,
    )
}
