package com.universalwallet.loyalty.feature.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.developer.DatabaseStats
import com.universalwallet.loyalty.core.developer.DeveloperModeManager
import com.universalwallet.loyalty.core.developer.MemorySnapshot
import com.universalwallet.loyalty.core.developer.ValidationReport
import com.universalwallet.loyalty.core.security.SecurityConfig
import com.universalwallet.loyalty.core.security.SecuritySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Collected outputs from the developer tools. */
data class DeveloperUiState(
    val dbStats: DatabaseStats? = null,
    val storeReport: ValidationReport? = null,
    val pluginReport: ValidationReport? = null,
    val architectureReport: ValidationReport? = null,
    val memory: MemorySnapshot? = null,
    val isBusy: Boolean = false,
)

/** Backs the Developer Mode screen; thin orchestration over [DeveloperModeManager]. */
@HiltViewModel
class DeveloperModeViewModel @Inject constructor(
    private val developerModeManager: DeveloperModeManager,
    private val securitySettings: SecuritySettings,
) : ViewModel() {

    val config: StateFlow<SecurityConfig> =
        securitySettings.config.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SecurityConfig())

    private val _state = MutableStateFlow(DeveloperUiState())
    val state: StateFlow<DeveloperUiState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    init {
        refreshStats()
        refreshMemory()
    }

    fun refreshStats() {
        viewModelScope.launch { _state.update { it.copy(dbStats = developerModeManager.databaseStats()) } }
    }

    fun validateStores() {
        viewModelScope.launch { _state.update { it.copy(storeReport = developerModeManager.validateStores()) } }
    }

    fun validatePlugins() {
        viewModelScope.launch { _state.update { it.copy(pluginReport = developerModeManager.validatePlugins()) } }
    }

    fun refreshMemory() {
        _state.update { it.copy(memory = developerModeManager.memorySnapshot()) }
    }

    fun generateDemoCards(count: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            val created = developerModeManager.generateDemoCards(count)
            _state.update { it.copy(isBusy = false) }
            _messages.send(if (created > 0) "Generated $created demo card(s)" else "No stores available")
            refreshStats()
        }
    }

    fun setDebugLogging(enabled: Boolean) {
        viewModelScope.launch { developerModeManager.setDebugLogging(enabled) }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            developerModeManager.sendTestNotification()
            _messages.send("Test notification sent (if notifications are allowed)")
        }
    }

    fun simulateSync() {
        viewModelScope.launch {
            val result = developerModeManager.simulateSync()
            _messages.send("Sync sim: winner=${result.winner}, merged=${result.merged}, conflicts=${result.conflicts}")
        }
    }

    fun validateArchitecture() {
        viewModelScope.launch {
            _state.update { it.copy(architectureReport = developerModeManager.validateArchitecture()) }
        }
    }
}
