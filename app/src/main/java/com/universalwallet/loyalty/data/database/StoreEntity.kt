package com.universalwallet.loyalty.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room row for a store definition (the local, queryable cache of the JSON
 * catalogue). List-valued fields (country, keywords, supported barcode types)
 * are stored as unit-separator-delimited strings and split back in the mapper.
 */
@Entity(
    tableName = "stores",
    indices = [
        Index(value = ["category"]),
        Index(value = ["isActive"]),
    ],
)
data class StoreEntity(
    @PrimaryKey @ColumnInfo(name = "storeId") val storeId: String,
    @ColumnInfo(name = "storeName") val storeName: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "country") val country: String,
    @ColumnInfo(name = "keywords") val keywords: String,
    @ColumnInfo(name = "primaryColor") val primaryColor: String,
    @ColumnInfo(name = "secondaryColor") val secondaryColor: String,
    @ColumnInfo(name = "darkColor") val darkColor: String,
    @ColumnInfo(name = "lightColor") val lightColor: String,
    @ColumnInfo(name = "supportedBarcodeTypes") val supportedBarcodeTypes: String,
    @ColumnInfo(name = "cardTemplateId") val cardTemplateId: String,
    @ColumnInfo(name = "logoAssetPath") val logoAssetPath: String? = null,
    @ColumnInfo(name = "isActive") val isActive: Boolean = true,
)
