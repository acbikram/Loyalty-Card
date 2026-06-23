package com.universalwallet.loyalty

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test confirming the app under test resolves on a device/emulator.
 * The application id carries the `.debug` suffix in debug builds.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun usesExpectedApplicationId() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertThat(context.packageName).startsWith("com.universalwallet.loyalty")
    }
}
