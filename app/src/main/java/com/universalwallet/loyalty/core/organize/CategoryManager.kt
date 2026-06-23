package com.universalwallet.loyalty.core.organize

import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import javax.inject.Inject

/**
 * Category aggregation helpers. The category set is fixed for now
 * ([CardCategory]); custom categories are a planned extension, hence this lives
 * behind a manager rather than being inlined into the UI.
 */
class CategoryManager @Inject constructor() {

    fun counts(cards: List<LoyaltyCard>): Map<CardCategory, Int> =
        cards.groupingBy { it.category }.eachCount()

    /** Categories that actually have at least one card, count-descending. */
    fun nonEmpty(cards: List<LoyaltyCard>): List<CardCategory> =
        counts(cards).entries.sortedByDescending { it.value }.map { it.key }

    fun allCategories(): List<CardCategory> = CardCategory.entries
}
