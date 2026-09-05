package com.insaner.fonecheck.journey

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.comparison.ReportComparisonEngine
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.localization.evidenceReasonStringRes
import com.insaner.fonecheck.ui.format.formatUiDateTime
import com.insaner.fonecheck.ui.screens.comparison.ReportComparisonScreen
import com.insaner.fonecheck.ui.screens.comparison.ReportComparisonState
import com.insaner.fonecheck.ui.screens.export.ReportExportScreen
import com.insaner.fonecheck.ui.screens.export.ReportExportState
import com.insaner.fonecheck.ui.screens.runall.FullCheckPreflightScreen
import com.insaner.fonecheck.ui.screens.runall.ReportSaveStatus
import com.insaner.fonecheck.ui.screens.runall.RunAllInterruptionReason
import com.insaner.fonecheck.ui.screens.runall.RunAllResultsScreen
import com.insaner.fonecheck.ui.screens.runall.RunAllSelections
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Uses synthetic reports and callbacks, without application storage or diagnostics. */
@RunWith(AndroidJUnit4::class)
class JourneyPresentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun retestIdentityAndSaveConfirmationFollowTheFrozenReport() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report = cameraCategoryReport()
        val save = mutableStateOf(ReportSaveStatus.FAILED)
        composeRule.setContent {
            FonecheckTheme {
                RunAllResultsScreen(report, save.value, {}, {}, {})
            }
        }
        assertScrolledText(report.stableId)
        assertScrolledText(context.getString(R.string.report_save_unconfirmed))
        composeRule.onNodeWithText(context.getString(R.string.report_save_confirmed)).assertDoesNotExist()
        composeRule.runOnIdle { save.value = ReportSaveStatus.SAVED }
        assertScrolledText(context.getString(R.string.report_save_confirmed))
        assertScrolledText(
            context.getString(R.string.report_scope_category, context.getString(R.string.home_cat_camera)),
        )
        assertScrolledText(formatUiDateTime(report.completedAt, context.resources.configuration.locales[0]))
    }

    @Test
    fun exportShowsTheLoadedCategoryReportScopeAndTime() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report = cameraCategoryReport()
        composeRule.setContent {
            FonecheckTheme { ReportExportScreen(ReportExportState.Ready(report), {}, {}, {}, {}) }
        }
        composeRule.onNodeWithText(report.stableId).performScrollTo().assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(R.string.report_scope_category, context.getString(R.string.home_cat_camera)),
            ).performScrollTo()
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(formatUiDateTime(report.completedAt, context.resources.configuration.locales[0]))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun interruptionExplanationIsAbsentOnFirstStartAndClearsForANewRun() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val interruption = mutableStateOf<RunAllInterruptionReason?>(null)
        composeRule.setContent {
            FonecheckTheme {
                FullCheckPreflightScreen(RunAllSelections(), {}, {}, interruption = interruption.value)
            }
        }
        composeRule.onNodeWithText(context.getString(R.string.run_all_interrupted)).assertDoesNotExist()
        composeRule.runOnIdle { interruption.value = RunAllInterruptionReason.BACKGROUND }
        composeRule.onNodeWithText(context.getString(R.string.run_all_interrupted)).assertIsDisplayed()
        composeRule.runOnIdle { interruption.value = null }
        composeRule.onNodeWithText(context.getString(R.string.run_all_interrupted)).assertDoesNotExist()
    }

    private fun assertScrolledText(text: String) {
        val matcher = hasText(text) or hasContentDescription(text)
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(matcher)
        composeRule.onNode(matcher).assertIsDisplayed()
    }

    private fun cameraCategoryReport(): DiagnosticReport {
        val report = syntheticJourneyReport()
        return report.copy(
            stableId = "synthetic-B",
            kind = ReportKind.CATEGORY_ONLY,
            categories = report.categories.filter { it.categoryId == DiagnosticCategoryId.CAMERA },
        )
    }

    @Test
    fun changedUnmeasuredReasonIsExplainedWithoutClaimingAValueChange() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val base = syntheticJourneyReport()
        val category = base.categories.single { it.categoryId == DiagnosticCategoryId.CAMERA }
        val beforeEvidence =
            category.evidence.single().copy(
                value = null,
                status = DiagnosticStatus.NOT_TESTED,
                reason = EvidenceReasonCode.PERMISSION_DENIED,
                source = EvidenceSource.ANDROID_API,
            )
        val before =
            base.copy(
                kind = ReportKind.CATEGORY_ONLY,
                categories = listOf(category.copy(evidence = listOf(beforeEvidence))),
            )
        val after =
            before.copy(
                stableId = "synthetic-B",
                categories =
                    listOf(
                        category.copy(
                            evidence =
                                listOf(
                                    beforeEvidence.copy(
                                        reason = EvidenceReasonCode.SKIPPED,
                                        source = EvidenceSource.USER_CONFIRMATION,
                                    ),
                                ),
                        ),
                    ),
            )
        composeRule.setContent {
            FonecheckTheme {
                ReportComparisonScreen(
                    ReportComparisonState.Content(ReportComparisonEngine.compare(before, after)),
                    {},
                    {},
                )
            }
        }
        composeRule
            .onNode(
                hasScrollToNodeAction(),
            ).performScrollToNode(hasText(context.getString(R.string.home_cat_camera)))
        composeRule.onNodeWithTag("comparison_category").performClick()
        assertScrolledText(context.getString(R.string.comparison_change_metadata))
        assertScrolledText(
            context.getString(
                R.string.comparison_category_status,
                context.getString(requireNotNull(evidenceReasonStringRes(EvidenceReasonCode.PERMISSION_DENIED))),
                context.getString(requireNotNull(evidenceReasonStringRes(EvidenceReasonCode.SKIPPED))),
            ),
        )
        assertScrolledText(
            context.getString(
                R.string.comparison_category_status,
                context.getString(R.string.report_source_android_api),
                context.getString(R.string.report_source_user),
            ),
        )
        composeRule.onNodeWithText(context.getString(R.string.comparison_change_value)).assertDoesNotExist()
    }
}
