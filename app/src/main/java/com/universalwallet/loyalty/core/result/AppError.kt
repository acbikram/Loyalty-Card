package com.universalwallet.loyalty.core.result

/**
 * The global error taxonomy. Every recoverable failure in the app is expressed
 * as one of these so the UI layer can map errors to user-facing messages in a
 * single place rather than inspecting raw exceptions.
 */
sealed class AppError(open val cause: Throwable? = null) {

    /** Connectivity / remote failures (relevant once cloud sync is added). */
    sealed class Network(cause: Throwable? = null) : AppError(cause) {
        data object NoConnection : Network()
        data object Timeout : Network()
        data class Unexpected(override val cause: Throwable?) : Network(cause)
    }

    /** Local persistence failures (Room / DataStore). */
    sealed class Database(cause: Throwable? = null) : AppError(cause) {
        data object NotFound : Database()
        data object WriteFailed : Database()
        data class Unexpected(override val cause: Throwable?) : Database(cause)
    }

    /** Input validation failures, carrying a machine-readable [reason]. */
    data class Validation(val reason: String) : AppError()

    /** Security subsystem failures (Keystore, biometrics, PIN). */
    sealed class Security(cause: Throwable? = null) : AppError(cause) {
        data object Unauthenticated : Security()
        data object KeyUnavailable : Security()
        data object BiometricUnavailable : Security()
        data class Unexpected(override val cause: Throwable?) : Security(cause)
    }

    /** Plugin / store-definition subsystem failures. */
    sealed class Plugin(cause: Throwable? = null) : AppError(cause) {
        /** No plugin is registered for the requested store id. */
        data class NotFound(val storeId: String) : Plugin()
        /** A plugin with the same store id was already registered. */
        data class DuplicateRegistration(val storeId: String) : Plugin()
        /** A plugin or store definition is structurally invalid. */
        data class InvalidDefinition(val reason: String) : Plugin()
        data class Unexpected(override val cause: Throwable?) : Plugin(cause)
    }

    /** Anything not otherwise classified. */
    data class Unknown(override val cause: Throwable?) : AppError(cause)
}
