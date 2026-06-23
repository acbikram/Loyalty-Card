package com.universalwallet.loyalty.di

import com.universalwallet.loyalty.core.logging.AppLogger
import com.universalwallet.loyalty.core.logging.DefaultAppLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds foundation-layer interfaces to their concrete implementations.
 *
 * `StorePluginRegistry`, `ErrorHandler`, `ArchitectureValidator`, and
 * `AppInitializer` are constructor-injected and need no binding here. The
 * `StorePluginLoader` binding lives in `DataModule`, since its implementation
 * (the JSON catalogue loader) belongs to the data layer.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FoundationModule {

    @Binds
    @Singleton
    abstract fun bindAppLogger(impl: DefaultAppLogger): AppLogger
}
