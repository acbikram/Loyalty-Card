package com.universalwallet.loyalty

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.search.SearchRankingEngine
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.feature.search.SearchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cards = listOf(
        card(id = "1", storeName = "Lulu", category = CardCategory.SUPERMARKET),
        card(id = "2", storeName = "Nesto", category = CardCategory.SUPERMARKET),
        card(id = "3", storeName = "Boots", category = CardCategory.PHARMACY),
    )

    private fun viewModel() = SearchViewModel(
        cardRepository = FakeLoyaltyCardRepository(cards),
        rankingEngine = SearchRankingEngine(),
    )

    @Test
    fun blankQueryYieldsNoResults() = runTest {
        viewModel().state.test {
            assertThat(awaitItem().results).isEmpty()
        }
    }

    @Test
    fun queryMatchesStoreName() = runTest {
        val vm = viewModel()
        vm.state.test {
            awaitItem() // initial
            vm.onQueryChange("lulu")
            val matched = awaitItem()
            assertThat(matched.results.map { it.storeName }).contains("Lulu")
        }
    }

    @Test
    fun commitSearchTracksRecentTerms() = runTest {
        val vm = viewModel()
        vm.state.test {
            awaitItem()
            vm.onQueryChange("Nesto")
            awaitItem()
            vm.commitSearch()
            val withRecent = awaitItem()
            assertThat(withRecent.recentSearches).contains("Nesto")
        }
    }

    @Test
    fun categoryFilterNarrowsResults() = runTest {
        val vm = viewModel()
        vm.state.test {
            awaitItem()
            vm.onQueryChange("o") // matches Nesto, Boots
            awaitItem()
            vm.selectCategory(CardCategory.PHARMACY)
            val filtered = expectMostRecentItem()
            assertThat(filtered.results.all { it.category == CardCategory.PHARMACY }).isTrue()
        }
    }
}
