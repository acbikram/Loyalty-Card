package com.universalwallet.loyalty.core.logging

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Timber-backed [AppLogger]. Uses a per-call tag so log records remain
 * structured and filterable regardless of where they originate.
 */
@Singleton
class DefaultAppLogger @Inject constructor() : AppLogger {

    override fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    override fun i(tag: String, message: String) {
        Timber.tag(tag).i(message)
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) Timber.tag(tag).w(throwable, message)
        else Timber.tag(tag).w(message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) Timber.tag(tag).e(throwable, message)
        else Timber.tag(tag).e(message)
    }
}
