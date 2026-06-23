package com.universalwallet.loyalty.core.security

/** How the user authenticates to unlock the app. */
enum class AuthMethod(val label: String) {
    BIOMETRIC("Biometric"),
    PIN("PIN"),
    DEVICE_CREDENTIAL("Device PIN/pattern"),
}

/** Immutable snapshot of all security preferences. */
data class SecurityConfig(
    val appLockEnabled: Boolean = false,
    val authMethod: AuthMethod = AuthMethod.BIOMETRIC,
    val autoLockTimeoutMs: Long = DEFAULT_TIMEOUT_MS,
    val lockOnBackground: Boolean = true,
    val requireAuthOnLaunch: Boolean = true,
    val screenshotProtection: Boolean = false,
    val clipboardProtection: Boolean = false,
    val developerModeEnabled: Boolean = false,
    val debugLogging: Boolean = false,
    val hasPin: Boolean = false,
) {
    companion object {
        const val DEFAULT_TIMEOUT_MS = 60_000L
    }
}

/** Result of an authentication attempt. */
sealed interface AuthResult {
    data object Success : AuthResult
    data object Cancelled : AuthResult
    data class Failed(val reason: String) : AuthResult
    data class Unavailable(val reason: String) : AuthResult
}

/** Domain-level security errors with user-friendly messages. */
sealed class SecurityError(val message: String) {
    data object KeystoreUnavailable : SecurityError("Secure storage is unavailable on this device.")
    data object EncryptionFailed : SecurityError("Couldn't secure your data. Please try again.")
    data object DecryptionFailed : SecurityError("Couldn't read secured data. The data may be corrupted.")
    data object AuthenticationFailed : SecurityError("Authentication failed. Please try again.")
    data object CorruptedBackup : SecurityError("This backup file is corrupted or unreadable.")
    data object InvalidBackup : SecurityError("This file isn't a valid wallet backup.")
    data object WrongPassword : SecurityError("Incorrect backup password.")
    data class Unknown(val detail: String) : SecurityError("Something went wrong. Please try again.")
}
