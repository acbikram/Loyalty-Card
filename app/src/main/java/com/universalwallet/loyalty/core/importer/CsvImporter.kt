package com.universalwallet.loyalty.core.importer

import com.universalwallet.loyalty.core.export.CardExport
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import javax.inject.Inject

/**
 * Tolerant CSV → [CardExport] parser. Accepts an optional header row and the
 * columns: storeName, cardNumber, barcodeType, [nickname], [category]. Unknown
 * barcode types/categories fall back to sensible defaults. Pure and
 * unit-testable; quoting is handled for simple double-quoted fields.
 */
class CsvImporter @Inject constructor() {

    fun parse(content: String): List<CardExport> {
        val lines = content.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()
        if (lines.isEmpty()) return emptyList()

        val startIndex = if (looksLikeHeader(lines.first())) 1 else 0
        return lines.drop(startIndex).mapNotNull { line ->
            val cols = splitCsvLine(line)
            val storeName = cols.getOrNull(0)?.trim().orEmpty()
            val cardNumber = cols.getOrNull(1)?.trim().orEmpty()
            if (storeName.isEmpty() || cardNumber.isEmpty()) return@mapNotNull null
            val barcodeType = cols.getOrNull(2)?.trim()
                ?.let { raw -> BarcodeType.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } }
                ?: BarcodeType.CODE128
            val nickname = cols.getOrNull(3)?.trim().orEmpty()
            val category = cols.getOrNull(4)?.trim()
                ?.let { raw -> CardCategory.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } }
                ?: CardCategory.GENERAL
            CardExport(
                storeId = storeName.lowercase().replace(" ", "_"),
                storeName = storeName,
                cardNumber = cardNumber,
                barcodeValue = cardNumber,
                barcodeType = barcodeType.name,
                nickname = nickname,
                category = category.name,
            )
        }
    }

    private fun looksLikeHeader(line: String): Boolean {
        val lower = line.lowercase()
        return lower.contains("store") && (lower.contains("number") || lower.contains("card"))
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> { result.add(current.toString()); current.clear() }
                else -> current.append(ch)
            }
        }
        result.add(current.toString())
        return result
    }
}
