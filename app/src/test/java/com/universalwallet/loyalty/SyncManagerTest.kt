package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.sync.ConflictResolver
import com.universalwallet.loyalty.core.sync.DeviceIdentity
import com.universalwallet.loyalty.core.sync.InMemorySyncQueue
import com.universalwallet.loyalty.core.sync.LocalOnlyCloudSyncProvider
import com.universalwallet.loyalty.core.sync.SyncManager
import com.universalwallet.loyalty.core.sync.SyncStatus
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SyncManagerTest {

    private fun manager(): SyncManager {
        val deviceIdentity = object : DeviceIdentity {
            override fun deviceId() = "test-device"
            override fun deviceName() = "Test Device"
        }
        return SyncManager(
            provider = LocalOnlyCloudSyncProvider(),
            conflictResolver = ConflictResolver(),
            queue = InMemorySyncQueue(),
            deviceIdentity = deviceIdentity,
        )
    }

    @Test
    fun localOnlyProviderShortCircuitsToNotConfigured() = runBlocking {
        val (result, cards) = manager().sync(localCards = emptyList(), lastSyncAtMillis = 0L)
        assertThat(result.status).isEqualTo(SyncStatus.NOT_CONFIGURED)
        assertThat(result.pushed).isEqualTo(0)
        assertThat(cards).isEmpty()
    }

    @Test
    fun deviceInfoIsPopulated() {
        val info = manager().deviceInfo()
        assertThat(info.deviceId).isEqualTo("test-device")
        assertThat(info.displayName).isEqualTo("Test Device")
    }

    @Test
    fun enqueueStoresPendingOperation() = runBlocking {
        val queue = InMemorySyncQueue()
        val mgr = SyncManager(
            provider = LocalOnlyCloudSyncProvider(),
            conflictResolver = ConflictResolver(),
            queue = queue,
            deviceIdentity = object : DeviceIdentity {
                override fun deviceId() = "d"
                override fun deviceName() = "n"
            },
        )
        mgr.enqueueDelete("card-1")
        assertThat(queue.pending()).hasSize(1)
    }
}
