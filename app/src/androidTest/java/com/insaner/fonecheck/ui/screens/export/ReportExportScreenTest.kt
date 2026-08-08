package com.insaner.fonecheck.ui.screens.export

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import java.time.Instant
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportExportScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun readyReportExplainsLocalPdfAndStartsGeneration() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var exported = false
        composeRule.setContent {
            FonecheckTheme {
                ReportExportScreen(
                    state = ReportExportState.Ready(report()),
                    onExportPdf = { exported = true },
                    onRetryLoad = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.export_local_only)).assertIsDisplayed()
        composeRule.onNodeWithTag("export_pdf").performClick()
        assertTrue(exported)
    }

    @Test
    fun generatingAndErrorStatesStayExplicit() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            FonecheckTheme {
                ReportExportScreen(
                    state = ReportExportState.Ready(report(), isGenerating = true),
                    onExportPdf = {},
                    onRetryLoad = {},
                    onBack = {},
                )
            }
        }
        composeRule.onNodeWithTag("export_pdf").assertIsNotEnabled()
        composeRule.onNodeWithText(context.getString(R.string.export_generating)).assertIsDisplayed()

        composeRule.setContent {
            FonecheckTheme {
                ReportExportScreen(
                    state = ReportExportState.Ready(report(), error = "pdf_export_failed"),
                    onExportPdf = {},
                    onRetryLoad = {},
                    onBack = {},
                )
            }
        }
        composeRule.onNodeWithText(context.getString(R.string.export_pdf_error)).assertIsDisplayed()
    }

    private fun report() =
        DiagnosticReport(
            stableId = "saved-report",
            kind = ReportKind.FULL_CHECK,
            startedAt = Instant.parse("2026-08-08T10:00:00Z"),
            completedAt = Instant.parse("2026-08-08T10:01:00Z"),
            device = ReportDeviceContext("Finnvek", "Test", "Fonecheck", "test", "16", 36, null),
            app = ReportAppContext("1.0.0", 1L),
            categories = emptyList(),
            score = ScoreSummary(ScoreVersion.CURRENT, 90, ScoreState.PARTIAL),
            coverage = CoverageSummary(4, 3, 1, 0, 75),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
}
