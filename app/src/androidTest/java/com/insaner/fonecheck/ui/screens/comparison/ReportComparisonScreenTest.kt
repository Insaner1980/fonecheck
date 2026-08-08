package com.insaner.fonecheck.ui.screens.comparison

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.comparison.ReportComparisonEngine
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class ReportComparisonScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun compatibleComparisonShowsDeltasCanonicalCategoriesAndDistinctChanges() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val before =
            report(
                id = "before",
                score = 80,
                coverage = 75,
                evidence =
                    listOf(
                        evidence("security", DiagnosticStatus.PASS),
                    ),
            )
        val after =
            report(
                id = "after",
                score = 86,
                coverage = 100,
                evidence =
                    listOf(
                        evidence("security", DiagnosticStatus.NOT_AVAILABLE),
                        evidence("added", DiagnosticStatus.NOT_AVAILABLE),
                    ),
            )

        composeRule.setContent {
            FonecheckTheme {
                ReportComparisonScreen(
                    state =
                        ReportComparisonState.Content(
                            ReportComparisonEngine.compare(before, after),
                        ),
                    onRetry = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.comparison_score_delta, 6)).assertIsDisplayed()
        composeRule.onAllNodesWithTag("comparison_category", useUnmergedTree = true).assertCountEquals(14)
        composeRule.onNodeWithTag("comparison_category_device").performScrollTo().performClick()
        composeRule
            .onNodeWithText(context.getString(R.string.comparison_change_newly_unavailable))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.comparison_change_added)).assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.comparison_disclaimer))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun incompatibleVersionsAndLoadFailuresAreExplained() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val comparison =
            ReportComparisonEngine.compare(
                report("before", 80, 75, ScoreVersion(1)),
                report("after", 86, 100, ScoreVersion(2)),
            )
        composeRule.setContent {
            FonecheckTheme {
                ReportComparisonScreen(
                    state = ReportComparisonState.Content(comparison),
                    onRetry = {},
                    onBack = {},
                )
            }
        }
        composeRule
            .onNodeWithText(context.getString(R.string.comparison_score_incompatible, 1, 2))
            .assertIsDisplayed()

        composeRule.setContent {
            FonecheckTheme {
                ReportComparisonScreen(
                    state = ReportComparisonState.Error,
                    onRetry = {},
                    onBack = {},
                )
            }
        }
        composeRule.onNodeWithText(context.getString(R.string.comparison_error)).assertIsDisplayed()
    }

    private fun evidence(
        suffix: String,
        status: DiagnosticStatus,
    ) = DiagnosticEvidence(
        categoryId = DiagnosticCategoryId.DEVICE,
        checkId = DiagnosticCheckId(DiagnosticCategoryId.DEVICE, "device.$suffix"),
        status = status,
        confidence = Confidence.HIGH,
        source = EvidenceSource.ANDROID_API,
        applicability = Applicability.APPLICABLE,
        capturedAt = Instant.parse("2026-08-08T10:00:00Z"),
    )

    private fun report(
        id: String,
        score: Int,
        coverage: Int,
        scoreVersion: ScoreVersion = ScoreVersion.CURRENT,
        evidence: List<DiagnosticEvidence> = emptyList(),
    ) = DiagnosticReport(
        stableId = id,
        kind = ReportKind.FULL_CHECK,
        startedAt = Instant.parse("2026-08-08T10:00:00Z"),
        completedAt = Instant.parse("2026-08-08T10:01:00Z"),
        device = ReportDeviceContext("Finnvek", "Test", "Fonecheck", "test", "16", 36, null),
        app = ReportAppContext("1.0.0", 1L),
        categories =
            if (evidence.isEmpty()) {
                emptyList()
            } else {
                listOf(
                    DiagnosticCategoryResult(
                        DiagnosticCategoryId.DEVICE,
                        evidence.maxBy { it.status.ordinal }.status,
                        evidence,
                    ),
                )
            },
        score = ScoreSummary(scoreVersion, score, ScoreState.PARTIAL),
        coverage = CoverageSummary(4, 3, 1, 0, coverage),
        schemaVersion = ReportSchemaVersion.CURRENT,
    )
}
