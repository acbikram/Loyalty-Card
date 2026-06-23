package com.universalwallet.loyalty.core.logging

/**
 * Abstraction over a crash-reporting backend. The default [NoOp] implementation
 * ships nothing and does nothing, honouring the "crash architecture prepared
 * but disabled by default" requirement. A real backend can be provided later
 * without touching any logging call sites.
 */
interface CrashReporter {

    fun log(priority: Int, tag: String?, message: String)

    fun recordException(throwable: Throwable)

    fun setCustomKey(key: String, value: String)

    /** No-operation reporter used until a real backend is wired in. */
    object NoOp : CrashReporter {
        override fun log(priority: Int, tag: String?, message: String) = Unit
        override fun recordException(throwable: Throwable) = Unit
        override fun setCustomKey(key: String, value: String) = Unit
    }
}
