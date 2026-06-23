package com.universalwallet.loyalty.domain.model

/**
 * High-level grouping for a loyalty card / store, used for filtering and
 * iconography. [GENERAL] is the safe default for anything unclassified.
 */
enum class CardCategory {
    SUPERMARKET,
    PHARMACY,
    FUEL,
    RESTAURANT,
    COFFEE,
    ELECTRONICS,
    FASHION,
    GENERAL;

    companion object {
        /** Parses a (case-insensitive) name, defaulting to [GENERAL]. */
        fun fromName(value: String?): CardCategory =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: GENERAL
    }
}
