package com.universalwallet.loyalty.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

/**
 * A presentation-layer string that may be either a literal value or a string
 * resource. This lets the domain/view-model layers produce user-facing text
 * without holding a [Context] reference, which keeps them testable and aids
 * localisation (including RTL languages such as Arabic).
 */
sealed interface UiText {

    data class Dynamic(val value: String) : UiText

    data class Resource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText

    /** Resolves the text outside composition (e.g. for notifications). */
    fun asString(context: Context): String = when (this) {
        is Dynamic -> value
        is Resource -> context.getString(resId, *args.toTypedArray())
    }

    /** Resolves the text inside composition. */
    @Composable
    fun asString(): String = when (this) {
        is Dynamic -> value
        is Resource -> stringResource(resId, *args.toTypedArray())
    }
}
