package com.universalwallet.loyalty.core.security

import android.util.Log

/**
 * Lightweight logging gate. Logging is OFF unless explicitly enabled via
 * Developer Mode, and call sites must never pass sensitive values (card numbers,
 * PINs, decrypted fields). This indirection keeps `Log` calls out of release
 * behaviour by default and gives one switch to silence everything.
 */
object AppLog {
    @Volatile
    var debugEnabled: Boolean = false

    fun d(tag: String, message: String) { if (debugEnabled) Log.d(tag, message) }
    fun i(tag: String, message: String) { if (debugEnabled) Log.i(tag, message) }
    fun w(tag: String, message: String) { if (debugEnabled) Log.w(tag, message) }
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (debugEnabled) Log.e(tag, message, throwable)
    }
}
