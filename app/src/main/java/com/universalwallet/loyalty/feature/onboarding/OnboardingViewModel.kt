package com.universalwallet.loyalty.feature.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.datastore.WalletPreferencesKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Persists the onboarding-complete flag once the user finishes or skips. */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    fun complete() {
        viewModelScope.launch {
            dataStore.edit { it[WalletPreferencesKeys.ONBOARDING_COMPLETE] = true }
        }
    }
}
