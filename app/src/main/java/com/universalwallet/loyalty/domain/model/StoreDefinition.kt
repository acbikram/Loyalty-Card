package com.universalwallet.loyalty.domain.model

/**
 * A store definition sourced from the JSON catalogue (`assets/stores/`). Stores
 * are data, not code: adding a store means adding a JSON file, never editing
 * Kotlin. Colours are hex strings so this model stays free of any UI framework.
 */
data class StoreDefinition(
    val storeId: String,
    val storeName: String,
    val category: CardCategory,
    val country: List<String>,
    val keywords: List<String>,
    val primaryColor: String,
    val secondaryColor: String,
    val darkColor: String,
    val lightColor: String,
    val supportedBarcodeTypes: List<BarcodeType>,
    val cardTemplateId: String,
    val logoAssetPath: String? = null,
    val isActive: Boolean = true,
)
