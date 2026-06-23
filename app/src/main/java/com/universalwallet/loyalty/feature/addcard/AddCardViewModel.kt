package com.universalwallet.loyalty.feature.addcard

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.barcode.BarcodeImageDecoder
import com.universalwallet.loyalty.core.barcode.BarcodeSymbology
import com.universalwallet.loyalty.core.barcode.BarcodeValidator
import com.universalwallet.loyalty.core.image.ImageProcessor
import com.universalwallet.loyalty.core.navigation.NavConstants
import com.universalwallet.loyalty.core.plugin.StorePluginRegistry
import com.universalwallet.loyalty.core.result.AppError
import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.data.mapper.BarcodeTypeMapper
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.model.StoreDefinition
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import com.universalwallet.loyalty.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Immutable UI state for the add-card form. */
data class AddCardUiState(
    val stores: List<StoreDefinition> = emptyList(),
    val storeQuery: String = "",
    val selectedStore: StoreDefinition? = null,
    val cardNumber: String = "",
    val nickname: String = "",
    val notes: String = "",
    val category: CardCategory = CardCategory.GENERAL,
    val barcodeType: BarcodeType = BarcodeType.CODE128,
    val styleId: String = "classic",
    val imagePath: String? = null,
    val duplicateOf: LoyaltyCard? = null,
    val isProcessingImage: Boolean = false,
    val isSaving: Boolean = false,
) {
    val filteredStores: List<StoreDefinition>
        get() = if (storeQuery.isBlank()) stores
        else stores.filter { it.storeName.contains(storeQuery, ignoreCase = true) }

    val canSave: Boolean get() = selectedStore != null && cardNumber.isNotBlank() && !isSaving
    val hasDuplicate: Boolean get() = duplicateOf != null
}

/** One-time effects from the add-card screen. */
sealed interface AddCardEvent {
    data object Saved : AddCardEvent
    data class AskReplace(val existing: LoyaltyCard) : AddCardEvent
    data class ShowMessage(val message: String) : AddCardEvent
}

/**
 * Add-card ViewModel and the heart of the card-creation flow. It:
 *  - streams the active store catalogue for the picker,
 *  - accepts a scanned barcode handed back from the scanner via [SavedStateHandle],
 *  - imports an image (auto-detecting any barcode inside it),
 *  - auto-detects the barcode type for manually typed numbers,
 *  - detects duplicates (same store + number/barcode) and asks before replacing,
 *  - persists through the repository, which runs the authoritative validation.
 */
