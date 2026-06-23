package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.notifications.NotificationPrefs
import com.universalwallet.loyalty.core.notifications.NotificationType
import org.junit.Test

class NotificationPrefsTest {

    @Test
    fun masterOffBlocksEverything() {
        val prefs = NotificationPrefs(enabled = false)
        NotificationType.entries.forEach { type ->
            assertThat(prefs.isAllowed(type)).isFalse()
        }
    }

    @Test
    fun perTypeTogglesAreRespected() {
        val prefs = NotificationPrefs(enabled = true, backupReminder = false, security = true)
        assertThat(prefs.isAllowed(NotificationType.BACKUP_REMINDER)).isFalse()
        assertThat(prefs.isAllowed(NotificationType.SECURITY_REMINDER)).isTrue()
    }

    @Test
    fun updatesCategoryCoversPluginAndImportExport() {
        val prefs = NotificationPrefs(enabled = true, updates = false)
        assertThat(prefs.isAllowed(NotificationType.NEW_STORE_PLUGIN)).isFalse()
        assertThat(prefs.isAllowed(NotificationType.IMPORT_COMPLETE)).isFalse()
        assertThat(prefs.isAllowed(NotificationType.EXPORT_COMPLETE)).isFalse()
    }

    @Test
    fun defaultsAllowStandardNotifications() {
        val prefs = NotificationPrefs()
        assertThat(prefs.isAllowed(NotificationType.UNUSED_CARDS)).isTrue()
        assertThat(prefs.isAllowed(NotificationType.SECURITY_REMINDER)).isTrue()
    }
}
