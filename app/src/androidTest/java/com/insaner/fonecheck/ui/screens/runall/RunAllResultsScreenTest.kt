package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
        composeRule
            .onNodeWithText(context.getString(R.string.home_cat_battery))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.batt_health_dead))
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
                    brand = "Fonecheck",
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
