package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.wallet.SmartWalletEngine
import org.junit.Test

class SmartWalletEngineTest {

    private val engine = SmartWalletEngine()
    private val now = 1_000L * 86_400_000L // arbitrary fixed "now"

    @Test
    fun pinnedCardOutranksEverythingElse() {
        val pinned = card(id = "p", storeName = "Pinned", isPinned = true)
        val heavilyUsed = card(id = "u", storeName = "Used", usageCount = 999, isFavorite = true, lastUsedTimestamp = now)
        val ranked = engine.rank(listOf(heavilyUsed, pinned), now)
        assertThat(ranked.first().id).isEqualTo("p")
    }

    @Test
    fun moreRecentCardScoresHigherThanStaleOne() {
        val recent = card(id = "r", lastUsedTimestamp = now - 86_400_000L) // 1 day ago
        val stale = card(id = "s", lastUsedTimestamp = now - 60L * 86_400_000L) // 60 days ago
        val ranked = engine.rank(listOf(stale, recent), now)
        assertThat(ranked.first().id).isEqualTo("r")
    }

    @Test
    fun frequencyBreaksTiesWhenRecencyEqual() {
        val ts = now - 86_400_000L
        val frequent = card(id = "f", usageCount = 20, lastUsedTimestamp = ts)
        val rare = card(id = "x", usageCount = 1, lastUsedTimestamp = ts)
        val ranked = engine.rank(listOf(rare, frequent), now)
        assertThat(ranked.first().id).isEqualTo("f")
    }

    @Test
    fun suggestionsRespectLimit() {
        val cards = (1..10).map { card(id = "c$it", usageCount = it, lastUsedTimestamp = now) }
        assertThat(engine.suggestions(cards, now, limit = 3)).hasSize(3)
    }
}
