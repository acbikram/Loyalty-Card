package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.security.PasswordCrypto
import org.junit.Test

class PasswordCryptoTest {

    @Test
    fun roundTripsWithCorrectPassword() {
        val plaintext = """{"version":1,"cards":[]}"""
        val payload = PasswordCrypto.encrypt(plaintext, "hunter2")
        assertThat(PasswordCrypto.decrypt(payload, "hunter2")).isEqualTo(plaintext)
    }

    @Test
    fun wrongPasswordFailsToDecrypt() {
        val payload = PasswordCrypto.encrypt("secret data", "correct")
        val result = runCatching { PasswordCrypto.decrypt(payload, "wrong") }
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun isEncryptedDetectsPayloadPrefix() {
        val payload = PasswordCrypto.encrypt("x", "p")
        assertThat(PasswordCrypto.isEncrypted(payload)).isTrue()
        assertThat(PasswordCrypto.isEncrypted("""{"version":1}""")).isFalse()
    }

    @Test
    fun ciphertextDiffersFromPlaintext() {
        val payload = PasswordCrypto.encrypt("VISA-1234", "pw")
        assertThat(payload).doesNotContain("VISA-1234")
    }
}
