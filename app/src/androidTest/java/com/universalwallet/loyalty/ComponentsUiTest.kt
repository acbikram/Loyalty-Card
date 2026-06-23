package com.universalwallet.loyalty

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.universalwallet.loyalty.core.components.ConfirmationDialog
import com.universalwallet.loyalty.core.components.EmptyState
import com.universalwallet.loyalty.core.components.SectionHeader
import com.universalwallet.loyalty.core.components.WalletButton
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the shared presentational components. These need no Hilt
 * graph (the components are stateless), so they run with a plain compose rule and
 * cover rendering, click handling, and state changes.
 */
class ComponentsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun walletButton_rendersLabel_andHandlesClick() {
        var clicked = false
        composeRule.setContent {
            MaterialTheme { WalletButton(text = "Save card", onClick = { clicked = true }) }
        }
        composeRule.onNodeWithText("Save card").assertIsDisplayed().performClick()
        assertTrue(clicked)
    }

    @Test
    fun emptyState_rendersTitleAndDescription() {
        composeRule.setContent {
            MaterialTheme {
                EmptyState(title = "No cards yet", description = "Add your first loyalty card")
            }
        }
        composeRule.onNodeWithText("No cards yet").assertIsDisplayed()
        composeRule.onNodeWithText("Add your first loyalty card").assertIsDisplayed()
    }

    @Test
    fun sectionHeader_rendersTitle() {
        composeRule.setContent { MaterialTheme { SectionHeader(title = "Privacy & security") } }
        composeRule.onNodeWithText("Privacy & security").assertIsDisplayed()
    }

    @Test
    fun confirmationDialog_confirmInvokesCallback() {
        var confirmed = false
        composeRule.setContent {
            MaterialTheme {
                ConfirmationDialog(
                    title = "Delete card?",
                    message = "This cannot be undone.",
                    confirmText = "Delete",
                    onConfirm = { confirmed = true },
                    onDismiss = {},
                )
            }
        }
        composeRule.onNodeWithText("Delete card?").assertIsDisplayed()
        composeRule.onNodeWithText("Delete").performClick()
        assertTrue(confirmed)
    }

    @Test
    fun walletButton_reflectsStateChange() {
        composeRule.setContent {
            MaterialTheme {
                var count by androidx.compose.runtime.remember { mutableStateOf(0) }
                WalletButton(text = "Count: $count", onClick = { count++ })
            }
        }
        composeRule.onNodeWithText("Count: 0").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Count: 1").assertIsDisplayed()
    }
}
