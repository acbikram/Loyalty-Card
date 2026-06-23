package com.universalwallet.loyalty.feature.splash

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.datastore.WalletPreferencesKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Loads the onboarding-complete flag so the splash can decide where to route.
 * The value is `null` until the first DataStore read resolves.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    dataStore: DataStore<Preferences>,
) : ViewModel() {

    val onboardingComplete: StateFlow<Boolean?> = dataStore.data
        .map { it[WalletPreferencesKeys.ONBOARDING_COMPLETE] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
