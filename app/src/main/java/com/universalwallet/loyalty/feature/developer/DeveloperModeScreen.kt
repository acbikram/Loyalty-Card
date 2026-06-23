package com.universalwallet.loyalty.feature.developer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.SwitchItem
import com.universalwallet.loyalty.core.components.WalletButton
import com.universalwallet.loyalty.core.components.WalletButtonStyle
import com.universalwallet.loyalty.core.developer.ValidationReport
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.core.ui.ObserveAsEvents
import kotlinx.coroutines.launch

/**
 * Hidden Developer Mode tools, reachable only when Developer Mode is enabled in
 * Security settings. Clearly separated from user features: database inspector,
 * store/plugin validators, demo-card generator, debug-logging toggle, and a
 * runtime performance monitor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperModeScreen(
    onBack: () -> Unit,
    viewModel: DeveloperModeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.messages) { scope.launch { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Developer mode") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(WalletIcons.Back, contentDescription = "Navigate back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Database inspector")
            state.dbStats?.let { stats ->
                InfoLine("Total cards", stats.totalCards.toString())
                InfoLine("Active", stats.activeCards.toString())
                InfoLine("Archived", stats.archivedCards.toString())
                InfoLine("Favourites", stats.favoriteCards.toString())
                InfoLine("Stores", stats.storeCount.toString())
            }
            ActionButton("Refresh stats", viewModel::refreshStats)

            HorizontalDivider()
            SectionHeader("Validators")
            ActionButton("Validate stores", viewModel::validateStores)
            state.storeReport?.let { ReportBlock("Stores", it) }
            ActionButton("Validate plugins", viewModel::validatePlugins)
            state.pluginReport?.let { ReportBlock("Plugins", it) }

            HorizontalDivider()
            SectionHeader("Demo data")
            ActionButton(if (state.isBusy) "Working…" else "Generate 10 demo cards") { viewModel.generateDemoCards(10) }

            HorizontalDivider()
            SectionHeader("Logging")
            SwitchItem(
                title = "Debug logging",
                subtitle = "Verbose logs (never includes card numbers or PINs)",
                icon = WalletIcons.Settings,
                checked = config.debugLogging,
                onCheckedChange = viewModel::setDebugLogging,
            )

            HorizontalDivider()
            SectionHeader("Architecture & sync tools")
            ActionButton("Send test notification", viewModel::sendTestNotification)
            ActionButton("Simulate sync (last-write-wins)", viewModel::simulateSync)
            ActionButton("Validate architecture", viewModel::validateArchitecture)
            state.architectureReport?.let { ReportBlock("Migrations", it) }

            HorizontalDivider()
            SectionHeader("Performance monitor")
            state.memory?.let { mem ->
                InfoLine("Heap used", "${mem.usedMb} MB")
                InfoLine("Heap total", "${mem.totalMb} MB")
                InfoLine("Heap max", "${mem.maxMb} MB")
            }
            ActionButton("Refresh memory", viewModel::refreshMemory)
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ReportBlock(label: String, report: ValidationReport) {
    Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs)) {
        Text(
            text = if (report.isHealthy) "$label: ${report.checked} checked, all healthy" else "$label: ${report.issues.size} issue(s)",
            style = MaterialTheme.typography.bodyMedium,
            color = if (report.isHealthy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
        report.issues.take(5).forEach { issue ->
            Text("• $issue", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    WalletButton(
        text = text,
        onClick = onClick,
        style = WalletButtonStyle.SECONDARY,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
    )
}
