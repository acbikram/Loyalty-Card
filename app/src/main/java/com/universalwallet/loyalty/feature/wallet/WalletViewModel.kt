package com.universalwallet.loyalty.feature.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.organize.FilterCriteria
import com.universalwallet.loyalty.core.organize.FilterManager
import com.universalwallet.loyalty.core.organize.SortOption
import com.universalwallet.loyalty.core.organize.SortingManager
import com.universalwallet.loyalty.core.ui.LayoutMode
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Immutable UI state for the full wallet screen. */
data class WalletUiState(
    val isLoading: Boolean = true,
    val cards: List<LoyaltyCard> = emptyList(),
    val selectedCategory: CardCategory? = null,
    val layoutMode: LayoutMode = LayoutMode.GRID,
    val sortOption: SortOption = SortOption.FAVORITES_FIRST,
    val favoritesOnly: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && cards.isEmpty()
}

private data class WalletControls(
    val category: CardCategory?,
    val layout: LayoutMode,
    val sort: SortOption,
    val favoritesOnly: Boolean,
)

/**
 * Wallet ViewModel: the complete (active) card collection with category filter,
 * favourites filter, sorting, and layout. Filtering and sorting are delegated to
 * the pure [FilterManager] / [SortingManager] so the logic is testable in
 * isolation.
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val cardRepository: LoyaltyCardRepository,
    private val filterManager: FilterManager,
    private val sortingManager: SortingManager,
) : ViewModel() {

    private val category = MutableStateFlow<CardCategory?>(null)
    private val layout = MutableStateFlow(LayoutMode.GRID)
    private val sort = MutableStateFlow(SortOption.FAVORITES_FIRST)
    private val favoritesOnly = MutableStateFlow(false)

    val state: StateFlow<WalletUiState> = combine(
        cardRepository.observeActiveCards(),
        combine(category, layout, sort, favoritesOnly) { c, l, s, f -> WalletControls(c, l, s, f) },
    ) { cards, controls ->
        val filtered = filterManager.filter(
            cards,
            FilterCriteria(category = controls.category, favoritesOnly = controls.favoritesOnly),
        )
        WalletUiState(
            isLoading = false,
            cards = sortingManager.sort(filtered, controls.sort),
            selectedCategory = controls.category,
            layoutMode = controls.layout,
            sortOption = controls.sort,
            favoritesOnly = controls.favoritesOnly,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WalletUiState())

    fun selectCategory(value: CardCategory?) { category.value = value }
    fun setSort(option: SortOption) { sort.value = option }
    fun toggleFavoritesOnly() { favoritesOnly.update { !it } }
    fun toggleLayout() {
        layout.update { if (it == LayoutMode.GRID) LayoutMode.LIST else LayoutMode.GRID }
    }
}
