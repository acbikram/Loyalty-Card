package com.universalwallet.loyalty.core.organize

import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import javax.inject.Inject

/**
 * Thin domain service for the favourite/pin toggles, so ViewModels don't each
 * re-implement the copy-and-persist dance. Persistence is delegated to the
 * repository (the single source of truth).
 */
class FavoriteManager @Inject constructor(
    private val repository: LoyaltyCardRepository,
) {
    suspend fun toggleFavorite(card: LoyaltyCard): DataResult<Unit> =
        repository.setFavorite(card.id, !card.isFavorite)

    suspend fun setFavorite(id: String, favorite: Boolean): DataResult<Unit> =
        repository.setFavorite(id, favorite)

    suspend fun togglePinned(card: LoyaltyCard): DataResult<Unit> =
        repository.setPinned(card.id, !card.isPinned)
}
