package com.universalwallet.loyalty.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons

private val openSourceLibraries = listOf(
    "Jetpack Compose & Material 3",
    "AndroidX Navigation Compose",
    "Dagger Hilt",
    "Room",
    "DataStore Preferences",
    "Kotlin Coroutines & Flow",
    "kotlinx.serialization",
    "Coil",
    "Timber",
)

/**
 * Static about screen: application identity, version, developer, and the list
 * of open-source libraries the app is built on. Stateless — no ViewModel needed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    versionName: String = "1.0.0",
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(WalletIcons.Back, contentDescription = "Navigate back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = WalletIcons.Wallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp),
                )
                Text(
                    text = "Universal Loyalty Wallet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.md),
                )
                Text(
                    text = "Version $versionName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = "An offline-first wallet for your loyalty and rewards cards, built for Gulf-market retailers with Arabic and RTL support in mind.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = Spacing.md),
            )

            SectionHeaderInline("Developer")
            Text("Bikram", style = MaterialTheme.typography.bodyLarge)

            SectionHeaderInline("Open-source libraries")
            openSourceLibraries.forEach { lib ->
                Text(
                    text = "•  $lib",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = Spacing.xxs),
                )
            }
        }
    }
}

@Composable
private fun SectionHeaderInline(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.sm),
    )
}

@Preview(showBackground = true)
@Composable
private fun AboutPreview() {
    AppTheme { AboutScreen(onBack = {}) }
}
