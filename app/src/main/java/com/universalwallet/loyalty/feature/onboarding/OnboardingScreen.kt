package com.universalwallet.loyalty.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.universalwallet.loyalty.core.components.WalletButton
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.Spacing
import kotlinx.coroutines.launch

/** A single onboarding page definition. */
private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val onboardingPages = listOf(
    OnboardingPage(Icons.Rounded.Wallet, "All your cards, one wallet", "Keep every loyalty and rewards card in a single, beautiful place."),
    OnboardingPage(Icons.Rounded.Search, "Find any card instantly", "Search by store, nickname, or number and get results as you type."),
    OnboardingPage(Icons.Rounded.CloudOff, "Works fully offline", "Your cards are always available at the counter — no signal required."),
    OnboardingPage(Icons.Rounded.Lock, "Private and secure", "Sensitive card numbers are designed to be encrypted on your device."),
    OnboardingPage(Icons.Rounded.Backup, "Backup is coming", "Cloud backup and sync are planned for a future update."),
)

/**
 * Onboarding flow: a swipeable set of pages with Skip / Next / Finish controls.
 * Completing or skipping persists the onboarding flag and calls [onFinished].
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    OnboardingContent(
        onFinished = {
            viewModel.complete()
            onFinished()
        },
    )
}

@Composable
private fun OnboardingContent(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLast by remember(pagerState) {
        derivedStateOf { pagerState.currentPage == onboardingPages.lastIndex }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(Spacing.lg)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onFinished) { Text("Skip") }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) { page ->
                OnboardingPageContent(onboardingPages[page])
            }

            PagerDots(count = onboardingPages.size, selected = pagerState.currentPage)

            WalletButton(
                text = if (isLast) "Get started" else "Next",
                onClick = {
                    if (isLast) {
                        onFinished()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.lg),
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .padding(Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp),
            )
        }
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.xl),
        )
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.md),
        )
    }
}

@Composable
private fun PagerDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = Spacing.md),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(count) { index ->
            val active = index == selected
            Box(
                modifier = Modifier
                    .padding(horizontal = Spacing.xs)
                    .size(if (active) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .androidxBackground(
                        if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                    ),
            )
        }
    }
}

private fun Modifier.androidxBackground(color: androidx.compose.ui.graphics.Color): Modifier =
    this.then(androidx.compose.foundation.background(color))

@Preview(showBackground = true)
@Composable
private fun OnboardingPreview() {
    AppTheme { OnboardingContent(onFinished = {}) }
}
