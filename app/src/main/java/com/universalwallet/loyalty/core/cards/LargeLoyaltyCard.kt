package com.universalwallet.loyalty.core.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard

/**
 * The premium, full-width loyalty-card face. Appearance is driven entirely by
 * the card's [LoyaltyCard.colorThemeId] (see [CardStyle]); supports all ten
 * styles, light and dark, with a gradient background, rounded corners, and a
 * soft shadow.
 *
 * @param masked when true, shows only the last four digits of the number.
 */
@Composable
fun LargeLoyaltyCard(
    card: LoyaltyCard,
    modifier: Modifier = Modifier,
    masked: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val style = CardStyle.fromId(card.colorThemeId)
    val visual = cardVisual(style)
    val shape = RoundedCornerShape(CornerRadius.extraLarge)
    val number = if (masked) maskCardNumber(card.cardNumber) else formatCardNumber(card.cardNumber)

    val description = buildString {
        append(card.storeName)
        if (card.nickname.isNotBlank()) append(", ${card.nickname}")
        append(", ${card.category.displayLabel()} card")
        if (card.isFavorite) append(", favourite")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(Elevation.level3, shape, clip = false)
            .clip(shape)
            .background(Brush.linearGradient(visual.gradient))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(Spacing.xl)
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.storeName,
                        style = MaterialTheme.typography.titleLarge,
                        color = visual.content,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (card.nickname.isNotBlank()) {
                        Text(
                            text = card.nickname,
                            style = MaterialTheme.typography.bodyMedium,
                            color = visual.contentMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (card.isFavorite) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = visual.content,
                        modifier = Modifier.padding(start = Spacing.sm),
                    )
                }
            }

            Column {
                Text(
                    text = card.category.displayLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = visual.contentMuted,
                )
                Text(
                    text = number,
                    style = MaterialTheme.typography.titleMedium,
                    color = visual.content,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Human label for a category, title-cased. */
fun CardCategory.displayLabel(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }

@Preview(name = "Large card", showBackground = true)
@Composable
private fun LargeLoyaltyCardPreview() {
    AppTheme {
        Box(Modifier.padding(Spacing.lg)) {
            LargeLoyaltyCard(card = sampleCard())
        }
    }
}

internal fun sampleCard(style: String = "gradient") = LoyaltyCard(
    id = "preview",
    storeId = "lulu",
    storeName = "Lulu Hypermarket",
    cardNumber = "6291000000001",
    barcodeValue = "6291000000001",
    barcodeType = BarcodeType.EAN13,
    nickname = "Groceries",
    category = CardCategory.SUPERMARKET,
    isFavorite = true,
    createdAt = 0L,
    updatedAt = 0L,
    colorThemeId = style,
)
