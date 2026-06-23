package com.universalwallet.loyalty.data.model

import com.universalwallet.loyalty.core.result.AppError

/**
 * Data-layer error taxonomy for storage and validation operations. It mirrors
 * the granularity the data layer cares about; failures are bridged to the
 * app-wide [AppError] via [toAppError] so they flow through the existing
 * `DataResult` / `ErrorHandler` pipeline unchanged.
 */
sealed class DataError {
    data class DatabaseError(val cause: Throwable? = null) : DataError()
    data class ValidationError(val reason: String) : DataError()
    data class NotFoundError(val id: String) : DataError()
    data class DuplicateError(val storeId: String, val cardNumber: String) : DataError()
    data class UnknownError(val cause: Throwable? = null) : DataError()
}

/** Bridges a [DataError] into the application-wide [AppError] taxonomy. */
fun DataError.toAppError(): AppError = when (this) {
    is DataError.DatabaseError -> AppError.Database.Unexpected(cause)
    is DataError.ValidationError -> AppError.Validation(reason)
    is DataError.NotFoundError -> AppError.Database.NotFound
    is DataError.DuplicateError -> AppError.Validation("Duplicate card for store $storeId")
    is DataError.UnknownError -> AppError.Unknown(cause)
}
