package com.universalwallet.loyalty.core.navigation

/**
 * Constants for navigation routes, arguments, and deep links. Keeping these in
 * one place prevents typo-prone string duplication across the graph.
 */
object NavConstants {
    /** Matches the `android:scheme` declared in the manifest. */
    const val DEEP_LINK_SCHEME = "universalwallet"
    const val DEEP_LINK_BASE = "$DEEP_LINK_SCHEME://"

    // Navigation argument keys
    const val ARG_CARD_ID = "cardId"

    // Result keys passed back from the scanner to the add-card screen.
    const val RESULT_SCANNED_VALUE = "scanned_value"
    const val RESULT_SCANNED_SYMBOLOGY = "scanned_symbology"
}
