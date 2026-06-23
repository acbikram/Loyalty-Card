package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.security.SecurityConfig
import com.universalwallet.loyalty.core.security.SessionPolicy
import org.junit.Test

class SessionPolicyTest {

    private val enabled = SecurityConfig(appLockEnabled = true, lockOnBackground = false, autoLockTimeoutMs = 60_000L)

    @Test
    fun noLockWhenAppLockDisabled() {
        val config = SecurityConfig(appLockEnabled = false)
        assertThat(SessionPolicy.shouldLockOnForeground(config, awayMs = 999_999L, alreadyLocked = false)).isFalse()
    }

    @Test
    fun staysLockedIfAlreadyLocked() {
        assertThat(SessionPolicy.shouldLockOnForeground(enabled, awayMs = 0L, alreadyLocked = true)).isTrue()
    }

    @Test
    fun gracePeriodAvoidsRelock() {
        assertThat(SessionPolicy.shouldLockOnForeground(enabled, awayMs = 1_000L, alreadyLocked = false)).isFalse()
    }

    @Test
    fun locksAfterTimeoutAway() {
        assertThat(SessionPolicy.shouldLockOnForeground(enabled, awayMs = 61_000L, alreadyLocked = false)).isTrue()
    }

    @Test
    fun lockOnBackgroundLocksBeyondGrace() {
        val config = enabled.copy(lockOnBackground = true)
        assertThat(SessionPolicy.shouldLockOnForeground(config, awayMs = 3_000L, alreadyLocked = false)).isTrue()
    }

    @Test
    fun idleTimeoutLocks() {
        assertThat(SessionPolicy.shouldLockOnIdle(enabled, idleMs = 70_000L, alreadyLocked = false)).isTrue()
        assertThat(SessionPolicy.shouldLockOnIdle(enabled, idleMs = 5_000L, alreadyLocked = false)).isFalse()
    }
}
