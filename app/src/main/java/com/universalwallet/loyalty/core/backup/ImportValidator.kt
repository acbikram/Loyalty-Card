package com.universalwallet.loyalty.core.backup

import com.universalwallet.loyalty.core.export.WalletExport
import com.universalwallet.loyalty.domain.model.BarcodeType
import javax.inject.Inject

/**
 * Validates a parsed [WalletExport] before import. Pure (no I/O), so it is fully
 * unit-testable. Checks the envelope version and each card's required fields and
 * barcode type, collecting human-readable issues rather than throwing.
 */
class ImportValidator @Inject constructor() {

    fun validate(export: WalletExport): ValidationResult {
        val issues = mutableListOf<String>()

        if (export.version > WalletExport.CURRENT_VERSION) {
            issues.add("Backup was created by a newer app version (v${export.version}); some data may be ignored.")
        }
        if (export.cards.isEmpty()) {
            issues.add("Backup contains no cards.")
        }

        var validCards = 0
        export.cards.forEachIndexed { index, card ->
            val rowIssues = mutableListOf<String>()
            if (card.storeName.isBlank()) rowIssues.add("missing store name")
            if (card.cardNumber.isBlank()) rowIssues.add("missing card number")
            if (BarcodeType.entries.none { it.name == card.barcodeType }) {
                rowIssues.add("unknown barcode type '${card.barcodeType}'")
            }
            if (rowIssues.isEmpty()) validCards++ else issues.add("Card ${index + 1}: ${rowIssues.joinToString(", ")}")
        }

        val isValid = validCards > 0 && export.version <= WalletExport.CURRENT_VERSION
        return ValidationResult(
            isValid = isValid,
            totalCards = export.cards.size,
            validCards = validCards,
            issues = issues,
        )
    }
}
