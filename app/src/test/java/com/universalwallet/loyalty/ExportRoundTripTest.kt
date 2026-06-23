package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.export.WalletExport
import com.universalwallet.loyalty.core.export.toDomain
import com.universalwallet.loyalty.core.export.toExport
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import kotlinx.serialization.json.Json
import org.junit.Test

/**
 * Verifies the serializable export contract and the card<->export mappers
 * survive a full JSON encode/decode round-trip (mirrors ExportManager.toJson and
 * ImportManager.parse without Android dependencies).
 */
class ExportRoundTripTest {

    private val json = Json { prettyPrint = true; encodeDefaults = true; ignoreUnknownKeys = true }

    @Test
    fun cardsSurviveEncodeDecodeRoundTrip() {
        val original = listOf(
            card(id = "1", storeName = "Lulu", nickname = "Main", cardNumber = "12345",
                barcodeType = BarcodeType.EAN13, category = CardCategory.SUPERMARKET, isFavorite = true),
            card(id = "2", storeName = "Boots", cardNumber = "67890",
                barcodeType = BarcodeType.QR, category = CardCategory.PHARMACY),
        )
        val export = WalletExport(exportedAt = 123L, cards = original.map { it.toExport() })

        val encoded = json.encodeToString(WalletExport.serializer(), export)
        val decoded = json.decodeFromString(WalletExport.serializer(), encoded)

        assertThat(decoded.version).isEqualTo(WalletExport.CURRENT_VERSION)
        assertThat(decoded.cards).hasSize(2)

        val restored = decoded.cards.map { it.toDomain(now = 0L) }
        assertThat(restored.map { it.storeName }).containsExactly("Lulu", "Boots")
        val lulu = restored.first { it.storeName == "Lulu" }
        assertThat(lulu.cardNumber).isEqualTo("12345")
        assertThat(lulu.barcodeType).isEqualTo(BarcodeType.EAN13)
        assertThat(lulu.category).isEqualTo(CardCategory.SUPERMARKET)
        assertThat(lulu.isFavorite).isTrue()
        assertThat(lulu.nickname).isEqualTo("Main")
    }
}
