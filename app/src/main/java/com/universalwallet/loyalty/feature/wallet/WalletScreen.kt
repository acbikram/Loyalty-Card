package com.universalwallet.loyalty.feature.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.cards.CardTile
import com.universalwallet.loyalty.core.cards.sampleCard
import com.universalwallet.loyalty.core.components.CategoryChipRow
import com.universalwallet.loyalty.core.components.EmptyState
import com.universalwallet.loyalty.core.components.WalletFab
import com.universalwallet.loyalty.core.components.WalletLoadingIndicator
import com.universalwallet.loyalty.core.organize.SortOption
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.core.ui.LayoutMode
import com.universalwallet.loyalty.core.ui.rememberAdaptiveColumns
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard

/** Stateful Wallet entry point: the complete card collection. */
@Composable
fun WalletScreen(
    onCardClick: (String) -> Unit,
    onAddClick: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WalletContent(
        state = state,
        onCardClick = onCardClick,
        onAddClick = onAddClick,
        onCategorySelect = viewModel::selectCategory,
        onToggleLayout = viewModel::toggleLayout,
        onSetSort = viewModel::setSort,
        onToggleFavorites = viewModel::toggleFavoritesOnly,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletContent(
    state: WalletUiState,
    onCardClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onCategorySelect: (CardCategory?) -> Unit,
    onToggleLayout: () -> Unit,
    onSetSort: (SortOption) -> Unit,
    onToggleFavorites: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet") },
                actions = {
                    SortMenu(current = state.sortOption, onSelect = onSetSort)
                    IconButton(onClick = onToggleLayout) {
                        Icon(
                            imageVector = if (state.layoutMode == LayoutMode.GRID) WalletIcons.ListView else WalletIcons.Grid,
                            contentDescription = if (state.layoutMode == LayoutMode.GRID) "List view" else "Grid view",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            WalletFab(icon = WalletIcons.Add, contentDescription = "Add a card", onClick = onAddClick)
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> WalletLoadingIndicator()
                state.isEmpty -> EmptyState(
                    title = if (state.favoritesOnly) "No favourites yet" else "Your wallet is empty",
                    description = if (state.favoritesOnly) "Mark cards as favourites to see them here." else "Tap the + button to add your first card.",
                    icon = WalletIcons.Wallet,
                )
                else -> WalletGrid(
                    state = state,
                    onCardClick = onCardClick,
                    onCategorySelect = onCategorySelect,
                    onToggleFavorites = onToggleFavorites,
                )
            }
        }
    }
}

@Composable
private fun SortMenu(current: SortOption, onSelect: (SortOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(WalletIcons.Sort, contentDescription = "Sort")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        SortOption.entries.forEach { option ->
            DropdownMenuItem(
                text = { Text(option.label) },
                onClick = { onSelect(option); expanded = false },
                trailingIcon = {
                    if (option == current) Icon(WalletIcons.Star, contentDescription = null)
                },
            )
        }
    }
}

@Composable
private fun WalletGrid(
    state: WalletUiState,
    onCardClick: (String) -> Unit,
    onCategorySelect: (CardCategory?) -> Unit,
    onToggleFavorites: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val columns = rememberAdaptiveColumns(maxWidth)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = Spacing.sm, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    FilterChip(
                        selected = state.favoritesOnly,
                        onClick = onToggleFavorites,
                        label = { Text("Favourites") },
                    )
                }
            }
            item {
                CategoryChipRow(
                    selected = state.selectedCategory,
                    onSelect = onCategorySelect,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (state.layoutMode == LayoutMode.LIST) {
                items(state.cards, key = { it.id }) { card ->
                    Box(Modifier.padding(horizontal = Spacing.screenHorizontal)) {
                        CardTile(card = card, onClick = { onCardClick(card.id) })
                    }
                }
            } else {
                items(state.cards.chunked(columns)) { rowCards ->
                    GridRow(rowCards = rowCards, columns = columns, onCardClick = onCardClick)
                }
            }
        }
    }
}

@Composable
private fun GridRow(rowCards: List<LoyaltyCard>, columns: Int, onCardClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        rowCards.forEach { card ->
            Box(Modifier.weight(1f)) {
                CardTile(card = card, onClick = { onCardClick(card.id) })
            }
        }
        repeat(columns - rowCards.size) { Spacer(Modifier.weight(1f)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun WalletPreview() {
    AppTheme {
        WalletContent(
            state = WalletUiState(
                isLoading = false,
                cards = listOf(sampleCard("classic"), sampleCard("neon"), sampleCard("luxury")),
            ),
            onCardClick = {}, onAddClick = {}, onCategorySelect = {}, onToggleLayout = {},
            onSetSort = {}, onToggleFavorites = {},
        )
    }
}
