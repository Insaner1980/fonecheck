package com.insaner.fonecheck.localization

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.export.ReportPdfRenderer
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.confidenceLabel
import com.insaner.fonecheck.ui.components.statusLabel
import com.insaner.fonecheck.ui.screens.runall.sourceLabel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ReportLabelParityTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun pdfUsesCanonicalLabelsInEveryShippedLanguage() {
        AppLanguage.entries.filter { it != AppLanguage.SYSTEM }.forEach { language ->
            val localized = localizedContext(context, Locale.forLanguageTag(language.languageTag))
            val labels = ReportPdfRenderer(localized).labels(localized)
            DiagnosticCategoryId.entries.forEach { category ->
                assertEquals(localized.getString(diagnosticCategoryStringRes(category)), labels.categoryName(category))
                val destination = diagnosticDestinations.single { it.category == category }
                assertEquals(localized.getString(destination.labelResId), labels.categoryName(category))
            }
            DiagnosticStatus.entries.forEach { status ->
                assertEquals(localized.getString(diagnosticStatusStringRes(status)), labels.statusName(status))
            }
            ScoreState.entries.forEach { state ->
                assertEquals(localized.getString(scoreStateStringRes(state)), labels.scoreStateName(state))
            }
            EvidenceSource.entries.forEach { source ->
                assertEquals(localized.getString(evidenceSourceStringRes(source)), labels.sourceName(source))
            }
            Confidence.entries.forEach { confidence ->
                assertEquals(localized.getString(confidenceStringRes(confidence)), labels.confidenceName(confidence))
            }
            assertEquals(
                localized.getString(R.string.run_all_status_unavailable),
                labels.statusName(DiagnosticStatus.NOT_AVAILABLE),
            )
        }
    }

    @Test
    fun composeStatusSourceAndConfidenceLabelsMatchPdf() {
        val labels = ReportPdfRenderer(context).labels(context)
        val expected =
            DiagnosticStatus.entries.map(labels.statusName) +
                EvidenceSource.entries.map(labels.sourceName) +
                Confidence.entries.map(labels.confidenceName)
        var actual = emptyList<String>()
        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides context,
                LocalConfiguration provides context.resources.configuration,
            ) {
                val rendered =
                    DiagnosticStatus.entries.map { statusLabel(it) } +
                        EvidenceSource.entries.map { sourceLabel(it) } +
                        Confidence.entries.map { confidenceLabel(it) }
                SideEffect { actual = rendered }
            }
        }
        composeRule.runOnIdle { assertEquals(expected, actual) }
    }

    @Test
    fun absentUiStatusRemainsTheShortUnavailablePlaceholder() {
        val expected = context.getString(R.string.value_unavailable_short)
        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides context,
                LocalConfiguration provides context.resources.configuration,
            ) {
                Text(statusLabel(null))
            }
        }
        composeRule.onNodeWithText(expected).assertTextEquals(expected)
    }
}
