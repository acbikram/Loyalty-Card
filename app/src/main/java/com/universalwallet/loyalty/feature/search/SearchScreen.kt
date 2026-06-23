package com.universalwallet.loyalty.feature.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.cards.CardTile
import com.universalwallet.loyalty.core.cards.sampleCard
import com.universalwallet.loyalty.core.components.CategoryChipRow
import com.universalwallet.loyalty.core.components.EmptyState
import com.universalwallet.loyalty.core.components.WalletSearchBar
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.domain.model.CardCategory

/** Stateful Search entry point. */
@Composable
fun SearchScreen(
    onCardClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchContent(
        state = state,
        onCardClick = onCardClick,
        onQueryChange = viewModel::onQueryChange,
        onClear = viewModel::clearQuery,
        onSubmit = viewModel::commitSearch,
        onRecentClick = viewModel::selectRecent,
        onCategorySelect = viewModel::selectCategory,
        onSortSelect = viewModel::setSort,
    )
}

@Composable
private fun SearchContent(
    state: SearchUiState,
    onCardClick: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    onRecentClick: (String) -> Unit,
    onCategorySelect: (CardCategory?) -> Unit,
    onSortSelect: (SearchSort) -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            WalletSearchBar(
                query = state.query,
                onQueryChange = onQueryChange,
                onClear = onClear,
                placeholder = "Search by store, nickname, number",
                modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
            )

            CategoryChipRow(
                selected = state.selectedCategory,
                onSelect = onCategorySelect,
                modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xs),
            )

            SortRow(selected = state.sort, onSelect = onSortSelect)

            Box(Modifier.fillMaxSize()) {
                when {
                    !state.isSearching -> RecentSearches(
                        recent = state.recentSearches,
                        onRecentClick = onRecentClick,
                    )
                    state.hasNoResults -> EmptyState(
                        title = "No matches",
                        description = "No cards match \"${state.query}\". Try a different term.",
                        icon = WalletIcons.Search,
                    )
                    else -> SearchResults(state = state, onCardClick = onCardClick)
                }
            }
        }
    }
}

@Composable
private fun SortRow(selected: SearchSort, onSelect: (SearchSort) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(WalletIcons.Sort, contentDescription = "Sort", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        SearchSort.entries.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option.label) },
            )
        }
    }
}

@Composable
private fun SearchResults(state: SearchUiState, onCardClick: (String) -> Unit) {
    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(state.results, key = { it.id }) { card ->
                SearchResultRow(card = card, query = state.query, onClick = { onCardClick(card.id) })
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    card: com.universalwallet.loyalty.domain.model.LoyaltyCard,
    query: String,
    onClick: () -> Unit,
) {
    val highlightColor = MaterialTheme.colorScheme.primary
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
        ) {
            Text(
                text = highlightMatches(card.storeName, query, highlightColor),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = card.nickname.ifBlank {
                com.universalwallet.loyalty.core.cards.maskCardNumber(card.cardNumber)
            }
            Text(
                text = highlightMatches(subtitle, query, highlightColor),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Bolds and tints the first occurrence of [query] within [text]. */
private fun highlightMatches(
    text: String,
    query: String,
    color: androidx.compose.ui.graphics.Color,
): androidx.compose.ui.text.AnnotatedString {
    val q = query.trim()
    if (q.isBlank()) return androidx.compose.ui.text.AnnotatedString(text)
    val index = text.indexOf(q, ignoreCase = true)
    if (index < 0) return androidx.compose.ui.text.AnnotatedString(text)
    return androidx.compose.ui.text.buildAnnotatedString {
        append(text.substring(0, index))
        androidx.compose.ui.text.withStyle(
            androidx.compose.ui.text.SpanStyle(
                color = color,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            ),
        ) {
            append(text.substring(index, index + q.length))
        }
        append(text.substring(index + q.length))
    }
}

@Composable
private fun RecentSearches(recent: List<String>, onRecentClick: (String) -> Unit) {
    if (recent.isEmpty()) {
        EmptyState(
            title = "Search your wallet",
            description = "Start typing to find a card instantly.",
            icon = WalletIcons.Search,
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            Text(
                text = "Recent searches",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = Spacing.sm),
            )
        }
        items(recent) { term ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRecentClick(term) }
                    .padding(vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = term,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = Spacing.md),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchPreview() {
    AppTheme {
        SearchContent(
            state = SearchUiState(
                query = "lulu",
                results = listOf(sampleCard("classic"), sampleCard("glass")),
            ),
            onCardClick = {}, onQueryChange = {}, onClear = {}, onSubmit = {},
            onRecentClick = {}, onCategorySelect = {}, onSortSelect = {},
        )
    }
}
