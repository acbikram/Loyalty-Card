package com.universalwallet.loyalty.feature.archive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.components.EmptyState
import com.universalwallet.loyalty.core.components.WalletLoadingIndicator
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.domain.model.LoyaltyCard

/** Stateful archived-cards screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onBack: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archived cards") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(WalletIcons.Back, contentDescription = "Navigate back") }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> WalletLoadingIndicator()
                state.isEmpty -> EmptyState(
                    title = "Nothing archived",
                    description = "Archived cards are hidden from your wallet but kept here so you can restore them.",
                    icon = WalletIcons.Card,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.screenHorizontal),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.sm),
                ) {
                    items(state.cards, key = { it.id }) { card ->
                        ArchivedRow(
                            card = card,
                            onRestore = { viewModel.restore(card.id) },
                            onDelete = { viewModel.delete(card.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivedRow(card: LoyaltyCard, onRestore: () -> Unit, onDelete: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(card.storeName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (card.nickname.isNotBlank()) {
                    Text(card.nickname, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            TextButton(onClick = onRestore) { Text("Restore") }
            IconButton(onClick = onDelete) { Icon(WalletIcons.Delete, contentDescription = "Delete permanently") }
        }
    }
}
