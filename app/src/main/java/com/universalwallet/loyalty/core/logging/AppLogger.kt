package com.universalwallet.loyalty.core.logging

/**
 * Structured logging facade over Timber.
 *
 * Rules enforced by the default implementation:
 *  - Verbose/debug output is emitted only in debug builds (Timber's planted
 *    trees decide the sink; see [CrashReportingTree] for release behaviour).
 *  - Callers must never pass sensitive values (card numbers, PINs, keys);
 *    [redact] is provided to mask anything that might be sensitive.
 *  - A consistent "tag: message" structure keeps logs greppable.
 *
 * Depending on this interface (rather than calling Timber directly) keeps call
 * sites testable — a fake logger can capture and assert on emitted records.
 */
interface AppLogger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)

    /** Masks all but the last [visible] characters of a sensitive value. */
    fun redact(value: String, visible: Int = 4): String {
        if (value.length <= visible) return "*".repeat(value.length)
        return "*".repeat(value.length - visible) + value.takeLast(visible)
    }
}
