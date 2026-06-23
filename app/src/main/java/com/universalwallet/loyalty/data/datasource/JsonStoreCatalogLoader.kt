package com.universalwallet.loyalty.data.datasource

import android.content.Context
import com.universalwallet.loyalty.core.plugin.StorePluginContract
import com.universalwallet.loyalty.core.plugin.StorePluginLoader
import com.universalwallet.loyalty.core.utils.Constants
import com.universalwallet.loyalty.core.utils.IoDispatcher
import com.universalwallet.loyalty.data.mapper.StoreMapper
import com.universalwallet.loyalty.data.model.StoreDefinitionDto
import com.universalwallet.loyalty.domain.model.StoreDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads every `*.json` file in `assets/stores/`, parses each into a
 * [StoreDefinition], and memoises the result. New stores are picked up
 * automatically — there is no hardcoded store list anywhere — so the catalogue
 * scales to hundreds of stores by simply adding files.
 *
 * It serves two roles via two interfaces:
 *  - [StoreCatalogDataSource] for the [StoreRepositoryImpl] (definitions), and
 *  - [StorePluginLoader] for the plugin registry (wrapped as contracts).
 *
 * Malformed individual files are logged and skipped rather than failing the
 * whole catalogue.
 */
@Singleton
class JsonStoreCatalogLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : StoreCatalogDataSource, StorePluginLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val mutex = Mutex()
    @Volatile
    private var cache: List<StoreDefinition>? = null

    override suspend fun loadStoreDefinitions(): List<StoreDefinition> {
        cache?.let { return it }
        return mutex.withLock {
            cache ?: parseCatalog().also { cache = it }
        }
    }

    override suspend fun getById(storeId: String): StoreDefinition? =
        loadStoreDefinitions().firstOrNull { it.storeId == storeId }

    override suspend fun loadAll(): List<StorePluginContract> =
        loadStoreDefinitions().map { DataDrivenStorePlugin(it) }

    private suspend fun parseCatalog(): List<StoreDefinition> = withContext(ioDispatcher) {
        val assets = context.assets
        val files = runCatching { assets.list(Constants.STORE_ASSETS_DIR) }
            .getOrNull()
            ?.filter { it.endsWith(SUFFIX, ignoreCase = true) }
            ?: emptyList()

        files.mapNotNull { fileName ->
            val path = "${Constants.STORE_ASSETS_DIR}/$fileName"
            runCatching {
                val text = assets.open(path).bufferedReader().use { it.readText() }
                val dto = json.decodeFromString(StoreDefinitionDto.serializer(), text)
                StoreMapper.fromDto(dto)
            }.onFailure { error ->
                Timber.tag(TAG).w(error, "Skipping malformed store file: %s", fileName)
            }.getOrNull()
        }
    }

    private companion object {
        const val TAG = "JsonStoreCatalogLoader"
        const val SUFFIX = ".json"
    }
}
