package com.universalwallet.loyalty.feature.security

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.backup.BackupManager
import com.universalwallet.loyalty.core.backup.RestoreManager
import com.universalwallet.loyalty.core.backup.RestorePreview
import com.universalwallet.loyalty.core.export.ConflictPolicy
import com.universalwallet.loyalty.core.security.AuthMethod
import com.universalwallet.loyalty.core.security.BiometricAuthenticator
import com.universalwallet.loyalty.core.security.BiometricAvailability
import com.universalwallet.loyalty.core.security.PinManager
import com.universalwallet.loyalty.core.security.SecurityConfig
import com.universalwallet.loyalty.core.security.SecuritySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Local UI state layered over the persisted [SecurityConfig]. */
data class SecuritySettingsUiState(
    val biometricAvailable: Boolean = false,
    val restorePreview: RestorePreview? = null,
    val isBusy: Boolean = false,
)

/**
 * Drives the Security settings screen: all toggles, PIN management, the auth
 * method/timeout, and backup/restore (export + a validated restore preview before
 * any import). User-facing outcomes are surfaced as snackbar messages.
 */
@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val securitySettings: SecuritySettings,
    private val pinManager: PinManager,
    biometricAuthenticator: BiometricAuthenticator,
    private val backupManager: BackupManager,
    private val restoreManager: RestoreManager,
) : ViewModel() {

    val config: StateFlow<SecurityConfig> =
        securitySettings.config.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SecurityConfig())

    private val _state = MutableStateFlow(
        SecuritySettingsUiState(
            biometricAvailable = biometricAuthenticator.availability(allowDeviceCredential = true) ==
                BiometricAvailability.AVAILABLE,
        ),
    )
    val state: StateFlow<SecuritySettingsUiState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    private var pendingRestoreUri: Uri? = null
    private var pendingRestorePassword: String? = null

    fun setAppLock(enabled: Boolean) = launchEdit {
        // Enabling app lock without any auth set up would lock the user out, so
        // require a PIN if biometrics aren't available.
        if (enabled && !it.hasPin && !_state.value.biometricAvailable) {
            _messages.send("Set a PIN first to enable App Lock")
        } else {
            securitySettings.setAppLockEnabled(enabled)
        }
    }

    fun setAuthMethod(method: AuthMethod) = launchEdit { securitySettings.setAuthMethod(method) }
    fun setAutoLockTimeout(ms: Long) = launchEdit { securitySettings.setAutoLockTimeout(ms) }
    fun setLockOnBackground(enabled: Boolean) = launchEdit { securitySettings.setLockOnBackground(enabled) }
    fun setRequireAuthOnLaunch(enabled: Boolean) = launchEdit { securitySettings.setRequireAuthOnLaunch(enabled) }
    fun setScreenshotProtection(enabled: Boolean) = launchEdit { securitySettings.setScreenshotProtection(enabled) }
    fun setClipboardProtection(enabled: Boolean) = launchEdit { securitySettings.setClipboardProtection(enabled) }
    fun setDeveloperMode(enabled: Boolean) = launchEdit { securitySettings.setDeveloperMode(enabled) }

    fun setPin(pin: String) {
        viewModelScope.launch {
            if (pin.length < 4) {
                _messages.send("PIN must be at least 4 digits")
                return@launch
            }
            pinManager.setPin(pin)
            _messages.send("PIN set")
        }
    }

    fun clearPin() {
        viewModelScope.launch {
            pinManager.clearPin()
            if (config.value.authMethod == AuthMethod.PIN) securitySettings.setAuthMethod(AuthMethod.BIOMETRIC)
            _messages.send("PIN removed")
        }
    }

    fun exportBackup(uri: Uri, password: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            backupManager.backupToUri(uri, password)
                .onSuccess { count ->
                    _messages.send(if (password.isNullOrBlank()) "Backed up $count card(s)" else "Encrypted backup of $count card(s) saved")
                }
                .onFailure { _messages.send("Backup failed") }
            _state.update { it.copy(isBusy = false) }
        }
    }

    fun loadRestorePreview(uri: Uri, password: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            restoreManager.preview(uri, password)
                .onSuccess { preview ->
                    pendingRestoreUri = uri
                    pendingRestorePassword = password
                    _state.update { it.copy(restorePreview = preview, isBusy = false) }
                }
                .onFailure {
                    _state.update { it.copy(isBusy = false) }
                    _messages.send(it.message ?: "Couldn't read backup")
                }
        }
    }

    fun confirmRestore(policy: ConflictPolicy) {
        val uri = pendingRestoreUri ?: return
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, restorePreview = null) }
            restoreManager.restore(uri, pendingRestorePassword, policy)
                .onSuccess { summary ->
                    _messages.send("Restored ${summary.added}, replaced ${summary.replaced}, skipped ${summary.skipped}")
                }
                .onFailure { _messages.send(it.message ?: "Restore failed") }
            pendingRestoreUri = null
            pendingRestorePassword = null
            _state.update { it.copy(isBusy = false) }
        }
    }

    fun dismissRestorePreview() {
        pendingRestoreUri = null
        pendingRestorePassword = null
        _state.update { it.copy(restorePreview = null) }
    }

    private fun launchEdit(block: suspend (SecurityConfig) -> Unit) {
        viewModelScope.launch { block(config.value) }
    }
}
