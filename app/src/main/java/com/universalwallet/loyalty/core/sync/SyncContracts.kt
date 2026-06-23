package com.universalwallet.loyalty.core.sync

/**
 * Cloud sync architecture — interfaces only (no live backend in this phase).
 * A future provider (Drive/iCloud/custom) implements [CloudSyncProvider]; the
 * rest of the app depends on these abstractions so the backend can be swapped
 * without touching feature code.
 */
interface CloudSyncProvider {
    /** Stable provider id (e.g. "google_drive", "local_only"). */
    val id: String

    suspend fun isAuthenticated(): Boolean

    /** Pulls cards changed since [sinceMillis] (incremental sync). */
    suspend fun pull(sinceMillis: Long): Result<List<RemoteCard>>

    /** Pushes upserts/tombstones to the remote. */
    suspend fun push(cards: List<RemoteCard>): Result<Unit>

    /** Lists devices participating in this account's sync set. */
    suspend fun devices(): Result<List<DeviceInfo>>
}

/** Authentication contract for a cloud account (interface only). */
interface AuthenticationProvider {
    val providerName: String
    suspend fun signIn(): Result<String?>
    suspend fun signOut()
    fun currentAccount(): String?
    fun isSignedIn(): Boolean
}

/** Stable per-device identity used to attribute changes. */
interface DeviceIdentity {
    fun deviceId(): String
    fun deviceName(): String
}

/** Durable queue of pending changes (offline + retry). */
interface SyncQueue {
    suspend fun enqueue(operation: SyncOperation)
    suspend fun pending(): List<SyncOperation>
    suspend fun markAttempted(id: String)
    suspend fun remove(id: String)
    suspend fun clear()
}

/**
 * Transport-level encryption for sync payloads (interface only). A future
 * implementation reuses the Keystore/password crypto from Part 5A so data is
 * encrypted end-to-end before it leaves the device.
 */
interface SyncCrypto {
    fun encryptPayload(plaintext: String): String
    fun decryptPayload(payload: String): String
}
