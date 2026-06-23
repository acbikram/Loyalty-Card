package com.universalwallet.loyalty.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.universalwallet.loyalty.core.datastore.WalletPreferencesKeys
import com.universalwallet.loyalty.core.theme.ThemeMode
import com.universalwallet.loyalty.core.utils.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observes and persists theme-related preferences (theme mode + dynamic colour)
 * via DataStore, exposing them as hot [StateFlow]s for the UI to collect.
 *
 * This is infrastructure for the theme engine, not feature business logic: it
 * holds no card data and makes no product decisions.
 */
@Singleton
class ThemeStateHolder @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val scope: CoroutineScope,
) {

    val themeMode: StateFlow<ThemeMode> = dataStore.data
        .map { prefs ->
            ThemeMode.fromKey(prefs[WalletPreferencesKeys.THEME_MODE])
        }
        .stateIn(scope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val dynamicColor: StateFlow<Boolean> = dataStore.data
        .map { prefs ->
            prefs[WalletPreferencesKeys.DYNAMIC_COLOR] ?: true
        }
        .stateIn(scope, SharingStarted.Eagerly, true)

    fun setThemeMode(mode: ThemeMode) {
        scope.launch {
            dataStore.edit { it[WalletPreferencesKeys.THEME_MODE] = mode.key }
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        scope.launch {
            dataStore.edit { it[WalletPreferencesKeys.DYNAMIC_COLOR] = enabled }
        }
    }
}
