package com.universalwallet.loyalty.core.security

/**
 * Abstraction over the device-backed encryptor used for field-level encryption
 * at rest (card numbers, notes, and other sensitive fields). Implemented by
 * [KeystoreEncryptionManager] against the Android Keystore; abstracted so call
 * sites and tests don't depend on Android.
 */
interface Encryptor {
    /** Encrypts [plaintext], returning a self-describing, versioned payload. */
    fun encrypt(plaintext: String): String

    /** Reverses [encrypt]. */
    fun decrypt(payload: String): String

    fun encryptOrNull(plaintext: String): String? = runCatching { encrypt(plaintext) }.getOrNull()
    fun decryptOrNull(payload: String): String? = runCatching { decrypt(payload) }.getOrNull()
}
