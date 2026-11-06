package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.ui.components.TelegramStickerSheet
import com.example.ui.theme.TelegramAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StickerDrawerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testStickerDrawerDisplayedAndCanSendSticker() {
        var sentSticker: String? = null

        composeTestRule.setContent {
            TelegramAppTheme {
                TelegramStickerSheet(
                    isDarkMode = false,
                    onSendSticker = { emoji ->
                        sentSticker = emoji
                    }
                )
            }
        }

        // Drawer is displayed
        composeTestRule.onNodeWithTag("sticker_selection_drawer").assertIsDisplayed()

        // Pack tabs exist
        composeTestRule.onNodeWithTag("pack_tab_recent").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pack_tab_favorites").assertIsDisplayed()

        // Switch to Ducks pack
        composeTestRule.onNodeWithTag("pack_tab_telegram_ducks").performClick()

        // Click a sticker item
        composeTestRule.onNodeWithTag("sticker_item_duck_cool").assertIsDisplayed().performClick()

        // Verify sent sticker was triggered
        assertEquals("🦆🕶️", sentSticker)
    }

    @Test
    fun testStickerSearchOpens() {
        composeTestRule.setContent {
            TelegramAppTheme {
                TelegramStickerSheet(
                    isDarkMode = true,
                    onSendSticker = {}
                )
            }
        }

        // Search open button should be visible
        composeTestRule.onNodeWithTag("sticker_search_open_button").assertIsDisplayed().performClick()

        // Search input field is displayed
        composeTestRule.onNodeWithTag("sticker_search_input").assertIsDisplayed()
    }
}
