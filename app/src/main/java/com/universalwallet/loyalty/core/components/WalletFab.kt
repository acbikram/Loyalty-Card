package com.universalwallet.loyalty.core.components

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Floating action button in icon-only or extended (icon + label) form. The
 * accessible label is required so the action is always announced.
 */
@Composable
fun WalletFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    if (label != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = { Icon(icon, contentDescription = null) },
            text = { Text(label) },
            modifier = modifier.semantics { this.contentDescription = contentDescription },
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.semantics { this.contentDescription = contentDescription },
        ) {
            Icon(icon, contentDescription = null)
        }
    }
}
