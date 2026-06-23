package com.universalwallet.loyalty.feature.addcard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.cards.BarcodeImage
import com.universalwallet.loyalty.core.cards.CardStyle
import com.universalwallet.loyalty.core.cards.LargeLoyaltyCard
import com.universalwallet.loyalty.core.cards.cardVisual
import com.universalwallet.loyalty.core.components.CategoryChipRow
import com.universalwallet.loyalty.core.components.ConfirmationDialog
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.StoreRow
import com.universalwallet.loyalty.core.components.WalletButton
import com.universalwallet.loyalty.core.components.WalletButtonStyle
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import com.universalwallet.loyalty.core.ui.ObserveAsEvents
import com.universalwallet.loyalty.data.mapper.BarcodeTypeMapper
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.model.StoreDefinition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Stateful Add-card entry point. */
@Composable
fun AddCardScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    onScanClick: () -> Unit,
    viewModel: AddCardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var replaceTarget by remember { mutableStateOf<LoyaltyCard?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is AddCardEvent.Saved -> showSuccess = true
            is AddCardEvent.AskReplace -> replaceTarget = event.existing
            is AddCardEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message) }
        }
    }

    LaunchedEffectSuccess(showSuccess, onSaved)

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let(viewModel::onImagePicked)
    }

    Box(Modifier.fillMaxSize()) {
        AddCardContent(
            state = state,
            snackbarHostState = snackbarHostState,
            onCancel = onCancel,
            onScanClick = onScanClick,
            onImportClick = { imagePicker.launch("image/*") },
            onStoreQueryChange = viewModel::onStoreQueryChange,
            onSelectStore = viewModel::onSelectStore,
            onNumberChange = viewModel::onNumberChange,
            onNicknameChange = viewModel::onNicknameChange,
            onCategoryChange = viewModel::onCategoryChange,
            onStyleChange = viewModel::onStyleChange,
            onSave = viewModel::save,
        )
        SuccessOverlay(visible = showSuccess)
    }

    replaceTarget?.let { existing ->
        ConfirmationDialog(
            title = "Card already exists",
            message = "A card for ${existing.storeName} with this number is already saved. Replace it?",
            confirmText = "Replace",
            onConfirm = {
                replaceTarget = null
                viewModel.confirmReplace()
            },
            onDismiss = { replaceTarget = null },
        )
    }
}

