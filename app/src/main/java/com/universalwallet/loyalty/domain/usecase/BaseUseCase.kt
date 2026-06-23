package com.universalwallet.loyalty.domain.usecase

import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.core.result.toAppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Base class for one-shot, suspend use cases.
 *
 * Why use cases? They give each unit of business logic a single, named entry
 * point that is trivial to test in isolation (no Android, no UI) and keeps that
 * logic out of ViewModels and repositories. The generic execution wrapper here
 * guarantees every use case runs on the right dispatcher and converts thrown
 * exceptions into a typed [DataResult] failure — so callers handle one result
 * type and never see raw exceptions. Cooperative cancellation is preserved by
 * rethrowing [CancellationException].
 *
 * @param P input parameter type ([Unit] when none is needed)
 * @param R success payload type
 */
abstract class BaseUseCase<in P, R>(
    private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(params: P): DataResult<R> = withContext(dispatcher) {
        try {
            DataResult.Success(execute(params))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            DataResult.Failure(throwable.toAppError())
        }
    }

    /** Implement the actual business logic here. */
    protected abstract suspend fun execute(params: P): R
}
