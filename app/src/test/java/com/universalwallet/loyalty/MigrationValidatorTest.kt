package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.migration.MigrationValidator
import org.junit.Test

class MigrationValidatorTest {

    private val validator = MigrationValidator()

    @Test
    fun continuousChainIsValid() {
        val report = validator.validate(listOf(1 to 2, 2 to 3), targetVersion = 3)
        assertThat(report.isValid).isTrue()
    }

    @Test
    fun singleStepToTargetIsValid() {
        val report = validator.validate(listOf(1 to 2), targetVersion = 2)
        assertThat(report.isValid).isTrue()
    }

    @Test
    fun gapIsReported() {
        val report = validator.validate(listOf(1 to 2, 3 to 4), targetVersion = 4)
        assertThat(report.isValid).isFalse()
        assertThat(report.issues.any { it.contains("Missing") }).isTrue()
    }

    @Test
    fun nonUnitStepIsReported() {
        val report = validator.validate(listOf(1 to 3), targetVersion = 3)
        assertThat(report.isValid).isFalse()
    }

    @Test
    fun chainEndingShortOfTargetIsReported() {
        val report = validator.validate(listOf(1 to 2), targetVersion = 3)
        assertThat(report.isValid).isFalse()
    }
}
