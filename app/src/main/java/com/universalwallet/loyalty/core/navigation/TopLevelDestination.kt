package com.universalwallet.loyalty.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.universalwallet.loyalty.core.theme.WalletIcons

/**
 * The small set of destinations reachable from the bottom navigation bar.
 * Restricting top-level entries keeps the primary navigation flat and fast.
 */
enum class TopLevelDestination(
    val destination: WalletDestination,
    val icon: ImageVector,
    val label: String,
) {
    HOME(WalletDestination.Home, WalletIcons.Home, "Home"),
    SEARCH(WalletDestination.Search, WalletIcons.Search, "Search"),
    WALLET(WalletDestination.Wallet, WalletIcons.Wallet, "Wallet"),
    STATISTICS(WalletDestination.Statistics, WalletIcons.Statistics, "Stats"),
    SETTINGS(WalletDestination.Settings, WalletIcons.Settings, "Settings"),
}
