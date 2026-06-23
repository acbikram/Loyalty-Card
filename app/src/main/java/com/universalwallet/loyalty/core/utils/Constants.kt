package com.universalwallet.loyalty.core.utils

/**
 * Centralised, app-wide constant values. Feature-specific constants belong in
 * their own feature package; only truly global values live here.
 */
object Constants {

    /** Room database file name. The schema itself is created in a later phase. */
    const val DATABASE_NAME: String = "universal_loyalty_wallet.db"

    /** DataStore (preferences) file name for non-sensitive user settings. */
    const val PREFERENCES_NAME: String = "wallet_preferences"

    /** Encrypted SharedPreferences file name for sensitive flags. */
    const val SECURE_PREFS_NAME: String = "secure_prefs"

    /** Bundled store-definition catalog path within the assets directory. */
    const val STORE_CATALOG_ASSET: String = "stores/stores.json"

    /** Assets sub-directory scanned for per-store JSON definition files. */
    const val STORE_ASSETS_DIR: String = "stores"

    /** Default session-timeout (auto-lock) duration, in milliseconds. */
    const val DEFAULT_SESSION_TIMEOUT_MS: Long = 60_000L
}
