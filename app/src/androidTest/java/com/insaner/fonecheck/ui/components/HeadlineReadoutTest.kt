package com.insaner.fonecheck.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test

class HeadlineReadoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersSeparateSupportingLinesWithoutJoiningThemWithASeparator() {
        composeRule.setContent {
            FonecheckTheme {
                HeadlineReadout(
                    value = "42.1",
                    unit = "% used",
                    supportingLines = listOf("27.6 GB used", "98.3 GB free", "128 GB total"),
                )
            }
        }

        composeRule.onNodeWithText("42.1", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("% used", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("27.6 GB used", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("98.3 GB free", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("128 GB total", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun rendersOneSupportingLineForAScore() {
        composeRule.setContent {
            FonecheckTheme {
                HeadlineReadout(
                    value = "42",
                    unit = "%",
                    supportingLines = listOf("Complete"),
                )
            }
        }

        composeRule.onNodeWithText("42", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Complete", useUnmergedTree = true).assertIsDisplayed()
    }
}
