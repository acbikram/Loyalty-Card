package com.universalwallet.loyalty.core.export

import android.content.Context
import android.net.Uri
import com.universalwallet.loyalty.core.utils.IoDispatcher
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serializes cards to JSON and writes them to a user-chosen location (a
 * `CreateDocument` URI). The [encrypted] flag is a forward-compatible hook for
 * the security phase; today it always writes plaintext JSON and is documented as
 * such. Single- and multi-card export share the same path.
 */
@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    private val json = Json { prettyPrint = true; encodeDefaults = true }

    fun toJson(cards: List<LoyaltyCard>, now: Long = System.currentTimeMillis()): String {
        val export = WalletExport(exportedAt = now, cards = cards.map { it.toExport() })
        return json.encodeToString(export)
    }

    /** Writes the export to [uri], returning the number of cards written. */
    suspend fun exportToUri(
        cards: List<LoyaltyCard>,
        uri: Uri,
        @Suppress("UNUSED_PARAMETER") encrypted: Boolean = false,
    ): Result<Int> = withContext(io) {
        runCatching {
            val payload = toJson(cards)
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
            } ?: error("Could not open output stream")
            cards.size
        }
    }
}
