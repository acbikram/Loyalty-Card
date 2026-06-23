package com.universalwallet.loyalty.feature.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.SettingsItem
import com.universalwallet.loyalty.core.components.SwitchItem
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons

/** Notification preferences: master toggle, per-type switches, and a test action. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(WalletIcons.Back, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            SwitchItem(
                title = "Allow notifications",
                subtitle = "Master switch for all notifications",
                icon = WalletIcons.Settings,
                checked = prefs.enabled,
                onCheckedChange = viewModel::setEnabled,
            )

            HorizontalDivider()
            SectionHeader("Reminders")
            SwitchItem(
                title = "Backup reminders",
                icon = WalletIcons.Share,
                checked = prefs.backupReminder,
                onCheckedChange = viewModel::setBackupReminder,
            )
            SwitchItem(
                title = "Unused card reminders",
                icon = WalletIcons.Card,
                checked = prefs.unusedCards,
                onCheckedChange = viewModel::setUnusedCards,
            )

            HorizontalDivider()
            SectionHeader("Alerts")
            SwitchItem(
                title = "Security alerts",
                icon = WalletIcons.Lock,
                checked = prefs.security,
                onCheckedChange = viewModel::setSecurity,
            )
            SwitchItem(
                title = "Updates & results",
                subtitle = "New store plugins, import/export results",
                icon = WalletIcons.Settings,
                checked = prefs.updates,
                onCheckedChange = viewModel::setUpdates,
            )

            HorizontalDivider()
            SettingsItem(
                title = "Send a test notification",
                icon = WalletIcons.Settings,
                onClick = viewModel::sendTest,
                enabled = prefs.enabled,
            )

            Text(
                text = "Tip: you can also manage notifications in Android system settings.",
                modifier = Modifier.fillMaxWidth().padding(Spacing.screenHorizontal),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
