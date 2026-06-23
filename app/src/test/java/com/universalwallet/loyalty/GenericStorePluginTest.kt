package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.plugin.GenericStorePlugin
import com.universalwallet.loyalty.domain.validation.ValidationResult
import org.junit.Test

/** Validates the permissive fallback plugin behaviour. */
class GenericStorePluginTest {

    private val plugin = GenericStorePlugin()

    @Test
    fun blankNumber_isInvalid() {
        assertThat(plugin.validateCard("   ").isValid).isFalse()
    }

    @Test
    fun reasonableNumber_isValid() {
        assertThat(plugin.validateCard("123456789").isValid).isTrue()
    }

    @Test
    fun overlyLongNumber_isInvalid() {
        val tooLong = "1".repeat(65)
        assertThat(plugin.validateCard(tooLong)).isInstanceOf(ValidationResult.Invalid::class.java)
    }

    @Test
    fun formatCard_trimsWhitespace() {
        assertThat(plugin.formatCard("  12345  ")).isEqualTo("12345")
    }
}
