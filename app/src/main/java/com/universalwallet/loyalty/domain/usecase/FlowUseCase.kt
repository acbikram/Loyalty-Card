package com.universalwallet.loyalty.domain.usecase

import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.core.result.toAppError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Base class for streaming use cases that emit over time (e.g. observing the
 * card list). Each emission is wrapped in [DataResult]; errors are converted to
 * typed failures rather than terminating the collector with an exception, and
 * the stream runs on the supplied [dispatcher].
 */
abstract class FlowUseCase<in P, R>(
    private val dispatcher: CoroutineDispatcher,
) {
    operator fun invoke(params: P): Flow<DataResult<R>> =
        execute(params)
            .map<R, DataResult<R>> { DataResult.Success(it) }
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                emit(DataResult.Failure(throwable.toAppError()))
            }
            .flowOn(dispatcher)

    /** Implement the cold source stream here. */
    protected abstract fun execute(params: P): Flow<R>
}
