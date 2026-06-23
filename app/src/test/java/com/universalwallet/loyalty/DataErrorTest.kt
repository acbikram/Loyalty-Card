package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.result.AppError
import com.universalwallet.loyalty.data.model.DataError
import com.universalwallet.loyalty.data.model.toAppError
import org.junit.Test

/** Verifies the DataError -> AppError bridge. */
class DataErrorTest {

    @Test
    fun notFound_mapsToDatabaseNotFound() {
        assertThat(DataError.NotFoundError("x").toAppError()).isEqualTo(AppError.Database.NotFound)
    }

    @Test
    fun duplicate_mapsToValidation() {
        assertThat(DataError.DuplicateError("lulu", "123").toAppError())
            .isInstanceOf(AppError.Validation::class.java)
    }

    @Test
    fun validation_preservesReason() {
        val mapped = DataError.ValidationError("bad").toAppError()
        assertThat(mapped).isInstanceOf(AppError.Validation::class.java)
        assertThat((mapped as AppError.Validation).reason).isEqualTo("bad")
    }
}
