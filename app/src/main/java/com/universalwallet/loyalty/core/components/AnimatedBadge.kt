package com.universalwallet.loyalty.core.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.Badge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * A small count badge that springs in scale whenever its [count] changes,
 * giving subtle feedback when a number updates. Hidden when [count] is zero.
 */
@Composable
fun AnimatedBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) return
    val scale = remember { Animatable(1f) }
    LaunchedEffect(count) {
        scale.snapTo(0.6f)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        )
    }
    Badge(
        modifier = modifier
            .scale(scale.value)
            .semantics { contentDescription = "$count items" },
    ) {
        Text(text = if (count > 99) "99+" else count.toString())
    }
}
