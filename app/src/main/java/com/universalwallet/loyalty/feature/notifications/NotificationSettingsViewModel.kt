package com.universalwallet.loyalty.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.notifications.NotificationContent
import com.universalwallet.loyalty.core.notifications.NotificationEngine
import com.universalwallet.loyalty.core.notifications.NotificationPrefs
import com.universalwallet.loyalty.core.notifications.NotificationSettings
import com.universalwallet.loyalty.core.notifications.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives notification preferences and a "send test" action. */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val settings: NotificationSettings,
    private val engine: NotificationEngine,
) : ViewModel() {

    val prefs: StateFlow<NotificationPrefs> =
        settings.prefs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationPrefs())

    fun setEnabled(value: Boolean) = update { settings.setEnabled(value) }
    fun setBackupReminder(value: Boolean) = update { settings.setBackupReminder(value) }
    fun setUnusedCards(value: Boolean) = update { settings.setUnusedCards(value) }
    fun setSecurity(value: Boolean) = update { settings.setSecurity(value) }
    fun setUpdates(value: Boolean) = update { settings.setUpdates(value) }

    fun sendTest() {
        viewModelScope.launch {
            engine.notify(
                NotificationContent(
                    type = NotificationType.IMPORT_COMPLETE,
                    title = "Test notification",
                    message = "Notifications are working.",
                ),
            )
        }
    }

    private fun update(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
