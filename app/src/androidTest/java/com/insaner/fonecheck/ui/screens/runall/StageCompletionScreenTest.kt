package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class StageCompletionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun failedResultRemainsVisibleUntilContinueIsPressed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var continueCount = 0
        composeRule.setContent {
            FonecheckTheme {
                StageCompletionScreen(
                    title = "Display",
                    outcome = RunAllStageOutcome.FAILED,
                    permissionLimited = false,
                    evidence = emptyList(),
                    onContinue = { continueCount++ },
                    onCancel = {},
                )
            }
        }
        composeRule.onNodeWithText("Display").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.run_all_status_fail)).assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, continueCount) }
        composeRule
            .onNodeWithText(context.getString(R.string.run_all_buttons_continue))
            .performScrollTo()
            .performClick()
        composeRule.runOnIdle { assertEquals(1, continueCount) }
    }

    @Test
    fun observationsShowValuesAndPrioritizeProblemsWhileOtherReadingsRemainExpandable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val evidence =
            listOf("health", "temperature", "level", "voltage").mapIndexed { index, name ->
                DiagnosticEvidence(
                    categoryId = DiagnosticCategoryId.BATTERY,
                    checkId = DiagnosticCheckId(DiagnosticCategoryId.BATTERY, "battery.$name"),
                    status = if (index == 3) DiagnosticStatus.WARNING else DiagnosticStatus.INFO,
                    confidence = Confidence.HIGH,
                    source = EvidenceSource.ANDROID_API,
                    applicability = Applicability.APPLICABLE,
                    value = EvidenceValue.IntValue(101 + index),
                    capturedAt = Instant.EPOCH,
                )
            }
        composeRule.setContent {
            FonecheckTheme {
                StageCompletionScreen(
                    title = "Battery",
                    outcome = RunAllStageOutcome.COMPLETED,
                    permissionLimited = false,
                    evidence = evidence,
                    onContinue = {},
                    onCancel = {},
                )
            }
        }
        composeRule.onNodeWithText("104").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("101").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("102").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("103").assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.report_observations))
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("103").performScrollTo().assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.report_observations))
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("103").assertDoesNotExist()
    }
}
