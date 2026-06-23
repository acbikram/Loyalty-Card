package com.universalwallet.loyalty.core.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.universalwallet.loyalty.core.theme.WalletIcons

/**
 * Standard top app bar with an optional back action. Centralising it keeps
 * title styling and the back affordance identical on every screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = WalletIcons.Back, contentDescription = "Navigate back")
                }
            }
        },
        actions = { actions() },
    )
}
