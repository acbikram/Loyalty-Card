package com.universalwallet.loyalty.feature.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.security.PinManager
import com.universalwallet.loyalty.core.security.SecurityConfig
import com.universalwallet.loyalty.core.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Lock-screen state: the configured method, PIN error, and attempt count. */
data class LockUiState(
    val error: String? = null,
    val attempts: Int = 0,
)

/**
 * Lock-screen ViewModel. Biometric/device-credential prompting is driven by the
 * host activity (it owns the FragmentActivity needed by BiometricPrompt); this
 * ViewModel owns PIN verification and unlock state. On any successful unlock the
 * shared [SecurityManager] clears the lock.
 */
@HiltViewModel
class LockViewModel @Inject constructor(
    private val securityManager: SecurityManager,
    private val pinManager: PinManager,
) : ViewModel() {

    val config: StateFlow<SecurityConfig> = securityManager.config

    private val _state = MutableStateFlow(LockUiState())
    val state: StateFlow<LockUiState> = _state.asStateFlow()

    fun submitPin(pin: String) {
        viewModelScope.launch {
            if (pinManager.verifyPin(pin)) {
                _state.value = LockUiState()
                securityManager.onUnlocked()
            } else {
                _state.value = _state.value.copy(error = "Incorrect PIN", attempts = _state.value.attempts + 1)
            }
        }
    }

    fun onUnlockedExternally() {
        _state.value = LockUiState()
        securityManager.onUnlocked()
    }

    fun onBiometricError(message: String) {
        _state.value = _state.value.copy(error = message)
    }
}
