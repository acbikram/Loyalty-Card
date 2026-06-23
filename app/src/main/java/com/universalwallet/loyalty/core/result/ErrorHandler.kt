package com.universalwallet.loyalty.core.result

import com.universalwallet.loyalty.R
import com.universalwallet.loyalty.core.logging.AppLogger
import com.universalwallet.loyalty.core.ui.UiText
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralised error sink. Every layer routes [AppError]s through here so that:
 *  - errors are logged once, consistently (and routed to crash reporting in
 *    release via the planted Timber tree);
 *  - the mapping from error → user-facing [UiText] lives in exactly one place;
 *  - raw exceptions are funnelled through [toAppError] before presentation.
 *
 * The handler never logs sensitive data: it logs the error *type*, not card
 * contents.
 */
@Singleton
class ErrorHandler @Inject constructor(
    private val logger: AppLogger,
) {

    /** Logs [error] and returns the message to show the user. */
    fun handle(error: AppError): UiText {
        logger.e(TAG, "Handled ${error::class.simpleName}", error.cause)
        return toUiText(error)
    }

    /** Convenience for raw throwables: classify, log, and map in one step. */
    fun handle(throwable: Throwable): UiText = handle(throwable.toAppError())

    /** Pure mapping from a typed error to a user-facing message. */
    fun toUiText(error: AppError): UiText = when (error) {
        is AppError.Network.NoConnection -> UiText.Resource(R.string.error_network_no_connection)
        is AppError.Network.Timeout -> UiText.Resource(R.string.error_network_timeout)
        is AppError.Network.Unexpected -> UiText.Resource(R.string.error_network_generic)
        is AppError.Database.NotFound -> UiText.Resource(R.string.error_not_found)
        is AppError.Database.WriteFailed -> UiText.Resource(R.string.error_database_generic)
        is AppError.Database.Unexpected -> UiText.Resource(R.string.error_database_generic)
        is AppError.Validation -> UiText.Resource(R.string.error_validation_generic)
        is AppError.Security.Unauthenticated -> UiText.Resource(R.string.error_security_unauthenticated)
        is AppError.Security.KeyUnavailable -> UiText.Resource(R.string.error_security_generic)
        is AppError.Security.BiometricUnavailable -> UiText.Resource(R.string.error_security_biometric)
        is AppError.Security.Unexpected -> UiText.Resource(R.string.error_security_generic)
        is AppError.Plugin.NotFound -> UiText.Resource(R.string.error_plugin_generic)
        is AppError.Plugin.DuplicateRegistration -> UiText.Resource(R.string.error_plugin_generic)
        is AppError.Plugin.InvalidDefinition -> UiText.Resource(R.string.error_plugin_generic)
        is AppError.Plugin.Unexpected -> UiText.Resource(R.string.error_plugin_generic)
        is AppError.Unknown -> UiText.Resource(R.string.error_unknown)
    }

    private companion object {
        const val TAG = "ErrorHandler"
    }
}
