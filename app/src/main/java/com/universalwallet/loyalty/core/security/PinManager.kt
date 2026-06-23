package com.universalwallet.loyalty.core.security

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the optional unlock PIN, delegating hashing to [PinHasher] and storage
 * to [SecuritySettings]. The plaintext PIN is never stored or logged.
 */
@Singleton
class PinManager @Inject constructor(
    private val settings: SecuritySettings,
) {
    suspend fun setPin(pin: String) {
        val salt = PinHasher.newSalt()
        settings.savePin(hash = PinHasher.hash(pin, salt), salt = salt)
    }

    suspend fun verifyPin(pin: String): Boolean {
        val hash = settings.pinHash() ?: return false
        val salt = settings.pinSalt() ?: return false
        return PinHasher.verify(pin, salt, hash)
    }

    suspend fun clearPin() = settings.clearPin()

    suspend fun hasPin(): Boolean = settings.pinHash() != null
}
