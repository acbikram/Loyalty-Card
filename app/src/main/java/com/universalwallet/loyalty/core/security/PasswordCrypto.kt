package com.universalwallet.loyalty.core.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-based authenticated encryption for portable encrypted backups. A
 * random salt derives an AES-256 key (PBKDF2), and AES/GCM provides
 * confidentiality + integrity (a wrong password or tampered file fails to
 * decrypt rather than yielding garbage). Pure JDK crypto, so it round-trips in
 * unit tests. Payload format: `pbk1:saltB64:ivB64:cipherB64`.
 */
object PasswordCrypto {
    private const val PREFIX = "pbk1"
    private const val ITERATIONS = 150_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val GCM_TAG_BITS = 128
    private const val PBKDF2 = "PBKDF2WithHmacSHA256"
    private const val TRANSFORM = "AES/GCM/NoPadding"

    fun encrypt(plaintext: String, password: String): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES).also { random.nextBytes(it) }
        val iv = ByteArray(IV_BYTES).also { random.nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val enc = Base64.getEncoder()
        return "$PREFIX:${enc.encodeToString(salt)}:${enc.encodeToString(iv)}:${enc.encodeToString(ciphertext)}"
    }

    /** @throws IllegalArgumentException for malformed payloads; crypto throws on wrong password. */
    fun decrypt(payload: String, password: String): String {
        val parts = payload.split(":")
        require(parts.size == 4 && parts[0] == PREFIX) { "Unrecognised encrypted payload" }
        val dec = Base64.getDecoder()
        val salt = dec.decode(parts[1])
        val iv = dec.decode(parts[2])
        val ciphertext = dec.decode(parts[3])
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    fun isEncrypted(payload: String): Boolean = payload.trimStart().startsWith("$PREFIX:")

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(PBKDF2)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }
}
