package com.universalwallet.loyalty.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.universalwallet.loyalty.core.utils.Constants

/**
 * Single DataStore instance for non-sensitive user preferences.
 *
 * Declared as a [Context] extension delegate so there is exactly one instance
 * per process (the recommended pattern). Sensitive values never go here; they
 * belong in encrypted storage introduced in the security phase.
 */
val Context.walletDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.PREFERENCES_NAME,
)

/** Strongly-typed preference keys. Add new keys here as features grow. */
object WalletPreferencesKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val SMART_WALLET_ENABLED = booleanPreferencesKey("smart_wallet_enabled")

    // Security (Part 5A). PIN material is a salted hash — never the PIN itself.
    val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    val AUTH_METHOD = stringPreferencesKey("auth_method")
    val AUTO_LOCK_TIMEOUT_MS = longPreferencesKey("auto_lock_timeout_ms")
    val LOCK_ON_BACKGROUND = booleanPreferencesKey("lock_on_background")
    val REQUIRE_AUTH_ON_LAUNCH = booleanPreferencesKey("require_auth_on_launch")
    val SCREENSHOT_PROTECTION = booleanPreferencesKey("screenshot_protection")
    val CLIPBOARD_PROTECTION = booleanPreferencesKey("clipboard_protection")
    val PIN_HASH = stringPreferencesKey("pin_hash")
    val PIN_SALT = stringPreferencesKey("pin_salt")
    val DEVELOPER_MODE_ENABLED = booleanPreferencesKey("developer_mode_enabled")
    val DEBUG_LOGGING = booleanPreferencesKey("debug_logging")

    // Notifications (Part 5B)
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val NOTIFY_BACKUP_REMINDER = booleanPreferencesKey("notify_backup_reminder")
    val NOTIFY_UNUSED_CARDS = booleanPreferencesKey("notify_unused_cards")
    val NOTIFY_SECURITY = booleanPreferencesKey("notify_security")
    val NOTIFY_UPDATES = booleanPreferencesKey("notify_updates")

    // Feature flags / experimental (Part 5B)
    val WIDGETS_ENABLED = booleanPreferencesKey("widgets_enabled")
    val WEAR_SYNC_ENABLED = booleanPreferencesKey("wear_sync_enabled")
    val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
    val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
    val EXPERIMENTAL_FEATURES = booleanPreferencesKey("experimental_features")
    val ACCENT_COLOR = stringPreferencesKey("accent_color")

    // Migration engine (Part 5B): tracks the app's logical data version.
    val APP_DATA_VERSION = androidx.datastore.preferences.core.intPreferencesKey("app_data_version")

    // Monetization (Part 6B): cached/local entitlement. Real entitlements will be
    // verified by a Google Play Billing provider; this is the local fallback.
    val PREMIUM_UNLOCKED = booleanPreferencesKey("premium_unlocked")

    // Localization (Part 6B): persisted BCP-47 language tag ("" = system default).
    val APP_LANGUAGE = stringPreferencesKey("app_language")
}
