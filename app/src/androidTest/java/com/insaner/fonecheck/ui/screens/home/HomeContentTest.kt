package com.insaner.fonecheck.ui.screens.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.navigation.History
import com.insaner.fonecheck.navigation.Report
import com.insaner.fonecheck.navigation.Settings
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class HomeContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun noSavedFullCheckShowsTruthfulEmptyState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        setHomeContent(LatestFullCheckState.Empty)

        composeRule.onNodeWithText(context.getString(R.string.home_latest_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithTag("home_latest_empty").assertIsDisplayed()
    }

    @Test
    fun loadingUnavailableAndErrorStatesStayExplicit() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var state by mutableStateOf<LatestFullCheckState>(LatestFullCheckState.Loading)
        composeRule.setContent {
            FonecheckTheme {
                HomeContent(
                    latestFullCheck = state,
                    onNavigate = {},
                    onRunAllTests = {},
                )
            }
        }
        composeRule.onNodeWithTag("home_latest_loading").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_latest_loading)).assertIsDisplayed()

        state = LatestFullCheckState.Unavailable(ReportReadFailure.CORRUPT_DATA)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_latest_unavailable").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.report_corrupt)).assertIsDisplayed()

        state = LatestFullCheckState.Error
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_latest_error").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_latest_retry)).assertIsDisplayed()
    }

    @Test
    fun completedReportWithoutAttentionShowsRealScoreAndStatus() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report = report("clean", listOf(DiagnosticStatus.PASS), score = 92)
        setHomeContent(LatestFullCheckState.Available(report))

        composeRule.onNodeWithText("92").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_latest_status_good)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_latest_no_attention)).assertIsDisplayed()
    }

    @Test
    fun warningAndFailureReportShowsAttentionSummary() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report =
            report(
                id = "attention",
                statuses = listOf(DiagnosticStatus.WARNING, DiagnosticStatus.FAIL),
                score = 44,
            )
        setHomeContent(LatestFullCheckState.Available(report))

        composeRule.onNodeWithText(context.getString(R.string.home_latest_status_fail)).assertIsDisplayed()
        composeRule
            .onNodeWithText(context.resources.getQuantityString(R.plurals.home_latest_attention_count, 2, 2))
            .assertIsDisplayed()
    }

    @Test
    fun incompleteReportDoesNotInventScore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report =
            report(
                id = "incomplete",
                statuses = listOf(DiagnosticStatus.PASS, DiagnosticStatus.NOT_TESTED),
                score = null,
                scoreState = ScoreState.INCOMPLETE,
                coveragePercentage = 50,
            )
        setHomeContent(LatestFullCheckState.Available(report))

        composeRule.onNodeWithText(context.getString(R.string.report_score_incomplete)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_latest_no_score)).assertIsDisplayed()
    }

    @Test
    fun headerAndLatestReportNavigateToExistingTypedRoutes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var route: Any? = null
        val report = report("report-42", listOf(DiagnosticStatus.PASS), score = 90)
        composeRule.setContent {
            FonecheckTheme {
                HomeContent(
                    latestFullCheck = LatestFullCheckState.Available(report),
                    onNavigate = { route = it },
                    onRunAllTests = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_history_content_description))
            .performClick()
        assertEquals(History, route)
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_settings_content_description))
            .performClick()
        assertEquals(Settings, route)
        composeRule.onNodeWithTag("home_latest_report_card").performClick()
        assertEquals(Report("report-42"), route)
    }

    @Test
    fun headerButtonsAndReportExposeRequiredSemanticsAndTouchTargets() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report = report("semantic", listOf(DiagnosticStatus.PASS), score = 90)
        setHomeContent(LatestFullCheckState.Available(report))

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_history_content_description))
            .assertHeightIsAtLeast(48.dp)
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_settings_content_description))
            .assertHeightIsAtLeast(48.dp)
        composeRule
            .onNodeWithTag("home_latest_report_card")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.StateDescription))
    }

    @Test
    fun lightThemeRendersPrimaryHomeEntries() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            FonecheckTheme(darkTheme = false) {
                HomeContent(
                    latestFullCheck = LatestFullCheckState.Empty,
                    onNavigate = {},
                    onRunAllTests = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_run_all)).assertIsDisplayed()
    }

    @Test
    fun darkThemeRemainsScrollableAtTwoHundredPercentFontScale() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 2f),
            ) {
                FonecheckTheme(darkTheme = true) {
                    HomeContent(
                        latestFullCheck = LatestFullCheckState.Empty,
                        onNavigate = {},
                        onRunAllTests = {},
                    )
                }
            }
        }

        composeRule
            .onNodeWithText(context.getString(R.string.home_cat_biometrics))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun rightToLeftLayoutKeepsHeaderActionsAndLatestReportAvailable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val report = report("rtl", listOf(DiagnosticStatus.PASS), score = 90)
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                FonecheckTheme {
                    HomeContent(
                        latestFullCheck = LatestFullCheckState.Available(report),
                        onNavigate = {},
                        onRunAllTests = {},
                    )
                }
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_history_content_description))
            .assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.home_settings_content_description))
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home_latest_report_card").assertIsDisplayed()
    }

    private fun setHomeContent(state: LatestFullCheckState) {
        composeRule.setContent {
            FonecheckTheme {
                HomeContent(
                    latestFullCheck = state,
                    onNavigate = {},
                    onRunAllTests = {},
                )
            }
        }
    }

    private fun report(
        id: String,
        statuses: List<DiagnosticStatus>,
        score: Int?,
        scoreState: ScoreState = if (score == null) ScoreState.INCOMPLETE else ScoreState.COMPLETE,
        coveragePercentage: Int = 100,
    ): DiagnosticReport {
        val categories =
            statuses.mapIndexed { index, status ->
                val categoryId = DiagnosticCategoryId.entries[index]
                val evidence =
                    DiagnosticEvidence(
                        categoryId = categoryId,
                        checkId = DiagnosticCheckId(categoryId, "${categoryId.stableId}.home_test"),
                        status = status,
                        confidence = Confidence.HIGH,
                        source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                        applicability = Applicability.APPLICABLE,
                        capturedAt = Instant.parse("2026-08-11T10:00:00Z"),
                    )
                DiagnosticCategoryResult(categoryId, status, listOf(evidence))
            }
        val completedCount = statuses.count { it != DiagnosticStatus.NOT_TESTED }
        return DiagnosticReport(
            stableId = id,
            kind = ReportKind.FULL_CHECK,
            startedAt = Instant.parse("2026-08-11T09:59:00Z"),
            completedAt = Instant.parse("2026-08-11T10:00:00Z"),
            device = ReportDeviceContext("Test", "Phone", "Test", "test", "16", 36, null),
            app = ReportAppContext("1.0", 1),
            categories = categories,
            score = ScoreSummary(ScoreVersion.CURRENT, score, scoreState),
            coverage =
                CoverageSummary(
                    applicableCount = statuses.size,
                    completedCount = completedCount,
                    notTestedCount = statuses.size - completedCount,
                    unavailableCount = 0,
                    percentage = coveragePercentage,
                ),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
    }
}
