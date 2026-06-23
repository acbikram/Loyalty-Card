package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.search.SearchRankingEngine
import com.universalwallet.loyalty.domain.model.CardCategory
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Coarse JVM performance budgets for pure engines. Thresholds are deliberately
 * generous so the test guards against large regressions (e.g. an accidental
 * O(n^2)) without flaking on slow CI runners. Device-level startup/scroll/render
 * benchmarks belong in a Macrobenchmark module (see PERFORMANCE.md); this keeps a
 * fast, dependency-free signal in the unit-test suite.
 */
class PerformanceBudgetTest {

    private val engine = SearchRankingEngine()

    private fun dataset(size: Int) = (0 until size).map { i ->
        card(
            id = i.toString(),
            storeName = "Store $i",
            nickname = if (i % 3 == 0) "Fav $i" else "",
            category = CardCategory.entries[i % CardCategory.entries.size],
        )
    }

    @Test
    fun searchOverLargeWalletIsFast() {
        val cards = dataset(2_000)
        // Warm up the JIT a little for a more stable measurement.
        repeat(3) { engine.search("store", cards) }

        var results = emptyList<com.universalwallet.loyalty.domain.model.LoyaltyCard>()
        val elapsed = measureTimeMillis { results = engine.search("Store", cards) }

        assertThat(results).isNotEmpty()
        // Budget: 2k-card search well under half a second even on a slow runner.
        assertThat(elapsed).isLessThan(500L)
    }

    @Test
    fun emptyQueryHandlingIsTrivial() {
        val cards = dataset(2_000)
        val elapsed = measureTimeMillis { engine.search("", cards) }
        assertThat(elapsed).isLessThan(200L)
    }
}
