package com.universalwallet.loyalty.core.utils

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Abstraction over the set of dispatchers used across the app.
 *
 * A single injectable provider is convenient for classes that need more than
 * one dispatcher, and trivial to fake in unit tests.
 */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
    val mainImmediate: CoroutineDispatcher
}