@HiltViewModel
class AddCardViewModel @Inject constructor(
    private val cardRepository: LoyaltyCardRepository,
    storeRepository: StoreRepository,
    private val pluginRegistry: StorePluginRegistry,
    private val barcodeImageDecoder: BarcodeImageDecoder,
    private val imageProcessor: ImageProcessor,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(AddCardUiState())
    val state = _state.asStateFlow()

    private val _events = Channel<AddCardEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var allCards: List<LoyaltyCard> = emptyList()

    init {
        viewModelScope.launch {
            storeRepository.observeActiveStores().collect { stores ->
                _state.update { it.copy(stores = stores) }
            }
        }
        viewModelScope.launch {
            cardRepository.observeCards().collect { cards ->
                allCards = cards
                recomputeDuplicate()
            }
        }
        // A scan result handed back from the scanner via the nav back stack.
        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>(NavConstants.RESULT_SCANNED_VALUE, null)
                .collect { scanned ->
                    if (!scanned.isNullOrBlank()) {
                        val symbology = savedStateHandle.get<String?>(NavConstants.RESULT_SCANNED_SYMBOLOGY)
                        applyScan(scanned, symbology)
                        savedStateHandle[NavConstants.RESULT_SCANNED_VALUE] = null
                    }
                }
        }
    }

    fun onStoreQueryChange(value: String) = _state.update { it.copy(storeQuery = value) }

    fun onSelectStore(store: StoreDefinition) {
        _state.update {
            it.copy(
                selectedStore = store,
                category = store.category,
                barcodeType = store.supportedBarcodeTypes.firstOrNull() ?: it.barcodeType,
                nickname = it.nickname.ifBlank { store.storeName },
                storeQuery = "",
            )
        }
        recomputeDuplicate()
    }

    fun onNumberChange(value: String) {
        val trimmed = value.trim()
        _state.update {
            val forcedSingleType = it.selectedStore?.supportedBarcodeTypes?.size == 1
            it.copy(
                cardNumber = trimmed,
                barcodeType = if (forcedSingleType) it.barcodeType else BarcodeValidator.detectType(trimmed),
            )
        }
        recomputeDuplicate()
    }

    fun onNicknameChange(value: String) = _state.update { it.copy(nickname = value) }
    fun onNotesChange(value: String) = _state.update { it.copy(notes = value) }
    fun onCategoryChange(value: CardCategory) = _state.update { it.copy(category = value) }
    fun onStyleChange(styleId: String) = _state.update { it.copy(styleId = styleId) }

    /** Applies a scanned (or image-detected) barcode to the form. */
    private fun applyScan(rawValue: String, symbologyName: String?) {
        val symbology = symbologyName?.let { runCatching { BarcodeSymbology.valueOf(it) }.getOrNull() }
        val type = symbology?.let { BarcodeTypeMapper.fromSymbology(it) }
            ?: BarcodeValidator.detectType(rawValue)
        _state.update { it.copy(cardNumber = rawValue.trim(), barcodeType = type) }
        recomputeDuplicate()
    }

    /** Imports a picked image: stores it, then tries to auto-detect a barcode. */
    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessingImage = true) }
            val processed = runCatching { imageProcessor.importFromUri(uri) }.getOrNull()
            if (processed != null) {
                _state.update { it.copy(imagePath = processed.path) }
            }
            val detected = barcodeImageDecoder.decodeFromUri(uri)
            _state.update { it.copy(isProcessingImage = false) }
            if (detected != null) {
                applyScan(detected.rawValue, detected.symbology.name)
            } else {
                _events.send(AddCardEvent.ShowMessage("No barcode found in image"))
            }
        }
    }

    fun save() {
        val s = _state.value
        if (s.selectedStore == null) return
        val duplicate = s.duplicateOf
        if (duplicate != null) {
            viewModelScope.launch { _events.send(AddCardEvent.AskReplace(duplicate)) }
            return
        }
        persistNew(s)
    }

    /** Confirmed "replace" path: overwrites the existing card's data. */
    fun confirmReplace() {
        val s = _state.value
        val existing = s.duplicateOf ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val updated = existing.copy(
                cardNumber = s.cardNumber,
                barcodeValue = s.cardNumber,
                barcodeType = s.barcodeType,
                nickname = s.nickname.ifBlank { existing.nickname },
                notes = s.notes,
                category = s.category,
                colorThemeId = s.styleId,
                imagePath = s.imagePath ?: existing.imagePath,
                updatedAt = System.currentTimeMillis(),
            )
            when (val result = cardRepository.updateCard(updated)) {
                is DataResult.Success -> _events.send(AddCardEvent.Saved)
                is DataResult.Failure -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(AddCardEvent.ShowMessage(result.error.toMessage()))
                }
            }
        }
    }

    private fun persistNew(s: AddCardUiState) {
        val store = s.selectedStore ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            val formattedNickname = s.nickname.ifBlank {
                pluginRegistry.resolve(store.storeId).getStoreName()
            }
            val card = LoyaltyCard(
                id = LoyaltyCard.newId(),
                storeId = store.storeId,
                storeName = store.storeName,
                cardNumber = s.cardNumber,
                barcodeValue = s.cardNumber,
                barcodeType = s.barcodeType,
                nickname = formattedNickname,
                notes = s.notes,
                category = s.category,
                createdAt = now,
                updatedAt = now,
                imagePath = s.imagePath,
                colorThemeId = s.styleId,
            )
            when (val result = cardRepository.addCard(card)) {
                is DataResult.Success -> _events.send(AddCardEvent.Saved)
                is DataResult.Failure -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(AddCardEvent.ShowMessage(result.error.toMessage()))
                }
            }
        }
    }

    private fun recomputeDuplicate() {
        val s = _state.value
        val store = s.selectedStore
        val duplicate = if (store == null || s.cardNumber.isBlank()) {
            null
        } else {
            allCards.firstOrNull {
                it.storeId == store.storeId &&
                    (it.cardNumber == s.cardNumber || it.barcodeValue == s.cardNumber)
            }
        }
        _state.update { it.copy(duplicateOf = duplicate) }
    }
}

/** Maps a domain error to a short, user-facing message. */
internal fun AppError.toMessage(): String = when (this) {
    is AppError.Validation -> reason
    is AppError.Database -> "Couldn't save the card. Please try again."
    else -> "Something went wrong. Please try again."
}
