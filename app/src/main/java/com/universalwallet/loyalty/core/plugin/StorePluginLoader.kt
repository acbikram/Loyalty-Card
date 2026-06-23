package com.universalwallet.loyalty.core.plugin

/**
 * Strategy for sourcing [StorePluginContract]s to seed the
 * [StorePluginRegistry].
 *
 * The JSON-catalogue-backed implementation lives in the data layer
 * (`JsonStoreCatalogLoader`) and is bound to this interface via Hilt, so the
 * registry is populated from `assets/stores/` with no hardcoded store classes.
 */
interface StorePluginLoader {

    /**
     * Returns all plugins this loader can provide. Implementations must be
     * safe to call off the main thread; callers invoke it from a background
     * dispatcher during app initialisation.
     */
    suspend fun loadAll(): List<StorePluginContract>
}
