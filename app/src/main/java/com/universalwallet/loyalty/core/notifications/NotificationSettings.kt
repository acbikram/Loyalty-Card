package com.universalwallet.loyalty.core.notifications

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.universalwallet.loyalty.core.datastore.WalletPreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Snapshot of notification preferences. */
data class NotificationPrefs(
    val enabled: Boolean = true,
    val backupReminder: Boolean = true,
    val unusedCards: Boolean = true,
    val security: Boolean = true,
    val updates: Boolean = true,
) {
    fun isAllowed(type: NotificationType): Boolean {
        if (!enabled) return false
        return when (type) {
            NotificationType.BACKUP_REMINDER -> backupReminder
            NotificationType.UNUSED_CARDS -> unusedCards
            NotificationType.SECURITY_REMINDER -> security
            NotificationType.NEW_STORE_PLUGIN,
            NotificationType.IMPORT_COMPLETE,
            NotificationType.EXPORT_COMPLETE -> updates
            NotificationType.OFFER, NotificationType.POINTS_EXPIRY -> updates
        }
    }
}

/** DataStore-backed notification preferences. */
@Singleton
class NotificationSettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val prefs: Flow<NotificationPrefs> = dataStore.data.map { p ->
        NotificationPrefs(
            enabled = p[WalletPreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            backupReminder = p[WalletPreferencesKeys.NOTIFY_BACKUP_REMINDER] ?: true,
            unusedCards = p[WalletPreferencesKeys.NOTIFY_UNUSED_CARDS] ?: true,
            security = p[WalletPreferencesKeys.NOTIFY_SECURITY] ?: true,
            updates = p[WalletPreferencesKeys.NOTIFY_UPDATES] ?: true,
        )
    }

    suspend fun current(): NotificationPrefs = prefs.first()

    suspend fun setEnabled(value: Boolean) = edit { it[WalletPreferencesKeys.NOTIFICATIONS_ENABLED] = value }
    suspend fun setBackupReminder(value: Boolean) = edit { it[WalletPreferencesKeys.NOTIFY_BACKUP_REMINDER] = value }
    suspend fun setUnusedCards(value: Boolean) = edit { it[WalletPreferencesKeys.NOTIFY_UNUSED_CARDS] = value }
    suspend fun setSecurity(value: Boolean) = edit { it[WalletPreferencesKeys.NOTIFY_SECURITY] = value }
    suspend fun setUpdates(value: Boolean) = edit { it[WalletPreferencesKeys.NOTIFY_UPDATES] = value }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit { prefs -> block(prefs) }
    }
}
