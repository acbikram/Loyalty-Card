package com.universalwallet.loyalty.core.organize

import com.universalwallet.loyalty.domain.model.LoyaltyCard
import javax.inject.Inject

/** User-selectable sort orders for card collections. */
enum class SortOption(val label: String) {
    FAVORITES_FIRST("Favourites first"),
    ALPHABETICAL("A–Z"),
    STORE_NAME("Store"),
    CATEGORY("Category"),
    MOST_RECENT("Recently used"),
    NEWEST_FIRST("Newest"),
    OLDEST_FIRST("Oldest"),
    MANUAL("Custom order"),
}

/** Pure, deterministic sorting of cards by a [SortOption]. */
class SortingManager @Inject constructor() {

    fun sort(cards: List<LoyaltyCard>, option: SortOption): List<LoyaltyCard> = when (option) {
        SortOption.FAVORITES_FIRST -> cards.sortedWith(
            compareByDescending<LoyaltyCard> { it.isFavorite }.thenBy { it.displayName().lowercase() },
        )
        SortOption.ALPHABETICAL -> cards.sortedBy { it.displayName().lowercase() }
        SortOption.STORE_NAME -> cards.sortedBy { it.storeName.lowercase() }
        SortOption.CATEGORY -> cards.sortedWith(
            compareBy<LoyaltyCard> { it.category.name }.thenBy { it.storeName.lowercase() },
        )
        SortOption.MOST_RECENT -> cards.sortedByDescending { it.lastUsedTimestamp }
        SortOption.NEWEST_FIRST -> cards.sortedByDescending { it.createdAt }
        SortOption.OLDEST_FIRST -> cards.sortedBy { it.createdAt }
        SortOption.MANUAL -> cards.sortedBy { it.sortIndex }
    }

    private fun LoyaltyCard.displayName(): String = nickname.ifBlank { storeName }
}
