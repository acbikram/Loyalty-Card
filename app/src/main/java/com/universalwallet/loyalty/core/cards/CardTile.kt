package com.universalwallet.loyalty.core.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.CornerRadius
import com.universalwallet.loyalty.core.theme.Elevation
import com.universalwallet.loyalty.core.theme.IconSize
import com.universalwallet.loyalty.core.theme.Spacing

/**
 * Compact card representation for grids and lists. Mirrors the [LargeLoyaltyCard]
 * styling at a smaller scale so a collection reads as a stack of real cards.
 */
@Composable
fun CardTile(
    card: com.universalwallet.loyalty.domain.model.LoyaltyCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = CardStyle.fromId(card.colorThemeId)
    val visual = cardVisual(style)
    val shape = RoundedCornerShape(CornerRadius.large)
    val description = buildString {
        append(card.storeName)
        append(", ending ${card.cardNumber.takeLast(4)}")
        if (card.isFavorite) append(", favourite")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .shadow(Elevation.level2, shape, clip = false)
            .clip(shape)
            .background(Brush.linearGradient(visual.gradient))
            .clickable(onClick = onClick)
            .padding(Spacing.lg)
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = card.storeName,
                    style = MaterialTheme.typography.titleSmall,
                    color = visual.content,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (card.isFavorite) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = visual.content,
                        modifier = Modifier
                            .padding(start = Spacing.xs)
                            .height(IconSize.small),
                    )
                }
            }
            Text(
                text = maskCardNumber(card.cardNumber),
                style = MaterialTheme.typography.bodyMedium,
                color = visual.contentMuted,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "Card tile", showBackground = true)
@Composable
private fun CardTilePreview() {
    AppTheme {
        Box(Modifier.padding(Spacing.lg)) {
            CardTile(card = sampleCard("classic"), onClick = {})
        }
    }
}
