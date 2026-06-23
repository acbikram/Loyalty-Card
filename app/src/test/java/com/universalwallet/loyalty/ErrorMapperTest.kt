package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.result.AppError
import com.universalwallet.loyalty.core.result.toAppError
import org.junit.Test
import java.io.IOException

/** Verifies raw throwables are classified into the AppError taxonomy. */
class ErrorMapperTest {

    @Test
    fun ioException_mapsToNetworkUnexpected() {
        val error = IOException("boom").toAppError()
        assertThat(error).isInstanceOf(AppError.Network.Unexpected::class.java)
    }

    @Test
    fun illegalArgument_mapsToValidation() {
        val error = IllegalArgumentException("bad").toAppError()
        assertThat(error).isInstanceOf(AppError.Validation::class.java)
    }

    @Test
    fun genericException_mapsToUnknown() {
        val error = RuntimeException("?").toAppError()
        assertThat(error).isInstanceOf(AppError.Unknown::class.java)
    }
}
