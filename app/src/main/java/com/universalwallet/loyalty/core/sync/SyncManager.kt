package com.universalwallet.loyalty.core.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates a sync pass: pull → merge (via [ConflictResolver]) → push queued
 * changes. With the default [LocalOnlyCloudSyncProvider] this short-circuits to
 * NOT_CONFIGURED, so the full pipeline is exercised only once a real provider is
 * bound — but the orchestration, status surface, and offline queue are all in
 * place and testable.
 */
@Singleton
class SyncManager @Inject constructor(
    private val provider: CloudSyncProvider,
    private val conflictResolver: ConflictResolver,
    private val queue: SyncQueue,
    private val deviceIdentity: DeviceIdentity,
) {
    private val _status = MutableStateFlow(SyncStatus.NOT_CONFIGURED)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    fun deviceInfo(): DeviceInfo =
        DeviceInfo(deviceId = deviceIdentity.deviceId(), displayName = deviceIdentity.deviceName())

    /** Queues a local change for the next sync pass. */
    suspend fun enqueueUpsert(card: RemoteCard) = queue.enqueue(
        SyncOperation(
            id = "${card.id}:${card.updatedAt}",
            type = SyncOperation.OpType.UPSERT,
            cardId = card.id,
            payload = card,
            enqueuedAt = System.currentTimeMillis(),
        ),
    )

    suspend fun enqueueDelete(cardId: String) = queue.enqueue(
        SyncOperation(
            id = "$cardId:delete",
            type = SyncOperation.OpType.DELETE,
            cardId = cardId,
            enqueuedAt = System.currentTimeMillis(),
        ),
    )

    /** Runs one sync pass against [localCards]. Returns the merged winners. */
    suspend fun sync(
        localCards: List<RemoteCard>,
        lastSyncAtMillis: Long,
        strategy: MergeStrategy = MergeStrategy.LAST_WRITE_WINS,
    ): Pair<SyncResult, List<RemoteCard>> {
        if (!provider.isAuthenticated()) {
            _status.value = SyncStatus.NOT_CONFIGURED
            return SyncResult(0, 0, 0, SyncStatus.NOT_CONFIGURED) to localCards
        }
        _status.value = SyncStatus.SYNCING
        val pulled = provider.pull(lastSyncAtMillis).getOrElse {
            _status.value = SyncStatus.FAILED
            return SyncResult(0, 0, 0, SyncStatus.FAILED) to localCards
        }
        val outcome = conflictResolver.merge(localCards, pulled, strategy)
        val pending = queue.pending().mapNotNull { it.payload }
        val pushResult = provider.push(pending)
        if (pushResult.isFailure) {
            _status.value = SyncStatus.FAILED
            return SyncResult(0, pulled.size, outcome.conflicts.size, SyncStatus.FAILED) to outcome.resolved
        }
        queue.clear()
        _status.value = SyncStatus.SUCCESS
        return SyncResult(pending.size, pulled.size, outcome.conflicts.size, SyncStatus.SUCCESS) to outcome.resolved
    }
}
