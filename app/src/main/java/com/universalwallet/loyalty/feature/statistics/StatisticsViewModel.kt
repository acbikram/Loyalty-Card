package com.universalwallet.loyalty.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Immutable UI state for the statistics dashboard. */
data class StatisticsUiState(
    val isLoading: Boolean = true,
    val totalCards: Int = 0,
    val favoriteCount: Int = 0,
    val categoryCounts: List<Pair<CardCategory, Int>> = emptyList(),
    val recentActivity: List<LoyaltyCard> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && totalCards == 0
    val topCategoryCount: Int get() = categoryCounts.maxOfOrNull { it.second } ?: 0
}

/**
 * Statistics ViewModel. Derives simple aggregate metrics from the full card
 * stream — totals, favourites, per-category counts, and recent activity — for
 * the dashboard. Pure derivation; no persistence.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    cardRepository: LoyaltyCardRepository,
) : ViewModel() {

    val state: StateFlow<StatisticsUiState> = cardRepository.observeCards()
        .map { cards ->
            StatisticsUiState(
                isLoading = false,
                totalCards = cards.size,
                favoriteCount = cards.count { it.isFavorite },
                categoryCounts = cards.groupingBy { it.category }.eachCount()
                    .toList()
                    .sortedByDescending { it.second },
                recentActivity = cards
                    .filter { it.lastUsedTimestamp > 0 }
                    .sortedByDescending { it.lastUsedTimestamp }
                    .take(5),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())
}
