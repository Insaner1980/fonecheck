package com.insaner.fonecheck.ui.screens.runall

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DataNetworkObservation
import com.insaner.fonecheck.domain.model.NetworkDisplayObservation
import com.insaner.fonecheck.domain.model.NetworkReadState
import com.insaner.fonecheck.domain.model.networkPresentation
import com.insaner.fonecheck.domain.model.toNetworkEvidence
import com.insaner.fonecheck.export.ReportPdfRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.Instant
import java.util.Locale

@RunWith(Parameterized::class)
class NetworkObservationRenderTest(
    private val language: String,
) {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun narrowLargeTextKeepsBothObservationsAndMatchesLocalizedPdfLabels() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val context =
            base.createConfigurationContext(
                Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag(language)) },
            )
        val time = Instant.parse("2026-09-23T18:00:00Z")
        val evidence =
            DataNetworkObservation(
                13,
                time,
                NetworkReadState.RECEIVED,
                NetworkDisplayObservation(NetworkReadState.RECEIVED, 13, 3, time),
                time,
            ).toNetworkEvidence().networkPresentation()
        composeRule.setContent {
            LargeTextTestContent(language) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                    evidence.forEach { StageCompletionEvidenceRow(it) }
                }
            }
        }
        val labels = ReportPdfRenderer(context).labels(context)
        assertEquals(context.getString(R.string.network_base_label), labels.checkName(evidence[0]))
        assertEquals(context.getString(R.string.network_display_label), labels.checkName(evidence[1]))
        assertEquals(context.getString(R.string.confidence_high), labels.confidenceName(Confidence.HIGH))
        assertEquals(context.getString(R.string.network_display_note), labels.reasonName(evidence[1].reason!!))
        val displayedTexts =
            listOf(
                context.getString(R.string.network_base_label),
                context.getString(R.string.network_display_label),
                context.getString(R.string.network_display_note),
                "LTE",
                "5G",
            )
        for (text in displayedTexts) {
            val node = composeRule.onNodeWithText(text, useUnmergedTree = true)
            node.performScrollTo().assertIsDisplayed()
            val layouts = mutableListOf<TextLayoutResult>()
            node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            val layout = layouts.single()
            assertFalse("$language: $text", layout.didOverflowHeight)
            for (line in 0 until layout.lineCount) {
                assertFalse(layout.isLineEllipsized(line))
                assertTrue(layout.getLineRight(line) <= layout.size.width + 0.5f)
            }
            assertEquals(text.length, layout.getLineEnd(layout.lineCount - 1, visibleEnd = true))
        }
        composeRule.onAllNodesWithText("4G").assertCountEquals(0)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}, 320dp, 200%")
        fun languages(): List<Array<String>> =
            listOf("en", "fi", "de", "es", "fr", "it", "pt-BR", "pl", "sv", "nb", "da", "tr", "id")
                .map { arrayOf(it) }
    }
}
