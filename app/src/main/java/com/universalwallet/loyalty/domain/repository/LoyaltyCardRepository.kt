package com.universalwallet.loyalty.domain.repository

import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for loyalty-card storage. Observation methods expose cold
 * [Flow]s from the single source of truth (Room); mutations return a
 * [DataResult]. The domain layer depends only on this interface — never on Room.
 */
interface LoyaltyCardRepository {
    fun observeCards(): Flow<List<LoyaltyCard>>
    fun observeActiveCards(): Flow<List<LoyaltyCard>>
    fun observeArchived(): Flow<List<LoyaltyCard>>
    fun observeMostUsed(limit: Int = 10): Flow<List<LoyaltyCard>>
    fun observeFavorites(): Flow<List<LoyaltyCard>>
    fun observeRecent(limit: Int = 10): Flow<List<LoyaltyCard>>
    fun observeByCategory(category: CardCategory): Flow<List<LoyaltyCard>>
    fun observeByStore(storeId: String): Flow<List<LoyaltyCard>>
    fun searchCards(query: String): Flow<List<LoyaltyCard>>

    suspend fun getCard(id: String): DataResult<LoyaltyCard>
    suspend fun addCard(card: LoyaltyCard): DataResult<LoyaltyCard>
    suspend fun updateCard(card: LoyaltyCard): DataResult<LoyaltyCard>
    suspend fun deleteCard(id: String): DataResult<Unit>
    suspend fun markUsed(id: String, timestamp: Long): DataResult<Unit>
    suspend fun incrementUsage(id: String): DataResult<Unit>
    suspend fun setFavorite(id: String, favorite: Boolean): DataResult<Unit>
    suspend fun setPinned(id: String, pinned: Boolean): DataResult<Unit>
    suspend fun setArchived(id: String, archived: Boolean): DataResult<Unit>
    suspend fun setHidden(id: String, hidden: Boolean): DataResult<Unit>
    suspend fun setManualOrder(orderedIds: List<String>): DataResult<Unit>
    suspend fun duplicateCard(id: String): DataResult<LoyaltyCard>
}
