package com.universalwallet.loyalty.core.architecture

import com.universalwallet.loyalty.core.logging.AppLogger
import com.universalwallet.loyalty.core.plugin.StorePluginRegistry
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A startup safeguard that checks the runtime-verifiable architectural
 * invariants of the foundation. It is intended to run in debug builds during
 * app initialisation; a failing report indicates a programming error, not a
 * user-facing condition.
 *
 * Scope note: rules that are purely structural — "no feature depends on another
 * feature", "no UI touches the database", "repositories are accessed only via
 * the domain layer" — are best enforced at build time with a static
 * architecture test (e.g. Konsist or ArchUnit) added in a later phase. This
 * class covers the invariants that can only be checked once objects exist,
 * principally the integrity of the plugin registry.
 */
@Singleton
class ArchitectureValidator @Inject constructor(
    private val registry: StorePluginRegistry,
    private val logger: AppLogger,
) {

    /** Result of a validation pass. */
    data class Report(val violations: List<String>) {
        val isValid: Boolean get() = violations.isEmpty()
    }

    fun validate(): Report {
        val violations = mutableListOf<String>()

        // 1. A fallback plugin must always be available.
        val unknown = registry.resolve("__definitely_not_a_real_store__")
        if (unknown !== registry.fallback) {
            violations += "Plugin registry does not resolve unknown ids to the fallback."
        }

        // 2. Plugin ids must be unique and non-blank.
        val ids = registry.all().map { it.getStoreId() }
        ids.filter { it.isBlank() }.forEach {
            violations += "A plugin is registered with a blank store id."
        }
        ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach { dup ->
            violations += "Duplicate plugin id registered: '$dup'."
        }

        // 3. Every plugin must expose at least one supported barcode symbology.
        registry.all().filter { it.getSupportedBarcodeTypes().isEmpty() }.forEach {
            violations += "Plugin '${it.getStoreId()}' declares no supported barcode types."
        }

        val report = Report(violations)
        if (report.isValid) {
            logger.d(TAG, "Architecture validation passed (${ids.size} plugins).")
        } else {
            report.violations.forEach { logger.e(TAG, "Architecture violation: $it") }
        }
        return report
    }

    private companion object {
        const val TAG = "ArchitectureValidator"
    }
}
