package com.universalwallet.loyalty.core.theme

/**
 * User-selectable theme mode. [SYSTEM] follows the device dark-theme setting.
 */
enum class ThemeMode(val key: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        /** Resolves a stored key back to a [ThemeMode], defaulting to [SYSTEM]. */
        fun fromKey(key: String?): ThemeMode =
            entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}
