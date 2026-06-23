package com.universalwallet.loyalty.feature.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.MotionDurations
import com.universalwallet.loyalty.core.theme.Spacing
import com.universalwallet.loyalty.core.theme.WalletIcons
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.padding

/**
 * Animated launch screen. Plays a short logo reveal, then — once the onboarding
 * flag has loaded — hands control to navigation via [onDecision], which receives
 * `true` when onboarding is already complete (go Home) and `false` otherwise.
 */
@Composable
fun SplashScreen(
    onDecision: (onboardingComplete: Boolean) -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val onboardingComplete by viewModel.onboardingComplete.collectAsStateWithLifecycle()

    var started by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.7f,
        animationSpec = tween(MotionDurations.slow),
        label = "logoScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(MotionDurations.slow),
        label = "logoAlpha",
    )

    LaunchedEffect(Unit) { started = true }

    // Navigate once the flag is known and a minimum splash time has elapsed.
    LaunchedEffect(onboardingComplete) {
        if (onboardingComplete != null) {
            delay(900)
            onDecision(onboardingComplete == true)
        }
    }

    SplashContent(scale = scale, alpha = alpha)
}

@Composable
private fun SplashContent(scale: Float, alpha: Float) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = WalletIcons.Wallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(96.dp)
                    .scale(scale)
                    .alpha(alpha),
            )
            Text(
                text = "Loyalty Wallet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(top = Spacing.lg)
                    .alpha(alpha),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashPreview() {
    AppTheme { SplashContent(scale = 1f, alpha = 1f) }
}
