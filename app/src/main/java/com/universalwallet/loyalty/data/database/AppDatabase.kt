package com.universalwallet.loyalty.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The Room database — the single source of truth for the offline-first app.
 *
 * Version 2 adds Part 4B organization/usage columns via [MIGRATION_1_2].
 * Two tables: loyalty cards and the cached store catalogue.
 */
@Database(
    entities = [
        LoyaltyCardEntity::class,
        StoreEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun loyaltyCardDao(): LoyaltyCardDao
    abstract fun storeDao(): StoreDao
}
