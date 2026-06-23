package com.universalwallet.loyalty.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room row for a stored loyalty card.
 *
 * Columns are deliberately primitive (String/Long/Boolean) — enum and list
 * conversions live in the mappers, keeping the entity dumb and avoiding Room
 * TypeConverters. Indices back the hot query paths: per-store, per-category,
 * favourites, and recency.
 */
@Entity(
    tableName = "loyalty_cards",
    indices = [
        Index(value = ["storeId"]),
        Index(value = ["category"]),
        Index(value = ["isFavorite"]),
        Index(value = ["lastUsedTimestamp"]),
        Index(value = ["isPinned"]),
        Index(value = ["isArchived"]),
    ],
)
data class LoyaltyCardEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "storeId") val storeId: String,
    @ColumnInfo(name = "storeName") val storeName: String,
    @ColumnInfo(name = "cardNumber") val cardNumber: String,
    @ColumnInfo(name = "barcodeValue") val barcodeValue: String,
    @ColumnInfo(name = "barcodeType") val barcodeType: String,
    @ColumnInfo(name = "qrCodeValue") val qrCodeValue: String? = null,
    @ColumnInfo(name = "customerName") val customerName: String? = null,
    @ColumnInfo(name = "nickname") val nickname: String = "",
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "isFavorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "lastUsedTimestamp") val lastUsedTimestamp: Long = 0L,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
    @ColumnInfo(name = "imagePath") val imagePath: String? = null,
    @ColumnInfo(name = "colorThemeId") val colorThemeId: String = "default",
    @ColumnInfo(name = "isPinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "isArchived") val isArchived: Boolean = false,
    @ColumnInfo(name = "isHidden") val isHidden: Boolean = false,
    @ColumnInfo(name = "usageCount") val usageCount: Int = 0,
    @ColumnInfo(name = "sortIndex") val sortIndex: Int = 0,
)
