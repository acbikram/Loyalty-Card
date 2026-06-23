package com.universalwallet.loyalty

import com.universalwallet.loyalty.core.result.AppError
import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [LoyaltyCardRepository] for unit tests. Backed by a StateFlow so
 * observers see mutations immediately; mutations succeed and return the data.
 */
class FakeLoyaltyCardRepository(initial: List<LoyaltyCard> = emptyList()) : LoyaltyCardRepository {

    private val cards = MutableStateFlow(initial)

    fun snapshot(): List<LoyaltyCard> = cards.value

    override fun observeCards(): Flow<List<LoyaltyCard>> = cards
    override fun observeActiveCards(): Flow<List<LoyaltyCard>> =
        cards.map { list -> list.filter { !it.isArchived && !it.isHidden } }
    override fun observeArchived(): Flow<List<LoyaltyCard>> =
        cards.map { list -> list.filter { it.isArchived } }
    override fun observeMostUsed(limit: Int): Flow<List<LoyaltyCard>> =
        cards.map { list -> list.sortedByDescending { it.usageCount }.take(limit) }
    override fun observeFavorites(): Flow<List<LoyaltyCard>> =
        cards.map { list -> list.filter { it.isFavorite } }
    override fun observeRecent(limit: Int): Flow<List<LoyaltyCard>> =
        cards.map { list -> list.sortedByDescending { it.lastUsedTimestamp }.take(limit) }
    override fun observeByCategory(category: CardCategory): Flow<List<LoyaltyCard>> =
        cards.map { list -> list.filter { it.category == category } }
    override fun observeByStore(storeId: String): Flow<List<LoyaltyCard>> =
        cards.map { list -> list.filter { it.storeId == storeId } }
    override fun searchCards(query: String): Flow<List<LoyaltyCard>> =
        cards.map { list -> list.filter { it.storeName.contains(query, ignoreCase = true) } }

    override suspend fun getCard(id: String): DataResult<LoyaltyCard> =
        cards.value.firstOrNull { it.id == id }
            ?.let { DataResult.Success(it) }
            ?: DataResult.Failure(AppError.Validation("Card not found"))

    override suspend fun addCard(card: LoyaltyCard): DataResult<LoyaltyCard> {
        cards.value = cards.value + card
        return DataResult.Success(card)
    }

    override suspend fun updateCard(card: LoyaltyCard): DataResult<LoyaltyCard> {
        cards.value = cards.value.map { if (it.id == card.id) card else it }
        return DataResult.Success(card)
    }

    override suspend fun deleteCard(id: String): DataResult<Unit> {
        cards.value = cards.value.filterNot { it.id == id }
        return DataResult.Success(Unit)
    }

    override suspend fun markUsed(id: String, timestamp: Long): DataResult<Unit> =
        mutate(id) { it.copy(lastUsedTimestamp = timestamp) }

    override suspend fun incrementUsage(id: String): DataResult<Unit> =
        mutate(id) { it.copy(usageCount = it.usageCount + 1) }

    override suspend fun setFavorite(id: String, favorite: Boolean): DataResult<Unit> =
        mutate(id) { it.copy(isFavorite = favorite) }

    override suspend fun setPinned(id: String, pinned: Boolean): DataResult<Unit> =
        mutate(id) { it.copy(isPinned = pinned) }

    override suspend fun setArchived(id: String, archived: Boolean): DataResult<Unit> =
        mutate(id) { it.copy(isArchived = archived) }

    override suspend fun setHidden(id: String, hidden: Boolean): DataResult<Unit> =
        mutate(id) { it.copy(isHidden = hidden) }

    override suspend fun setManualOrder(orderedIds: List<String>): DataResult<Unit> {
        val byId = cards.value.associateBy { it.id }
        cards.value = orderedIds.mapNotNull { byId[it] } + cards.value.filter { it.id !in orderedIds }
        return DataResult.Success(Unit)
    }

    override suspend fun duplicateCard(id: String): DataResult<LoyaltyCard> {
        val original = cards.value.firstOrNull { it.id == id }
            ?: return DataResult.Failure(AppError.Validation("Card not found"))
        val copy = original.copy(id = LoyaltyCard.newId())
        cards.value = cards.value + copy
        return DataResult.Success(copy)
    }

    private fun mutate(id: String, transform: (LoyaltyCard) -> LoyaltyCard): DataResult<Unit> {
        cards.value = cards.value.map { if (it.id == id) transform(it) else it }
        return DataResult.Success(Unit)
    }
}
