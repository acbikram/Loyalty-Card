package com.universalwallet.loyalty.core.sync

import javax.inject.Inject

/** The decision for a single card during a sync merge. */
sealed interface Resolution {
    data class UseLocal(val card: RemoteCard) : Resolution
    data class UseRemote(val card: RemoteCard) : Resolution
    data class Conflict(val local: RemoteCard, val remote: RemoteCard) : Resolution
    data object NoChange : Resolution
}

/**
 * Pure conflict resolution between a local and a remote version of a card.
 * Default is last-write-wins by [RemoteCard.updatedAt], with tombstones treated
 * as ordinary timestamped writes (a later delete wins over an earlier edit, and
 * vice-versa). Fully unit-testable; no I/O.
 */
class ConflictResolver @Inject constructor() {

    fun resolve(
        local: RemoteCard?,
        remote: RemoteCard?,
        strategy: MergeStrategy = MergeStrategy.LAST_WRITE_WINS,
    ): Resolution = when {
        local == null && remote == null -> Resolution.NoChange
        local == null && remote != null -> Resolution.UseRemote(remote)
        local != null && remote == null -> Resolution.UseLocal(local)
        else -> {
            val l = local!!
            val r = remote!!
            when (strategy) {
                MergeStrategy.PREFER_LOCAL -> Resolution.UseLocal(l)
                MergeStrategy.PREFER_REMOTE -> Resolution.UseRemote(r)
                MergeStrategy.MANUAL ->
                    if (l == r) Resolution.NoChange else Resolution.Conflict(l, r)
                MergeStrategy.LAST_WRITE_WINS -> when {
                    l == r -> Resolution.NoChange
                    l.updatedAt >= r.updatedAt -> Resolution.UseLocal(l)
                    else -> Resolution.UseRemote(r)
                }
            }
        }
    }

    /** Resolves a whole set, keyed by card id, returning the winning records. */
    fun merge(
        local: List<RemoteCard>,
        remote: List<RemoteCard>,
        strategy: MergeStrategy = MergeStrategy.LAST_WRITE_WINS,
    ): MergeOutcome {
        val byIdLocal = local.associateBy { it.id }
        val byIdRemote = remote.associateBy { it.id }
        val ids = byIdLocal.keys + byIdRemote.keys
        val winners = mutableListOf<RemoteCard>()
        val conflicts = mutableListOf<Resolution.Conflict>()
        ids.forEach { id ->
            when (val res = resolve(byIdLocal[id], byIdRemote[id], strategy)) {
                is Resolution.UseLocal -> winners.add(res.card)
                is Resolution.UseRemote -> winners.add(res.card)
                is Resolution.Conflict -> conflicts.add(res)
                Resolution.NoChange -> byIdLocal[id]?.let(winners::add)
            }
        }
        return MergeOutcome(winners, conflicts)
    }
}

/** Result of merging two card sets. */
data class MergeOutcome(
    val resolved: List<RemoteCard>,
    val conflicts: List<Resolution.Conflict>,
)
