package com.universalwallet.loyalty.core.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted PBKDF2 hashing for the optional unlock PIN. The PIN is never stored;
 * only a per-user random salt and the derived hash are persisted. Verification
 * is constant-time. Pure JDK crypto (no Android), so it is fully unit-testable.
 */
object PinHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun newSalt(): String {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hash(pin: String, saltBase64: String): String {
        val salt = Base64.getDecoder().decode(saltBase64)
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val derived = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(derived)
    }

    fun verify(pin: String, saltBase64: String, expectedHashBase64: String): Boolean =
        constantTimeEquals(hash(pin, saltBase64), expectedHashBase64)

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val x = a.toByteArray()
        val y = b.toByteArray()
        if (x.size != y.size) return false
        var result = 0
        for (i in x.indices) result = result or (x[i].toInt() xor y[i].toInt())
        return result == 0
    }
}
