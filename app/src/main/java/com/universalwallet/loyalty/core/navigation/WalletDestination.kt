package com.universalwallet.loyalty.core.navigation

/**
 * Type-safe catalogue of every navigation destination in the app.
 *
 * Routes are defined here once; screens that render them arrive in their own
 * feature phases. Destinations that take arguments expose a [createRoute]
 * helper so callers never hand-assemble route strings.
 */
sealed class WalletDestination(val route: String) {

    data object Splash : WalletDestination("splash")
    data object Onboarding : WalletDestination("onboarding")
    data object Home : WalletDestination("home")
    data object Wallet : WalletDestination("wallet")
    data object Search : WalletDestination("search")
    data object AddCard : WalletDestination("add_card")
    data object Scanner : WalletDestination("scanner")
    data object Settings : WalletDestination("settings")
    data object Backup : WalletDestination("backup")
    data object SecurityCenter : WalletDestination("security")
    data object Developer : WalletDestination("developer")
    data object ImportWizard : WalletDestination("import_wizard")
    data object NotificationSettings : WalletDestination("notification_settings")
    data object Experimental : WalletDestination("experimental")
    data object Statistics : WalletDestination("statistics")
    data object StoreBrowser : WalletDestination("store_browser")
    data object Archive : WalletDestination("archive")
    data object About : WalletDestination("about")

    /** Card details takes a card id argument and supports a deep link. */
    data object CardDetails : WalletDestination("card_details/{${NavConstants.ARG_CARD_ID}}") {
        fun createRoute(cardId: String): String = "card_details/$cardId"
        val deepLink: String =
            "${NavConstants.DEEP_LINK_BASE}card/{${NavConstants.ARG_CARD_ID}}"
    }

    /** Edit card takes the id of the card to edit. */
    data object EditCard : WalletDestination("edit_card/{${NavConstants.ARG_CARD_ID}}") {
        fun createRoute(cardId: String): String = "edit_card/$cardId"
    }

    companion object {
        /** The destination shown first when the app launches. */
        val START_DESTINATION: WalletDestination = Splash
    }
}
