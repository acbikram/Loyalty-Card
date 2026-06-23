package com.universalwallet.loyalty.feature.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for the archived-cards screen. */
data class ArchiveUiState(
    val isLoading: Boolean = true,
    val cards: List<LoyaltyCard> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && cards.isEmpty()
}

/** Lists archived cards and supports restoring or permanently deleting them. */
@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val cardRepository: LoyaltyCardRepository,
) : ViewModel() {

    val state: StateFlow<ArchiveUiState> = cardRepository.observeArchived()
        .map { ArchiveUiState(isLoading = false, cards = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArchiveUiState())

    fun restore(id: String) {
        viewModelScope.launch { cardRepository.setArchived(id, false) }
    }

    fun delete(id: String) {
        viewModelScope.launch { cardRepository.deleteCard(id) }
    }
}
