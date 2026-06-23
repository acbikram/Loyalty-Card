package com.universalwallet.loyalty

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.universalwallet.loyalty.core.theme.AppTheme
import com.universalwallet.loyalty.core.theme.ThemeMode
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI test verifying that content hosted inside [AppTheme] composes and
 * renders. This exercises the theme engine end-to-end (colour scheme,
 * typography, shapes) without depending on any feature screen.
 */
class AppThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun appTheme_rendersChildContent() {
        composeRule.setContent {
            AppTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Text(text = "Loyalty Wallet")
            }
        }

        composeRule.onNodeWithText("Loyalty Wallet").assertIsDisplayed()
    }
}
