package com.universalwallet.loyalty.data.repository

import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.data.database.StoreDao
import com.universalwallet.loyalty.data.datasource.StoreCatalogDataSource
import com.universalwallet.loyalty.data.mapper.StoreMapper
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.StoreDefinition
import com.universalwallet.loyalty.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Store catalogue repository. The JSON catalogue ([StoreCatalogDataSource]) is
 * the authoring source; it is synced into Room ([StoreDao]) once on first
 * observation and on explicit [syncCatalog]. Queries go through the DAO so the
 * cache is the single source of truth for reads.
 */
class StoreRepositoryImpl @Inject constructor(
    private val dao: StoreDao,
    private val catalog: StoreCatalogDataSource,
) : BaseRepository(), StoreRepository {

    private val seeded = AtomicBoolean(false)

    override fun observeStores(): Flow<List<StoreDefinition>> =
        dao.getAll().onStart { ensureSeeded() }.map(StoreMapper::toDomainList)

    override fun observeActiveStores(): Flow<List<StoreDefinition>> =
        dao.getActive().onStart { ensureSeeded() }.map(StoreMapper::toDomainList)

    override fun observeByCategory(category: CardCategory): Flow<List<StoreDefinition>> =
        dao.getByCategory(category.name).onStart { ensureSeeded() }.map(StoreMapper::toDomainList)

    override fun searchStores(query: String): Flow<List<StoreDefinition>> =
        dao.search(query).onStart { ensureSeeded() }.map(StoreMapper::toDomainList)

    override suspend fun getStore(storeId: String): DataResult<StoreDefinition> = safeCall {
        ensureSeeded()
        dao.getById(storeId)?.let(StoreMapper::toDomain)
            ?: catalog.getById(storeId)
            ?: throw NoSuchElementException("No store with id $storeId")
    }

    override suspend fun syncCatalog(): DataResult<Int> = safeCall {
        val definitions = catalog.loadStoreDefinitions()
        dao.upsertAll(StoreMapper.toEntityList(definitions))
        definitions.size
    }

    /** Seeds the Room cache from JSON exactly once (best-effort). */
    private suspend fun ensureSeeded() {
        if (seeded.compareAndSet(false, true)) {
            runCatching {
                if (dao.count() == 0) {
                    val definitions = catalog.loadStoreDefinitions()
                    dao.upsertAll(StoreMapper.toEntityList(definitions))
                }
            }.onFailure { seeded.set(false) }
        }
    }
}
