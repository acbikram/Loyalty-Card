package com.universalwallet.loyalty.core.wallet

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.universalwallet.loyalty.core.datastore.WalletPreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists whether Smart Wallet (behavioural suggestions) is enabled. Defaults
 * to on; users can disable it. Backed by DataStore — never the network.
 */
@Singleton
class SmartWalletSettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val enabled: Flow<Boolean> = dataStore.data.map {
        it[WalletPreferencesKeys.SMART_WALLET_ENABLED] ?: true
    }

    suspend fun setEnabled(value: Boolean) {
        dataStore.edit { it[WalletPreferencesKeys.SMART_WALLET_ENABLED] = value }
    }
}
