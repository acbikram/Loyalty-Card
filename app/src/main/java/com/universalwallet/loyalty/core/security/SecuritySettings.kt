package com.universalwallet.loyalty.core.security

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.universalwallet.loyalty.core.datastore.WalletPreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed persistence for all security preferences. The PIN is stored
 * only as a salted hash (never the PIN itself), and even the hash/salt live here
 * rather than in plaintext app state. The entire config is derived from a single
 * preferences snapshot, avoiding multi-flow combination.
 */
@Singleton
class SecuritySettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val config: Flow<SecurityConfig> = dataStore.data.map { p ->
        SecurityConfig(
            appLockEnabled = p[WalletPreferencesKeys.APP_LOCK_ENABLED] ?: false,
            authMethod = p[WalletPreferencesKeys.AUTH_METHOD]
                ?.let { runCatching { AuthMethod.valueOf(it) }.getOrNull() }
                ?: AuthMethod.BIOMETRIC,
            autoLockTimeoutMs = p[WalletPreferencesKeys.AUTO_LOCK_TIMEOUT_MS] ?: SecurityConfig.DEFAULT_TIMEOUT_MS,
            lockOnBackground = p[WalletPreferencesKeys.LOCK_ON_BACKGROUND] ?: true,
            requireAuthOnLaunch = p[WalletPreferencesKeys.REQUIRE_AUTH_ON_LAUNCH] ?: true,
            screenshotProtection = p[WalletPreferencesKeys.SCREENSHOT_PROTECTION] ?: false,
            clipboardProtection = p[WalletPreferencesKeys.CLIPBOARD_PROTECTION] ?: false,
            developerModeEnabled = p[WalletPreferencesKeys.DEVELOPER_MODE_ENABLED] ?: false,
            debugLogging = p[WalletPreferencesKeys.DEBUG_LOGGING] ?: false,
            hasPin = p[WalletPreferencesKeys.PIN_HASH] != null,
        )
    }

    suspend fun current(): SecurityConfig = config.first()

    suspend fun setAppLockEnabled(value: Boolean) = put { it[WalletPreferencesKeys.APP_LOCK_ENABLED] = value }
    suspend fun setAuthMethod(method: AuthMethod) = put { it[WalletPreferencesKeys.AUTH_METHOD] = method.name }
    suspend fun setAutoLockTimeout(ms: Long) = put { it[WalletPreferencesKeys.AUTO_LOCK_TIMEOUT_MS] = ms }
    suspend fun setLockOnBackground(value: Boolean) = put { it[WalletPreferencesKeys.LOCK_ON_BACKGROUND] = value }
    suspend fun setRequireAuthOnLaunch(value: Boolean) = put { it[WalletPreferencesKeys.REQUIRE_AUTH_ON_LAUNCH] = value }
    suspend fun setScreenshotProtection(value: Boolean) = put { it[WalletPreferencesKeys.SCREENSHOT_PROTECTION] = value }
    suspend fun setClipboardProtection(value: Boolean) = put { it[WalletPreferencesKeys.CLIPBOARD_PROTECTION] = value }
    suspend fun setDeveloperMode(value: Boolean) = put { it[WalletPreferencesKeys.DEVELOPER_MODE_ENABLED] = value }
    suspend fun setDebugLogging(value: Boolean) = put { it[WalletPreferencesKeys.DEBUG_LOGGING] = value }

    suspend fun savePin(hash: String, salt: String) = put {
        it[WalletPreferencesKeys.PIN_HASH] = hash
        it[WalletPreferencesKeys.PIN_SALT] = salt
    }

    suspend fun clearPin() = put {
        it.remove(WalletPreferencesKeys.PIN_HASH)
        it.remove(WalletPreferencesKeys.PIN_SALT)
    }

    suspend fun pinHash(): String? = dataStore.data.first()[WalletPreferencesKeys.PIN_HASH]
    suspend fun pinSalt(): String? = dataStore.data.first()[WalletPreferencesKeys.PIN_SALT]

    private suspend fun put(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit { prefs -> block(prefs) }
    }
}
