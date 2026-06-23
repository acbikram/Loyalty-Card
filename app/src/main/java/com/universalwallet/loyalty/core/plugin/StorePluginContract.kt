package com.universalwallet.loyalty.core.plugin

import com.universalwallet.loyalty.core.barcode.BarcodeSymbology
import com.universalwallet.loyalty.domain.validation.ValidationResult

/**
 * The contract every store integration must satisfy.
 *
 * A "plugin" encapsulates everything that varies from one retailer to another —
 * identity, branding, supported barcodes, card template, and the rules for
 * validating and formatting a card number — behind a single stable interface.
 * Feature code depends only on this contract, never on a concrete store, which
 * is what lets the catalogue scale to hundreds of stores without edits to the
 * rest of the app.
 */
interface StorePluginContract {

    /** Stable, unique identifier (e.g. "lulu"). Used as the registry key. */
    fun getStoreId(): String

    /** Human-readable, default-locale store name. */
    fun getStoreName(): String

    /** Barcode symbologies this store accepts, most-preferred first. */
    fun getSupportedBarcodeTypes(): List<BarcodeSymbology>

    /** The store's visual identity. */
    fun getTheme(): StoreTheme

    /** The store's card capture/display template. */
    fun getCardTemplate(): CardTemplate

    /** Validates a raw card number against the store's rules. */
    fun validateCard(cardNumber: String): ValidationResult

    /** Normalises a card number for storage/display (e.g. trims, groups). */
    fun formatCard(cardNumber: String): String
}
