package com.insaner.fonecheck.ui.screens.report

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.repository.ReportReadFailure
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
import com.insaner.fonecheck.navigation.CategoryRetest
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class ReportDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun savedReportShowsMetadataStatusesLongEvidenceAndRetest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val longValue = "A deliberately long saved value that must wrap without truncating the evidence content."
        var retestRoute: Any? = null

        composeRule.setContent {
            FonecheckTheme {
                ReportDetailScreen(
                    state = ReportDetailState.Content(report(longValue = longValue)),
                    onRetry = {},
                    onBack = {},
                    onRetest = { retestRoute = it },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.report_saved_title)).assertIsDisplayed()
        composeRule.onNodeWithText("Finnvek Test Device").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.report_kind_full)).assertIsDisplayed()
        composeRule.onNodeWithText(longValue).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.report_source_android_api)).assertIsDisplayed()
        composeRule.onNodeWithText("Future vendor reason", substring = true).assertIsDisplayed()
        assertCategoryStatus(DiagnosticCategoryId.DEVICE, R.string.run_all_status_pass)
        assertCategoryStatus(DiagnosticCategoryId.PERFORMANCE, R.string.run_all_status_warning)
        assertCategoryStatus(DiagnosticCategoryId.SIM, R.string.run_all_status_info)
        assertCategoryStatus(DiagnosticCategoryId.STORAGE, R.string.run_all_status_fail)
        assertCategoryStatus(DiagnosticCategoryId.CAMERA, R.string.run_all_status_unavailable)
        assertCategoryStatus(DiagnosticCategoryId.AUDIO, R.string.run_all_status_not_tested)
        composeRule
            .onNodeWithText(context.getString(R.string.report_retest))
            .performScrollTo()
            .performClick()

        assertEquals(CategoryRetest("performance"), retestRoute)
        composeRule
            .onNodeWithText(context.getString(R.string.home_cat_biometrics))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun partialAndCategoryOnlyScoreStatesAreRenderedFromTheSavedReport() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var state by
            mutableStateOf<ReportDetailState>(
                ReportDetailState.Content(report(scoreState = ScoreState.PARTIAL)),
            )
        composeRule.setContent {
            FonecheckTheme {
                ReportDetailScreen(state, onRetry = {}, onBack = {}, onRetest = {})
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.report_score_partial)).assertIsDisplayed()

        state =
            ReportDetailState.Content(
                report(
                    kind = ReportKind.CATEGORY_ONLY,
                    scoreState = ScoreState.INCOMPLETE,
                    scoreValue = null,
                ),
            )
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.report_kind_category)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.report_score_incomplete)).assertIsDisplayed()
    }

    @Test
    fun loadingMissingCorruptAndErrorStatesHaveExplicitRecoveryContent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var retries = 0
        var state by mutableStateOf<ReportDetailState>(ReportDetailState.Loading)
        composeRule.setContent {
            FonecheckTheme {
                ReportDetailScreen(
                    state = state,
                    onRetry = { retries += 1 },
                    onBack = {},
                    onRetest = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.report_loading)).assertIsDisplayed()
        state = ReportDetailState.NotFound
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.report_not_found)).assertIsDisplayed()
        state = ReportDetailState.Unavailable(ReportReadFailure.CORRUPT_DATA)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.report_corrupt)).assertIsDisplayed()
        state = ReportDetailState.Error
        composeRule.waitForIdle()
        composeRule
            .onNodeWithText(context.getString(R.string.report_retry))
            .performClick()
        assertEquals(1, retries)
    }

    private fun report(
        longValue: String = "saved value",
        kind: ReportKind = ReportKind.FULL_CHECK,
        scoreState: ScoreState = ScoreState.COMPLETE,
        scoreValue: Int? = 76,
    ): DiagnosticReport {
        val capturedAt = Instant.parse("2026-08-08T10:00:30Z")
        return DiagnosticReport(
            stableId = "saved-report",
            kind = kind,
            startedAt = Instant.parse("2026-08-08T10:00:00Z"),
            completedAt = Instant.parse("2026-08-08T10:01:05Z"),
            device = ReportDeviceContext("Finnvek", "Test Device", "Fonecheck", "test", "16", 36, "2026-08-01"),
            app = ReportAppContext("1.2.3", 42L),
            categories =
                if (kind == ReportKind.CATEGORY_ONLY) {
                    listOf(category(DiagnosticCategoryId.STORAGE, DiagnosticStatus.WARNING))
                } else {
                    listOf(
                        category(DiagnosticCategoryId.DEVICE, DiagnosticStatus.PASS),
                        category(
                            DiagnosticCategoryId.PERFORMANCE,
                            DiagnosticStatus.WARNING,
                            DiagnosticEvidence(
                                categoryId = DiagnosticCategoryId.PERFORMANCE,
                                checkId = DiagnosticCheckId(DiagnosticCategoryId.PERFORMANCE, "performance.cpu"),
                                status = DiagnosticStatus.WARNING,
                                confidence = Confidence.HIGH,
                                source = EvidenceSource.ANDROID_API,
                                applicability = Applicability.APPLICABLE,
                                reason = EvidenceReasonCode("future_vendor_reason"),
                                value = EvidenceValue.RawTextValue(longValue),
                                capturedAt = capturedAt,
                            ),
                        ),
                        category(DiagnosticCategoryId.SIM, DiagnosticStatus.INFO),
                        category(DiagnosticCategoryId.AUDIO, DiagnosticStatus.NOT_TESTED),
                        category(DiagnosticCategoryId.CAMERA, DiagnosticStatus.NOT_AVAILABLE),
                        category(DiagnosticCategoryId.STORAGE, DiagnosticStatus.FAIL),
                    )
                },
            score = ScoreSummary(ScoreVersion.CURRENT, scoreValue, scoreState),
            coverage = CoverageSummary(14, 4, 1, 1, 29),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
    }

    private fun category(
        id: DiagnosticCategoryId,
        status: DiagnosticStatus,
        vararg evidence: DiagnosticEvidence,
    ) = DiagnosticCategoryResult(id, status, evidence.toList())

    private fun assertCategoryStatus(
        categoryId: DiagnosticCategoryId,
        statusStringRes: Int,
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule
            .onNodeWithTag("report_category_${categoryId.stableId}", useUnmergedTree = true)
            .performScrollTo()
        composeRule
            .onAllNodes(
                hasText(context.getString(statusStringRes)) and
                    hasAnyAncestor(hasTestTag("report_category_${categoryId.stableId}")),
                useUnmergedTree = true,
            ).assertCountEquals(1)
    }
}
