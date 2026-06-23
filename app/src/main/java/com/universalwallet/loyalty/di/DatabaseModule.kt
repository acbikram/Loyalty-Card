package com.universalwallet.loyalty.di

import android.content.Context
import androidx.room.Room
import com.universalwallet.loyalty.core.utils.Constants
import com.universalwallet.loyalty.data.database.AppDatabase
import com.universalwallet.loyalty.data.database.MIGRATION_1_2
import com.universalwallet.loyalty.data.database.LoyaltyCardDao
import com.universalwallet.loyalty.data.database.StoreDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provides the Room database and its DAOs. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        Constants.DATABASE_NAME,
    )
        .addMigrations(MIGRATION_1_2)
        // Safety net for any unforeseen pre-release schema drift.
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideLoyaltyCardDao(db: AppDatabase): LoyaltyCardDao = db.loyaltyCardDao()

    @Provides
    fun provideStoreDao(db: AppDatabase): StoreDao = db.storeDao()
}
