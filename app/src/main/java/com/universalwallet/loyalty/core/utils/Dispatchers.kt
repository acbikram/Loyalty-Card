package com.universalwallet.loyalty.core.utils

import javax.inject.Qualifier

/**
 * Hilt qualifiers for the application's coroutine dispatchers.
 *
 * Injecting dispatchers (rather than referencing [kotlinx.coroutines.Dispatchers]
 * directly) keeps suspending code testable: tests can supply a single test
 * dispatcher in place of all four.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainImmediateDispatcher

/** Qualifier for the application-wide [kotlinx.coroutines.CoroutineScope]. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
