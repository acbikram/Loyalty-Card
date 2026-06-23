package com.universalwallet.loyalty.core.billing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.universalwallet.loyalty.core.datastore.WalletPreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default [EntitlementProvider]: reads a locally-persisted premium flag. The app
 * ships free; this keeps the dependency graph closed and lets Developer Mode (or
 * a future billing provider) flip premium on. It performs no network or billing
 * calls.
 */
@Singleton
class LocalEntitlementProvider @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : EntitlementProvider {

    override fun entitlement(): Flow<Entitlement> = dataStore.data.map { prefs ->
        val premium = prefs[WalletPreferencesKeys.PREMIUM_UNLOCKED] ?: false
        if (premium) Entitlement.fullPremium(EntitlementSource.LOCAL_OVERRIDE) else Entitlement.FREE
    }

    override suspend fun refresh() = Unit

    /** Local override used by Developer Mode / testing. */
    suspend fun setLocalPremium(enabled: Boolean) {
        dataStore.edit { it[WalletPreferencesKeys.PREMIUM_UNLOCKED] = enabled }
    }
}
