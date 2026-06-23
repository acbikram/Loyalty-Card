package com.universalwallet.loyalty.core.plugin

import com.universalwallet.loyalty.core.result.AppError
import com.universalwallet.loyalty.core.result.DataResult
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe registry of [StorePluginContract]s, keyed by store id.
 *
 * ### Why a registry?
 * Stores differ only in data and a handful of rules. A registry lets the app
 * treat every store uniformly through [StorePluginContract] while keeping the
 * concrete plugins fully decoupled from feature code — features ask the
 * registry for a plugin by id and never reference a store directly.
 *
 * ### Scaling to 500+ stores
 * Resolution is an O(1) map lookup regardless of catalogue size, and plugins
 * are registered from data (the bundled JSON catalogue in a later phase) rather
 * than code, so adding stores never touches the registry or any feature.
 *
 * ### Avoiding tight coupling
 * Nothing depends on a specific store type; everything depends on the contract
 * and on this registry. New stores cannot create new compile-time dependencies.
 *
 * A [fallback] (the [GenericStorePlugin]) is always present, so [resolve] never
 * returns null — unknown ids degrade gracefully to the custom-card behaviour.
 */
@Singleton
class StorePluginRegistry @Inject constructor() {

    private val plugins = ConcurrentHashMap<String, StorePluginContract>()

    /** The always-available generic plugin returned when no match is found. */
    val fallback: StorePluginContract = GenericStorePlugin()

    init {
        // The fallback is also addressable by its own id.
        plugins[fallback.getStoreId()] = fallback
    }

    /**
     * Registers [plugin], refusing to overwrite an existing id. Returns a
     * [DataResult] so callers can react to duplicates without exceptions.
     */
    fun register(plugin: StorePluginContract): DataResult<Unit> {
        val id = plugin.getStoreId()
        val existing = plugins.putIfAbsent(id, plugin)
        return if (existing != null && existing !== fallback) {
            DataResult.Failure(AppError.Plugin.DuplicateRegistration(id))
        } else {
            // Allow a real plugin to replace the fallback's self-registration.
            plugins[id] = plugin
            DataResult.Success(Unit)
        }
    }

    /** Registers many plugins, collecting any duplicate-id failures. */
    fun registerAll(newPlugins: List<StorePluginContract>): List<AppError.Plugin> {
        val failures = mutableListOf<AppError.Plugin>()
        newPlugins.forEach { plugin ->
            val result = register(plugin)
            if (result is DataResult.Failure && result.error is AppError.Plugin) {
                failures += result.error as AppError.Plugin
            }
        }
        return failures
    }

    /** Resolves a plugin by [storeId], falling back to the generic plugin. */
    fun resolve(storeId: String): StorePluginContract =
        plugins[storeId] ?: fallback

    /** Resolves strictly: a [DataResult] failure when no real plugin exists. */
    fun resolveStrict(storeId: String): DataResult<StorePluginContract> {
        val plugin = plugins[storeId]
        return if (plugin == null || plugin === fallback) {
            DataResult.Failure(AppError.Plugin.NotFound(storeId))
        } else {
            DataResult.Success(plugin)
        }
    }

    /** Snapshot of all registered plugins. */
    fun all(): List<StorePluginContract> = plugins.values.toList()

    /** True if a non-fallback plugin is registered for [storeId]. */
    fun isRegistered(storeId: String): Boolean =
        plugins[storeId]?.let { it !== fallback } ?: false

    /** Clears all non-fallback registrations. Primarily for tests. */
    fun clear() {
        plugins.clear()
        plugins[fallback.getStoreId()] = fallback
    }
}
