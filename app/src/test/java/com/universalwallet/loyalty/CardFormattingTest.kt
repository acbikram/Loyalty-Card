package com.universalwallet.loyalty

import com.google.common.truth.Truth.assertThat
import com.universalwallet.loyalty.core.cards.formatCardNumber
import com.universalwallet.loyalty.core.cards.maskCardNumber
import org.junit.Test

class CardFormattingTest {

    @Test
    fun formatGroupsIntoBlocksOfFour() {
        assertThat(formatCardNumber("1234567890")).isEqualTo("1234 5678 90")
    }

    @Test
    fun maskHidesAllButLastFour() {
        assertThat(maskCardNumber("1234567890")).isEqualTo("•••• 7890")
    }

    @Test
    fun maskLeavesShortNumbersUntouched() {
        assertThat(maskCardNumber("123")).isEqualTo("123")
        assertThat(maskCardNumber("1234")).isEqualTo("1234")
    }

    @Test
    fun formattersTrimWhitespace() {
        assertThat(maskCardNumber("  1234567890  ")).isEqualTo("•••• 7890")
        assertThat(formatCardNumber("  12345678  ")).isEqualTo("1234 5678")
    }
}
