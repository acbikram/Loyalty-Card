package com.universalwallet.loyalty.core.developer

import com.universalwallet.loyalty.core.plugin.StorePluginRegistry
import com.universalwallet.loyalty.core.migration.MigrationRegistry
import com.universalwallet.loyalty.core.migration.MigrationValidator
import com.universalwallet.loyalty.core.notifications.NotificationContent
import com.universalwallet.loyalty.core.notifications.NotificationEngine
import com.universalwallet.loyalty.core.notifications.NotificationType
import com.universalwallet.loyalty.core.security.SecuritySettings
import com.universalwallet.loyalty.core.sync.ConflictResolver
import com.universalwallet.loyalty.core.sync.RemoteCard
import com.universalwallet.loyalty.core.sync.Resolution
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import com.universalwallet.loyalty.domain.repository.StoreRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/** Snapshot of database contents for the inspector. */
data class DatabaseStats(
    val totalCards: Int,
    val activeCards: Int,
    val archivedCards: Int,
    val favoriteCards: Int,
    val storeCount: Int,
)

/** Result of a validation sweep (stores or plugins). */
data class ValidationReport(val checked: Int, val issues: List<String>) {
    val isHealthy: Boolean get() = issues.isEmpty()
}

/** Runtime memory snapshot for the performance monitor. */
data class MemorySnapshot(val usedMb: Long, val totalMb: Long, val maxMb: Long)

/** Outcome of a simulated sync merge (no network). */
data class SyncSimulationResult(val winner: String, val conflicts: Int, val merged: Int)

/**
 * Backs the hidden Developer Mode tools: a database inspector, store/plugin
 * validators, a demo-card generator, the debug-logging toggle, and a runtime
 * performance/memory readout. Strictly separated from user-facing features and
 * reachable only when Developer Mode is enabled in Security settings.
 */
@Singleton
class DeveloperModeManager @Inject constructor(
    private val cardRepository: LoyaltyCardRepository,
    private val storeRepository: StoreRepository,
    private val pluginRegistry: StorePluginRegistry,
    private val securitySettings: SecuritySettings,
    private val notificationEngine: NotificationEngine,
    private val conflictResolver: ConflictResolver,
    private val migrationRegistry: MigrationRegistry,
    private val migrationValidator: MigrationValidator,
) {
    suspend fun databaseStats(): DatabaseStats {
        val all = cardRepository.observeCards().first()
        val stores = storeRepository.observeStores().first()
        return DatabaseStats(
            totalCards = all.size,
            activeCards = all.count { !it.isArchived && !it.isHidden },
            archivedCards = all.count { it.isArchived },
            favoriteCards = all.count { it.isFavorite },
            storeCount = stores.size,
        )
    }

    suspend fun validateStores(): ValidationReport {
        val stores = storeRepository.observeStores().first()
        val issues = mutableListOf<String>()
        stores.forEach { store ->
            if (store.storeId.isBlank()) issues.add("Store with blank id (${store.storeName})")
            if (store.storeName.isBlank()) issues.add("Store ${store.storeId} has blank name")
            if (store.supportedBarcodeTypes.isEmpty()) issues.add("Store ${store.storeId} has no barcode types")
        }
        return ValidationReport(checked = stores.size, issues = issues)
    }

    suspend fun validatePlugins(): ValidationReport {
        val stores = storeRepository.observeStores().first()
        val issues = mutableListOf<String>()
        stores.forEach { store ->
            val plugin = pluginRegistry.resolve(store.storeId)
            if (plugin.getStoreId().isBlank()) issues.add("Plugin for ${store.storeId} returns blank id")
            if (plugin.getSupportedBarcodeTypes().isEmpty()) issues.add("Plugin for ${store.storeId} has no symbologies")
        }
        return ValidationReport(checked = stores.size, issues = issues)
    }

    /** Inserts [count] demo cards drawn from the real store catalogue. */
    suspend fun generateDemoCards(count: Int): Int {
        val stores = storeRepository.observeActiveStores().first()
        if (stores.isEmpty()) return 0
        var created = 0
        repeat(count) {
            val store = stores.random()
            val number = (1..13).map { Random.nextInt(0, 10) }.joinToString("")
            val now = System.currentTimeMillis()
            val card = LoyaltyCard(
                id = LoyaltyCard.newId(),
                storeId = store.storeId,
                storeName = store.storeName,
                cardNumber = number,
                barcodeValue = number,
                barcodeType = store.supportedBarcodeTypes.firstOrNull() ?: BarcodeType.CODE128,
                nickname = "Demo ${store.storeName}",
                category = store.category,
                createdAt = now,
                updatedAt = now,
                usageCount = Random.nextInt(0, 25),
                lastUsedTimestamp = now - Random.nextLong(0, 7L * 86_400_000L),
            )
            cardRepository.addCard(card)
            created++
        }
        return created
    }

    suspend fun setDebugLogging(enabled: Boolean) = securitySettings.setDebugLogging(enabled)

    /** Notification tester: raises a sample notification through the real engine. */
    suspend fun sendTestNotification() {
        notificationEngine.notify(
            NotificationContent(
                type = NotificationType.IMPORT_COMPLETE,
                title = "Developer test",
                message = "This is a test notification from Developer Mode.",
            ),
        )
    }

    /**
     * Sync simulator: builds two timestamped versions of the same card and runs
     * the pure [ConflictResolver] so a developer can see last-write-wins and
     * conflict detection without any backend.
     */
    fun simulateSync(): SyncSimulationResult {
        val now = System.currentTimeMillis()
        val local = RemoteCard(
            id = "sim-1", storeId = "lulu", storeName = "Lulu", cardNumber = "111",
            barcodeValue = "111", barcodeType = BarcodeType.EAN13.name, updatedAt = now - 10_000,
        )
        val remote = local.copy(cardNumber = "222", barcodeValue = "222", updatedAt = now)
        val outcome = conflictResolver.merge(listOf(local), listOf(remote))
        val winner = when (val r = conflictResolver.resolve(local, remote)) {
            is Resolution.UseRemote -> "remote (newer)"
            is Resolution.UseLocal -> "local (newer)"
            is Resolution.Conflict -> "conflict"
            Resolution.NoChange -> "no change"
        }
        return SyncSimulationResult(winner = winner, conflicts = outcome.conflicts.size, merged = outcome.resolved.size)
    }

    /**
     * Architecture validator: checks the Room migration chain is continuous up to
     * the current schema version. Surfaces structural problems early.
     */
    fun validateArchitecture(): ValidationReport {
        val report = migrationValidator.validate(migrationRegistry.roomSteps(), migrationRegistry.schemaVersion)
        return ValidationReport(checked = migrationRegistry.roomSteps().size, issues = report.issues)
    }

    fun memorySnapshot(): MemorySnapshot {
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / BYTES_PER_MB
        val total = runtime.totalMemory() / BYTES_PER_MB
        val max = runtime.maxMemory() / BYTES_PER_MB
        return MemorySnapshot(usedMb = used, totalMb = total, maxMb = max)
    }

    private companion object {
        const val BYTES_PER_MB = 1024L * 1024L
    }
}
