package com.universalwallet.loyalty.feature.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.universalwallet.loyalty.core.cards.displayLabel
import com.universalwallet.loyalty.core.components.EmptyState
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.WalletLoadingIndicator
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.domain.model.CardCategory
import androidx.compose.foundation.lazy.items

/** Stateful Statistics entry point. */
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StatisticsContent(state)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsContent(state: StatisticsUiState) {
    Scaffold(topBar = { TopAppBar(title = { Text("Statistics") }) }) { padding ->
        Box(Modifier.fillMaxWidth().padding(padding)) {
            when {
                state.isLoading -> WalletLoadingIndicator()
                state.isEmpty -> EmptyState(
                    title = "No data yet",
                    description = "Add cards to see insights about your wallet.",
                    icon = WalletIcons.Statistics,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = Spacing.xxl),
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(Spacing.screenHorizontal),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            StatCard("Total cards", state.totalCards.toString(), Modifier.weight(1f))
                            StatCard("Favourites", state.favoriteCount.toString(), Modifier.weight(1f))
                        }
                    }
                    item { SectionHeader("By category") }
                    items(state.categoryCounts) { (category, count) ->
                        CategoryBar(
                            category = category,
                            count = count,
                            maxCount = state.topCategoryCount,
                        )
                    }
                    if (state.recentActivity.isNotEmpty()) {
                        item { SectionHeader("Recent activity") }
                        items(state.recentActivity) { card ->
                            Text(
                                text = card.storeName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(Spacing.lg)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CategoryBar(category: CardCategory, count: Int, maxCount: Int) {
    val fraction = if (maxCount == 0) 0f else count.toFloat() / maxCount
    val animated by animateFloatAsState(targetValue = fraction, animationSpec = tween(450), label = "barFraction")
    Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category.displayLabel(), style = MaterialTheme.typography.bodyMedium)
            Text(count.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticsPreview() {
    AppTheme {
        StatisticsContent(
            StatisticsUiState(
                isLoading = false,
                totalCards = 12,
                favoriteCount = 4,
                categoryCounts = listOf(
                    CardCategory.SUPERMARKET to 6,
                    CardCategory.PHARMACY to 3,
                    CardCategory.FUEL to 2,
                    CardCategory.COFFEE to 1,
                ),
            ),
        )
    }
}
