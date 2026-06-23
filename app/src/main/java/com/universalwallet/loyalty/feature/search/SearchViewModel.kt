package com.universalwallet.loyalty.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.search.SearchRankingEngine
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** Sort orders offered on the search screen. RELEVANCE keeps ranking order. */
enum class SearchSort(val label: String) {
    RELEVANCE("Relevance"),
    NAME("Name"),
    STORE("Store"),
}

/** Immutable UI state for search. */
data class SearchUiState(
    val query: String = "",
    val results: List<LoyaltyCard> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val selectedCategory: CardCategory? = null,
    val sort: SearchSort = SearchSort.RELEVANCE,
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val hasNoResults: Boolean get() = isSearching && results.isEmpty()
}

/**
 * Search ViewModel. Matching is delegated to the pure [SearchRankingEngine]
 * (multi-field, case-insensitive, partial, with basic typo tolerance) over the
 * active card set. A category filter and an optional re-sort layer on top, and
 * recent search terms are tracked in memory. Ranking is computed synchronously
 * in a flow transform — comfortably under the 100 ms target for realistic
 * wallet sizes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cardRepository: LoyaltyCardRepository,
    private val rankingEngine: SearchRankingEngine,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<CardCategory?>(null)
    private val sort = MutableStateFlow(SearchSort.RELEVANCE)
    private val recent = MutableStateFlow<List<String>>(emptyList())

    private val results = combine(query, category) { q, c -> q to c }
        .flatMapLatest { (q, c) ->
            if (q.isBlank()) {
                flowOf(emptyList())
            } else {
                cardRepository.observeActiveCards().map { cards ->
                    val ranked = rankingEngine.search(q, cards)
                    if (c == null) ranked else ranked.filter { it.category == c }
                }
            }
        }

    val state: StateFlow<SearchUiState> = combine(
        query, results, recent, category, sort,
    ) { q, res, rec, c, s ->
        SearchUiState(
            query = q,
            results = applySort(res, s),
            recentSearches = rec,
            selectedCategory = c,
            sort = s,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    private fun applySort(cards: List<LoyaltyCard>, sort: SearchSort): List<LoyaltyCard> = when (sort) {
        SearchSort.RELEVANCE -> cards
        SearchSort.NAME -> cards.sortedBy { it.nickname.ifBlank { it.storeName }.lowercase() }
        SearchSort.STORE -> cards.sortedBy { it.storeName.lowercase() }
    }

    fun onQueryChange(value: String) { query.value = value }
    fun clearQuery() { query.value = "" }

    fun commitSearch() {
        val q = query.value.trim()
        if (q.isNotEmpty()) {
            recent.update { (listOf(q) + it).distinct().take(8) }
        }
    }

    fun selectRecent(value: String) { query.value = value }
    fun selectCategory(value: CardCategory?) { category.value = value }
    fun setSort(value: SearchSort) { sort.value = value }
}
