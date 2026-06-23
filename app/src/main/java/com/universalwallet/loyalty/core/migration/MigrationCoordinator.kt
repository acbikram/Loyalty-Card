package com.universalwallet.loyalty.core.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.universalwallet.loyalty.core.datastore.WalletPreferencesKeys
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs pending app-data migrations on startup. Reads the stored data version,
 * executes each ordered [DataMigrationStep] whose `fromVersion` is at or beyond
 * it, then persists the new version. Idempotent: with no pending steps it only
 * records the current version.
 */
@Singleton
class MigrationCoordinator @Inject constructor(
    private val registry: MigrationRegistry,
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun runPendingMigrations() {
        val stored = dataStore.data.first()[WalletPreferencesKeys.APP_DATA_VERSION] ?: registry.dataVersion
        if (stored >= registry.dataVersion) {
            persist(registry.dataVersion)
            return
        }
        registry.dataMigrations
            .filter { it.fromVersion >= stored }
            .sortedBy { it.fromVersion }
            .forEach { it.migrate() }
        persist(registry.dataVersion)
    }

    private suspend fun persist(version: Int) {
        dataStore.edit { it[WalletPreferencesKeys.APP_DATA_VERSION] = version }
    }
}
