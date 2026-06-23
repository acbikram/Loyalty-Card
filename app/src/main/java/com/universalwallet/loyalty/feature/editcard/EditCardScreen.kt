package com.universalwallet.loyalty.feature.editcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.components.CategoryChipRow
import com.universalwallet.loyalty.core.components.ConfirmationDialog
import com.universalwallet.loyalty.core.components.ErrorState
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.WalletButton
import com.universalwallet.loyalty.core.components.WalletButtonStyle
import com.universalwallet.loyalty.core.components.WalletLoadingIndicator
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.core.ui.ObserveAsEvents
import com.universalwallet.loyalty.domain.model.CardCategory
import kotlinx.coroutines.launch

/** Stateful Edit-card entry point. */
@Composable
fun EditCardScreen(
    onDone: () -> Unit,
    viewModel: EditCardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is EditCardEvent.Saved -> onDone()
            is EditCardEvent.Deleted -> onDone()
            is EditCardEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message) }
        }
    }

    EditCardContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onCancel = onDone,
        onNumberChange = viewModel::onNumberChange,
        onNicknameChange = viewModel::onNicknameChange,
        onNotesChange = viewModel::onNotesChange,
        onCategoryChange = viewModel::onCategoryChange,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCardContent(
    state: EditCardUiState,
    snackbarHostState: SnackbarHostState,
    onCancel: () -> Unit,
    onNumberChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCategoryChange: (CardCategory) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit card") },
                navigationIcon = {
                    IconButton(onClick = onCancel) { Icon(WalletIcons.Close, contentDescription = "Cancel") }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(WalletIcons.Delete, contentDescription = "Delete card")
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> WalletLoadingIndicator()
                state.isError || state.original == null -> ErrorState(
                    title = "Card not found",
                    description = "This card may have been deleted.",
                    onRetry = onCancel,
                )
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = Spacing.xxl),
                ) {
                    SectionHeader("Card details")
                    OutlinedTextField(
                        value = state.cardNumber,
                        onValueChange = onNumberChange,
                        label = { Text("Membership number") },
                        singleLine = true,
                        isError = state.cardNumber.isBlank(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal),
                    )
                    OutlinedTextField(
                        value = state.nickname,
                        onValueChange = onNicknameChange,
                        label = { Text("Nickname (optional)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
                    )
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = onNotesChange,
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal),
                    )

                    SectionHeader("Category")
                    CategoryChipRow(
                        selected = state.category,
                        onSelect = { onCategoryChange(it ?: CardCategory.GENERAL) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Spacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        WalletButton(text = "Cancel", onClick = onCancel, style = WalletButtonStyle.TEXT, modifier = Modifier.weight(1f))
                        WalletButton(text = "Save", onClick = onSave, enabled = state.canSave, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete card?",
            message = "This will permanently remove this card from your wallet.",
            confirmText = "Delete",
            onConfirm = { showDeleteDialog = false; onDelete() },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditCardPreview() {
    AppTheme {
        EditCardContent(
            state = EditCardUiState(isLoading = false, original = com.universalwallet.loyalty.core.cards.sampleCard(), cardNumber = "6291000000001", nickname = "Groceries"),
            snackbarHostState = remember { SnackbarHostState() },
            onCancel = {}, onNumberChange = {}, onNicknameChange = {}, onNotesChange = {},
            onCategoryChange = {}, onSave = {}, onDelete = {},
        )
    }
}
