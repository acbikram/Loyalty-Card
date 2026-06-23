package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.security.PinHasher
import org.junit.Test

class PinHasherTest {

    @Test
    fun sameInputsProduceSameHash() {
        val salt = PinHasher.newSalt()
        assertThat(PinHasher.hash("1234", salt)).isEqualTo(PinHasher.hash("1234", salt))
    }

    @Test
    fun verifyAcceptsCorrectPinAndRejectsWrong() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("2468", salt)
        assertThat(PinHasher.verify("2468", salt, hash)).isTrue()
        assertThat(PinHasher.verify("0000", salt, hash)).isFalse()
    }

    @Test
    fun differentSaltsProduceDifferentHashes() {
        val a = PinHasher.hash("1234", PinHasher.newSalt())
        val b = PinHasher.hash("1234", PinHasher.newSalt())
        assertThat(a).isNotEqualTo(b)
    }
}
