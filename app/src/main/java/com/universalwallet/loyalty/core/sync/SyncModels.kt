package com.universalwallet.loyalty.core.sync

import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import kotlinx.serialization.Serializable

/**
 * Wire model for a card as it travels to/from a cloud provider. Includes an
 * [updatedAt] for last-write-wins resolution and a [deleted] tombstone so
 * deletions propagate across devices. This is the stable contract a future API
 * layer serializes; it is intentionally decoupled from the Room entity.
 */
@Serializable
data class RemoteCard(
    val id: String,
    val storeId: String,
    val storeName: String,
    val cardNumber: String,
    val barcodeValue: String,
    val barcodeType: String,
    val category: String = CardCategory.GENERAL.name,
    val nickname: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val colorThemeId: String = "default",
    val updatedAt: Long,
    val deleted: Boolean = false,
)

/** Wire model for a store-catalogue entry (versioned for incremental sync). */
@Serializable
data class RemoteStore(
    val storeId: String,
    val storeName: String,
    val category: String,
    val catalogVersion: Int,
    val updatedAt: Long,
)

/** Identity of a participating device in a multi-device sync set. */
@Serializable
data class DeviceInfo(
    val deviceId: String,
    val displayName: String,
    val platform: String = "android",
    val lastSeenAt: Long = 0L,
)

/** Overall state of the sync subsystem, surfaced to the UI. */
enum class SyncStatus { IDLE, SYNCING, SUCCESS, FAILED, OFFLINE, NOT_CONFIGURED }

/** How to reconcile a card that changed on more than one device. */
enum class MergeStrategy { LAST_WRITE_WINS, PREFER_LOCAL, PREFER_REMOTE, MANUAL }

/** A single queued change awaiting upload. */
@Serializable
data class SyncOperation(
    val id: String,
    val type: OpType,
    val cardId: String,
    val payload: RemoteCard? = null,
    val attempts: Int = 0,
    val enqueuedAt: Long = 0L,
) {
    enum class OpType { UPSERT, DELETE }
}

/** Summary of a completed sync pass. */
data class SyncResult(
    val pushed: Int,
    val pulled: Int,
    val conflicts: Int,
    val status: SyncStatus,
)

/** Errors the sync layer can surface, with friendly messages. */
sealed class SyncError(val message: String) {
    data object NotConfigured : SyncError("Cloud sync isn't set up yet.")
    data object NotAuthenticated : SyncError("Sign in to sync across devices.")
    data object Offline : SyncError("You're offline. Changes will sync later.")
    data class Transport(val detail: String) : SyncError("Sync failed. Will retry.")
}

/** Mapping helpers between the domain card and the wire model. */
fun LoyaltyCard.toRemote(): RemoteCard = RemoteCard(
    id = id,
    storeId = storeId,
    storeName = storeName,
    cardNumber = cardNumber,
    barcodeValue = barcodeValue,
    barcodeType = barcodeType.name,
    category = category.name,
    nickname = nickname,
    notes = notes,
    isFavorite = isFavorite,
    colorThemeId = colorThemeId,
    updatedAt = updatedAt,
    deleted = false,
)

fun RemoteCard.toDomain(now: Long): LoyaltyCard = LoyaltyCard(
    id = id,
    storeId = storeId,
    storeName = storeName,
    cardNumber = cardNumber,
    barcodeValue = barcodeValue,
    barcodeType = BarcodeType.fromName(barcodeType),
    category = CardCategory.fromName(category),
    nickname = nickname,
    notes = notes,
    isFavorite = isFavorite,
    createdAt = now,
    updatedAt = updatedAt,
    colorThemeId = colorThemeId,
)
