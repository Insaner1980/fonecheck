package com.insaner.fonecheck.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test

class AccessibilitySemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun disclosureHeaderExposesStateWithoutDrawingItAsContent() {
        composeRule.setContent {
            FonecheckTheme {
                DisclosureHeader(
                    label = "GPS",
                    summary = "Ready",
                    expanded = false,
                    onClick = {},
                    modifier = Modifier.testTag("disclosure"),
                )
            }
        }

        composeRule
            .onNodeWithTag("disclosure")
            .assertHeightIsAtLeast(48.dp)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription))
        composeRule
            .onNodeWithText("GPS", useUnmergedTree = true)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeRule.onNodeWithText("Ready", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Collapsed", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun expandableSectionExposesHeadingStateAndTouchTarget() {
        composeRule.setContent {
            FonecheckTheme {
                TestSectionCard(
                    icon = "BAT",
                    title = "Battery",
                    statusText = "Pass",
                    statusColor = Color.Green,
                    isExpanded = false,
                    onClick = {},
                    modifier = Modifier.testTag("section"),
                ) {}
            }
        }

        composeRule
            .onNodeWithTag("section")
            .assertHeightIsAtLeast(48.dp)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription))
        composeRule
            .onNodeWithText("Battery", useUnmergedTree = true)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }

    @Test
    fun statusBadgeExposesTextAsStateDescription() {
        composeRule.setContent {
            FonecheckTheme {
                StatusBadge(
                    text = "Pass",
                    color = Color.Green,
                    modifier = Modifier.testTag("status"),
                )
            }
        }

        composeRule
            .onNodeWithTag("status")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "Pass",
                ),
            )
    }
}
