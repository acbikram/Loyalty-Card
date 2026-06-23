package com.universalwallet.loyalty.domain.repository

import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.StoreDefinition
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for the store catalogue. Backed by the JSON catalogue cached
 * in Room; [syncCatalog] (re)loads JSON into the cache, and observation methods
 * stream the cached definitions.
 */
interface StoreRepository {
    fun observeStores(): Flow<List<StoreDefinition>>
    fun observeActiveStores(): Flow<List<StoreDefinition>>
    fun observeByCategory(category: CardCategory): Flow<List<StoreDefinition>>
    fun searchStores(query: String): Flow<List<StoreDefinition>>

    suspend fun getStore(storeId: String): DataResult<StoreDefinition>
    suspend fun syncCatalog(): DataResult<Int>
}
