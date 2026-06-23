package com.universalwallet.loyalty.core.security

import com.universalwallet.loyalty.core.utils.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Facade coordinating security state for the rest of the app. Surfaces the
 * current [SecurityConfig] and the live lock state, exposes lifecycle hooks the
 * activity calls (launch / background / foreground / interaction), and keeps the
 * [AppLog] debug switch in sync with the persisted Developer Mode setting.
 */
@Singleton
class SecurityManager @Inject constructor(
    private val settings: SecuritySettings,
    private val sessionManager: SessionManager,
    @ApplicationScope private val scope: CoroutineScope,
) {
    val config: StateFlow<SecurityConfig> =
        settings.config.stateIn(scope, SharingStarted.Eagerly, SecurityConfig())

    val isLocked: StateFlow<Boolean> = sessionManager.locked

    init {
        // Keep the global debug-logging gate in sync; never logs sensitive data.
        settings.config
            .onEach { AppLog.debugEnabled = it.debugLogging }
            .launchIn(scope)
    }

    /** Decide the initial lock state when the process starts. */
    fun initializeOnLaunch() {
        val c = config.value
        if (c.appLockEnabled && c.requireAuthOnLaunch) sessionManager.lock() else sessionManager.unlock()
    }

    fun onUnlocked() = sessionManager.unlock()
    fun onUserInteraction() = sessionManager.recordInteraction()
    fun onAppBackgrounded() = sessionManager.onBackgrounded()
    fun onAppForegrounded() = sessionManager.onForegrounded(config.value)
    fun lockNow() = sessionManager.lock()
}
