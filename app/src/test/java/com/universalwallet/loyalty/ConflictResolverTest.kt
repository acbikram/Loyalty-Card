package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.sync.ConflictResolver
import com.universalwallet.loyalty.core.sync.MergeStrategy
import com.universalwallet.loyalty.core.sync.RemoteCard
import com.universalwallet.loyalty.core.sync.Resolution
import org.junit.Test

class ConflictResolverTest {

    private val resolver = ConflictResolver()

    private fun card(id: String, number: String, updatedAt: Long, deleted: Boolean = false) = RemoteCard(
        id = id, storeId = "lulu", storeName = "Lulu", cardNumber = number,
        barcodeValue = number, barcodeType = "EAN13", updatedAt = updatedAt, deleted = deleted,
    )

    @Test
    fun newerRemoteWinsUnderLastWriteWins() {
        val local = card("1", "111", updatedAt = 100)
        val remote = card("1", "222", updatedAt = 200)
        assertThat(resolver.resolve(local, remote)).isEqualTo(Resolution.UseRemote(remote))
    }

    @Test
    fun newerLocalWins() {
        val local = card("1", "111", updatedAt = 300)
        val remote = card("1", "222", updatedAt = 200)
        assertThat(resolver.resolve(local, remote)).isEqualTo(Resolution.UseLocal(local))
    }

    @Test
    fun identicalCardsAreNoChange() {
        val local = card("1", "111", updatedAt = 100)
        val remote = card("1", "111", updatedAt = 100)
        assertThat(resolver.resolve(local, remote)).isEqualTo(Resolution.NoChange)
    }

    @Test
    fun missingSideUsesTheOther() {
        val remote = card("1", "222", updatedAt = 200)
        assertThat(resolver.resolve(null, remote)).isEqualTo(Resolution.UseRemote(remote))
        val local = card("2", "333", updatedAt = 50)
        assertThat(resolver.resolve(local, null)).isEqualTo(Resolution.UseLocal(local))
    }

    @Test
    fun tombstoneWinsWhenNewer() {
        val local = card("1", "111", updatedAt = 100)
        val remoteDeleted = card("1", "111", updatedAt = 500, deleted = true)
        assertThat(resolver.resolve(local, remoteDeleted)).isEqualTo(Resolution.UseRemote(remoteDeleted))
    }

    @Test
    fun manualStrategyFlagsConflict() {
        val local = card("1", "111", updatedAt = 100)
        val remote = card("1", "222", updatedAt = 200)
        val result = resolver.resolve(local, remote, MergeStrategy.MANUAL)
        assertThat(result).isInstanceOf(Resolution.Conflict::class.java)
    }

    @Test
    fun preferLocalAndPreferRemoteAreHonoured() {
        val local = card("1", "111", updatedAt = 100)
        val remote = card("1", "222", updatedAt = 200)
        assertThat(resolver.resolve(local, remote, MergeStrategy.PREFER_LOCAL)).isEqualTo(Resolution.UseLocal(local))
        assertThat(resolver.resolve(local, remote, MergeStrategy.PREFER_REMOTE)).isEqualTo(Resolution.UseRemote(remote))
    }

    @Test
    fun mergeReturnsWinnersAndCountsConflicts() {
        val local = listOf(card("1", "111", updatedAt = 100), card("2", "aaa", updatedAt = 100))
        val remote = listOf(card("1", "222", updatedAt = 200), card("3", "ccc", updatedAt = 50))
        val outcome = resolver.merge(local, remote)
        // ids 1 (remote wins), 2 (local only), 3 (remote only) -> 3 winners, 0 conflicts under LWW
        assertThat(outcome.resolved.map { it.id }.toSet()).isEqualTo(setOf("1", "2", "3"))
        assertThat(outcome.conflicts).isEmpty()
    }

    @Test
    fun mergeUnderManualCollectsConflicts() {
        val local = listOf(card("1", "111", updatedAt = 100))
        val remote = listOf(card("1", "222", updatedAt = 200))
        val outcome = resolver.merge(local, remote, MergeStrategy.MANUAL)
        assertThat(outcome.conflicts).hasSize(1)
    }
}
