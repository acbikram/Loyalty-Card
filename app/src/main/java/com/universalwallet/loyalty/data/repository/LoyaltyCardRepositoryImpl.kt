package com.universalwallet.loyalty.data.repository

import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.core.result.toAppError
import com.universalwallet.loyalty.data.database.LoyaltyCardDao
import com.universalwallet.loyalty.data.mapper.LoyaltyCardMapper
import com.universalwallet.loyalty.data.model.DataError
import com.universalwallet.loyalty.data.model.toAppError
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room-backed implementation of [LoyaltyCardRepository]. Maps Entity ↔ Domain
 * via [LoyaltyCardMapper], wraps failures with the [BaseRepository] helpers, and
 * delegates ranked search to [CardSearchEngine]. Room is never exposed upward.
 */
class LoyaltyCardRepositoryImpl @Inject constructor(
    private val dao: LoyaltyCardDao,
    private val searchEngine: CardSearchEngine,
) : BaseRepository(), LoyaltyCardRepository {

    override fun observeCards(): Flow<List<LoyaltyCard>> =
        dao.getAll().map(LoyaltyCardMapper::toDomainList)

    override fun observeActiveCards(): Flow<List<LoyaltyCard>> =
        dao.getActive().map(LoyaltyCardMapper::toDomainList)

    override fun observeArchived(): Flow<List<LoyaltyCard>> =
        dao.getArchived().map(LoyaltyCardMapper::toDomainList)

    override fun observeMostUsed(limit: Int): Flow<List<LoyaltyCard>> =
        dao.getMostUsed(limit).map(LoyaltyCardMapper::toDomainList)

    override fun observeFavorites(): Flow<List<LoyaltyCard>> =
        dao.getFavorites().map(LoyaltyCardMapper::toDomainList)

    override fun observeRecent(limit: Int): Flow<List<LoyaltyCard>> =
        dao.getRecent(limit).map(LoyaltyCardMapper::toDomainList)

    override fun observeByCategory(category: CardCategory): Flow<List<LoyaltyCard>> =
        dao.getByCategory(category.name).map(LoyaltyCardMapper::toDomainList)

    override fun observeByStore(storeId: String): Flow<List<LoyaltyCard>> =
        dao.getByStore(storeId).map(LoyaltyCardMapper::toDomainList)

    override fun searchCards(query: String): Flow<List<LoyaltyCard>> =
        dao.getAll().map { entities ->
            searchEngine.search(query, LoyaltyCardMapper.toDomainList(entities))
        }

    override suspend fun getCard(id: String): DataResult<LoyaltyCard> = safeCall {
        dao.getById(id)?.let(LoyaltyCardMapper::toDomain)
            ?: throw NoSuchElementException("No card with id $id")
    }

    override suspend fun addCard(card: LoyaltyCard): DataResult<LoyaltyCard> =
        try {
            if (dao.countByStoreAndNumber(card.storeId, card.cardNumber) > 0) {
                DataResult.Failure(DataError.DuplicateError(card.storeId, card.cardNumber).toAppError())
            } else {
                dao.insert(LoyaltyCardMapper.toEntity(card))
                DataResult.Success(card)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            DataResult.Failure(throwable.toAppError())
        }

    override suspend fun updateCard(card: LoyaltyCard): DataResult<LoyaltyCard> = safeCall {
        val updated = card.copy(updatedAt = System.currentTimeMillis())
        dao.update(LoyaltyCardMapper.toEntity(updated))
        updated
    }

    override suspend fun deleteCard(id: String): DataResult<Unit> = safeCall {
        dao.deleteById(id)
    }

    override suspend fun markUsed(id: String, timestamp: Long): DataResult<Unit> = safeCall {
        dao.updateLastUsed(id, timestamp)
    }

    override suspend fun incrementUsage(id: String): DataResult<Unit> = safeCall {
        dao.incrementUsage(id, System.currentTimeMillis())
    }

    override suspend fun setFavorite(id: String, favorite: Boolean): DataResult<Unit> = safeCall {
        dao.setFavorite(id, favorite, System.currentTimeMillis())
    }

    override suspend fun setPinned(id: String, pinned: Boolean): DataResult<Unit> = safeCall {
        dao.setPinned(id, pinned, System.currentTimeMillis())
    }

    override suspend fun setArchived(id: String, archived: Boolean): DataResult<Unit> = safeCall {
        dao.setArchived(id, archived, System.currentTimeMillis())
    }

    override suspend fun setHidden(id: String, hidden: Boolean): DataResult<Unit> = safeCall {
        dao.setHidden(id, hidden, System.currentTimeMillis())
    }

    override suspend fun setManualOrder(orderedIds: List<String>): DataResult<Unit> = safeCall {
        val now = System.currentTimeMillis()
        orderedIds.forEachIndexed { index, id -> dao.setSortIndex(id, index, now) }
    }

    override suspend fun duplicateCard(id: String): DataResult<LoyaltyCard> = safeCall {
        val original = dao.getById(id)?.let(LoyaltyCardMapper::toDomain)
            ?: throw NoSuchElementException("No card with id $id")
        val now = System.currentTimeMillis()
        val copy = original.copy(
            id = LoyaltyCard.newId(),
            nickname = (original.nickname.ifBlank { original.storeName }) + " (copy)",
            isFavorite = false,
            isPinned = false,
            usageCount = 0,
            lastUsedTimestamp = 0L,
            createdAt = now,
            updatedAt = now,
        )
        dao.insert(LoyaltyCardMapper.toEntity(copy))
        copy
    }
}
