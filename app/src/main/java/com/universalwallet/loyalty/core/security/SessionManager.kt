package com.universalwallet.loyalty.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the runtime lock state and the timestamps that feed [SessionPolicy].
 * Process-scoped (singleton) so the lock survives across screens. Decisions are
 * delegated to the pure policy; this class only tracks state.
 */
@Singleton
class SessionManager @Inject constructor() {

    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    private var lastInteraction: Long = now()
    private var backgroundedAt: Long = 0L

    private fun now(): Long = System.currentTimeMillis()

    fun lock() { _locked.value = true }

    fun unlock() {
        _locked.value = false
        lastInteraction = now()
    }

    fun recordInteraction() { lastInteraction = now() }

    fun onBackgrounded() { backgroundedAt = now() }

    fun onForegrounded(config: SecurityConfig) {
        val awayMs = if (backgroundedAt == 0L) 0L else now() - backgroundedAt
        if (SessionPolicy.shouldLockOnForeground(config, awayMs, _locked.value)) lock()
    }

    /** Called periodically (or on interaction checks) to enforce idle timeout. */
    fun enforceIdleTimeout(config: SecurityConfig) {
        val idleMs = now() - lastInteraction
        if (SessionPolicy.shouldLockOnIdle(config, idleMs, _locked.value)) lock()
    }
}
