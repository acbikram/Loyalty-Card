package com.universalwallet.loyalty.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.SettingsItem
import com.universalwallet.loyalty.core.components.SwitchItem
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.ThemeMode
import com.universalwallet.loyalty.core.theme.WalletIcons
import kotlinx.coroutines.launch

/** Stateful Settings entry point. */
@Composable
fun SettingsScreen(
    onAbout: () -> Unit,
    onBrowseStores: () -> Unit,
    onArchived: () -> Unit,
    onSecurity: () -> Unit,
    onImportWizard: () -> Unit,
    onNotifications: () -> Unit,
    onExperimental: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    com.universalwallet.loyalty.core.ui.ObserveAsEvents(viewModel.messages) { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(viewModel::exportTo) }
    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let(viewModel::importFrom) }

    SettingsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onThemeModeChange = viewModel::setThemeMode,
        onDynamicColorChange = viewModel::setDynamicColor,
        onSmartWalletChange = viewModel::setSmartWallet,
        onExport = { exportLauncher.launch("loyalty-wallet-backup.json") },
        onImport = { importLauncher.launch("application/json") },
        onArchived = onArchived,
        onSecurity = onSecurity,
        onImportWizard = onImportWizard,
        onNotifications = onNotifications,
        onExperimental = onExperimental,
        onAbout = onAbout,
        onBrowseStores = onBrowseStores,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onSmartWalletChange: (Boolean) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onArchived: () -> Unit,
    onSecurity: () -> Unit,
    onImportWizard: () -> Unit,
    onNotifications: () -> Unit,
    onExperimental: () -> Unit,
    onAbout: () -> Unit,
    onBrowseStores: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var developerMode by rememberSaveable { mutableStateOf(false) }
    val comingSoon: (String) -> Unit = { label ->
        scope.launch { snackbarHostState.showSnackbar("$label is coming in a future update") }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Settings") }) },
    ) { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Appearance")
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            SwitchItem(
                title = "Dynamic colour",
                subtitle = "Use Material You colours (Android 12+)",
                icon = WalletIcons.Palette,
                checked = state.dynamicColor,
                onCheckedChange = onDynamicColorChange,
            )

            HorizontalDivider()
            SectionHeader("Smart Wallet")
            SwitchItem(
                title = "Smart suggestions",
                subtitle = "Surface likely cards based on your usage. Stays on-device.",
                icon = WalletIcons.Star,
                checked = state.smartWalletEnabled,
                onCheckedChange = onSmartWalletChange,
            )

            HorizontalDivider()
            SectionHeader("Data")
            SettingsItem(title = "Export cards", subtitle = "Save a JSON backup", icon = WalletIcons.Share, onClick = onExport)
            SettingsItem(title = "Import cards", subtitle = "Restore from a JSON backup", icon = WalletIcons.Add, onClick = onImport)
            SettingsItem(title = "Import wizard", subtitle = "JSON, CSV, image, encrypted", icon = WalletIcons.Add, onClick = onImportWizard)
            SettingsItem(title = "Archived cards", subtitle = "View and restore", icon = WalletIcons.Card, onClick = onArchived)

            HorizontalDivider()
            SectionHeader("General")
            SettingsItem(title = "Language", subtitle = "English", icon = WalletIcons.Language, onClick = { comingSoon("More languages") })
            SettingsItem(title = "Browse stores", icon = WalletIcons.Store, onClick = onBrowseStores)
            SettingsItem(title = "Notifications", subtitle = "Reminders and alerts", icon = WalletIcons.Settings, onClick = onNotifications)
            SettingsItem(title = "Features & accessibility", subtitle = "Widgets, sync, motion, experimental", icon = WalletIcons.Grid, onClick = onExperimental)

            HorizontalDivider()
            SectionHeader("Privacy & security")
            SettingsItem(title = "Security & App Lock", subtitle = "Biometrics, PIN, encryption, backup", icon = WalletIcons.Lock, onClick = onSecurity)
            SettingsItem(title = "Backup & sync", subtitle = "Coming soon", icon = WalletIcons.Card, enabled = false)
            SettingsItem(title = "Home-screen widgets", subtitle = "Coming soon", icon = WalletIcons.Wallet, enabled = false)

            HorizontalDivider()
            SectionHeader("About")
            SettingsItem(title = "About this app", icon = WalletIcons.Info, onClick = onAbout)
            SettingsItem(title = "Open-source licenses", icon = WalletIcons.Info, onClick = { comingSoon("Licenses") })
            SettingsItem(title = "Privacy policy", icon = WalletIcons.Info, onClick = { comingSoon("Privacy policy") })
            SettingsItem(title = "Terms of service", icon = WalletIcons.Info, onClick = { comingSoon("Terms of service") })
            SwitchItem(
                title = "Developer mode",
                icon = WalletIcons.Settings,
                checked = developerMode,
                onCheckedChange = { developerMode = it },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    AppTheme {
        SettingsContent(
            state = SettingsUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onThemeModeChange = {}, onDynamicColorChange = {}, onSmartWalletChange = {},
            onExport = {}, onImport = {}, onArchived = {}, onSecurity = {}, onImportWizard = {}, onNotifications = {}, onExperimental = {}, onAbout = {}, onBrowseStores = {},
        )
    }
}
