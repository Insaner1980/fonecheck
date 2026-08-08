package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullCheckPreflightScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun preflightDisclosesLocalWorkAndAppliesChoicesBeforeContinuing() {
        lateinit var labels: Labels
        var selections by mutableStateOf(RunAllSelections())
        var accepted: RunAllSelections? = null
        composeRule.setContent {
            labels =
                Labels(
                    storage = stringResource(R.string.run_all_preflight_storage),
                    local = stringResource(R.string.run_all_preflight_local_report),
                    camera = stringResource(R.string.run_all_preflight_camera_option),
                    start = stringResource(R.string.run_all_preflight_start),
                )
            FonecheckTheme {
                FullCheckPreflightScreen(
                    selections = selections,
                    onSelectionsChange = { selections = it },
                    onContinue = { accepted = selections },
                )
            }
        }

        composeRule.onNodeWithText(labels.storage).assertIsDisplayed()
        composeRule.onNodeWithText(labels.local).assertIsDisplayed()
        composeRule.onNodeWithText(labels.camera).performClick()
        composeRule.onNodeWithText(labels.start).performClick()

        assertFalse(requireNotNull(accepted).includeCamera)
    }

    @Test
    fun disabledTestWarningsHideOptionalDisclosureListButKeepChoices() {
        lateinit var disclosure: String
        lateinit var choice: String
        composeRule.setContent {
            disclosure = stringResource(R.string.run_all_preflight_storage)
            choice = stringResource(R.string.run_all_preflight_camera_option)
            FonecheckTheme {
                FullCheckPreflightScreen(
                    selections = RunAllSelections(),
                    onSelectionsChange = {},
                    onContinue = {},
                    showWarnings = false,
                )
            }
        }

        composeRule.onAllNodesWithText(disclosure).assertCountEquals(0)
        composeRule.onNodeWithText(choice).assertIsDisplayed()
    }

    private data class Labels(
        val storage: String,
        val local: String,
        val camera: String,
        val start: String,
    )
}
