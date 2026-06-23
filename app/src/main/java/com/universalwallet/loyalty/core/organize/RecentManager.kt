package com.universalwallet.loyalty.core.organize

import com.universalwallet.loyalty.domain.model.LoyaltyCard
import javax.inject.Inject

/** Time buckets for the "Recently used" view. */
enum class RecentBucket(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This week"),
    OLDER("Older"),
}

/**
 * Groups recently-used cards into [RecentBucket]s by elapsed time since last
 * use. Threshold-based (not calendar-based) so it is deterministic and easy to
 * test. Cards never used (timestamp 0) are excluded.
 */
class RecentManager @Inject constructor() {

    fun group(
        cards: List<LoyaltyCard>,
        now: Long = System.currentTimeMillis(),
    ): Map<RecentBucket, List<LoyaltyCard>> =
        cards.filter { it.lastUsedTimestamp > 0L }
            .sortedByDescending { it.lastUsedTimestamp }
            .groupBy { bucketFor(it.lastUsedTimestamp, now) }

    fun bucketFor(timestamp: Long, now: Long): RecentBucket {
        val elapsed = (now - timestamp).coerceAtLeast(0L)
        return when {
            elapsed < DAY -> RecentBucket.TODAY
            elapsed < 2 * DAY -> RecentBucket.YESTERDAY
            elapsed < 7 * DAY -> RecentBucket.THIS_WEEK
            else -> RecentBucket.OLDER
        }
    }

    private companion object {
        const val DAY = 86_400_000L
    }
}
