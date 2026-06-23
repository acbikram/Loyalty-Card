package com.universalwallet.loyalty.feature.stores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.StoreDefinition
import com.universalwallet.loyalty.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Immutable UI state for the store browser. */
data class StoreBrowserUiState(
    val isLoading: Boolean = true,
    val stores: List<StoreDefinition> = emptyList(),
    val countries: List<String> = emptyList(),
    val query: String = "",
    val selectedCategory: CardCategory? = null,
    val selectedCountry: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && stores.isEmpty()
}

/**
 * Store-browser ViewModel. Streams the full catalogue and applies search,
 * category, and country filters, returning an alphabetically sorted list plus
 * the set of countries available for filtering.
 */
@HiltViewModel
class StoreBrowserViewModel @Inject constructor(
    storeRepository: StoreRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<CardCategory?>(null)
    private val country = MutableStateFlow<String?>(null)

    val state: StateFlow<StoreBrowserUiState> = combine(
        storeRepository.observeStores(), query, category, country,
    ) { stores, q, c, co ->
        val filtered = stores
            .filter { q.isBlank() || it.storeName.contains(q, ignoreCase = true) || it.keywords.any { k -> k.contains(q, true) } }
            .filter { c == null || it.category == c }
            .filter { co == null || it.country.contains(co) }
            .sortedBy { it.storeName.lowercase() }
        StoreBrowserUiState(
            isLoading = false,
            stores = filtered,
            countries = stores.flatMap { it.country }.distinct().sorted(),
            query = q,
            selectedCategory = c,
            selectedCountry = co,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StoreBrowserUiState())

    fun setQuery(value: String) { query.value = value }
    fun setCategory(value: CardCategory?) { category.value = value }
    fun setCountry(value: String?) { country.value = value }
}
