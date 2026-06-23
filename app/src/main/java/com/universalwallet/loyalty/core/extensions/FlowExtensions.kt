package com.universalwallet.loyalty.core.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Converts a cold [Flow] into a hot [StateFlow] using the conventional
 * "while subscribed (5s stop timeout)" sharing policy, which is the safe
 * default for UI state in view-models.
 */
fun <T> Flow<T>.stateInWhileSubscribed(
    scope: CoroutineScope,
    initialValue: T,
    stopTimeoutMillis: Long = 5_000L,
): StateFlow<T> = stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
    initialValue = initialValue,
)
