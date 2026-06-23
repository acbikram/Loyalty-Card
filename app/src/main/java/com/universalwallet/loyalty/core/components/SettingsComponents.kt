package com.universalwallet.loyalty.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.universalwallet.loyalty.core.theme.Dimensions
import com.universalwallet.loyalty.core.theme.IconSize
import com.universalwallet.loyalty.core.theme.Spacing

/** A small section title used to group settings and content blocks. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            start = Spacing.screenHorizontal,
            end = Spacing.screenHorizontal,
            top = Spacing.lg,
            bottom = Spacing.sm,
        ),
    )
}

/** A tappable settings row: leading icon, title, optional subtitle, optional trailing. */
@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md)
            .heightInMin(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            modifier = Modifier.size(IconSize.medium),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.lg),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailing != null) trailing()
    }
}

/** A settings row that toggles a boolean via a trailing [Switch]. */
@Composable
fun SwitchItem(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    SettingsItem(
        title = title,
        icon = icon,
        subtitle = subtitle,
        enabled = enabled,
        onClick = { if (enabled) onCheckedChange(!checked) },
        modifier = modifier.semantics { contentDescription = title },
        trailing = {
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        },
    )
}

/** Enforces the accessible minimum touch-target height. */
private fun Modifier.heightInMin(): Modifier =
    this.then(androidx.compose.foundation.layout.heightIn(min = Dimensions.minTouchTarget))
