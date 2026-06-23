package com.universalwallet.loyalty.feature.importwizard

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.backup.RestoreManager
import com.universalwallet.loyalty.core.backup.RestorePreview
import com.universalwallet.loyalty.core.barcode.BarcodeImageDecoder
import com.universalwallet.loyalty.core.export.CardExport
import com.universalwallet.loyalty.core.export.ConflictPolicy
import com.universalwallet.loyalty.core.export.ImportManager
import com.universalwallet.loyalty.core.export.WalletExport
import com.universalwallet.loyalty.core.importer.CsvImporter
import com.universalwallet.loyalty.core.utils.IoDispatcher
import com.universalwallet.loyalty.data.mapper.BarcodeTypeMapper
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Supported import sources. Wallet imports are placeholders for the future. */
enum class ImportSourceType { JSON, ENCRYPTED_BACKUP, CSV, IMAGE, GOOGLE_WALLET, APPLE_WALLET }

enum class WizardStep { SELECT, PREVIEW, RESULT }

data class ImportWizardUiState(
    val step: WizardStep = WizardStep.SELECT,
    val source: ImportSourceType? = null,
    val jsonPreview: RestorePreview? = null,
    val parsedCount: Int = 0,
    val addedCount: Int = 0,
    val skippedCount: Int = 0,
    val canUndo: Boolean = false,
    val isBusy: Boolean = false,
)

/**
 * Drives the multi-step import wizard: pick a source → preview → import →
 * result (with undo). JSON/encrypted reuse the Part 5A [RestoreManager]; CSV uses
 * [CsvImporter]; image uses the Part 4A [BarcodeImageDecoder]. Undo works by
 * diffing card ids before/after, so a botched import is one tap to reverse.
 */
@HiltViewModel
class ImportWizardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
    private val restoreManager: RestoreManager,
    private val importManager: ImportManager,
    private val csvImporter: CsvImporter,
    private val barcodeImageDecoder: BarcodeImageDecoder,
    private val cardRepository: LoyaltyCardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ImportWizardUiState())
    val state: StateFlow<ImportWizardUiState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    private var pendingUri: Uri? = null
    private var pendingPassword: String? = null
    private var pendingCards: List<CardExport> = emptyList()
    private var lastImportedIds: List<String> = emptyList()

    fun selectJson(uri: Uri, password: String?) {
        pendingUri = uri
        pendingPassword = password
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            restoreManager.preview(uri, password)
                .onSuccess { preview ->
                    _state.update {
                        it.copy(step = WizardStep.PREVIEW, source = ImportSourceType.JSON, jsonPreview = preview, isBusy = false)
                    }
                }
                .onFailure {
                    _state.update { it.copy(isBusy = false) }
                    _messages.send(it.message ?: "Couldn't read backup")
                }
        }
    }

    fun selectCsv(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            val text = withContext(io) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                }.getOrNull()
            }
            if (text == null) {
                _state.update { it.copy(isBusy = false) }
                _messages.send("Couldn't read CSV file")
                return@launch
            }
            pendingCards = csvImporter.parse(text)
            _state.update {
                it.copy(step = WizardStep.PREVIEW, source = ImportSourceType.CSV, parsedCount = pendingCards.size, isBusy = false)
            }
        }
    }

    fun selectImage(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            val detected = barcodeImageDecoder.decodeFromUri(uri)
            if (detected == null) {
                _state.update { it.copy(isBusy = false) }
                _messages.send("No barcode found in image")
                return@launch
            }
            val type = BarcodeTypeMapper.fromSymbology(detected.symbology)
            pendingCards = listOf(
                CardExport(
                    storeId = "imported",
                    storeName = "Imported card",
                    cardNumber = detected.rawValue,
                    barcodeValue = detected.rawValue,
                    barcodeType = type.name,
                ),
            )
            _state.update {
                it.copy(step = WizardStep.PREVIEW, source = ImportSourceType.IMAGE, parsedCount = 1, isBusy = false)
            }
        }
    }

    fun confirm(policy: ConflictPolicy) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            val before = cardRepository.observeCards().first().map { it.id }.toSet()
            val outcome: ImportOutcome? = when (_state.value.source) {
                ImportSourceType.JSON, ImportSourceType.ENCRYPTED_BACKUP -> {
                    val uri = pendingUri ?: return@launch
                    restoreManager.restore(uri, pendingPassword, policy).fold(
                        onSuccess = { ImportOutcome(it.added, it.skipped) },
                        onFailure = { null },
                    )
                }
                ImportSourceType.CSV, ImportSourceType.IMAGE -> {
                    val export = WalletExport(exportedAt = System.currentTimeMillis(), cards = pendingCards)
                    val s = importManager.importCards(export, policy)
                    ImportOutcome(s.added, s.skipped)
                }
                else -> null
            }
            val after = cardRepository.observeCards().first().map { it.id }.toSet()
            lastImportedIds = (after - before).toList()

            if (outcome == null) {
                _state.update { it.copy(isBusy = false) }
                _messages.send("Import failed")
                return@launch
            }
            _state.update {
                it.copy(
                    step = WizardStep.RESULT,
                    addedCount = outcome.added,
                    skippedCount = outcome.skipped,
                    canUndo = lastImportedIds.isNotEmpty(),
                    isBusy = false,
                )
            }
        }
    }

    private data class ImportOutcome(val added: Int, val skipped: Int)

    fun undo() {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true) }
            lastImportedIds.forEach { cardRepository.deleteCard(it) }
            val removed = lastImportedIds.size
            lastImportedIds = emptyList()
            _state.update { it.copy(canUndo = false, isBusy = false) }
            _messages.send("Undid import — removed $removed card(s)")
        }
    }

    fun reset() {
        pendingUri = null
        pendingPassword = null
        pendingCards = emptyList()
        _state.value = ImportWizardUiState()
    }
}
