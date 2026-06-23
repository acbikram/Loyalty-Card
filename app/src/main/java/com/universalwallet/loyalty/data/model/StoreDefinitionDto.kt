package com.universalwallet.loyalty.data.model

import kotlinx.serialization.Serializable

/**
 * Wire model for a single store JSON file in `assets/stores/`. Enum-valued
 * fields are parsed as strings here and resolved to domain enums in the mapper,
 * so an unknown value in a JSON file degrades gracefully instead of throwing.
 * Defaults make every field except the ids optional in the JSON.
 */
@Serializable
data class StoreDefinitionDto(
    val storeId: String,
    val storeName: String,
    val category: String = "GENERAL",
    val country: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val primaryColor: String = "#000000",
    val secondaryColor: String = "#000000",
    val darkColor: String = "#000000",
    val lightColor: String = "#FFFFFF",
    val supportedBarcodeTypes: List<String> = emptyList(),
    val cardTemplateId: String = "standard",
    val logoAssetPath: String? = null,
    val isActive: Boolean = true,
)
