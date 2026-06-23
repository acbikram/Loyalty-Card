package com.universalwallet.loyalty.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.universalwallet.loyalty.core.theme.CornerRadius
import com.universalwallet.loyalty.core.theme.Elevation
import com.universalwallet.loyalty.core.theme.Spacing
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * A reusable surface container with the app's standard wallet-card styling.
 * An optional [accentColor] (typically taken from a store definition) tints the
 * container while [content] supplies the body.
 */
@Composable
fun WalletCard(
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = if (accentColor != null) {
        CardDefaults.cardColors(containerColor = accentColor)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    }
    val shape = RoundedCornerShape(CornerRadius.large)
    val elevation = CardDefaults.cardElevation(defaultElevation = Elevation.level2)

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, elevation = elevation) {
            Box(Modifier.padding(Spacing.lg)) { content() }
        }
    } else {
        Card(modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, elevation = elevation) {
            Box(Modifier.padding(Spacing.lg)) { content() }
        }
    }
}
