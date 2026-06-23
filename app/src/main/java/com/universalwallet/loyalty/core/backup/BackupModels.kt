package com.universalwallet.loyalty.core.backup

import com.universalwallet.loyalty.core.export.CardExport

/** Outcome of validating a parsed backup before any data is written. */
data class ValidationResult(
    val isValid: Boolean,
    val totalCards: Int,
    val validCards: Int,
    val issues: List<String>,
) {
    val invalidCards: Int get() = totalCards - validCards
}

/** A card in the backup that already exists locally (same store + number). */
data class ConflictInfo(
    val storeName: String,
    val cardNumber: String,
)

/**
 * Everything the restore screen needs to show the user *before* importing:
 * validation summary, how many are new vs conflicting, and the encryption state.
 */
data class RestorePreview(
    val validation: ValidationResult,
    val newCards: List<CardExport>,
    val conflicts: List<ConflictInfo>,
    val wasEncrypted: Boolean,
    val exportedAt: Long,
) {
    val canRestore: Boolean get() = validation.isValid && (newCards.isNotEmpty() || conflicts.isNotEmpty())
}
