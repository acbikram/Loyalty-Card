package com.universalwallet.loyalty.di

import com.universalwallet.loyalty.core.sync.AuthenticationProvider
import com.universalwallet.loyalty.core.sync.CloudSyncProvider
import com.universalwallet.loyalty.core.sync.DeviceIdentity
import com.universalwallet.loyalty.core.sync.DeviceIdentityImpl
import com.universalwallet.loyalty.core.sync.InMemorySyncQueue
import com.universalwallet.loyalty.core.sync.LocalOnlyCloudSyncProvider
import com.universalwallet.loyalty.core.sync.NoopAuthenticationProvider
import com.universalwallet.loyalty.core.sync.SyncQueue
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds the sync architecture to its default (local-only) implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds @Singleton
    abstract fun bindCloudSyncProvider(impl: LocalOnlyCloudSyncProvider): CloudSyncProvider

    @Binds @Singleton
    abstract fun bindAuthProvider(impl: NoopAuthenticationProvider): AuthenticationProvider

    @Binds @Singleton
    abstract fun bindSyncQueue(impl: InMemorySyncQueue): SyncQueue

    @Binds @Singleton
    abstract fun bindDeviceIdentity(impl: DeviceIdentityImpl): DeviceIdentity
}
