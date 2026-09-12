package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.then
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenStateCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersTheWholeStateMatrixAndActions() {
        var primaryClicked = false
        var secondaryClicked = false
        composeRule.setContent {
            FonecheckTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    ScreenStateType.entries.forEach { type ->
                        ScreenStateCard(
                            type = type,
                            message = "State message",
                            actionLabel = "Retry",
                            onAction = { primaryClicked = true },
                            secondaryActionLabel = "Back",
                            onSecondaryAction = { secondaryClicked = true },
                        )
                    }
                }
            }
        }

        composeRule.onAllNodesWithText("State message").assertCountEquals(ScreenStateType.entries.size)
        composeRule.onAllNodesWithText("Retry")[0].performClick()
        composeRule.onAllNodesWithText("Back")[0].performClick()
        assertTrue(primaryClicked && secondaryClicked)
    }

    @Test
    fun reportRecoveryActionsRemainReachableInShortWindowAtLargeFontScale() {
        var retried = false
        var wentBack = false
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride
                    .Locales(LocaleList("en"))
                    .then(DeviceConfigurationOverride.FontScale(2f))
                    .then(DeviceConfigurationOverride.ForcedSize(DpSize(320.dp, 180.dp))),
            ) {
                FonecheckTheme {
                    ReportStateScreen(
                        type = ScreenStateType.ERROR,
                        message = stringResource(R.string.report_load_error),
                        onRetry = { retried = true },
                        onBack = { wentBack = true },
                    )
                }
            }
        }

        composeRule
            .onNodeWithText("Retry")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule
            .onNodeWithText("Back")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        assertTrue(retried && wentBack)
    }

    @Test
    fun exposesHeadingAndUrgentErrorAnnouncement() {
        composeRule.setContent {
            FonecheckTheme {
                ScreenStateCard(
                    type = ScreenStateType.ERROR,
                    message = "Could not load",
                )
            }
        }

        composeRule
            .onNodeWithText("Error", useUnmergedTree = true)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeRule
            .onNodeWithTag("screen_state_error")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.LiveRegion,
                    LiveRegionMode.Assertive,
                ),
            )
    }
}
