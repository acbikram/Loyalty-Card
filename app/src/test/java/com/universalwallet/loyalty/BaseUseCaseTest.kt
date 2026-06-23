package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.domain.usecase.BaseUseCase
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

/** Confirms the BaseUseCase wrapper produces typed results for success/failure. */
class BaseUseCaseTest {

    private class DoublingUseCase(dispatcher: kotlinx.coroutines.CoroutineDispatcher) :
        BaseUseCase<Int, Int>(dispatcher) {
        override suspend fun execute(params: Int): Int {
            require(params >= 0) { "must be non-negative" }
            return params * 2
        }
    }

    @Test
    fun success_wrapsValue() = runTest {
        val useCase = DoublingUseCase(StandardTestDispatcher(testScheduler))
        val result = useCase(21)
        assertThat(result).isInstanceOf(DataResult.Success::class.java)
        assertThat(result.getOrNull()).isEqualTo(42)
    }

    @Test
    fun thrownException_wrapsAsFailure() = runTest {
        val useCase = DoublingUseCase(StandardTestDispatcher(testScheduler))
        val result = useCase(-1)
        assertThat(result).isInstanceOf(DataResult.Failure::class.java)
    }
}
