package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.localization.diagnosticStatusStringRes
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class RunAllResultsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun canonicalReportScoreAndEvidenceAreRendered() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            FonecheckTheme {
                RunAllResultsScreen(
                    report = report(),
                    saveStatus = ReportSaveStatus.SAVED,
                    onRetrySave = {},
                    onOpenCategory = {},
                    onDone = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.run_all_results_title)).assertIsDisplayed()
        composeRule.onNodeWithText("42").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_cat_battery), ignoreCase = true).assertIsDisplayed()
        composeRule
            .scrollToReportText(context.getString(R.string.report_coverage))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .scrollToReportText(context.getString(R.string.report_coverage_value, "100"))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .scrollToReportText(context.getString(R.string.report_observations_completed))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .scrollToReportText(context.getString(R.string.report_observations_excluded))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .scrollToReportText(context.getString(R.string.home_cat_battery), ignoreCase = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .scrollToReportText(context.getString(R.string.batt_health_dead))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun failedSaveOffersRetryWithoutReplacingTheReport() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var retries = 0
        composeRule.setContent {
            FonecheckTheme {
                RunAllResultsScreen(
                    report = report(),
                    saveStatus = ReportSaveStatus.FAILED,
                    onRetrySave = { retries += 1 },
                    onOpenCategory = {},
                    onDone = {},
                )
            }
        }

        composeRule
            .onNodeWithText(context.getString(R.string.run_all_retry_save))
            .performScrollTo()
            .performClick()

        assertEquals(1, retries)
    }

    @Test
    fun informationalCategoryUsesInformationalSummaryAndCount() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report = report()
        val informational =
            report.copy(
                categories =
                    report.categories.map { category ->
                        category.copy(
                            aggregateStatus = DiagnosticStatus.INFO,
                            evidence = category.evidence.map { it.copy(status = DiagnosticStatus.INFO) },
                        )
                    },
                score = ScoreSummary(ScoreVersion.CURRENT, null, ScoreState.INCOMPLETE),
            )

        composeRule.setContent {
            FonecheckTheme {
                RunAllResultsScreen(
                    report = informational,
                    saveStatus = ReportSaveStatus.SAVED,
                    onRetrySave = {},
                    onOpenCategory = {},
                    onDone = {},
                )
            }
        }

        composeRule
            .scrollToReportText(context.getString(R.string.run_all_summary_info))
            .performScrollTo()
            .assertIsDisplayed()
        // The count is a row now: the status word labels it and the figure sits in the value.
        composeRule
            .onNode(hasScrollToIndexAction())
            .performScrollToNode(hasTestTag("report_count_INFO"))
        composeRule
            .onNode(
                hasText("1") and hasAnyAncestor(hasTestTag("report_count_INFO")),
                useUnmergedTree = true,
            ).performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun allStatusesKeepTheirGroupsAndEvidenceLabelsInEveryReportMode() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val baseline = report()
        var currentReport by mutableStateOf(baseline)
        var mode by mutableStateOf(ReportResultMode.COMPLETED_RUN)
        composeRule.setContent {
            FonecheckTheme {
                key(currentReport, mode) {
                    RunAllResultsScreen(
                        report = currentReport,
                        saveStatus = ReportSaveStatus.SAVED,
                        onRetrySave = {},
                        onOpenCategory = {},
                        onDone = {},
                        mode = mode,
                    )
                }
            }
        }

        for (resultMode in ReportResultMode.entries) {
            for (kind in ReportKind.entries) {
                for (status in DiagnosticStatus.entries) {
                    composeRule.runOnIdle {
                        mode = resultMode
                        currentReport =
                            baseline.copy(
                                kind = kind,
                                categories =
                                    baseline.categories.map { category ->
                                        category.copy(
                                            aggregateStatus = status,
                                            evidence = category.evidence.map { it.copy(status = status) },
                                        )
                                    },
                            )
                    }
                    val attention = status == DiagnosticStatus.FAIL || status == DiagnosticStatus.WARNING
                    val group =
                        when (status) {
                            DiagnosticStatus.FAIL, DiagnosticStatus.WARNING -> R.string.run_all_needs_attention
                            DiagnosticStatus.PASS, DiagnosticStatus.INFO -> R.string.run_all_completed
                            DiagnosticStatus.NOT_AVAILABLE,
                            DiagnosticStatus.NOT_TESTED,
                            -> R.string.run_all_not_completed
                        }
                    composeRule
                        .scrollToReportText(context.getString(group), ignoreCase = true)
                        .performScrollTo()
                        .assertIsDisplayed()
                    val title = context.getString(R.string.home_cat_battery)
                    val header = composeRule.scrollToReportText(title, ignoreCase = true)
                    header.performScrollTo().assert(
                        SemanticsMatcher.expectValue(
                            SemanticsProperties.StateDescription,
                            context.getString(
                                if (attention) R.string.accessibility_expanded else R.string.accessibility_collapsed,
                            ),
                        ),
                    )
                    val label = context.getString(diagnosticStatusStringRes(status))
                    header.assert(hasText(label))
                    if (!attention) header.performClick()

                    // Exclude the clickable category header: this must be the evidence status.
                    composeRule
                        .onNode(
                            hasText(label) and
                                hasAnyAncestor(hasTestTag("report_category_battery")) and
                                !hasClickAction(),
                        ).performScrollTo()
                        .assertIsDisplayed()
                }
            }
        }
    }

    private fun report(): DiagnosticReport {
        val capturedAt = Instant.parse("2026-08-07T12:00:30Z")
        return DiagnosticReport(
            stableId = "report-ui",
            kind = ReportKind.FULL_CHECK,
            startedAt = Instant.parse("2026-08-07T12:00:00Z"),
            completedAt = Instant.parse("2026-08-07T12:01:00Z"),
            device =
                ReportDeviceContext(
                    manufacturer = "Finnvek",
                    model = "Test Device",
                    brand = "fonecheck",
                    product = "ui-test",
                    androidRelease = "16",
                    apiLevel = 36,
                    securityPatch = "2026-08-01",
                ),
            app = ReportAppContext(versionName = "1.0.0", versionCode = 1L),
            categories =
                listOf(
                    DiagnosticCategoryResult(
                        categoryId = DiagnosticCategoryId.BATTERY,
                        aggregateStatus = DiagnosticStatus.FAIL,
                        evidence =
                            listOf(
                                DiagnosticEvidence(
                                    categoryId = DiagnosticCategoryId.BATTERY,
                                    checkId =
                                        DiagnosticCheckId(
                                            DiagnosticCategoryId.BATTERY,
                                            "battery.health",
                                        ),
                                    status = DiagnosticStatus.FAIL,
                                    confidence = Confidence.HIGH,
                                    source = EvidenceSource.ANDROID_API,
                                    applicability = Applicability.APPLICABLE,
                                    reason = EvidenceReasonCode.DEGRADED,
                                    value = EvidenceValue.StableTextCodeValue("dead"),
                                    capturedAt = capturedAt,
                                ),
                            ),
                    ),
                ),
            score = ScoreSummary(ScoreVersion.CURRENT, 42, ScoreState.COMPLETE),
            coverage =
                CoverageSummary(
                    applicableCount = 1,
                    completedCount = 1,
                    notTestedCount = 0,
                    unavailableCount = 0,
                    percentage = 100,
                ),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
    }
}
