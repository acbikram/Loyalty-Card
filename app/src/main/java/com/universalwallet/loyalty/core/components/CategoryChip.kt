package com.universalwallet.loyalty.core.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.universalwallet.loyalty.core.cards.displayLabel
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.domain.model.CardCategory

/** A single selectable category filter chip. */
@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(),
    )
}

/**
 * Horizontal, scrollable row of category chips with a leading "All" option.
 * [selected] is `null` when "All" is chosen.
 */
@Composable
fun CategoryChipRow(
    selected: CardCategory?,
    onSelect: (CardCategory?) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<CardCategory> = CardCategory.entries,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal),
    ) {
        item {
            CategoryChip(label = "All", selected = selected == null, onClick = { onSelect(null) })
        }
        items(categories) { category ->
            CategoryChip(
                label = category.displayLabel(),
                selected = selected == category,
                onClick = { onSelect(category) },
            )
        }
    }
}
