package com.universalwallet.loyalty.core.components

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Convenience factory for a remembered [SnackbarHostState] plus a matching
 * [SnackbarHost], so screens get consistent snackbar placement and behaviour.
 */
@Composable
fun rememberWalletSnackbarHostState(): SnackbarHostState = remember { SnackbarHostState() }

@Composable
fun WalletSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier)
}
