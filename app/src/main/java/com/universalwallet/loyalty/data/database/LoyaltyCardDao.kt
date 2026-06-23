package com.universalwallet.loyalty.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data-access object for [LoyaltyCardEntity]. Observation queries return cold
 * [Flow]s so the repository (and ultimately the UI) react to changes; one-shot
 * operations are suspend functions. SQLite `LIKE` is case-insensitive for ASCII.
 */
@Dao
interface LoyaltyCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: LoyaltyCardEntity)

    @Update
    suspend fun update(card: LoyaltyCardEntity)

    @Delete
    suspend fun delete(card: LoyaltyCardEntity)

    @Query("DELETE FROM loyalty_cards WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM loyalty_cards WHERE id = :id")
    suspend fun getById(id: String): LoyaltyCardEntity?

    @Query("SELECT * FROM loyalty_cards ORDER BY storeName COLLATE NOCASE ASC")
    fun getAll(): Flow<List<LoyaltyCardEntity>>

    @Query("SELECT * FROM loyalty_cards WHERE isArchived = 0 AND isHidden = 0 ORDER BY storeName COLLATE NOCASE ASC")
    fun getActive(): Flow<List<LoyaltyCardEntity>>

    @Query("SELECT * FROM loyalty_cards WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchived(): Flow<List<LoyaltyCardEntity>>

    @Query("SELECT * FROM loyalty_cards WHERE isArchived = 0 AND isHidden = 0 ORDER BY usageCount DESC, lastUsedTimestamp DESC LIMIT :limit")
    fun getMostUsed(limit: Int): Flow<List<LoyaltyCardEntity>>

    @Query(
        "SELECT * FROM loyalty_cards WHERE " +
            "storeName LIKE '%' || :query || '%' OR " +
            "cardNumber LIKE '%' || :query || '%' OR " +
            "nickname LIKE '%' || :query || '%' OR " +
            "barcodeValue LIKE '%' || :query || '%' " +
            "ORDER BY lastUsedTimestamp DESC",
    )
    fun search(query: String): Flow<List<LoyaltyCardEntity>>

    @Query("SELECT * FROM loyalty_cards WHERE category = :category ORDER BY storeName COLLATE NOCASE ASC")
    fun getByCategory(category: String): Flow<List<LoyaltyCardEntity>>

    @Query("SELECT * FROM loyalty_cards WHERE storeId = :storeId ORDER BY createdAt DESC")
    fun getByStore(storeId: String): Flow<List<LoyaltyCardEntity>>

    @Query("SELECT * FROM loyalty_cards WHERE isFavorite = 1 ORDER BY storeName COLLATE NOCASE ASC")
    fun getFavorites(): Flow<List<LoyaltyCardEntity>>

    @Query("SELECT * FROM loyalty_cards ORDER BY lastUsedTimestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<LoyaltyCardEntity>>

    @Query("SELECT COUNT(*) FROM loyalty_cards WHERE storeId = :storeId AND cardNumber = :cardNumber")
    suspend fun countByStoreAndNumber(storeId: String, cardNumber: String): Int

    @Query("UPDATE loyalty_cards SET lastUsedTimestamp = :timestamp, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: String, timestamp: Long)

    @Query("UPDATE loyalty_cards SET usageCount = usageCount + 1, lastUsedTimestamp = :timestamp, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementUsage(id: String, timestamp: Long)

    @Query("UPDATE loyalty_cards SET isFavorite = :favorite, updatedAt = :timestamp WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean, timestamp: Long)

    @Query("UPDATE loyalty_cards SET isPinned = :pinned, updatedAt = :timestamp WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean, timestamp: Long)

    @Query("UPDATE loyalty_cards SET isArchived = :archived, updatedAt = :timestamp WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, timestamp: Long)

    @Query("UPDATE loyalty_cards SET isHidden = :hidden, updatedAt = :timestamp WHERE id = :id")
    suspend fun setHidden(id: String, hidden: Boolean, timestamp: Long)

    @Query("UPDATE loyalty_cards SET sortIndex = :index, updatedAt = :timestamp WHERE id = :id")
    suspend fun setSortIndex(id: String, index: Int, timestamp: Long)

    @Query("SELECT COUNT(*) FROM loyalty_cards")
    suspend fun count(): Int
}
