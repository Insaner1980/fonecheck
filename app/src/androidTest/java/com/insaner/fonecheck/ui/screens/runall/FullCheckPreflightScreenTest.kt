package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
                    whatHappens = stringResource(R.string.run_all_preflight_what_happens_title),
                    permissionsAndControl = stringResource(R.string.run_all_preflight_permissions_control_title),
                    privacy = stringResource(R.string.run_all_preflight_privacy_title),
                    optionalTests = stringResource(R.string.run_all_preflight_choices_title),
                    storage = stringResource(R.string.run_all_preflight_storage),
                    local = stringResource(R.string.run_all_preflight_local_report),
                    speaker = stringResource(R.string.run_all_preflight_speaker_option),
                    microphone = stringResource(R.string.run_all_preflight_microphone_option),
                    camera = stringResource(R.string.run_all_preflight_camera_option),
                    storageOption = stringResource(R.string.run_all_preflight_storage_option),
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

        composeRule.onNodeWithContentDescription(labels.whatHappens).performScrollTo().assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(labels.permissionsAndControl)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription(labels.privacy).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription(labels.optionalTests).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(labels.storage).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(labels.local).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(labels.speaker).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(labels.microphone).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(labels.camera).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(labels.storageOption).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(labels.camera).performScrollTo().performClick()
        composeRule.onNodeWithText(labels.start).performScrollTo().performClick()

        assertFalse(requireNotNull(accepted).includeCamera)
    }

    @Test
    fun disabledTestWarningsHideOptionalDisclosureListButKeepChoices() {
        lateinit var disclosureHeadings: List<String>
        lateinit var disclosureParagraphs: List<String>
        lateinit var optionalTests: String
        lateinit var choices: List<String>
        composeRule.setContent {
            disclosureHeadings =
                listOf(
                    stringResource(R.string.run_all_preflight_what_happens_title),
                    stringResource(R.string.run_all_preflight_permissions_control_title),
                    stringResource(R.string.run_all_preflight_privacy_title),
                )
            disclosureParagraphs =
                listOf(
                    stringResource(R.string.run_all_preflight_interactions),
                    stringResource(R.string.run_all_preflight_audio_vibration),
                    stringResource(R.string.run_all_preflight_storage),
                    stringResource(R.string.run_all_preflight_permissions),
                    stringResource(R.string.run_all_preflight_unsupported),
                    stringResource(R.string.run_all_preflight_local_report),
                    stringResource(R.string.run_all_preflight_no_network),
                )
            optionalTests = stringResource(R.string.run_all_preflight_choices_title)
            choices =
                listOf(
                    stringResource(R.string.run_all_preflight_speaker_option),
                    stringResource(R.string.run_all_preflight_microphone_option),
                    stringResource(R.string.run_all_preflight_camera_option),
                    stringResource(R.string.run_all_preflight_storage_option),
                )
            FonecheckTheme {
                FullCheckPreflightScreen(
                    selections = RunAllSelections(),
                    onSelectionsChange = {},
                    onContinue = {},
                    showWarnings = false,
                )
            }
        }

        disclosureHeadings.forEach { disclosure ->
            composeRule.onAllNodesWithContentDescription(disclosure).assertCountEquals(0)
        }
        disclosureParagraphs.forEach { disclosure ->
            composeRule.onAllNodesWithText(disclosure).assertCountEquals(0)
        }
        composeRule.onNodeWithContentDescription(optionalTests).performScrollTo().assertIsDisplayed()
        choices.forEach { choice ->
            composeRule.onNodeWithText(choice).performScrollTo().assertIsDisplayed()
        }
    }

    private data class Labels(
        val whatHappens: String,
        val permissionsAndControl: String,
        val privacy: String,
        val optionalTests: String,
        val storage: String,
        val local: String,
        val speaker: String,
        val microphone: String,
        val camera: String,
        val storageOption: String,
        val start: String,
    )
}
