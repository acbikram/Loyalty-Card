package com.universalwallet.loyalty.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.universalwallet.loyalty.core.theme.Dimensions
import com.universalwallet.loyalty.core.theme.IconSize
import com.universalwallet.loyalty.core.theme.Spacing

/** Visual emphasis levels for [WalletButton]. */
enum class WalletButtonStyle { PRIMARY, SECONDARY, TEXT }

/**
 * The single button component for the app. Enforces the standard button height
 * and an accessible content description, and supports an optional leading icon.
 *
 * @param contentDescription overrides the announced label; defaults to [text].
 */
@Composable
fun WalletButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: WalletButtonStyle = WalletButtonStyle.PRIMARY,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    contentDescription: String? = null,
) {
    val content: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(IconSize.medium),
                )
            }
            Text(text = text)
        }
    }

    val buttonModifier = modifier
        .heightIn(min = Dimensions.buttonHeight)
        .clearAndSetSemantics { this.contentDescription = contentDescription ?: text }

    when (style) {
        WalletButtonStyle.PRIMARY ->
            Button(onClick = onClick, enabled = enabled, modifier = buttonModifier) { content() }
        WalletButtonStyle.SECONDARY ->
            OutlinedButton(onClick = onClick, enabled = enabled, modifier = buttonModifier) { content() }
        WalletButtonStyle.TEXT ->
            TextButton(onClick = onClick, enabled = enabled, modifier = buttonModifier) { content() }
    }
}
