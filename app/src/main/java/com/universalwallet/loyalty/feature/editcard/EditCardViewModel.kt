package com.universalwallet.loyalty.feature.editcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.navigation.NavConstants
import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import com.universalwallet.loyalty.feature.addcard.toMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Immutable UI state for editing an existing card. */
data class EditCardUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val original: LoyaltyCard? = null,
    val cardNumber: String = "",
    val nickname: String = "",
    val notes: String = "",
    val category: CardCategory = CardCategory.GENERAL,
    val styleId: String = "classic",
    val isSaving: Boolean = false,
) {
    val canSave: Boolean get() = original != null && cardNumber.isNotBlank() && !isSaving
}

/** One-time effects from the edit-card screen. */
sealed interface EditCardEvent {
    data object Saved : EditCardEvent
    data object Deleted : EditCardEvent
    data class ShowMessage(val message: String) : EditCardEvent
}

/**
 * Edit-card ViewModel. Loads the card by navigation argument into an editable
 * form, then persists updates or deletes via the repository. Immutable
 * identity fields (id, store, created-at) are preserved across the update.
 */
@HiltViewModel
class EditCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: LoyaltyCardRepository,
) : ViewModel() {

    private val cardId: String = checkNotNull(savedStateHandle[NavConstants.ARG_CARD_ID]) {
        "EditCard requires a ${NavConstants.ARG_CARD_ID} argument"
    }

    private val _state = MutableStateFlow(EditCardUiState())
    val state = _state.asStateFlow()

    private val _events = Channel<EditCardEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            when (val result = cardRepository.getCard(cardId)) {
                is DataResult.Success -> {
                    val c = result.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            original = c,
                            cardNumber = c.cardNumber,
                            nickname = c.nickname,
                            notes = c.notes,
                            category = c.category,
                            styleId = c.colorThemeId,
                        )
                    }
                }
                is DataResult.Failure -> _state.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }

    fun onNumberChange(value: String) = _state.update { it.copy(cardNumber = value.trim()) }
    fun onNicknameChange(value: String) = _state.update { it.copy(nickname = value) }
    fun onNotesChange(value: String) = _state.update { it.copy(notes = value) }
    fun onCategoryChange(value: CardCategory) = _state.update { it.copy(category = value) }
    fun onStyleChange(styleId: String) = _state.update { it.copy(styleId = styleId) }

    fun save() {
        val s = _state.value
        val original = s.original ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val updated = original.copy(
                cardNumber = s.cardNumber,
                barcodeValue = s.cardNumber,
                nickname = s.nickname,
                notes = s.notes,
                category = s.category,
                colorThemeId = s.styleId,
                updatedAt = System.currentTimeMillis(),
            )
            when (val result = cardRepository.updateCard(updated)) {
                is DataResult.Success -> _events.send(EditCardEvent.Saved)
                is DataResult.Failure -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(EditCardEvent.ShowMessage(result.error.toMessage()))
                }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            when (cardRepository.deleteCard(cardId)) {
                is DataResult.Success -> _events.send(EditCardEvent.Deleted)
                is DataResult.Failure -> _events.send(EditCardEvent.ShowMessage("Couldn't delete card"))
            }
        }
    }
}
