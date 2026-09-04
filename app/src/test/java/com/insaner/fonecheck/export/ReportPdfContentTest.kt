package com.insaner.fonecheck.export

import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceValue
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
        assertTrue(text.contains("Score version: 2"))
        assertTrue(text.contains("App: 1.0.0 (1)"))
        assertTrue(text.contains("Device: Finnvek Test Device"))
        assertTrue(text.contains("Coverage: 100%"))
        assertTrue(text.contains("battery.level"))
        assertTrue(text.contains("Source: android_api"))
        assertTrue(text.contains("Confidence: high"))
        assertTrue(text.contains("Reason: permission denied"))
        assertTrue(text.contains("Differences and measurements do not prove physical device health."))
    }

    @Test
    fun scoreStateIsASeparateBodyLineBetweenScoreAndCoverage() {
        val blocks = ReportPdfContentBuilder.build(report(), PdfReportLabels.english())
        val scoreIndex = blocks.indexOf(PdfTextBlock("Score: —", PdfTextStyle.HEADING))
        val scoreStateIndex = blocks.indexOf(PdfTextBlock("Score state: incomplete", PdfTextStyle.BODY))
        val coverageIndex = blocks.indexOf(PdfTextBlock("Coverage: 100%", PdfTextStyle.HEADING))

        assertTrue(scoreIndex >= 0)
        assertEquals(scoreIndex + 1, scoreStateIndex)
        assertEquals(scoreStateIndex + 1, coverageIndex)
    }

    @Test
    fun booleanEvidenceUsesTheLocalizedDisplayValue() {
        val report =
            report().let { source ->
                source.copy(
                    categories =
                        source.categories.map { category ->
                            category.copy(
                                evidence =
                                    category.evidence.map { evidence ->
                                        evidence.copy(value = EvidenceValue.BooleanValue(true), unit = null)
                                    },
                            )
                        },
                )
            }

        val blocks = ReportPdfContentBuilder.build(report, PdfReportLabels.english())

        assertTrue(blocks.any { it.text == "yes" })
    }

    @Test
    fun completedAndCapturedTimestampsUseTheLocalizedDateFormatter() {
        val labels = PdfReportLabels.english().copy(completedValue = { "localized date" })

        val blocks = ReportPdfContentBuilder.build(report(), labels)

        assertEquals(2, blocks.count { it.text.endsWith("localized date") })
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
        assertEquals("Heading $marker Last line", text)
        assertTrue(pages.flatten().all { it.text.length <= it.style.maxCharacters })
    }

    @Test
    fun canonicalCategoriesMissingFromSnapshotRemainExplicitlyNotTested() {
        val blocks = ReportPdfContentBuilder.build(report(), PdfReportLabels.english())
        val categoryHeadings = blocks.filter { it.style == PdfTextStyle.CATEGORY }.map(PdfTextBlock::text)

        assertEquals(14, categoryHeadings.size)
        assertEquals("Device — not measured", categoryHeadings.first())
        assertEquals("Biometrics — not measured", categoryHeadings.last())
    }

    private fun report() =
        batteryReport(
            id = "report-123",
            deviceModel = "Test Device",
            reason = EvidenceReasonCode.PERMISSION_DENIED,
        )
}
