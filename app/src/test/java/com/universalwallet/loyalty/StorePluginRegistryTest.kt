package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.barcode.BarcodeSymbology
import com.universalwallet.loyalty.core.plugin.CardTemplate
import com.universalwallet.loyalty.core.plugin.StorePluginContract
import com.universalwallet.loyalty.core.plugin.StorePluginRegistry
import com.universalwallet.loyalty.core.plugin.StoreTheme
import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.domain.validation.ValidationResult
import org.junit.Before
import org.junit.Test

/** Verifies registry registration, duplicate prevention, and fallback resolution. */
class StorePluginRegistryTest {

    private lateinit var registry: StorePluginRegistry

    @Before
    fun setUp() {
        registry = StorePluginRegistry()
    }

    private fun fakePlugin(id: String) = object : StorePluginContract {
        override fun getStoreId() = id
        override fun getStoreName() = "Fake $id"
        override fun getSupportedBarcodeTypes() = listOf(BarcodeSymbology.CODE_128)
        override fun getTheme() = StoreTheme("#000000", "#FFFFFF")
        override fun getCardTemplate() =
            CardTemplate("Number", "رقم", BarcodeSymbology.CODE_128, ".+")
        override fun validateCard(cardNumber: String) = ValidationResult.Valid
        override fun formatCard(cardNumber: String) = cardNumber.trim()
    }

    @Test
    fun unknownId_resolvesToFallback() {
        assertThat(registry.resolve("nope")).isSameInstanceAs(registry.fallback)
    }

    @Test
    fun register_thenResolve_returnsThatPlugin() {
        val plugin = fakePlugin("lulu")
        val result = registry.register(plugin)
        assertThat(result).isInstanceOf(DataResult.Success::class.java)
        assertThat(registry.resolve("lulu")).isSameInstanceAs(plugin)
        assertThat(registry.isRegistered("lulu")).isTrue()
    }

    @Test
    fun duplicateRegistration_fails() {
        registry.register(fakePlugin("lulu"))
        val second = registry.register(fakePlugin("lulu"))
        assertThat(second).isInstanceOf(DataResult.Failure::class.java)
    }

    @Test
    fun registerAll_reportsDuplicatesOnly() {
        val failures = registry.registerAll(
            listOf(fakePlugin("a"), fakePlugin("b"), fakePlugin("a")),
        )
        assertThat(failures).hasSize(1)
    }
}
