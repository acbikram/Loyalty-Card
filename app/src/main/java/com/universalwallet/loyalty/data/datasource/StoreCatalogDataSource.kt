package com.universalwallet.loyalty.data.datasource

import com.universalwallet.loyalty.domain.model.StoreDefinition

/**
 * Source of truth for store definitions before they are cached in Room. The
 * concrete implementation reads the JSON catalogue; defining it as an interface
 * keeps the repository testable with a fake catalogue.
 */
interface StoreCatalogDataSource {
    /** Loads (and memoises) all store definitions from the catalogue. */
    suspend fun loadStoreDefinitions(): List<StoreDefinition>

    /** Fast lookup of a single definition by store id. */
    suspend fun getById(storeId: String): StoreDefinition?
}
