package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.organize.FilterCriteria
import com.universalwallet.loyalty.core.organize.FilterManager
import com.universalwallet.loyalty.domain.model.CardCategory
import org.junit.Test

class FilterManagerTest {

    private val manager = FilterManager()
    private val now = 100L * 86_400_000L

    @Test
    fun excludesArchivedAndHiddenByDefault() {
        val cards = listOf(
            card(id = "a", isArchived = true),
            card(id = "h", isHidden = true),
            card(id = "ok"),
        )
        val result = manager.filter(cards, FilterCriteria(), now)
        assertThat(result.map { it.id }).containsExactly("ok")
    }

    @Test
    fun favoritesOnlyKeepsOnlyFavorites() {
        val cards = listOf(card(id = "f", isFavorite = true), card(id = "n"))
        val result = manager.filter(cards, FilterCriteria(favoritesOnly = true), now)
        assertThat(result.map { it.id }).containsExactly("f")
    }

    @Test
    fun categoryFilterMatches() {
        val cards = listOf(
            card(id = "pharm", category = CardCategory.PHARMACY),
            card(id = "super", category = CardCategory.SUPERMARKET),
        )
        val result = manager.filter(cards, FilterCriteria(category = CardCategory.PHARMACY), now)
        assertThat(result.map { it.id }).containsExactly("pharm")
    }

    @Test
    fun recentlyUsedWithinDaysFiltersOldUsage() {
        val cards = listOf(
            card(id = "recent", lastUsedTimestamp = now - 2L * 86_400_000L),
            card(id = "old", lastUsedTimestamp = now - 30L * 86_400_000L),
        )
        val result = manager.filter(cards, FilterCriteria(recentlyUsedWithinDays = 7), now)
        assertThat(result.map { it.id }).containsExactly("recent")
    }

    @Test
    fun hasImageFilterRequiresImagePath() {
        val cards = listOf(card(id = "img", imagePath = "/x.jpg"), card(id = "none"))
        val result = manager.filter(cards, FilterCriteria(hasImage = true), now)
        assertThat(result.map { it.id }).containsExactly("img")
    }
}
