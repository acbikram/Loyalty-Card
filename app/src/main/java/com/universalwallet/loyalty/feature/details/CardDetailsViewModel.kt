package com.universalwallet.loyalty.feature.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.core.navigation.NavConstants
import com.universalwallet.loyalty.core.organize.FavoriteManager
import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.core.share.ShareManager
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Immutable UI state for the card-details screen. */
data class CardDetailsUiState(
    val isLoading: Boolean = true,
    val card: LoyaltyCard? = null,
    val isError: Boolean = false,
)

/** One-time effects emitted by the details screen. */
sealed interface CardDetailsEvent {
    data object Deleted : CardDetailsEvent
    data object Archived : CardDetailsEvent
    data class ShowMessage(val message: String) : CardDetailsEvent
}

/**
 * Card-details ViewModel. Loads the card by navigation argument, counts the open
 * as a use (feeding Smart Wallet), and exposes favourite/pin/archive/duplicate
 * /delete plus the three confirmed share actions. Sharing is delegated to
 * [ShareManager]; the UI gates it behind an explicit "what to share" choice.
 */
@HiltViewModel
class CardDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: LoyaltyCardRepository,
    private val favoriteManager: FavoriteManager,
    private val shareManager: ShareManager,
) : ViewModel() {

    private val cardId: String = checkNotNull(savedStateHandle[NavConstants.ARG_CARD_ID]) {
        "CardDetails requires a ${NavConstants.ARG_CARD_ID} argument"
    }

    private val _state = MutableStateFlow(CardDetailsUiState())
    val state = _state.asStateFlow()

    private val _events = Channel<CardDetailsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
        viewModelScope.launch { cardRepository.incrementUsage(cardId) }
    }

    private fun load() {
        viewModelScope.launch {
            when (val result = cardRepository.getCard(cardId)) {
                is DataResult.Success -> _state.update { it.copy(isLoading = false, card = result.data, isError = false) }
                is DataResult.Failure -> _state.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }

    fun toggleFavorite() {
        val current = _state.value.card ?: return
        viewModelScope.launch {
            when (favoriteManager.toggleFavorite(current)) {
                is DataResult.Success -> _state.update { it.copy(card = current.copy(isFavorite = !current.isFavorite)) }
                is DataResult.Failure -> _events.send(CardDetailsEvent.ShowMessage("Couldn't update favourite"))
            }
        }
    }

    fun togglePin() {
        val current = _state.value.card ?: return
        viewModelScope.launch {
            when (favoriteManager.togglePinned(current)) {
                is DataResult.Success -> {
                    _state.update { it.copy(card = current.copy(isPinned = !current.isPinned)) }
                    _events.send(CardDetailsEvent.ShowMessage(if (!current.isPinned) "Pinned" else "Unpinned"))
                }
                is DataResult.Failure -> _events.send(CardDetailsEvent.ShowMessage("Couldn't pin card"))
            }
        }
    }

    fun archive() {
        viewModelScope.launch {
            when (cardRepository.setArchived(cardId, true)) {
                is DataResult.Success -> _events.send(CardDetailsEvent.Archived)
                is DataResult.Failure -> _events.send(CardDetailsEvent.ShowMessage("Couldn't archive card"))
            }
        }
    }

    fun duplicate() {
        viewModelScope.launch {
            when (cardRepository.duplicateCard(cardId)) {
                is DataResult.Success -> _events.send(CardDetailsEvent.ShowMessage("Card duplicated"))
                is DataResult.Failure -> _events.send(CardDetailsEvent.ShowMessage("Couldn't duplicate card"))
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            when (cardRepository.deleteCard(cardId)) {
                is DataResult.Success -> _events.send(CardDetailsEvent.Deleted)
                is DataResult.Failure -> _events.send(CardDetailsEvent.ShowMessage("Couldn't delete card"))
            }
        }
    }

    fun shareNumber() {
        _state.value.card?.let { shareManager.shareCardNumber(it) }
    }

    fun shareDetails() {
        _state.value.card?.let { shareManager.shareDetails(it) }
    }

    fun shareBarcode() {
        val card = _state.value.card ?: return
        viewModelScope.launch { shareManager.shareBarcodeImage(card) }
    }
}
