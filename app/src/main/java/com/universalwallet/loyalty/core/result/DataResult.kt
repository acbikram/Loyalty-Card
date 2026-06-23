package com.universalwallet.loyalty.core.result

/**
 * A lightweight, allocation-friendly result wrapper used across the data and
 * domain layers. Prefer this over throwing for *expected* failures (e.g. a
 * card not found), reserving exceptions for truly exceptional situations.
 */
sealed interface DataResult<out T> {

    data class Success<T>(val data: T) : DataResult<T>

    data class Failure(val error: AppError) : DataResult<Nothing>

    val isSuccess: Boolean get() = this is Success

    /** Returns the data on success, or `null` on failure. */
    fun getOrNull(): T? = (this as? Success)?.data
}

/** Maps the success value, leaving failures untouched. */
inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}

/** Runs [action] only when this result is a success. */
inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> = apply {
    if (this is DataResult.Success) action(data)
}

/** Runs [action] only when this result is a failure. */
inline fun <T> DataResult<T>.onFailure(action: (AppError) -> Unit): DataResult<T> = apply {
    if (this is DataResult.Failure) action(error)
}
