package com.universalwallet.loyalty.feature.stores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.components.CategoryChipRow
import com.universalwallet.loyalty.core.components.EmptyState
import com.universalwallet.loyalty.core.components.StoreRow
import com.universalwallet.loyalty.core.components.WalletLoadingIndicator
import com.universalwallet.loyalty.core.components.WalletSearchBar
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.domain.model.CardCategory

/** Stateful Store-browser entry point. */
@Composable
fun StoreBrowserScreen(
    onBack: () -> Unit,
    onStoreSelected: (String) -> Unit,
    viewModel: StoreBrowserViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StoreBrowserContent(
        state = state,
        onBack = onBack,
        onStoreSelected = onStoreSelected,
        onQueryChange = viewModel::setQuery,
        onCategorySelect = viewModel::setCategory,
        onCountrySelect = viewModel::setCountry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreBrowserContent(
    state: StoreBrowserUiState,
    onBack: () -> Unit,
    onStoreSelected: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (CardCategory?) -> Unit,
    onCountrySelect: (String?) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse stores") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(WalletIcons.Back, contentDescription = "Navigate back") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            WalletSearchBar(
                query = state.query,
                onQueryChange = onQueryChange,
                placeholder = "Search stores",
                modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
            )
            CategoryChipRow(
                selected = state.selectedCategory,
                onSelect = onCategorySelect,
                modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xs),
            )
            if (state.countries.isNotEmpty()) {
                CountryFilterRow(
                    countries = state.countries,
                    selected = state.selectedCountry,
                    onSelect = onCountrySelect,
                )
            }
            Box(Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> WalletLoadingIndicator()
                    state.isEmpty -> EmptyState(
                        title = "No stores found",
                        description = "Try a different search or filter.",
                        icon = WalletIcons.Store,
                    )
                    else -> LazyColumn(Modifier.fillMaxSize()) {
                        items(state.stores, key = { it.storeId }) { store ->
                            StoreRow(store = store, onClick = { onStoreSelected(store.storeId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryFilterRow(
    countries: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xs),
        contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All countries") })
        }
        items(countries) { country ->
            FilterChip(
                selected = selected == country,
                onClick = { onSelect(country) },
                label = { Text(country) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StoreBrowserPreview() {
    AppTheme {
        StoreBrowserContent(
            state = StoreBrowserUiState(isLoading = false, countries = listOf("AE", "SA")),
            onBack = {}, onStoreSelected = {}, onQueryChange = {}, onCategorySelect = {}, onCountrySelect = {},
        )
    }
}
