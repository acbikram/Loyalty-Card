package com.universalwallet.loyalty.core.featureflags

import com.universalwallet.loyalty.core.billing.PremiumFeature

/** The bucket a feature belongs to, which determines how it is gated. */
enum class FeatureCategory { FREE, PREMIUM, EXPERIMENTAL, DEVELOPER }

/**
 * A single switchable capability. [premiumFeature] links a PREMIUM feature to its
 * entitlement; [defaultEnabled] is the value before any runtime override.
 */
data class AppFeature(
    val key: String,
    val title: String,
    val category: FeatureCategory,
    val premiumFeature: PremiumFeature? = null,
    val defaultEnabled: Boolean = true,
)

/**
 * Central catalogue of every feature flag in the app, across all four categories.
 * Adding a flag here makes it available to [FeatureFlagManager] and any settings/
 * developer UI that lists flags.
 */
object FeatureCatalog {
    // --- Free (always available) ---
    val SCANNER = AppFeature("scanner", "Barcode scanner", FeatureCategory.FREE)
    val MANUAL_ENTRY = AppFeature("manual_entry", "Manual card entry", FeatureCategory.FREE)
    val BACKUP_RESTORE = AppFeature("backup_restore", "Backup & restore", FeatureCategory.FREE)
    val APP_LOCK = AppFeature("app_lock", "App lock", FeatureCategory.FREE)

    // --- Premium (gated by entitlement) ---
    val UNLIMITED_CARDS = AppFeature(
        "unlimited_cards", "Unlimited cards", FeatureCategory.PREMIUM, PremiumFeature.UNLIMITED_CARDS,
    )
    val UNLIMITED_WIDGETS = AppFeature(
        "unlimited_widgets", "Unlimited widgets", FeatureCategory.PREMIUM, PremiumFeature.UNLIMITED_WIDGETS,
    )
    val ADVANCED_THEMES = AppFeature(
        "advanced_themes", "Advanced themes", FeatureCategory.PREMIUM, PremiumFeature.ADVANCED_THEMES,
    )
    val CLOUD_SYNC = AppFeature(
        "cloud_sync", "Cloud sync", FeatureCategory.PREMIUM, PremiumFeature.CLOUD_SYNC, defaultEnabled = false,
    )
    val FAMILY_SHARING = AppFeature(
        "family_sharing", "Family sharing", FeatureCategory.PREMIUM, PremiumFeature.FAMILY_SHARING, defaultEnabled = false,
    )

    // --- Experimental (opt-in via settings) ---
    val GLASS_SURFACES = AppFeature(
        "glass_surfaces", "Glass surfaces", FeatureCategory.EXPERIMENTAL, defaultEnabled = true,
    )
    val SMART_SUGGESTIONS = AppFeature(
        "smart_suggestions", "Smart suggestions", FeatureCategory.EXPERIMENTAL, defaultEnabled = true,
    )

    // --- Developer (off unless explicitly enabled) ---
    val SYNC_SIMULATOR = AppFeature(
        "sync_simulator", "Sync simulator", FeatureCategory.DEVELOPER, defaultEnabled = false,
    )
    val ARCH_VALIDATOR = AppFeature(
        "arch_validator", "Architecture validator", FeatureCategory.DEVELOPER, defaultEnabled = false,
    )

    val all: List<AppFeature> = listOf(
        SCANNER, MANUAL_ENTRY, BACKUP_RESTORE, APP_LOCK,
        UNLIMITED_CARDS, UNLIMITED_WIDGETS, ADVANCED_THEMES, CLOUD_SYNC, FAMILY_SHARING,
        GLASS_SURFACES, SMART_SUGGESTIONS,
        SYNC_SIMULATOR, ARCH_VALIDATOR,
    )

    fun byKey(key: String): AppFeature? = all.firstOrNull { it.key == key }

    fun byCategory(category: FeatureCategory): List<AppFeature> = all.filter { it.category == category }
}
