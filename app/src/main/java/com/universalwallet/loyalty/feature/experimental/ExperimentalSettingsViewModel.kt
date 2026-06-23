package com.universalwallet.loyalty.feature.experimental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.settings.FeatureFlags
import com.universalwallet.loyalty.core.settings.FeatureSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives the experimental/features settings screen. */
@HiltViewModel
class ExperimentalSettingsViewModel @Inject constructor(
    private val featureSettings: FeatureSettings,
) : ViewModel() {

    val flags: StateFlow<FeatureFlags> =
        featureSettings.flags.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeatureFlags())

    fun setWidgets(value: Boolean) = launch { featureSettings.setWidgetsEnabled(value) }
    fun setWearSync(value: Boolean) = launch { featureSettings.setWearSyncEnabled(value) }
    fun setCloudSync(value: Boolean) = launch { featureSettings.setCloudSyncEnabled(value) }
    fun setReducedMotion(value: Boolean) = launch { featureSettings.setReducedMotion(value) }
    fun setExperimental(value: Boolean) = launch { featureSettings.setExperimentalFeatures(value) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
