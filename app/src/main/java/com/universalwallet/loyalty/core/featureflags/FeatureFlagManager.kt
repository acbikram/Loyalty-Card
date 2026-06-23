package com.universalwallet.loyalty.core.featureflags

import com.universalwallet.loyalty.core.billing.Entitlement
import com.universalwallet.loyalty.core.billing.EntitlementProvider
import com.universalwallet.loyalty.core.billing.FreeTierLimits
import com.universalwallet.loyalty.core.billing.PremiumFeature
import com.universalwallet.loyalty.core.settings.FeatureFlags
import com.universalwallet.loyalty.core.settings.FeatureSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central feature-flag resolver (Part 6B). Combines three sources:
 *  1. persisted experimental/accessibility toggles ([FeatureSettings]),
 *  2. the premium [Entitlement] (from [EntitlementProvider]),
 *  3. in-memory **runtime overrides** (for Developer Mode / QA).
 *
 * Feature code asks `isEnabled(feature)` rather than checking tiers directly, so
 * gating rules live in one place and a future billing provider changes nothing
 * downstream.
 */
@Singleton
class FeatureFlagManager @Inject constructor(
    private val featureSettings: FeatureSettings,
    private val entitlementProvider: EntitlementProvider,
) {
    private val runtimeOverrides = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    /** Immutable snapshot of everything needed to resolve a flag. */
    data class FeatureState(
        val flags: FeatureFlags,
        val entitlement: Entitlement,
        val overrides: Map<String, Boolean>,
    ) {
        fun isEnabled(feature: AppFeature): Boolean {
            overrides[feature.key]?.let { return it }
            return when (feature.category) {
                FeatureCategory.FREE -> feature.defaultEnabled
                FeatureCategory.PREMIUM ->
                    feature.premiumFeature?.let { entitlement.has(it) } ?: entitlement.isPremium
                FeatureCategory.EXPERIMENTAL -> flags.experimentalFeatures && feature.defaultEnabled
                FeatureCategory.DEVELOPER -> false
            }
        }
    }

    val state: Flow<FeatureState> = combine(
        featureSettings.flags,
        entitlementProvider.entitlement(),
        runtimeOverrides,
    ) { flags, entitlement, overrides ->
        FeatureState(flags, entitlement, overrides)
    }.distinctUntilChanged()

    val overrides: Flow<Map<String, Boolean>> = runtimeOverrides.asStateFlow()

    fun isEnabled(feature: AppFeature): Flow<Boolean> =
        state.map { it.isEnabled(feature) }.distinctUntilChanged()

    // --- Tier gating (premium removes caps) ---

    fun cardLimit(): Flow<Int> = entitlementProvider.entitlement().map {
        if (it.has(PremiumFeature.UNLIMITED_CARDS)) Int.MAX_VALUE else FreeTierLimits.MAX_CARDS
    }

    fun canAddCard(currentCount: Int): Flow<Boolean> = cardLimit().map { currentCount < it }

    fun widgetLimit(): Flow<Int> = entitlementProvider.entitlement().map {
        if (it.has(PremiumFeature.UNLIMITED_WIDGETS)) Int.MAX_VALUE else FreeTierLimits.MAX_WIDGETS
    }

    fun canAddWidget(currentCount: Int): Flow<Boolean> = widgetLimit().map { currentCount < it }

    // --- Runtime configuration (Developer Mode / QA) ---

    fun setRuntimeOverride(key: String, enabled: Boolean) =
        runtimeOverrides.update { it + (key to enabled) }

    fun clearRuntimeOverride(key: String) =
        runtimeOverrides.update { it - key }

    fun clearAllOverrides() {
        runtimeOverrides.value = emptyMap()
    }
}
