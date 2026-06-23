package com.universalwallet.loyalty.core.organize

import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import javax.inject.Inject

/** Composable filter predicate set. `null` fields mean "no constraint". */
data class FilterCriteria(
    val category: CardCategory? = null,
    val storeId: String? = null,
    val barcodeType: BarcodeType? = null,
    val favoritesOnly: Boolean = false,
    val includeArchived: Boolean = false,
    val includeHidden: Boolean = false,
    val hasImage: Boolean = false,
    val recentlyAddedWithinDays: Int? = null,
    val recentlyUsedWithinDays: Int? = null,
) {
    val isEmpty: Boolean
        get() = category == null && storeId == null && barcodeType == null &&
            !favoritesOnly && !hasImage && recentlyAddedWithinDays == null &&
            recentlyUsedWithinDays == null
}

/** Pure filtering of cards against a [FilterCriteria]. */
class FilterManager @Inject constructor() {

    fun filter(
        cards: List<LoyaltyCard>,
        criteria: FilterCriteria,
        now: Long = System.currentTimeMillis(),
    ): List<LoyaltyCard> = cards.filter { card ->
        (criteria.includeArchived || !card.isArchived) &&
            (criteria.includeHidden || !card.isHidden) &&
            (criteria.category == null || card.category == criteria.category) &&
            (criteria.storeId == null || card.storeId == criteria.storeId) &&
            (criteria.barcodeType == null || card.barcodeType == criteria.barcodeType) &&
            (!criteria.favoritesOnly || card.isFavorite) &&
            (!criteria.hasImage || card.imagePath != null) &&
            withinDays(card.createdAt, criteria.recentlyAddedWithinDays, now) &&
            withinDays(card.lastUsedTimestamp, criteria.recentlyUsedWithinDays, now)
    }

    private fun withinDays(timestamp: Long, days: Int?, now: Long): Boolean {
        if (days == null) return true
        if (timestamp <= 0L) return false
        return now - timestamp <= days * 86_400_000L
    }
}
