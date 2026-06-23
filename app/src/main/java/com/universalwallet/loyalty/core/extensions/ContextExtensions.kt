package com.universalwallet.loyalty.core.extensions

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

/**
 * Walks the [Context] wrapper chain to find the hosting [ComponentActivity],
 * or returns `null` if none is found. Useful for biometric prompts and window
 * flags that require an Activity.
 */
tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
