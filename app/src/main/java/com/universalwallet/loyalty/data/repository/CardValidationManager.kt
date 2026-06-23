package com.universalwallet.loyalty.data.repository

import com.universalwallet.loyalty.data.model.DataError
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import com.universalwallet.loyalty.domain.model.StoreDefinition
import javax.inject.Inject

/**
 * Validates a card before it is persisted. Pure and side-effect free: callers
 * pass the existing cards needed for duplicate detection, so this class never
 * touches the database and is trivially unit-testable.
 *
 * Returns the list of violations (empty == valid) as [DataError]s.
 */
class CardValidationManager @Inject constructor() {

    fun validate(
        card: LoyaltyCard,
        store: StoreDefinition?,
        existingCards: List<LoyaltyCard>,
    ): List<DataError> {
        val errors = mutableListOf<DataError>()

        // Required fields.
        if (card.storeId.isBlank()) errors += DataError.ValidationError("Store is required")
        if (card.cardNumber.isBlank()) errors += DataError.ValidationError("Card number is required")
        if (card.barcodeValue.isBlank()) errors += DataError.ValidationError("Barcode value is required")

        // Barcode format per type.
        if (card.barcodeValue.isNotBlank() && !matchesFormat(card.barcodeType, card.barcodeValue)) {
            errors += DataError.ValidationError(
                "Barcode value does not match ${card.barcodeType.name} format",
            )
        }

        // Store compatibility.
        if (store != null &&
            store.supportedBarcodeTypes.isNotEmpty() &&
            card.barcodeType !in store.supportedBarcodeTypes
        ) {
            errors += DataError.ValidationError(
                "${card.barcodeType.name} is not supported by ${store.storeName}",
            )
        }

        // Duplicate (same store + same number, ignoring this card's own id).
        val isDuplicate = existingCards.any {
            it.id != card.id && it.storeId == card.storeId && it.cardNumber == card.cardNumber
        }
        if (isDuplicate) errors += DataError.DuplicateError(card.storeId, card.cardNumber)

        return errors
    }

    private fun matchesFormat(type: BarcodeType, value: String): Boolean =
        formatRegex(type).matches(value)

    private fun formatRegex(type: BarcodeType): Regex = when (type) {
        BarcodeType.EAN13 -> Regex("^\\d{13}$")
        BarcodeType.EAN8 -> Regex("^\\d{8}$")
        BarcodeType.UPC -> Regex("^\\d{12}$")
        BarcodeType.ITF -> Regex("^(?:\\d{2})+$")
        BarcodeType.CODE128 -> Regex("^[\\u0000-\\u007F]+$")
        BarcodeType.CODE39 -> Regex("^[0-9A-Z\\-. $/+%]+$")
        BarcodeType.CODE93 -> Regex("^[0-9A-Za-z\\-. $/+%]+$")
        // 2D symbologies accept arbitrary non-empty payloads.
        BarcodeType.QR, BarcodeType.PDF417, BarcodeType.AZTEC -> Regex("^.+$", RegexOption.DOT_MATCHES_ALL)
    }
}
