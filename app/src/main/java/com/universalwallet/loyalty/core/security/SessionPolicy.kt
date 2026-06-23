package com.universalwallet.loyalty.core.security

/**
 * Pure decision logic for when the app should re-lock. Extracted from
 * [SessionManager] so the timeout/background/grace rules are unit-testable
 * without any Android lifecycle.
 */
object SessionPolicy {
    /** Short window after returning so quick app-switches don't demand re-auth. */
    const val GRACE_MS = 2_000L

    fun shouldLockOnForeground(
        config: SecurityConfig,
        awayMs: Long,
        alreadyLocked: Boolean,
    ): Boolean {
        if (!config.appLockEnabled) return false
        if (alreadyLocked) return true
        if (awayMs <= GRACE_MS) return false
        if (config.lockOnBackground) return true
        return awayMs >= config.autoLockTimeoutMs
    }

    fun shouldLockOnIdle(config: SecurityConfig, idleMs: Long, alreadyLocked: Boolean): Boolean {
        if (!config.appLockEnabled || alreadyLocked) return alreadyLocked && config.appLockEnabled
        return idleMs >= config.autoLockTimeoutMs
    }
}
