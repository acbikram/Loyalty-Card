package com.universalwallet.loyalty.core.result

import kotlinx.coroutines.TimeoutCancellationException
import java.io.IOException

/**
 * Classifies an arbitrary [Throwable] into the app's [AppError] taxonomy. This
 * is the single place raw exceptions become typed errors, so base classes and
 * repositories can wrap failures consistently without duplicating `when`-chains.
 *
 * Note: [kotlinx.coroutines.CancellationException] is intentionally NOT mapped
 * here — cancellation must propagate so coroutines cancel correctly. Callers
 * should rethrow it before invoking this mapper.
 */
fun Throwable.toAppError(): AppError = when (this) {
    is TimeoutCancellationException -> AppError.Network.Timeout
    is NoSuchElementException -> AppError.Database.NotFound
    is IOException -> AppError.Network.Unexpected(this)
    is IllegalArgumentException -> AppError.Validation(message ?: "Invalid input")
    else -> AppError.Unknown(this)
}
