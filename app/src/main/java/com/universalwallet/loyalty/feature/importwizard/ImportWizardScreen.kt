package com.universalwallet.loyalty.feature.importwizard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.SettingsItem
import com.universalwallet.loyalty.core.components.WalletButton
import com.universalwallet.loyalty.core.components.WalletButtonStyle
import com.universalwallet.loyalty.core.export.ConflictPolicy
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.core.ui.ObserveAsEvents
import kotlinx.coroutines.launch

/** Multi-step import wizard with preview and undo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportWizardScreen(
    onBack: () -> Unit,
    viewModel: ImportWizardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showEncryptedPwd by remember { mutableStateOf(false) }
    var pendingPassword by remember { mutableStateOf("") }

    ObserveAsEvents(viewModel.messages) { scope.launch { snackbarHostState.showSnackbar(it) } }

    val jsonLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.selectJson(it, null) }
    }
    val encryptedLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.selectJson(it, pendingPassword.ifBlank { null }) }
    }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.selectCsv(it) }
    }
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.selectImage(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Import wizard") },
                navigationIcon = {
                    IconButton(onClick = { if (state.step == WizardStep.SELECT) onBack() else viewModel.reset() }) {
                        Icon(WalletIcons.Back, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            when (state.step) {
                WizardStep.SELECT -> {
                    SectionHeader("Choose a source")
                    SettingsItem(title = "JSON backup", subtitle = "Plain .json export", icon = WalletIcons.Add, onClick = { jsonLauncher.launch("application/json") })
                    SettingsItem(title = "Encrypted backup", subtitle = "Password-protected backup", icon = WalletIcons.Lock, onClick = { showEncryptedPwd = true })
                    SettingsItem(title = "CSV file", subtitle = "store, number, type, nickname, category", icon = WalletIcons.Add, onClick = { csvLauncher.launch("*/*") })
                    SettingsItem(title = "From image", subtitle = "Detect a barcode in a screenshot/photo", icon = WalletIcons.Scan, onClick = { imageLauncher.launch("image/*") })
                    SettingsItem(title = "Google Wallet", subtitle = "Coming soon", icon = WalletIcons.Card, enabled = false)
                    SettingsItem(title = "Apple Wallet", subtitle = "Coming soon", icon = WalletIcons.Card, enabled = false)
                }

                WizardStep.PREVIEW -> {
                    SectionHeader("Preview")
                    val preview = state.jsonPreview
                    if (preview != null) {
                        InfoText("New cards: ${preview.newCards.size}")
                        InfoText("Already in wallet: ${preview.conflicts.size}")
                        InfoText("Valid: ${preview.validation.validCards} of ${preview.validation.totalCards}")
                        if (preview.wasEncrypted) InfoText("Source: encrypted backup")
                    } else {
                        InfoText("Cards found: ${state.parsedCount}")
                    }
                    WalletButton(
                        text = "Import (skip duplicates)",
                        onClick = { viewModel.confirm(ConflictPolicy.SKIP) },
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
                    )
                    WalletButton(
                        text = "Import (replace duplicates)",
                        onClick = { viewModel.confirm(ConflictPolicy.REPLACE) },
                        style = WalletButtonStyle.SECONDARY,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
                    )
                    WalletButton(
                        text = "Cancel",
                        onClick = viewModel::reset,
                        style = WalletButtonStyle.TEXT,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal),
                    )
                }

                WizardStep.RESULT -> {
                    SectionHeader("Done")
                    InfoText("Added: ${state.addedCount}")
                    InfoText("Skipped: ${state.skippedCount}")
                    if (state.canUndo) {
                        WalletButton(
                            text = "Undo import",
                            onClick = viewModel::undo,
                            style = WalletButtonStyle.SECONDARY,
                            enabled = !state.isBusy,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
                        )
                    }
                    WalletButton(
                        text = "Finish",
                        onClick = { viewModel.reset(); onBack() },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
                    )
                }
            }
        }
    }

    if (showEncryptedPwd) {
        AlertDialog(
            onDismissRequest = { showEncryptedPwd = false },
            title = { Text("Backup password") },
            text = {
                OutlinedTextField(
                    value = pendingPassword,
                    onValueChange = { pendingPassword = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                )
            },
            confirmButton = {
                TextButton(onClick = { showEncryptedPwd = false; encryptedLauncher.launch("application/json") }) { Text("Choose file") }
            },
            dismissButton = { TextButton(onClick = { showEncryptedPwd = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun InfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xxs),
    )
}
