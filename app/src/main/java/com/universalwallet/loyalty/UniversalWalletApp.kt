package com.universalwallet.loyalty

import android.app.Application
import com.universalwallet.loyalty.core.initialization.AppInitializer
import com.universalwallet.loyalty.core.logging.CrashReportingTree
import com.universalwallet.loyalty.core.notifications.NotificationEngine
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Application entry point.
 *
 * Responsibilities at this stage:
 *  - Bootstraps the Hilt dependency-injection graph via [HiltAndroidApp].
 *  - Plants logging first (a verbose [Timber.DebugTree] in debug builds, a
 *    quiet, redacting [CrashReportingTree] in release builds) so everything
 *    after it can log safely.
 *  - Delegates the remaining ordered startup steps to [AppInitializer].
 *
 * No business logic lives here by design; it is intentionally thin.
 */
@HiltAndroidApp
class UniversalWalletApp : Application() {

    @Inject
    lateinit var appInitializer: AppInitializer

    @Inject
    lateinit var notificationEngine: NotificationEngine

    override fun onCreate() {
        super.onCreate()
        initLogging()
        notificationEngine.ensureChannels()
        appInitializer.initialize()
    }

    private fun initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }
}
