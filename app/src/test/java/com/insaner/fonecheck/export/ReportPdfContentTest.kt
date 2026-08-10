package com.insaner.fonecheck.export

import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.testing.batteryReport
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
        assertTrue(text.contains("Reason: permission denied"))
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
        batteryReport(
            id = "report-123",
            deviceModel = "Test Device",
            reason = EvidenceReasonCode.PERMISSION_DENIED,
        )
}
