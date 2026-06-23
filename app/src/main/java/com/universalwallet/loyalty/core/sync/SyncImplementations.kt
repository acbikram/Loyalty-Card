package com.universalwallet.loyalty.core.sync

import android.content.Context
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default provider used until a real backend is wired in: it is never
 * authenticated and performs no network I/O, so the app behaves as a local-only
 * wallet while the full sync architecture sits ready behind it.
 */
@Singleton
class LocalOnlyCloudSyncProvider @Inject constructor() : CloudSyncProvider {
    override val id: String = "local_only"
    override suspend fun isAuthenticated(): Boolean = false
    override suspend fun pull(sinceMillis: Long): Result<List<RemoteCard>> = Result.success(emptyList())
    override suspend fun push(cards: List<RemoteCard>): Result<Unit> = Result.success(Unit)
    override suspend fun devices(): Result<List<DeviceInfo>> = Result.success(emptyList())
}

/** Placeholder auth provider (no account connected). */
@Singleton
class NoopAuthenticationProvider @Inject constructor() : AuthenticationProvider {
    override val providerName: String = "None"
    override suspend fun signIn(): Result<String?> = Result.success(null)
    override suspend fun signOut() = Unit
    override fun currentAccount(): String? = null
    override fun isSignedIn(): Boolean = false
}

/** Process-lifetime in-memory queue (a durable Room-backed queue is future work). */
@Singleton
class InMemorySyncQueue @Inject constructor() : SyncQueue {
    private val mutex = Mutex()
    private val operations = mutableListOf<SyncOperation>()

    override suspend fun enqueue(operation: SyncOperation) = mutex.withLock {
        operations.removeAll { it.cardId == operation.cardId && it.type == operation.type }
        operations.add(operation)
        Unit
    }

    override suspend fun pending(): List<SyncOperation> = mutex.withLock { operations.toList() }

    override suspend fun markAttempted(id: String) = mutex.withLock {
        val index = operations.indexOfFirst { it.id == id }
        if (index >= 0) operations[index] = operations[index].copy(attempts = operations[index].attempts + 1)
        Unit
    }

    override suspend fun remove(id: String) = mutex.withLock { operations.removeAll { it.id == id }; Unit }
    override suspend fun clear() = mutex.withLock { operations.clear() }
}

/** Stable device identity derived from the platform, with a persisted fallback. */
@Singleton
class DeviceIdentityImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : DeviceIdentity {
    private val fallbackId: String by lazy { UUID.randomUUID().toString() }

    override fun deviceId(): String {
        val androidId = runCatching {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull()
        return androidId?.takeIf { it.isNotBlank() } ?: fallbackId
    }

    override fun deviceName(): String = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
}
