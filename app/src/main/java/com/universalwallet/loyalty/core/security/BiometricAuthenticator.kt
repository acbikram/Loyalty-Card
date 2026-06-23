package com.universalwallet.loyalty.core.security

import android.content.Context
import androidx.biometric.BiometricManager as AndroidBiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Whether biometric (or device-credential) auth can be used right now. */
enum class BiometricAvailability { AVAILABLE, NONE_ENROLLED, NO_HARDWARE, UNAVAILABLE }

/**
 * Thin wrapper over AndroidX [BiometricPrompt]. Keeps all biometric API surface
 * in one place and exposes a simple callback-based [authenticate]. Supports
 * fingerprint/face (BIOMETRIC_STRONG) and an optional device-credential
 * fallback (PIN/pattern/password).
 */
@Singleton
class BiometricAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun availability(allowDeviceCredential: Boolean): BiometricAvailability {
        val authenticators = authenticators(allowDeviceCredential)
        return when (AndroidBiometricManager.from(context).canAuthenticate(authenticators)) {
            AndroidBiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            AndroidBiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
            AndroidBiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            AndroidBiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.NO_HARDWARE
            else -> BiometricAvailability.UNAVAILABLE
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        allowDeviceCredential: Boolean,
        onResult: (AuthResult) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(AuthResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val cancelled = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    onResult(if (cancelled) AuthResult.Cancelled else AuthResult.Failed(errString.toString()))
                }
            },
        )

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators(allowDeviceCredential))
        // A negative button is required unless device credential is allowed.
        if (!allowDeviceCredential) builder.setNegativeButtonText("Cancel")
        prompt.authenticate(builder.build())
    }

    private fun authenticators(allowDeviceCredential: Boolean): Int =
        if (allowDeviceCredential) {
            AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG or
                AndroidBiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG
        }
}
