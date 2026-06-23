package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.result.AppError
import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.core.result.map
import com.universalwallet.loyalty.core.result.onFailure
import com.universalwallet.loyalty.core.result.onSuccess
import org.junit.Test

/**
 * Pure-logic tests for the [DataResult] wrapper and its transform helpers. No
 * Android framework dependencies, so this runs on the local JVM.
 */
class DataResultTest {

    @Test
    fun success_reportsSuccessAndExposesData() {
        val result: DataResult<Int> = DataResult.Success(42)
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(42)
    }

    @Test
    fun failure_reportsFailureAndNullData() {
        val result: DataResult<Int> = DataResult.Failure(AppError.Database.NotFound)
        assertThat(result.isSuccess).isFalse()
        assertThat(result.getOrNull()).isNull()
    }

    @Test
    fun map_transformsSuccessOnly() {
        val mapped = DataResult.Success(21).map { it * 2 }
        assertThat(mapped.getOrNull()).isEqualTo(42)

        val failure: DataResult<Int> = DataResult.Failure(AppError.Validation("bad"))
        val mappedFailure = failure.map { it * 2 }
        assertThat(mappedFailure.isSuccess).isFalse()
    }

    @Test
    fun onSuccess_onFailure_invokeCorrectBranch() {
        var successData: Int? = null
        DataResult.Success(7).onSuccess { successData = it }
        assertThat(successData).isEqualTo(7)

        var capturedError: AppError? = null
        val failure: DataResult<Int> = DataResult.Failure(AppError.Network.Timeout)
        failure.onFailure { capturedError = it }
        assertThat(capturedError).isEqualTo(AppError.Network.Timeout)
    }
}
