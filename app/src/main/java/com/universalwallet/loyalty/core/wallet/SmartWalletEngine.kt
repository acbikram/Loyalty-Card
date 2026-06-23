package com.universalwallet.loyalty.core.wallet

import com.universalwallet.loyalty.domain.model.LoyaltyCard
import javax.inject.Inject

/** Tunable weights for the Smart Wallet scoring algorithm. */
data class SmartWeights(
    val pinned: Double = 1000.0,
    val favorite: Double = 120.0,
    val frequency: Double = 60.0,
    val recency: Double = 80.0,
)

/**
 * Ranks cards by how likely the user is to want them next, fully offline. The
 * score combines manual pinning (dominant), favourite status, usage frequency
 * (normalised against the most-used card), and recency (a smooth decay since
 * last use). Pure and deterministic — no I/O, no clock except the [now] passed
 * in — so it is straightforward to unit-test.
 */
class SmartWalletEngine @Inject constructor() {

    fun score(card: LoyaltyCard, now: Long, maxUsage: Int, weights: SmartWeights = SmartWeights()): Double {
        val pinned = if (card.isPinned) weights.pinned else 0.0
        val favorite = if (card.isFavorite) weights.favorite else 0.0
        val frequency = if (maxUsage > 0) weights.frequency * (card.usageCount.toDouble() / maxUsage) else 0.0
        val recency = weights.recency * recencyFactor(card.lastUsedTimestamp, now)
        return pinned + favorite + frequency + recency
    }

    /** Returns [cards] ordered most-to-least likely to be used next. */
    fun rank(cards: List<LoyaltyCard>, now: Long, weights: SmartWeights = SmartWeights()): List<LoyaltyCard> {
        val maxUsage = cards.maxOfOrNull { it.usageCount } ?: 0
        return cards.sortedByDescending { score(it, now, maxUsage, weights) }
    }

    /** Top [limit] suggestions for the dashboard's Smart Suggestions section. */
    fun suggestions(
        cards: List<LoyaltyCard>,
        now: Long,
        limit: Int = 5,
        weights: SmartWeights = SmartWeights(),
    ): List<LoyaltyCard> = rank(cards, now, weights).take(limit)

    /** 1.0 for "used right now", decaying smoothly toward 0 as days pass. */
    private fun recencyFactor(lastUsed: Long, now: Long): Double {
        if (lastUsed <= 0L) return 0.0
        val days = (now - lastUsed).coerceAtLeast(0L) / 86_400_000.0
        return 1.0 / (1.0 + days)
    }
}
