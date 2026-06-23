package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.organize.SortOption
import com.universalwallet.loyalty.core.organize.SortingManager
import org.junit.Test

class SortingManagerTest {

    private val manager = SortingManager()

    @Test
    fun alphabeticalSortsByDisplayName() {
        val cards = listOf(card(storeName = "Zara"), card(storeName = "Apple"), card(storeName = "Microsoft"))
        val sorted = manager.sort(cards, SortOption.ALPHABETICAL)
        assertThat(sorted.map { it.storeName }).containsExactly("Apple", "Microsoft", "Zara").inOrder()
    }

    @Test
    fun favoritesFirstPutsFavoritesAhead() {
        val fav = card(storeName = "Zen", isFavorite = true)
        val plain = card(storeName = "Apple", isFavorite = false)
        val sorted = manager.sort(listOf(plain, fav), SortOption.FAVORITES_FIRST)
        assertThat(sorted.first().isFavorite).isTrue()
    }

    @Test
    fun manualUsesSortIndex() {
        val cards = listOf(card(storeName = "C", sortIndex = 2), card(storeName = "A", sortIndex = 0), card(storeName = "B", sortIndex = 1))
        val sorted = manager.sort(cards, SortOption.MANUAL)
        assertThat(sorted.map { it.storeName }).containsExactly("A", "B", "C").inOrder()
    }

    @Test
    fun newestFirstOrdersByCreatedDescending() {
        val cards = listOf(card(storeName = "Old", createdAt = 1), card(storeName = "New", createdAt = 100))
        val sorted = manager.sort(cards, SortOption.NEWEST_FIRST)
        assertThat(sorted.first().storeName).isEqualTo("New")
    }
}