@Composable
private fun LaunchedEffectSuccess(showSuccess: Boolean, onSaved: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(800)
            onSaved()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCardContent(
    state: AddCardUiState,
    snackbarHostState: SnackbarHostState,
    onCancel: () -> Unit,
    onScanClick: () -> Unit,
    onImportClick: () -> Unit,
    onStoreQueryChange: (String) -> Unit,
    onSelectStore: (StoreDefinition) -> Unit,
    onNumberChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onCategoryChange: (CardCategory) -> Unit,
    onStyleChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add card") },
                navigationIcon = {
                    IconButton(onClick = onCancel) { Icon(WalletIcons.Close, contentDescription = "Cancel") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = Spacing.xxl),
        ) {
            // Live preview card.
            val previewCard = remember(
                state.selectedStore, state.cardNumber, state.nickname,
                state.category, state.styleId, state.barcodeType,
            ) {
                LoyaltyCard(
                    id = "preview",
                    storeId = state.selectedStore?.storeId.orEmpty(),
                    storeName = state.selectedStore?.storeName ?: "Your store",
                    cardNumber = state.cardNumber.ifBlank { "0000000000000" },
                    barcodeValue = state.cardNumber.ifBlank { "0000000000000" },
                    barcodeType = state.barcodeType,
                    nickname = state.nickname,
                    category = state.category,
                    createdAt = 0L,
                    updatedAt = 0L,
                    colorThemeId = state.styleId,
                )
            }
            Box(Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm)) {
                LargeLoyaltyCard(card = previewCard)
            }
            if (state.cardNumber.isNotBlank()) {
                BarcodeImage(
                    content = state.cardNumber,
                    symbology = BarcodeTypeMapper.toSymbology(state.barcodeType),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal)
                        .heightIn(min = 64.dp),
                )
            }

            if (state.hasDuplicate) {
                DuplicateBanner()
            }

            SectionHeader("Store")
            if (state.selectedStore == null) {
                OutlinedTextField(
                    value = state.storeQuery,
                    onValueChange = onStoreQueryChange,
                    label = { Text("Search stores") },
                    leadingIcon = { Icon(WalletIcons.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal),
                )
                Column(Modifier.heightIn(max = 240.dp)) {
                    state.filteredStores.take(6).forEach { store ->
                        StoreRow(store = store, onClick = { onSelectStore(store) })
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(state.selectedStore.storeName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onStoreQueryChange("") }) {
                        Icon(WalletIcons.Close, contentDescription = "Change store")
                    }
                }
            }

            SectionHeader("Card number")
            OutlinedTextField(
                value = state.cardNumber,
                onValueChange = onNumberChange,
                label = { Text("Membership number") },
                singleLine = true,
                isError = state.cardNumber.isBlank() && state.selectedStore != null,
                trailingIcon = {
                    IconButton(onClick = onScanClick) {
                        Icon(WalletIcons.Scan, contentDescription = "Scan barcode")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal),
            )
            Text(
                text = "Detected type: ${state.barcodeType.name}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xxs),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                WalletButton(text = "Scan", onClick = onScanClick, leadingIcon = WalletIcons.Scan, style = WalletButtonStyle.SECONDARY, modifier = Modifier.weight(1f))
                WalletButton(text = "Import image", onClick = onImportClick, style = WalletButtonStyle.SECONDARY, enabled = !state.isProcessingImage, modifier = Modifier.weight(1f))
            }

            OutlinedTextField(
                value = state.nickname,
                onValueChange = onNicknameChange,
                label = { Text("Nickname (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal),
            )

            SectionHeader("Category")
            CategoryChipRow(
                selected = state.category,
                onSelect = { onCategoryChange(it ?: CardCategory.GENERAL) },
                modifier = Modifier.fillMaxWidth(),
            )

            SectionHeader("Card style")
            StylePicker(selectedId = state.styleId, onSelect = onStyleChange)

            Row(
                modifier = Modifier.fillMaxWidth().padding(Spacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                WalletButton(text = "Cancel", onClick = onCancel, style = WalletButtonStyle.TEXT, modifier = Modifier.weight(1f))
                WalletButton(text = if (state.hasDuplicate) "Replace" else "Save", onClick = onSave, enabled = state.canSave, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DuplicateBanner() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
    ) {
        Text(
            text = "A card with this number already exists at this store. Saving will offer to replace it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(Spacing.md),
        )
    }
}

@Composable
private fun StylePicker(selectedId: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(CardStyle.entries) { style ->
            val visual = cardVisual(style)
            val selected = style.id == selectedId
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(visual.gradient))
                    .then(if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                    .clickable { onSelect(style.id) },
                contentAlignment = Alignment.Center,
            ) {
                Text(style.displayName.take(1), color = visual.content, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SuccessOverlay(visible: Boolean) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            val scale by animateFloatAsState(
                targetValue = if (visible) 1f else 0.5f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "successScale",
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Saved",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(96.dp).scale(scale),
                )
                Text("Card saved", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = Spacing.md))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun AddCardPreview() {
    AppTheme {
        AddCardContent(
            state = AddCardUiState(cardNumber = "6291000000001"),
            snackbarHostState = remember { SnackbarHostState() },
            onCancel = {}, onScanClick = {}, onImportClick = {}, onStoreQueryChange = {},
            onSelectStore = {}, onNumberChange = {}, onNicknameChange = {},
            onCategoryChange = {}, onStyleChange = {}, onSave = {},
        )
    }
}
