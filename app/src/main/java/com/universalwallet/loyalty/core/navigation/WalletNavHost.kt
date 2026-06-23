package com.universalwallet.loyalty.core.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.universalwallet.loyalty.feature.about.AboutScreen
import com.universalwallet.loyalty.feature.addcard.AddCardScreen
import com.universalwallet.loyalty.feature.details.CardDetailsScreen
import com.universalwallet.loyalty.feature.editcard.EditCardScreen
import com.universalwallet.loyalty.feature.home.HomeScreen
import com.universalwallet.loyalty.feature.onboarding.OnboardingScreen
import com.universalwallet.loyalty.feature.scanner.ScannerScreen
import com.universalwallet.loyalty.feature.search.SearchScreen
import com.universalwallet.loyalty.feature.archive.ArchiveScreen
import com.universalwallet.loyalty.feature.developer.DeveloperModeScreen
import com.universalwallet.loyalty.feature.importwizard.ImportWizardScreen
import com.universalwallet.loyalty.feature.notifications.NotificationSettingsScreen
import com.universalwallet.loyalty.feature.experimental.ExperimentalSettingsScreen
import com.universalwallet.loyalty.feature.security.SecuritySettingsScreen
import com.universalwallet.loyalty.feature.settings.SettingsScreen
import com.universalwallet.loyalty.feature.splash.SplashScreen
import com.universalwallet.loyalty.feature.statistics.StatisticsScreen
import com.universalwallet.loyalty.feature.stores.StoreBrowserScreen
import com.universalwallet.loyalty.feature.wallet.WalletScreen

/**
 * The application's single navigation graph. Every destination from
 * [WalletDestination] is registered here with its real screen, navigation
 * arguments, deep links, and shared fade/slide transitions.
 */
@Composable
fun WalletNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: WalletDestination = WalletDestination.START_DESTINATION,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
        enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 12 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 12 } },
    ) {
        composable(WalletDestination.Splash.route) {
            SplashScreen(
                onDecision = { onboardingComplete ->
                    val target = if (onboardingComplete) WalletDestination.Home else WalletDestination.Onboarding
                    navController.navigate(target.route) {
                        popUpTo(WalletDestination.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(WalletDestination.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(WalletDestination.Home.route) {
                        popUpTo(WalletDestination.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        composable(WalletDestination.Home.route) {
            HomeScreen(
                onCardClick = { navController.navigate(WalletDestination.CardDetails.createRoute(it)) },
                onAddClick = { navController.navigate(WalletDestination.AddCard.route) },
                onSearchClick = { navController.navigateToTopLevel(TopLevelDestination.SEARCH) },
            )
        }

        composable(WalletDestination.Search.route) {
            SearchScreen(
                onCardClick = { navController.navigate(WalletDestination.CardDetails.createRoute(it)) },
            )
        }

        composable(WalletDestination.Wallet.route) {
            WalletScreen(
                onCardClick = { navController.navigate(WalletDestination.CardDetails.createRoute(it)) },
                onAddClick = { navController.navigate(WalletDestination.AddCard.route) },
            )
        }

        composable(WalletDestination.Statistics.route) {
            StatisticsScreen()
        }

        composable(WalletDestination.Settings.route) {
            SettingsScreen(
                onAbout = { navController.navigate(WalletDestination.About.route) },
                onBrowseStores = { navController.navigate(WalletDestination.StoreBrowser.route) },
                onArchived = { navController.navigate(WalletDestination.Archive.route) },
                onSecurity = { navController.navigate(WalletDestination.SecurityCenter.route) },
                onImportWizard = { navController.navigate(WalletDestination.ImportWizard.route) },
                onNotifications = { navController.navigate(WalletDestination.NotificationSettings.route) },
                onExperimental = { navController.navigate(WalletDestination.Experimental.route) },
            )
        }

        composable(WalletDestination.AddCard.route) {
            AddCardScreen(
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
                onScanClick = { navController.navigate(WalletDestination.Scanner.route) },
            )
        }

        composable(WalletDestination.Scanner.route) {
            ScannerScreen(
                onScanned = { result ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set(NavConstants.RESULT_SCANNED_SYMBOLOGY, result.symbology.name)
                        set(NavConstants.RESULT_SCANNED_VALUE, result.rawValue)
                    }
                },
                onClose = { navController.popBackStack() },
            )
        }

        composable(
            route = WalletDestination.EditCard.route,
            arguments = listOf(navArgument(NavConstants.ARG_CARD_ID) { type = NavType.StringType }),
        ) {
            EditCardScreen(onDone = { navController.popBackStack() })
        }

        composable(
            route = WalletDestination.CardDetails.route,
            arguments = listOf(navArgument(NavConstants.ARG_CARD_ID) { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = WalletDestination.CardDetails.deepLink }),
        ) {
            CardDetailsScreen(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(WalletDestination.EditCard.createRoute(it)) },
            )
        }

        composable(WalletDestination.SecurityCenter.route) {
            SecuritySettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenDeveloper = { navController.navigate(WalletDestination.Developer.route) },
            )
        }

        composable(WalletDestination.Developer.route) {
            DeveloperModeScreen(onBack = { navController.popBackStack() })
        }

        composable(WalletDestination.ImportWizard.route) {
            ImportWizardScreen(onBack = { navController.popBackStack() })
        }

        composable(WalletDestination.NotificationSettings.route) {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(WalletDestination.Experimental.route) {
            ExperimentalSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(WalletDestination.Archive.route) {
            ArchiveScreen(onBack = { navController.popBackStack() })
        }

        composable(WalletDestination.StoreBrowser.route) {
            StoreBrowserScreen(
                onBack = { navController.popBackStack() },
                onStoreSelected = { navController.navigate(WalletDestination.AddCard.route) },
            )
        }

        composable(WalletDestination.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
