package com.universalwallet.loyalty.feature.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.export.ConflictPolicy
import com.universalwallet.loyalty.core.export.ExportManager
import com.universalwallet.loyalty.core.export.ImportManager
import com.universalwallet.loyalty.core.theme.ThemeMode
import com.universalwallet.loyalty.core.wallet.SmartWalletSettings
import com.universalwallet.loyalty.di.ThemeStateHolder
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Immutable UI state for settings. */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val smartWalletEnabled: Boolean = true,
)

/**
 * Settings ViewModel. Adapts the theme preferences and the Smart Wallet toggle,
 * and drives JSON export/import through [ExportManager] / [ImportManager]. User-
 * facing outcomes are surfaced as one-shot snackbar messages.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeStateHolder: ThemeStateHolder,
    private val smartWalletSettings: SmartWalletSettings,
    private val exportManager: ExportManager,
    private val importManager: ImportManager,
    private val cardRepository: LoyaltyCardRepository,
) : ViewModel() {

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    val state: StateFlow<SettingsUiState> = combine(
        themeStateHolder.themeMode,
        themeStateHolder.dynamicColor,
        smartWalletSettings.enabled,
    ) { mode, dynamic, smart ->
        SettingsUiState(themeMode = mode, dynamicColor = dynamic, smartWalletEnabled = smart)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) = themeStateHolder.setThemeMode(mode)
    fun setDynamicColor(enabled: Boolean) = themeStateHolder.setDynamicColor(enabled)

    fun setSmartWallet(enabled: Boolean) {
        viewModelScope.launch { smartWalletSettings.setEnabled(enabled) }
    }

    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            val cards = cardRepository.observeCards().first()
            if (cards.isEmpty()) {
                _messages.send("No cards to export")
                return@launch
            }
            exportManager.exportToUri(cards, uri)
                .onSuccess { count -> _messages.send("Exported $count card(s)") }
                .onFailure { _messages.send("Export failed") }
        }
    }

    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            importManager.importFromUri(uri, ConflictPolicy.SKIP)
                .onSuccess { summary ->
                    _messages.send(
                        "Imported ${summary.added}, skipped ${summary.skipped}" +
                            if (summary.invalid > 0) ", ${summary.invalid} invalid" else "",
                    )
                }
                .onFailure { _messages.send("Import failed — invalid file") }
        }
    }
}
