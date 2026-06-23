package com.universalwallet.loyalty.core.barcode

import com.universalwallet.loyalty.domain.model.BarcodeType

/**
 * Core-layer mapping from the domain [BarcodeType] to [BarcodeSymbology], so UI
 * (which must not depend on the data layer) can resolve a card's symbology for
 * rendering. Mirrors the data-layer mapper used for persistence.
 */
fun BarcodeType.toSymbology(): BarcodeSymbology = when (this) {
    BarcodeType.EAN13 -> BarcodeSymbology.EAN_13
    BarcodeType.EAN8 -> BarcodeSymbology.EAN_8
    BarcodeType.CODE128 -> BarcodeSymbology.CODE_128
    BarcodeType.CODE39 -> BarcodeSymbology.CODE_39
    BarcodeType.CODE93 -> BarcodeSymbology.CODE_93
    BarcodeType.UPC -> BarcodeSymbology.UPC_A
    BarcodeType.ITF -> BarcodeSymbology.ITF
    BarcodeType.PDF417 -> BarcodeSymbology.PDF_417
    BarcodeType.QR -> BarcodeSymbology.QR_CODE
    BarcodeType.AZTEC -> BarcodeSymbology.AZTEC
}

/**
 * Core-layer mapping from a (possibly scanner-detected) [BarcodeSymbology] to
 * the domain [BarcodeType]. UPC-E collapses to [BarcodeType.UPC] and Data Matrix
 * to [BarcodeType.QR], matching the data-layer mapper.
 */
fun BarcodeSymbology.toBarcodeType(): BarcodeType = when (this) {
    BarcodeSymbology.EAN_13 -> BarcodeType.EAN13
    BarcodeSymbology.EAN_8 -> BarcodeType.EAN8
    BarcodeSymbology.CODE_128 -> BarcodeType.CODE128
    BarcodeSymbology.CODE_39 -> BarcodeType.CODE39
    BarcodeSymbology.CODE_93 -> BarcodeType.CODE93
    BarcodeSymbology.UPC_A -> BarcodeType.UPC
    BarcodeSymbology.UPC_E -> BarcodeType.UPC
    BarcodeSymbology.ITF -> BarcodeType.ITF
    BarcodeSymbology.PDF_417 -> BarcodeType.PDF417
    BarcodeSymbology.QR_CODE -> BarcodeType.QR
    BarcodeSymbology.AZTEC -> BarcodeType.AZTEC
    BarcodeSymbology.DATA_MATRIX -> BarcodeType.QR
}
