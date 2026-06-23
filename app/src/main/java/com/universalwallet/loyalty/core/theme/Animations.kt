package com.universalwallet.loyalty.core.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * Motion standards: standardised durations (ms) and easing curves so every
 * animation in the app feels part of one motion language. Animations should
 * degrade gracefully when the system "remove animations" setting is enabled.
 */
object MotionDurations {
    const val instant = 0
    const val fast = 150
    const val medium = 300
    const val slow = 450
    const val deliberate = 600
}

object MotionEasing {
    /** Material 3 standard / emphasized easing curves. */
    val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val decelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)
    val accelerate: Easing = CubicBezierEasing(0.3f, 0f, 1f, 1f)
}
