package com.insaner.fonecheck.export

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
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportPdfContentTest {
    @Test
    fun contentContainsRequiredVersionedReportEvidenceAndDisclaimer() {
        val blocks = ReportPdfContentBuilder.build(report(), PdfReportLabels.english())
        val text = blocks.joinToString("\n", transform = PdfTextBlock::text)

        assertTrue(text.contains("fonecheck diagnostic report"))
        assertTrue(text.contains("report-123"))
        assertTrue(text.contains("Report format: 1"))
        assertTrue(text.contains("Score version: 1"))
        assertTrue(text.contains("App: 1.0.0 (1)"))
        assertTrue(text.contains("Device: Finnvek Test Device"))
        assertTrue(text.contains("Coverage: 75%"))
        assertTrue(text.contains("battery.level"))
        assertTrue(text.contains("Source: android_api"))
        assertTrue(text.contains("Confidence: high"))
        assertTrue(text.contains("Differences and measurements do not prove physical device health."))
    }

    @Test
    fun longLocalizedContentPaginatesWithoutDroppingOrReorderingText() {
        val marker = (1..180).joinToString(" ") { "word$it" }
        val blocks =
            listOf(
                PdfTextBlock("Heading", PdfTextStyle.HEADING),
                PdfTextBlock(marker, PdfTextStyle.BODY),
                PdfTextBlock("Last line", PdfTextStyle.BODY),
            )

        val pages = PdfLayoutEngine.paginate(blocks, contentHeight = 180)
        val text = pages.flatten().joinToString(" ", transform = PdfTextLine::text)

        assertTrue(pages.size > 1)
        assertTrue(pages.all { it.isNotEmpty() })
        assertTrue(text.startsWith("Heading word1"))
        assertTrue(text.endsWith("word180 Last line"))
        assertTrue(pages.flatten().all { it.text.length <= it.style.maxCharacters })
    }

    @Test
    fun canonicalCategoriesMissingFromSnapshotRemainExplicitlyNotTested() {
        val blocks = ReportPdfContentBuilder.build(report(), PdfReportLabels.english())
        val categoryHeadings = blocks.filter { it.style == PdfTextStyle.CATEGORY }.map(PdfTextBlock::text)

        assertEquals(14, categoryHeadings.size)
        assertEquals("Device — not_tested", categoryHeadings.first())
        assertEquals("Biometrics — not_tested", categoryHeadings.last())
    }

    private fun report() =
        DiagnosticReport(
            stableId = "report-123",
            kind = ReportKind.FULL_CHECK,
            startedAt = Instant.parse("2026-08-08T10:00:00Z"),
            completedAt = Instant.parse("2026-08-08T10:01:00Z"),
            device = ReportDeviceContext("Finnvek", "Test Device", "Fonecheck", "test", "16", 36, null),
            app = ReportAppContext("1.0.0", 1L),
            categories =
                listOf(
                    DiagnosticCategoryResult(
                        DiagnosticCategoryId.BATTERY,
                        DiagnosticStatus.PASS,
                        listOf(
                            DiagnosticEvidence(
                                categoryId = DiagnosticCategoryId.BATTERY,
                                checkId = DiagnosticCheckId(DiagnosticCategoryId.BATTERY, "battery.level"),
                                status = DiagnosticStatus.PASS,
                                confidence = Confidence.HIGH,
                                source = EvidenceSource.ANDROID_API,
                                applicability = Applicability.APPLICABLE,
                                value = EvidenceValue.IntValue(80),
                                capturedAt = Instant.parse("2026-08-08T10:00:30Z"),
                            ),
                        ),
                    ),
                ),
            score = ScoreSummary(ScoreVersion.CURRENT, 92, ScoreState.PARTIAL),
            coverage = CoverageSummary(4, 3, 1, 0, 75),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
}
