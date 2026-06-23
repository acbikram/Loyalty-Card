package com.universalwallet.loyalty.di

import com.universalwallet.loyalty.core.utils.DefaultDispatcher
import com.universalwallet.loyalty.core.utils.DispatcherProvider
import com.universalwallet.loyalty.core.utils.IoDispatcher
import com.universalwallet.loyalty.core.utils.MainDispatcher
import com.universalwallet.loyalty.core.utils.MainImmediateDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Provides the application's coroutine dispatchers, each behind a qualifier,
 * plus a convenience [DispatcherProvider] aggregate.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @MainImmediateDispatcher
    fun provideMainImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate

    @Provides
    @Singleton
    fun provideDispatcherProvider(
        @IoDispatcher io: CoroutineDispatcher,
        @DefaultDispatcher default: CoroutineDispatcher,
        @MainDispatcher main: CoroutineDispatcher,
        @MainImmediateDispatcher mainImmediate: CoroutineDispatcher,
    ): DispatcherProvider = object : DispatcherProvider {
        override val io: CoroutineDispatcher = io
        override val default: CoroutineDispatcher = default
        override val main: CoroutineDispatcher = main
        override val mainImmediate: CoroutineDispatcher = mainImmediate
    }
}
