package com.universalwallet.loyalty.core.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Resolves the active [ColorScheme] from the user's [ThemeMode] selection, the
 * dynamic-colour toggle, and the current system dark-mode state.
 *
 * Dynamic colour (Material You) is only available on Android 12 (API 31)+; on
 * older devices the curated brand schemes in [Color.kt] are used instead.
 */
@Composable
internal fun resolveColorScheme(
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    systemInDarkTheme: Boolean,
): ColorScheme {
    val useDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDarkTheme
    }

    val supportsDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    return if (supportsDynamic) {
        val context = LocalContext.current
        if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (useDark) WalletDarkColorScheme else WalletLightColorScheme
    }
}
