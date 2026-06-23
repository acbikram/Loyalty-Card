package com.universalwallet.loyalty.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.universalwallet.loyalty.core.datastore.WalletPreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Feature flags and experimental/accessibility toggles. */
data class FeatureFlags(
    val widgetsEnabled: Boolean = true,
    val wearSyncEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = false,
    val reducedMotion: Boolean = false,
    val experimentalFeatures: Boolean = false,
    val accentColor: String = "default",
)

/** DataStore-backed feature flags (Part 5B settings expansion). */
@Singleton
class FeatureSettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val flags: Flow<FeatureFlags> = dataStore.data.map { p ->
        FeatureFlags(
            widgetsEnabled = p[WalletPreferencesKeys.WIDGETS_ENABLED] ?: true,
            wearSyncEnabled = p[WalletPreferencesKeys.WEAR_SYNC_ENABLED] ?: false,
            cloudSyncEnabled = p[WalletPreferencesKeys.CLOUD_SYNC_ENABLED] ?: false,
            reducedMotion = p[WalletPreferencesKeys.REDUCED_MOTION] ?: false,
            experimentalFeatures = p[WalletPreferencesKeys.EXPERIMENTAL_FEATURES] ?: false,
            accentColor = p[WalletPreferencesKeys.ACCENT_COLOR] ?: "default",
        )
    }

    suspend fun setWidgetsEnabled(value: Boolean) = put { it[WalletPreferencesKeys.WIDGETS_ENABLED] = value }
    suspend fun setWearSyncEnabled(value: Boolean) = put { it[WalletPreferencesKeys.WEAR_SYNC_ENABLED] = value }
    suspend fun setCloudSyncEnabled(value: Boolean) = put { it[WalletPreferencesKeys.CLOUD_SYNC_ENABLED] = value }
    suspend fun setReducedMotion(value: Boolean) = put { it[WalletPreferencesKeys.REDUCED_MOTION] = value }
    suspend fun setExperimentalFeatures(value: Boolean) = put { it[WalletPreferencesKeys.EXPERIMENTAL_FEATURES] = value }
    suspend fun setAccentColor(value: String) = put { it[WalletPreferencesKeys.ACCENT_COLOR] = value }

    private suspend fun put(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit { prefs -> block(prefs) }
    }
}
