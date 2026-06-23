package com.universalwallet.loyalty.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.universalwallet.loyalty.core.theme.IconSize
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons

/**
 * Shared "state" components: empty, loading, error, and success placeholders,
 * plus progress indicators. Every screen uses these so the four canonical UI
 * states look and behave identically throughout the app.
 */

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = WalletIcons.Wallet,
    action: @Composable (() -> Unit)? = null,
) {
    CenteredMessage(
        modifier = modifier,
        icon = icon,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        title = title,
        description = description,
        action = action,
    )
}

@Composable
fun ErrorState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    CenteredMessage(
        modifier = modifier,
        icon = WalletIcons.Error,
        iconTint = MaterialTheme.colorScheme.error,
        title = title,
        description = description,
        action = onRetry?.let {
            { WalletButton(text = "Retry", onClick = it, style = WalletButtonStyle.SECONDARY) }
        },
    )
}

@Composable
fun SuccessState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    CenteredMessage(
        modifier = modifier,
        icon = WalletIcons.Card,
        iconTint = MaterialTheme.colorScheme.primary,
        title = title,
        description = description,
        action = action,
    )
}

@Composable
fun WalletLoadingIndicator(
    modifier: Modifier = Modifier,
    label: String = "Loading",
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun WalletLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun CenteredMessage(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(IconSize.extraLarge),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.lg),
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.sm),
        )
        if (action != null) {
            Column(Modifier.padding(top = Spacing.xl)) { action() }
        }
    }
}
