package com.universalwallet.loyalty.domain.model

/**
 * Barcode symbologies the app can store and (in a later phase) render. This is
 * the domain-level enum; the rendering/scanning contracts in `core.barcode`
 * use their own `BarcodeSymbology`, and the two are bridged by a mapper in the
 * data layer.
 */
enum class BarcodeType {
    EAN13,
    EAN8,
    CODE128,
    CODE39,
    CODE93,
    UPC,
    ITF,
    PDF417,
    QR,
    AZTEC;

    /** True for 2D symbologies (QR/PDF417/Aztec), which carry richer payloads. */
    val isTwoDimensional: Boolean
        get() = this == QR || this == PDF417 || this == AZTEC

    companion object {
        /** Parses a (case-insensitive) name, defaulting to [CODE128]. */
        fun fromName(value: String?): BarcodeType =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: CODE128
    }
}
