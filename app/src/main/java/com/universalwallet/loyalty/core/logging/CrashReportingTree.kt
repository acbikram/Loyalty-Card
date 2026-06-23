package com.universalwallet.loyalty.core.logging

import android.util.Log
import timber.log.Timber

/**
 * Release-build logging tree.
 *
 * - Drops verbose/debug logs entirely (they never reach release output).
 * - Forwards warnings and errors to [CrashReporter], which is a no-op by
 *   default. This is the single integration seam for a future crash-reporting
 *   backend (e.g. Crashlytics): swap the [CrashReporter] implementation and
 *   nothing else changes.
 */
class CrashReportingTree(
    private val crashReporter: CrashReporter = CrashReporter.NoOp,
) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) return

        crashReporter.log(priority, tag, message)
        if (t != null && priority >= Log.ERROR) {
            crashReporter.recordException(t)
        }
    }
}
