package com.universalwallet.loyalty.feature.experimental

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.SwitchItem
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons

/** Features, accessibility, and experimental toggles (Part 5B settings expansion). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalSettingsScreen(
    onBack: () -> Unit,
    viewModel: ExperimentalSettingsViewModel = hiltViewModel(),
) {
    val flags by viewModel.flags.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Features & accessibility") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(WalletIcons.Back, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Surfaces")
            SwitchItem(
                title = "Home-screen widgets",
                subtitle = "Show widget data and allow refresh",
                icon = WalletIcons.Grid,
                checked = flags.widgetsEnabled,
                onCheckedChange = viewModel::setWidgets,
            )
            SwitchItem(
                title = "Wear OS sync",
                subtitle = "Send cards to a paired watch (when available)",
                icon = WalletIcons.Card,
                checked = flags.wearSyncEnabled,
                onCheckedChange = viewModel::setWearSync,
            )
            SwitchItem(
                title = "Cloud sync",
                subtitle = "Multi-device sync (requires sign-in; preview)",
                icon = WalletIcons.Wallet,
                checked = flags.cloudSyncEnabled,
                onCheckedChange = viewModel::setCloudSync,
            )

            HorizontalDivider()
            SectionHeader("Accessibility")
            SwitchItem(
                title = "Reduced motion",
                subtitle = "Minimise animations and transitions",
                icon = WalletIcons.Settings,
                checked = flags.reducedMotion,
                onCheckedChange = viewModel::setReducedMotion,
            )

            HorizontalDivider()
            SectionHeader("Experimental")
            SwitchItem(
                title = "Experimental features",
                subtitle = "Opt in to early, unfinished features",
                icon = WalletIcons.Star,
                checked = flags.experimentalFeatures,
                onCheckedChange = viewModel::setExperimental,
            )
            Text(
                text = "Experimental features may change or be removed. Cloud and Wear sync are architecture previews in this build.",
                modifier = Modifier.fillMaxWidth().padding(Spacing.screenHorizontal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
