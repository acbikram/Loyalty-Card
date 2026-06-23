package com.universalwallet.loyalty.feature.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.universalwallet.loyalty.core.barcode.BarcodeSymbology
import com.universalwallet.loyalty.core.cards.BarcodeImage
import com.universalwallet.loyalty.core.cards.LargeLoyaltyCard
import com.universalwallet.loyalty.data.mapper.BarcodeTypeMapper
import com.universalwallet.loyalty.core.cards.displayLabel
import com.universalwallet.loyalty.core.cards.formatCardNumber
import com.universalwallet.loyalty.core.cards.sampleCard
import com.universalwallet.loyalty.core.components.ConfirmationDialog
import com.universalwallet.loyalty.core.components.ErrorState
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.WalletLoadingIndicator
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.core.ui.ObserveAsEvents
import com.universalwallet.loyalty.domain.model.LoyaltyCard

/** Stateful Card-details entry point. */
@Composable
fun CardDetailsScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: CardDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is CardDetailsEvent.Deleted -> onBack()
            is CardDetailsEvent.Archived -> onBack()
            is CardDetailsEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message) }
        }
    }

    CardDetailsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onEdit = onEdit,
        onToggleFavorite = viewModel::toggleFavorite,
        onTogglePin = viewModel::togglePin,
        onArchive = viewModel::archive,
        onDuplicate = viewModel::duplicate,
        onDelete = viewModel::delete,
        onShareNumber = viewModel::shareNumber,
        onShareDetails = viewModel::shareDetails,
        onShareBarcode = viewModel::shareBarcode,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardDetailsContent(
    state: CardDetailsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit,
    onArchive: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onShareNumber: () -> Unit,
    onShareDetails: () -> Unit,
    onShareBarcode: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFullscreen by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.card?.storeName ?: "Card") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(WalletIcons.Back, contentDescription = "Navigate back") }
                },
                actions = {
                    val card = state.card
                    if (card != null) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (card.isFavorite) WalletIcons.Star else WalletIcons.StarOutline,
                                contentDescription = if (card.isFavorite) "Remove from favourites" else "Add to favourites",
                            )
                        }
                        IconButton(onClick = { onEdit(card.id) }) {
                            Icon(WalletIcons.Edit, contentDescription = "Edit card")
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "More actions")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(if (card.isPinned) "Unpin" else "Pin to top") },
                                onClick = { showMenu = false; onTogglePin() },
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                onClick = { showMenu = false; onDuplicate() },
                            )
                            DropdownMenuItem(
                                text = { Text("Archive") },
                                onClick = { showMenu = false; onArchive() },
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = { showMenu = false; showDeleteDialog = true },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> WalletLoadingIndicator()
                state.isError || state.card == null -> ErrorState(
                    title = "Card not found",
                    description = "This card may have been deleted.",
                    onRetry = onBack,
                )
                else -> {
                    val card = state.card
                    CardDetailsBody(
                        card = card,
                        onTapCard = { showFullscreen = true },
                        onCopy = {
                            clipboard.setText(AnnotatedString(card.cardNumber))
                            scope.launch { snackbarHostState.showSnackbar("Card number copied") }
                        },
                        onShare = { showShareSheet = true },
                    )
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

    if (showFullscreen && state.card != null) {
        FullscreenCardPreview(card = state.card, onDismiss = { showFullscreen = false })
    }

    if (showShareSheet) {
        ShareSheet(
            onDismiss = { showShareSheet = false },
            onShareNumber = { showShareSheet = false; onShareNumber() },
            onShareDetails = { showShareSheet = false; onShareDetails() },
            onShareBarcode = { showShareSheet = false; onShareBarcode() },
        )
    }
}

@Composable
private fun CardDetailsBody(
    card: LoyaltyCard,
    onTapCard: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        LargeLoyaltyCard(card = card, onClick = onTapCard)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Membership number", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatCardNumber(card.cardNumber), style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = onCopy) { Icon(WalletIcons.Copy, contentDescription = "Copy number") }
            IconButton(onClick = onShare) { Icon(WalletIcons.Share, contentDescription = "Share card") }
        }

        SectionHeader("Barcode")
        BarcodeImage(
            content = card.barcodeValue,
            symbology = BarcodeTypeMapper.toSymbology(card.barcodeType),
            modifier = Modifier.fillMaxWidth(),
        )

        if (card.qrCodeValue != null) {
            SectionHeader("QR code")
            BarcodeImage(
                content = card.qrCodeValue,
                symbology = BarcodeSymbology.QR_CODE,
                modifier = Modifier
                    .size(180.dp)
                    .padding(top = Spacing.xs),
            )
        }

        SectionHeader("Details")
        DetailRow("Store", card.storeName)
        DetailRow("Category", card.category.displayLabel())
        if (card.nickname.isNotBlank()) DetailRow("Nickname", card.nickname)
        if (card.notes.isNotBlank()) DetailRow("Notes", card.notes)
        if (card.customerName?.isNotBlank() == true) DetailRow("Cardholder", card.customerName!!)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Spacing.xs)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(0.6f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareSheet(
    onDismiss: () -> Unit,
    onShareNumber: () -> Unit,
    onShareDetails: () -> Unit,
    onShareBarcode: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(bottom = Spacing.xl)) {
            SectionHeader("Share")
            ShareOption("Share card number", onShareNumber)
            ShareOption("Share card details", onShareDetails)
            ShareOption("Share barcode image", onShareBarcode)
        }
    }
}

@Composable
private fun ShareOption(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
    )
}

/** Fullscreen, edge-to-edge card view for presenting the card at a counter. */
@Composable
private fun FullscreenCardPreview(card: LoyaltyCard, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            LargeLoyaltyCard(card = card, onClick = onDismiss)
            BarcodeImage(
                content = card.barcodeValue,
                symbology = BarcodeTypeMapper.toSymbology(card.barcodeType),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun CardDetailsPreview() {
    AppTheme {
        CardDetailsContent(
            state = CardDetailsUiState(isLoading = false, card = sampleCard("business")),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {}, onEdit = {}, onToggleFavorite = {}, onTogglePin = {}, onArchive = {},
            onDuplicate = {}, onDelete = {}, onShareNumber = {}, onShareDetails = {}, onShareBarcode = {},
        )
    }
}
