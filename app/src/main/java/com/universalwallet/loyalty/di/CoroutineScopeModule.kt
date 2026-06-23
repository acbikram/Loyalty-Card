package com.universalwallet.loyalty.di

import com.universalwallet.loyalty.core.utils.ApplicationScope
import com.universalwallet.loyalty.core.utils.DefaultDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Provides a process-lifetime [CoroutineScope] for fire-and-forget work that
 * must outlive any single screen (e.g. settings persistence, future sync).
 *
 * A [SupervisorJob] ensures one failed child does not cancel the whole scope.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
