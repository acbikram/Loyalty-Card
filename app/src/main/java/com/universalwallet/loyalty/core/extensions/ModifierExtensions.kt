package com.universalwallet.loyalty.core.extensions

import androidx.compose.ui.Modifier

/**
 * Applies [block] to the modifier only when [condition] is true, otherwise
 * returns the modifier unchanged. Keeps call sites free of `if`-expressions.
 */
inline fun Modifier.applyIf(
    condition: Boolean,
    block: Modifier.() -> Modifier,
): Modifier = if (condition) block() else this
