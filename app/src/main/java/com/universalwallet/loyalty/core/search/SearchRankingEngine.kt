package com.universalwallet.loyalty.core.search

import com.universalwallet.loyalty.domain.model.LoyaltyCard
import javax.inject.Inject
import kotlin.math.min

/**
 * Instant, ranked, multi-field search with basic typo tolerance. Matches across
 * store name, nickname, card number, barcode value, category, and notes,
 * weighting earlier fields higher and exact/prefix matches above contains and
 * fuzzy matches. Pure and allocation-light so it stays well under the 100 ms
 * target for the catalogue sizes this app targets.
 */
class SearchRankingEngine @Inject constructor() {

    fun search(query: String, cards: List<LoyaltyCard>): List<LoyaltyCard> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return cards
        return cards
            .map { it to score(it, q) }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<LoyaltyCard, Int>> { it.second }
                .thenBy { it.first.storeName.lowercase() })
            .map { it.first }
    }

    private fun score(card: LoyaltyCard, q: String): Int = maxOf(
        fieldScore(card.storeName, q, 100),
        fieldScore(card.nickname, q, 90),
        fieldScore(card.cardNumber, q, 85),
        fieldScore(card.barcodeValue, q, 70),
        fieldScore(card.category.name, q, 60),
        fieldScore(card.notes, q, 45),
    )

    private fun fieldScore(field: String, q: String, weight: Int): Int {
        if (field.isBlank()) return 0
        val f = field.lowercase()
        val raw = when {
            f == q -> weight + 50
            f.startsWith(q) -> weight + 30
            f.contains(q) -> weight + 12
            isFuzzyMatch(f, q) -> weight - 25
            else -> 0
        }
        return raw.coerceAtLeast(0)
    }

    /** Token-level fuzzy match: any word within edit distance 1 of the query. */
    private fun isFuzzyMatch(field: String, q: String): Boolean {
        if (q.length < 3) return false
        return field.split(' ', '-', '_').any { token ->
            token.isNotEmpty() && levenshtein(token, q) <= 1
        }
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var previous = IntArray(b.length + 1) { it }
        var current = IntArray(b.length + 1)
        for (i in 1..a.length) {
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = min(min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost)
            }
            val tmp = previous
            previous = current
            current = tmp
        }
        return previous[b.length]
    }
}
