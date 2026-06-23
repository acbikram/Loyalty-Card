package com.universalwallet.loyalty.core.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding

/**
 * Application shell: a [Scaffold] whose bottom [NavigationBar] hosts the five
 * top-level destinations and wraps the single [WalletNavHost]. The bar is only
 * shown while a top-level route is active, so full-screen flows (splash,
 * onboarding, add/edit, details) take over the whole screen.
 *
 * Tab navigation uses the standard save/restore pattern so each tab keeps its
 * own back stack and scroll position across switches.
 */
@Composable
fun WalletApp(
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val isTopLevel = TopLevelDestination.entries.any { top ->
        currentDestination?.hierarchy?.any { it.route == top.destination.route } == true
    }

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == destination.destination.route
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateToTopLevel(destination) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        WalletNavHost(
            navController = navController,
            modifier = Modifier.padding(padding),
        )
    }
}

/** Switches to a top-level tab, preserving and restoring per-tab state. */
internal fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
