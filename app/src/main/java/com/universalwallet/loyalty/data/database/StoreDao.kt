package com.universalwallet.loyalty.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data-access object for [StoreEntity] (the local store catalogue cache). */
@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stores: List<StoreEntity>)

    @Query("SELECT * FROM stores WHERE storeId = :storeId")
    suspend fun getById(storeId: String): StoreEntity?

    @Query("SELECT * FROM stores ORDER BY storeName COLLATE NOCASE ASC")
    fun getAll(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE isActive = 1 ORDER BY storeName COLLATE NOCASE ASC")
    fun getActive(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE category = :category ORDER BY storeName COLLATE NOCASE ASC")
    fun getByCategory(category: String): Flow<List<StoreEntity>>

    @Query(
        "SELECT * FROM stores WHERE " +
            "storeName LIKE '%' || :query || '%' OR " +
            "keywords LIKE '%' || :query || '%' " +
            "ORDER BY storeName COLLATE NOCASE ASC",
    )
    fun search(query: String): Flow<List<StoreEntity>>

    @Query("SELECT COUNT(*) FROM stores")
    suspend fun count(): Int
}
