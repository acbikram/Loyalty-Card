package com.universalwallet.loyalty.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android Keystore-backed AES-256/GCM encryptor for field-level encryption at
 * rest. The key never leaves the hardware-backed keystore. The payload is
 * versioned (`v1:ivB64:cipherB64`) so a future key rotation can introduce `v2`
 * while still decrypting old `v1` data — see [CURRENT_VERSION] and
 * [rotateKey]. GCM provides integrity, so tampering fails decryption.
 */
@Singleton
class KeystoreEncryptionManager @Inject constructor() : Encryptor {

    override fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey(aliasFor(CURRENT_VERSION)))
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val enc = Base64.getEncoder()
        return "$CURRENT_VERSION:${enc.encodeToString(iv)}:${enc.encodeToString(ciphertext)}"
    }

    override fun decrypt(payload: String): String {
        val parts = payload.split(":")
        require(parts.size == 3) { "Malformed encrypted payload" }
        val version = parts[0]
        val dec = Base64.getDecoder()
        val iv = dec.decode(parts[1])
        val ciphertext = dec.decode(parts[2])
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(aliasFor(version)), GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    /**
     * Generates the next key version. New writes use it immediately; existing
     * `v1` payloads remain decryptable because their version is embedded. Bulk
     * re-encryption of old records can then run in the background.
     */
    fun rotateKey(): String {
        val next = "v" + (CURRENT_VERSION.removePrefix("v").toInt() + 1)
        getOrCreateKey(aliasFor(next))
        return next
    }

    private fun aliasFor(version: String): String = "${KEY_ALIAS_BASE}_$version"

    private fun getOrCreateKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS_BASE = "uw_field_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
        const val CURRENT_VERSION = "v1"
    }
}
