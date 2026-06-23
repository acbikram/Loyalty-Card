package com.universalwallet.loyalty.di

import com.universalwallet.loyalty.core.plugin.StorePluginLoader
import com.universalwallet.loyalty.data.datasource.JsonStoreCatalogLoader
import com.universalwallet.loyalty.data.datasource.StoreCatalogDataSource
import com.universalwallet.loyalty.data.repository.LoyaltyCardRepositoryImpl
import com.universalwallet.loyalty.data.repository.StoreRepositoryImpl
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import com.universalwallet.loyalty.domain.repository.StoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds data-layer implementations to their domain/contract interfaces. The
 * single [JsonStoreCatalogLoader] satisfies both the data-source contract and
 * the plugin-loader contract, so the JSON catalogue feeds both the store
 * repository and the plugin registry.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindLoyaltyCardRepository(impl: LoyaltyCardRepositoryImpl): LoyaltyCardRepository

    @Binds
    @Singleton
    abstract fun bindStoreRepository(impl: StoreRepositoryImpl): StoreRepository

    @Binds
    @Singleton
    abstract fun bindStoreCatalogDataSource(impl: JsonStoreCatalogLoader): StoreCatalogDataSource

    @Binds
    @Singleton
    abstract fun bindStorePluginLoader(impl: JsonStoreCatalogLoader): StorePluginLoader
}
