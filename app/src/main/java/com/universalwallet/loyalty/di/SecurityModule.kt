package com.universalwallet.loyalty.di

import com.universalwallet.loyalty.core.security.Encryptor
import com.universalwallet.loyalty.core.security.KeystoreEncryptionManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds the security abstractions to their Keystore-backed implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindEncryptor(impl: KeystoreEncryptionManager): Encryptor
}
