package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.data.repository.CardSearchEngine
import com.universalwallet.loyalty.domain.model.BarcodeType
import com.universalwallet.loyalty.domain.model.CardCategory
import com.universalwallet.loyalty.domain.model.LoyaltyCard
import org.junit.Test

/** Verifies ranked, case-insensitive, partial-match search behaviour. */
class CardSearchEngineTest {

    private val engine = CardSearchEngine()

    private fun card(id: String, store: String, nickname: String = "", number: String = "0000") =
        LoyaltyCard(
            id = id,
            storeId = id,
            storeName = store,
            cardNumber = number,
            barcodeValue = number,
            barcodeType = BarcodeType.CODE128,
            nickname = nickname,
            category = CardCategory.SUPERMARKET,
            createdAt = 0L,
            updatedAt = 0L,
        )

    private val cards = listOf(
        card("1", "Lulu Hypermarket", nickname = "Groceries"),
        card("2", "Carrefour", nickname = "Lulu backup"),
        card("3", "Nesto"),
    )

    @Test
    fun blankQuery_returnsAll() {
        assertThat(engine.search("", cards)).hasSize(3)
    }

    @Test
    fun storeNameMatch_outranksNicknameMatch() {
        val results = engine.search("lulu", cards)
        // Card 1 matches on store name (weight 100) and should rank above
        // card 2 which only matches on nickname (weight 80).
        assertThat(results.first().id).isEqualTo("1")
        assertThat(results.map { it.id }).containsExactly("1", "2").inOrder()
    }

    @Test
    fun search_isCaseInsensitiveAndPartial() {
        val results = engine.search("CARRE", cards)
        assertThat(results.single().id).isEqualTo("2")
    }
}
