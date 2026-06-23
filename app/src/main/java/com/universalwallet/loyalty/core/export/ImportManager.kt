package com.universalwallet.loyalty.core.export

import android.content.Context
import android.net.Uri
import com.universalwallet.loyalty.core.barcode.BarcodeValidator
import com.universalwallet.loyalty.core.result.DataResult
import com.universalwallet.loyalty.core.utils.IoDispatcher
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** How to resolve a card that already exists (same store + number). */
enum class ConflictPolicy { SKIP, REPLACE }

/** Outcome of an import run. */
data class ImportSummary(val added: Int, val replaced: Int, val skipped: Int, val invalid: Int)

/**
 * Reads a wallet-export JSON, validates each card, and persists it via the
 * repository with explicit duplicate-conflict resolution. Lenient about the
 * envelope version (forward-compatible) but strict about required fields.
 */
@Singleton
class ImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
    private val repository: LoyaltyCardRepository,
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parse(content: String): Result<WalletExport> =
        runCatching { json.decodeFromString(WalletExport.serializer(), content) }

    suspend fun importFromUri(
        uri: Uri,
        policy: ConflictPolicy = ConflictPolicy.SKIP,
    ): Result<ImportSummary> = withContext(io) {
        runCatching {
            val content = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes().toString(Charsets.UTF_8)
            } ?: error("Could not open file")
            val export = parse(content).getOrThrow()
            apply(export, policy)
        }
    }

    private suspend fun apply(export: WalletExport, policy: ConflictPolicy): ImportSummary =
        importCards(export, policy)

    /** Persists an already-parsed export (used by the restore flow). */
    suspend fun importCards(export: WalletExport, policy: ConflictPolicy): ImportSummary {
        val now = System.currentTimeMillis()
        val existing = repository.observeCards().first()
        var added = 0
        var replaced = 0
        var skipped = 0
        var invalid = 0

        export.cards.forEach { dto ->
            val card = dto.toDomain(now)
            if (card.cardNumber.isBlank() ||
                !BarcodeValidator.validate(card.barcodeType, card.barcodeValue).isValid
            ) {
                invalid++
                return@forEach
            }
            val duplicate = existing.firstOrNull {
                it.storeId == card.storeId && it.cardNumber == card.cardNumber
            }
            when {
                duplicate == null -> {
                    if (repository.addCard(card) is DataResult.Success) added++ else invalid++
                }
                policy == ConflictPolicy.REPLACE -> {
                    val merged = duplicate.copy(
                        barcodeValue = card.barcodeValue,
                        barcodeType = card.barcodeType,
                        nickname = card.nickname.ifBlank { duplicate.nickname },
                        notes = card.notes,
                        category = card.category,
                        colorThemeId = card.colorThemeId,
                        updatedAt = now,
                    )
                    if (repository.updateCard(merged) is DataResult.Success) replaced++ else invalid++
                }
                else -> skipped++
            }
        }
        return ImportSummary(added, replaced, skipped, invalid)
    }
}
