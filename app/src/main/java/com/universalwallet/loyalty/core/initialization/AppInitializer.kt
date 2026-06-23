package com.universalwallet.loyalty.core.initialization

import com.universalwallet.loyalty.BuildConfig
import com.universalwallet.loyalty.core.architecture.ArchitectureValidator
import com.universalwallet.loyalty.core.logging.AppLogger
import com.universalwallet.loyalty.core.plugin.StorePluginLoader
import com.universalwallet.loyalty.core.plugin.StorePluginRegistry
import com.universalwallet.loyalty.core.utils.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single, ordered entry point for application startup.
 *
 * ### Startup order (and why it matters)
 *  1. **Logging** is bootstrapped first, by the [android.app.Application] before
 *     this initialiser runs, so every subsequent step can log safely.
 *  2. **Security layer** preparation (Keystore key existence, secure prefs) —
 *     a no-op until the security phase, but sequenced before any data access so
 *     encrypted reads/writes always have a key available.
 *  3. **DataStore** is created lazily on first access; no eager step is needed,
 *     and deliberately not blocking startup keeps cold-start fast.
 *  4. **Plugin registry preload** runs off the main thread and populates the
 *     registry before features resolve stores. The generic fallback is present
 *     from construction, so features are safe even before this completes.
 *  5. **Architecture validation** runs last (debug only), once everything is
 *     wired, to assert the foundation's invariants hold.
 *
 * Heavy work is dispatched to the application scope so [initialize] returns
 * quickly and never blocks the main thread during cold start.
 */
@Singleton
class AppInitializer @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
    private val logger: AppLogger,
    private val registry: StorePluginRegistry,
    private val loader: StorePluginLoader,
    private val architectureValidator: ArchitectureValidator,
) {

    fun initialize() {
        logger.i(TAG, "Application initialisation started")

        scope.launch {
            // Step 4: preload store plugins into the registry.
            val plugins = loader.loadAll()
            val failures = registry.registerAll(plugins)
            failures.forEach { logger.w(TAG, "Plugin registration issue: ${it::class.simpleName}") }
            logger.i(TAG, "Plugin registry ready (${registry.all().size} plugins)")

            // Step 5: validate architectural invariants in debug builds.
            if (BuildConfig.DEBUG) {
                val report = architectureValidator.validate()
                if (!report.isValid) {
                    logger.e(TAG, "Architecture validation found ${report.violations.size} issue(s)")
                }
            }

            logger.i(TAG, "Application initialisation complete")
        }
    }

    private companion object {
        const val TAG = "AppInitializer"
    }
}
