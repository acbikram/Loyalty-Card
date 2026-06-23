package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.search.SearchRankingEngine
import org.junit.Test

class SearchRankingEngineTest {

    private val engine = SearchRankingEngine()

    @Test
    fun exactStoreMatchRanksAboveContainsMatch() {
        val exact = card(id = "exact", storeName = "Lulu")
        val contains = card(id = "contains", storeName = "Lulu Hypermarket")
        val results = engine.search("lulu", listOf(contains, exact))
        assertThat(results.first().id).isEqualTo("exact")
    }

    @Test
    fun caseInsensitivePartialMatch() {
        val cards = listOf(card(id = "c", storeName = "Carrefour"))
        assertThat(engine.search("CARR", cards).map { it.id }).containsExactly("c")
    }

    @Test
    fun matchesAcrossMultipleFields() {
        val byNickname = card(id = "nick", storeName = "X", nickname = "Grocery")
        val byNotes = card(id = "notes", storeName = "Y", notes = "grocery points card")
        val results = engine.search("grocery", listOf(byNickname, byNotes))
        assertThat(results.map { it.id }).containsExactly("nick", "notes")
    }

    @Test
    fun toleratesSingleCharacterTypo() {
        val cards = listOf(card(id = "n", storeName = "Nesto"))
        // "nesta" is edit-distance 1 from "nesto"
        assertThat(engine.search("nesta", cards).map { it.id }).containsExactly("n")
    }

    @Test
    fun nonMatchingQueryReturnsEmpty() {
        val cards = listOf(card(storeName = "Prime"))
        assertThat(engine.search("zzzzzz", cards)).isEmpty()
    }

    @Test
    fun blankQueryReturnsAll() {
        val cards = listOf(card(id = "a"), card(id = "b"))
        assertThat(engine.search("", cards)).hasSize(2)
    }
}
