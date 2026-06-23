package com.universalwallet.loyalty.data.repository

import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.core.result.toAppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Base type every repository implementation extends.
 *
 * It does not touch any data source itself; it only supplies two helpers that
 * enforce the project's error-wrapping rule so repositories never leak raw
 * exceptions to the domain layer:
 *  - [safeCall] for one-shot suspend operations, and
 *  - [safeFlow] for streaming sources.
 *
 * Both convert failures into typed [DataResult.Failure]s via [toAppError] and
 * preserve coroutine cancellation. Concrete repositories (with Room/data
 * sources) are implemented in a later phase.
 */
abstract class BaseRepository {

    protected suspend fun <T> safeCall(block: suspend () -> T): DataResult<T> =
        try {
            DataResult.Success(block())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            DataResult.Failure(throwable.toAppError())
        }

    protected fun <T> safeFlow(source: () -> Flow<T>): Flow<DataResult<T>> =
        source()
            .map<T, DataResult<T>> { DataResult.Success(it) }
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                emit(DataResult.Failure(throwable.toAppError()))
            }
}
