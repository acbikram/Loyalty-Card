package com.universalwallet.loyalty.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.cards.CardTile
import com.universalwallet.loyalty.core.cards.sampleCard
import com.universalwallet.loyalty.core.components.CategoryChipRow
import com.universalwallet.loyalty.core.components.EmptyState
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.WalletFab
import com.universalwallet.loyalty.core.components.WalletLoadingIndicator
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.core.ui.LayoutMode
import com.universalwallet.loyalty.core.ui.rememberAdaptiveColumns
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import java.time.LocalTime

/** Stateful Home entry point. Collects state and forwards navigation callbacks. */
@Composable
fun HomeScreen(
    onCardClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        onCardClick = onCardClick,
        onAddClick = onAddClick,
        onSearchClick = onSearchClick,
        onCategorySelect = viewModel::selectCategory,
        onToggleLayout = viewModel::toggleLayout,
        onRefresh = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    onCardClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCategorySelect: (CardCategory?) -> Unit,
    onToggleLayout: () -> Unit,
    onRefresh: () -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            WalletFab(
                icon = WalletIcons.Add,
                contentDescription = "Add a card",
                label = "Add card",
                onClick = onAddClick,
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> WalletLoadingIndicator()
                state.isEmpty -> EmptyState(
                    title = "No cards yet",
                    description = "Add your first loyalty card to get started.",
                    icon = WalletIcons.Card,
                )
                else -> HomeList(
                    state = state,
                    onCardClick = onCardClick,
                    onSearchClick = onSearchClick,
                    onCategorySelect = onCategorySelect,
                    onToggleLayout = onToggleLayout,
                )
            }
        }
    }
}

@Composable
private fun HomeList(
    state: HomeUiState,
    onCardClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCategorySelect: (CardCategory?) -> Unit,
    onToggleLayout: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columns = rememberAdaptiveColumns(maxWidth)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp, top = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item { GreetingHeader(layoutMode = state.layoutMode, onToggleLayout = onToggleLayout) }
            item { TappableSearchBar(onClick = onSearchClick) }
            item {
                CategoryChipRow(
                    selected = state.selectedCategory,
                    onSelect = onCategorySelect,
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
                )
            }

            if (state.totalCount > 0) {
                item { StatsSummary(total = state.totalCount, favorites = state.favoriteCount) }
            }

            if (state.smartEnabled && state.suggestions.isNotEmpty()) {
                item { SectionHeader("Smart suggestions") }
                item { HorizontalCardRow(cards = state.suggestions, onCardClick = onCardClick) }
            }

            if (state.favorites.isNotEmpty()) {
                item { SectionHeader("Favourites") }
                item { HorizontalCardRow(cards = state.favorites, onCardClick = onCardClick) }
            }
            if (state.recent.isNotEmpty()) {
                item { SectionHeader("Recent") }
                item { HorizontalCardRow(cards = state.recent, onCardClick = onCardClick) }
            }

            item { SectionHeader("All cards") }
            if (state.layoutMode == LayoutMode.LIST) {
                items(state.all, key = { it.id }) { card ->
                    Box(Modifier.padding(horizontal = Spacing.screenHorizontal)) {
                        CardTile(card = card, onClick = { onCardClick(card.id) })
                    }
                }
            } else {
                items(state.all.chunked(columns)) { rowCards ->
                    GridRow(rowCards = rowCards, columns = columns, onCardClick = onCardClick)
                }
            }
        }
    }
}

@Composable
private fun GreetingHeader(
    layoutMode: LayoutMode,
    onToggleLayout: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = greeting(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Your wallet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = onToggleLayout) {
            Icon(
                imageVector = if (layoutMode == LayoutMode.GRID) WalletIcons.ListView else WalletIcons.Grid,
                contentDescription = if (layoutMode == LayoutMode.GRID) "Switch to list view" else "Switch to grid view",
            )
        }
    }
}

@Composable
private fun TappableSearchBar(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "Search cards",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = Spacing.md),
        )
    }
}

@Composable
private fun HorizontalCardRow(cards: List<LoyaltyCard>, onCardClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        items(cards, key = { it.id }) { card ->
            Box(Modifier.width(280.dp)) {
                CardTile(card = card, onClick = { onCardClick(card.id) })
            }
        }
    }
}

@Composable
private fun GridRow(rowCards: List<LoyaltyCard>, columns: Int, onCardClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        rowCards.forEach { card ->
            Box(Modifier.weight(1f)) {
                CardTile(card = card, onClick = { onCardClick(card.id) })
            }
        }
        // Pad the final row so cards keep a consistent width.
        repeat(columns - rowCards.size) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatsSummary(total: Int, favorites: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        StatPill(label = "Cards", value = total.toString(), modifier = Modifier.weight(1f))
        StatPill(label = "Favourites", value = favorites.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.sm),
            )
        }
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Welcome back"
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    AppTheme {
        HomeContent(
            state = HomeUiState(
                isLoading = false,
                favorites = listOf(sampleCard("luxury")),
                recent = listOf(sampleCard("modern")),
                all = listOf(sampleCard("classic"), sampleCard("neon"), sampleCard("soft")),
            ),
            onCardClick = {}, onAddClick = {}, onSearchClick = {},
            onCategorySelect = {}, onToggleLayout = {}, onRefresh = {},
        )
    }
}
