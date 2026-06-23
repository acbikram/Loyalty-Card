package com.universalwallet.loyalty.core.plugin

/**
 * Plugin-level visual identity for a store, expressed in framework-agnostic
 * terms (hex strings) so the plugin layer carries no Compose dependency. The UI
 * layer converts these into Compose `Color`s when rendering a card.
 *
 * @property brandColorHex   primary brand colour, e.g. "#0F8A3C"
 * @property onBrandColorHex colour for content drawn on top of the brand colour
 * @property logoAsset       optional asset path within `assets/` for the logo
 */
data class StoreTheme(
    val brandColorHex: String,
    val onBrandColorHex: String,
    val logoAsset: String? = null,
)
