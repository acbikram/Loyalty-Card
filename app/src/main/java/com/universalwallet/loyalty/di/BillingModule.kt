package com.universalwallet.loyalty.di

import com.universalwallet.loyalty.core.billing.EntitlementProvider
import com.universalwallet.loyalty.core.billing.LocalEntitlementProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the entitlement source. Today this is the local provider (the app ships
 * free); a future Google Play Billing module swaps the binding here without
 * touching feature code.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {

    @Binds
    @Singleton
    abstract fun bindEntitlementProvider(impl: LocalEntitlementProvider): EntitlementProvider
}
