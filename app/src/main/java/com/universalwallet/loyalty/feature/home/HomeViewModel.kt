package com.universalwallet.loyalty.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.ui.LayoutMode
import com.universalwallet.loyalty.core.wallet.SmartWalletEngine
import com.universalwallet.loyalty.core.wallet.SmartWalletSettings
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import com.universalwallet.loyalty.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Immutable UI state for the Home dashboard. */
data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val favorites: List<LoyaltyCard> = emptyList(),
    val recent: List<LoyaltyCard> = emptyList(),
    val all: List<LoyaltyCard> = emptyList(),
    val suggestions: List<LoyaltyCard> = emptyList(),
    val selectedCategory: CardCategory? = null,
    val layoutMode: LayoutMode = LayoutMode.GRID,
    val smartEnabled: Boolean = true,
    val totalCount: Int = 0,
    val favoriteCount: Int = 0,
) {
    val isEmpty: Boolean get() = !isLoading && all.isEmpty()
}

private data class HomeControls(
    val category: CardCategory?,
    val layout: LayoutMode,
    val refreshing: Boolean,
    val smartEnabled: Boolean,
)

/**
 * Home dashboard ViewModel. Composes favourites, recents, the active card set,
 * smart suggestions (via [SmartWalletEngine], honouring the user's Smart Wallet
 * toggle), and the category-filtered list shown in the grid. Archived/hidden
 * cards are excluded everywhere via the repository's active queries.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cardRepository: LoyaltyCardRepository,
    private val storeRepository: StoreRepository,
    private val smartWalletEngine: SmartWalletEngine,
    smartWalletSettings: SmartWalletSettings,
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<CardCategory?>(null)
    private val layoutMode = MutableStateFlow(LayoutMode.GRID)
    private val refreshing = MutableStateFlow(false)

    private val filteredActive = selectedCategory.flatMapLatest { category ->
        if (category == null) cardRepository.observeActiveCards()
        else cardRepository.observeByCategory(category)
    }

    val state: StateFlow<HomeUiState> = combine(
        combine(
            cardRepository.observeFavorites(),
            cardRepository.observeRecent(8),
            cardRepository.observeActiveCards(),
        ) { favorites, recent, active -> Triple(favorites, recent, active) },
        combine(
            selectedCategory, layoutMode, refreshing, smartWalletSettings.enabled,
        ) { category, layout, isRefreshing, smartEnabled ->
            HomeControls(category, layout, isRefreshing, smartEnabled)
        },
        filteredActive,
    ) { base, controls, filtered ->
        val (favorites, recent, active) = base
        val now = System.currentTimeMillis()
        HomeUiState(
            isLoading = false,
            isRefreshing = controls.refreshing,
            favorites = favorites,
            recent = recent,
            all = filtered,
            suggestions = if (controls.smartEnabled) smartWalletEngine.suggestions(active, now, 5) else emptyList(),
            selectedCategory = controls.category,
            layoutMode = controls.layout,
            smartEnabled = controls.smartEnabled,
            totalCount = active.size,
            favoriteCount = favorites.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun selectCategory(category: CardCategory?) {
        selectedCategory.value = category
    }

    fun toggleLayout() {
        layoutMode.update { if (it == LayoutMode.GRID) LayoutMode.LIST else LayoutMode.GRID }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            storeRepository.syncCatalog()
            refreshing.value = false
        }
    }
}
