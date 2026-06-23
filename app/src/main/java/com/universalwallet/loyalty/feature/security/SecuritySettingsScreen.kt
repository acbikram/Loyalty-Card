package com.universalwallet.loyalty.feature.security

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.SettingsItem
import com.universalwallet.loyalty.core.components.SwitchItem
import com.universalwallet.loyalty.core.export.ConflictPolicy
import com.universalwallet.loyalty.core.security.AuthMethod
import com.universalwallet.loyalty.core.security.SecurityConfig
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.core.ui.ObserveAsEvents
import kotlinx.coroutines.launch

/** Stateful Security settings entry point. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onBack: () -> Unit,
    onOpenDeveloper: () -> Unit,
    viewModel: SecuritySettingsViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.messages) { scope.launch { snackbarHostState.showSnackbar(it) } }

    var showPinDialog by remember { mutableStateOf(false) }
    var showExportPwd by remember { mutableStateOf(false) }
    var showRestorePwd by remember { mutableStateOf(false) }
    var pendingPassword by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.exportBackup(it, pendingPassword.ifBlank { null }) } }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.loadRestorePreview(it, pendingPassword.ifBlank { null }) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Security") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(WalletIcons.Back, contentDescription = "Navigate back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("App Lock")
            SwitchItem(
                title = "App Lock",
                subtitle = "Require authentication to open the wallet",
                icon = WalletIcons.Lock,
                checked = config.appLockEnabled,
                onCheckedChange = viewModel::setAppLock,
            )
            if (config.appLockEnabled) {
                SwitchItem(
                    title = "Require on launch",
                    icon = WalletIcons.Lock,
                    checked = config.requireAuthOnLaunch,
                    onCheckedChange = viewModel::setRequireAuthOnLaunch,
                )
                SwitchItem(
                    title = "Lock when sent to background",
                    icon = WalletIcons.Lock,
                    checked = config.lockOnBackground,
                    onCheckedChange = viewModel::setLockOnBackground,
                )
                AuthMethodRow(config = config, biometricAvailable = state.biometricAvailable, onSelect = viewModel::setAuthMethod)
                TimeoutRow(current = config.autoLockTimeoutMs, onSelect = viewModel::setAutoLockTimeout)
            }

            HorizontalDivider()
            SectionHeader("PIN")
            SettingsItem(
                title = if (config.hasPin) "Change PIN" else "Set a PIN",
                subtitle = "A numeric PIN as a fallback unlock method",
                icon = WalletIcons.Lock,
                onClick = { showPinDialog = true },
            )
            if (config.hasPin) {
                SettingsItem(title = "Remove PIN", icon = WalletIcons.Delete, onClick = viewModel::clearPin)
            }

            HorizontalDivider()
            SectionHeader("Privacy")
            SwitchItem(
                title = "Screenshot protection",
                subtitle = "Block screenshots and hide content in the app switcher",
                icon = WalletIcons.Lock,
                checked = config.screenshotProtection,
                onCheckedChange = viewModel::setScreenshotProtection,
            )
            SwitchItem(
                title = "Clipboard protection",
                subtitle = "Auto-clear copied card numbers",
                icon = WalletIcons.Copy,
                checked = config.clipboardProtection,
                onCheckedChange = viewModel::setClipboardProtection,
            )

            HorizontalDivider()
            SectionHeader("Backup")
            SettingsItem(
                title = "Create backup",
                subtitle = "Export all cards to a JSON file (optionally encrypted)",
                icon = WalletIcons.Share,
                onClick = { showExportPwd = true },
            )
            SettingsItem(
                title = "Restore backup",
                subtitle = "Preview before importing",
                icon = WalletIcons.Add,
                onClick = { showRestorePwd = true },
            )

            HorizontalDivider()
            SectionHeader("Developer")
            SwitchItem(
                title = "Developer mode",
                subtitle = "Unlock diagnostic tools",
                icon = WalletIcons.Settings,
                checked = config.developerModeEnabled,
                onCheckedChange = viewModel::setDeveloperMode,
            )
            if (config.developerModeEnabled) {
                SettingsItem(title = "Developer tools", icon = WalletIcons.Settings, onClick = onOpenDeveloper)
            }
        }
    }

    if (showPinDialog) {
        PinInputDialog(
            onConfirm = { pin -> showPinDialog = false; viewModel.setPin(pin) },
            onDismiss = { showPinDialog = false },
        )
    }

    if (showExportPwd) {
        PasswordDialog(
            title = "Backup password (optional)",
            confirmText = "Choose file",
            onConfirm = { pwd -> pendingPassword = pwd; showExportPwd = false; exportLauncher.launch("loyalty-wallet-backup.json") },
            onDismiss = { showExportPwd = false },
        )
    }

    if (showRestorePwd) {
        PasswordDialog(
            title = "Backup password (if encrypted)",
            confirmText = "Choose file",
            onConfirm = { pwd -> pendingPassword = pwd; showRestorePwd = false; importLauncher.launch("application/json") },
            onDismiss = { showRestorePwd = false },
        )
    }

    state.restorePreview?.let { preview ->
        RestorePreviewDialog(
            preview = preview,
            onSkip = { viewModel.confirmRestore(ConflictPolicy.SKIP) },
            onReplace = { viewModel.confirmRestore(ConflictPolicy.REPLACE) },
            onDismiss = viewModel::dismissRestorePreview,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthMethodRow(config: SecurityConfig, biometricAvailable: Boolean, onSelect: (AuthMethod) -> Unit) {
    SectionHeader("Method")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        AuthMethod.entries.forEach { method ->
            val enabled = method != AuthMethod.BIOMETRIC || biometricAvailable
            FilterChip(
                selected = config.authMethod == method,
                onClick = { onSelect(method) },
                enabled = enabled,
                label = { Text(method.label) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeoutRow(current: Long, onSelect: (Long) -> Unit) {
    val options = listOf(0L to "Immediately", 30_000L to "30s", 60_000L to "1 min", 300_000L to "5 min")
    SectionHeader("Auto-lock after")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        options.forEach { (ms, label) ->
            FilterChip(selected = current == ms, onClick = { onSelect(ms) }, label = { Text(label) })
        }
    }
}

@Composable
private fun PinInputDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set PIN") },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 12 && it.all(Char::isDigit)) pin = it },
                label = { Text("New PIN (min 4 digits)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(pin) }, enabled = pin.length >= 4) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PasswordDialog(title: String, confirmText: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (leave blank for none)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(password) }) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun RestorePreviewDialog(
    preview: com.universalwallet.loyalty.core.backup.RestorePreview,
    onSkip: () -> Unit,
    onReplace: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore preview") },
        text = {
            Column {
                Text("New cards: ${preview.newCards.size}")
                Text("Already in wallet: ${preview.conflicts.size}")
                Text("Valid cards: ${preview.validation.validCards} of ${preview.validation.totalCards}")
                if (preview.wasEncrypted) Text("Source: encrypted backup")
                if (preview.validation.issues.isNotEmpty()) {
                    Text(
                        text = preview.validation.issues.take(3).joinToString("\n"),
                        modifier = Modifier.padding(top = Spacing.sm),
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onSkip, enabled = preview.canRestore) { Text("Skip duplicates") } },
        dismissButton = {
            Row {
                TextButton(onClick = onReplace, enabled = preview.canRestore) { Text("Replace") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}
