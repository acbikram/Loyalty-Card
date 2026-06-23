package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.organize.RecentBucket
import com.universalwallet.loyalty.core.organize.RecentManager
import org.junit.Test

class RecentManagerTest {

    private val manager = RecentManager()
    private val now = 100L * 86_400_000L
    private val day = 86_400_000L

    @Test
    fun bucketThresholds() {
        assertThat(manager.bucketFor(now - day / 2, now)).isEqualTo(RecentBucket.TODAY)
        assertThat(manager.bucketFor(now - day - 1, now)).isEqualTo(RecentBucket.YESTERDAY)
        assertThat(manager.bucketFor(now - 4 * day, now)).isEqualTo(RecentBucket.THIS_WEEK)
        assertThat(manager.bucketFor(now - 30 * day, now)).isEqualTo(RecentBucket.OLDER)
    }

    @Test
    fun groupExcludesNeverUsedCards() {
        val cards = listOf(
            card(id = "used", lastUsedTimestamp = now - day / 2),
            card(id = "never", lastUsedTimestamp = 0L),
        )
        val grouped = manager.group(cards, now)
        val all = grouped.values.flatten().map { it.id }
        assertThat(all).containsExactly("used")
    }

    @Test
    fun groupsByCorrectBucket() {
        val cards = listOf(
            card(id = "today", lastUsedTimestamp = now - day / 4),
            card(id = "week", lastUsedTimestamp = now - 3 * day),
        )
        val grouped = manager.group(cards, now)
        assertThat(grouped[RecentBucket.TODAY]?.map { it.id }).containsExactly("today")
        assertThat(grouped[RecentBucket.THIS_WEEK]?.map { it.id }).containsExactly("week")
    }
}
