package com.universalwallet.loyalty.data.repository

import com.universalwallet.loyalty.domain.model.LoyaltyCard
import javax.inject.Inject

/** Why a candidate card is considered a duplicate of an existing one. */
enum class DuplicateReason { SAME_BARCODE, SAME_NUMBER_AND_STORE }

/** A detected duplicate: the [existing] card and the [reason] it matched. */
data class DuplicateMatch(val existing: LoyaltyCard, val reason: DuplicateReason)

/**
 * Pure duplicate detector used before a card is created. A candidate is a
 * duplicate if another card shares its exact barcode value (anywhere), or shares
 * both store and membership number. Side-effect free and unit-testable; callers
 * supply the existing cards.
 */
class DuplicateDetector @Inject constructor() {

    fun detect(candidate: LoyaltyCard, existing: List<LoyaltyCard>): DuplicateMatch? {
        existing.firstOrNull {
            it.id != candidate.id &&
                candidate.barcodeValue.isNotBlank() &&
                it.barcodeValue == candidate.barcodeValue
        }?.let { return DuplicateMatch(it, DuplicateReason.SAME_BARCODE) }

        existing.firstOrNull {
            it.id != candidate.id &&
                it.storeId == candidate.storeId &&
                it.cardNumber.isNotBlank() &&
                it.cardNumber == candidate.cardNumber
        }?.let { return DuplicateMatch(it, DuplicateReason.SAME_NUMBER_AND_STORE) }

        return null
    }
}
