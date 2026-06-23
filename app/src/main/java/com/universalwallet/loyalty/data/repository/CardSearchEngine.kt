package com.universalwallet.loyalty.data.repository

import com.universalwallet.loyalty.domain.model.LoyaltyCard
import javax.inject.Inject

/**
 * In-memory, multi-field ranked search over loyalty cards.
 *
 * Algorithm choice: a single linear pass with weighted scoring. For the target
 * scale (1000+ cards) a scan is a few thousand cheap string operations — well
 * under the 100 ms budget — and it avoids the complexity and rigidity of SQL
 * ranking or maintaining an FTS index. Matching is case-insensitive (everything
 * is lower-cased once) and supports partial matches via `contains`, with bonus
 * weight for exact and prefix matches so the most relevant cards rank first.
 *
 * Field weights (most → least significant): store name, nickname, card number,
 * barcode value, category.
 */
class CardSearchEngine @Inject constructor() {

    fun search(query: String, cards: List<LoyaltyCard>): List<LoyaltyCard> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return cards

        return cards
            .mapNotNull { card -> score(card, q)?.let { card to it } }
            .sortedWith(
                compareByDescending<Pair<LoyaltyCard, Int>> { it.second }
                    .thenBy { it.first.storeName.lowercase() },
            )
            .map { it.first }
    }

    private fun score(card: LoyaltyCard, q: String): Int? {
        var total = 0
        total += fieldScore(card.storeName, q, base = 100)
        total += fieldScore(card.nickname, q, base = 80)
        total += fieldScore(card.cardNumber, q, base = 60)
        total += fieldScore(card.barcodeValue, q, base = 40)
        total += fieldScore(card.category.name, q, base = 30)
        return total.takeIf { it > 0 }
    }

    private fun fieldScore(value: String?, q: String, base: Int): Int {
        val v = value?.lowercase().orEmpty()
        if (v.isEmpty()) return 0
        return when {
            v == q -> base * 3
            v.startsWith(q) -> base * 2
            v.contains(q) -> base
            else -> 0
        }
    }
}
