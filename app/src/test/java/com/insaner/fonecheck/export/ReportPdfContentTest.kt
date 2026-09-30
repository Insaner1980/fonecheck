package com.insaner.fonecheck.export

import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.testing.batteryReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ReportPdfContentTest {
    @Test
    fun sampleEvidenceUsesTheLocalizedQuantityWithoutAppendingAnotherUnit() {
        val original = report()
        val labels = PdfReportLabels.english().copy(sampleCountValue = { count -> "próbki: $count" })
        listOf(1, 2, 5, 12, 22, 25).forEach { count ->
            val changed =
                original.copy(
                    categories =
                        original.categories.map { category ->
                            category.copy(
                                evidence =
                                    category.evidence.map { evidence ->
                                        evidence.copy(
                                            value = EvidenceValue.IntValue(count),
                                            unit = EvidenceUnitCode("samples"),
                                        )
                                    },
                            )
                        },
                )
            val blocks = ReportPdfContentBuilder.build(changed, labels)
            assertTrue(blocks.any { "próbki: $count" in it.columns })
            assertTrue(blocks.none { it.text.contains("samples") })
        }
    }

    @Test
    fun contentContainsRequiredVersionedReportEvidenceAndDisclaimer() {
        val blocks = ReportPdfContentBuilder.build(report(), PdfReportLabels.english())
        val text = blocks.joinToString("\n", transform = PdfTextBlock::allText)

        assertTrue(text.contains("fonecheck diagnostic report"))
        assertTrue(text.contains("report-123"))
        assertTrue(text.contains("Report format: 1"))
        assertTrue(text.contains("Score version: 2"))
        assertTrue(text.contains("App: 1.0.0 (1)"))
        assertTrue(text.contains("Device: Finnvek Test Device"))
        assertTrue(text.contains("Coverage: 100%"))
        assertTrue(text.contains("battery.level"))
        // High confidence is the unmarked case; only a lower confidence is printed.
        assertTrue(blocks.any { it.text == "android_api, Captured: 2026-08-08T10:00:30Z" })
        assertTrue(text.contains("Reason: permission denied"))
        assertTrue(text.contains("This report summarizes observations recorded in fonecheck."))
    }

    @Test
    fun scoreStateIsASeparateBodyLineBetweenScoreAndCoverage() {
        val blocks = ReportPdfContentBuilder.build(report(), PdfReportLabels.english())
        val scoreIndex = blocks.indexOf(PdfTextBlock("Score: n/a", PdfTextStyle.HEADING))
        val scoreStateIndex = blocks.indexOf(PdfTextBlock("incomplete", PdfTextStyle.BODY))
        val coverageIndex =
            blocks.indexOf(PdfTextBlock("Coverage: 100%, Completed / applicable: 1/1", PdfTextStyle.BODY))

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

        assertTrue(blocks.any { "yes" in it.columns })
    }

    @Test
    fun completedAndCapturedTimestampsUseTheLocalizedFormatters() {
        val report = report()
        var reference: Instant? = null
        val labels =
            PdfReportLabels.english().copy(
                completedValue = { "localized date" },
                observedAtValue = { _, completed ->
                    reference = completed
                    "localized time"
                },
            )

        val blocks = ReportPdfContentBuilder.build(report, labels)

        assertEquals(1, blocks.count { it.text.startsWith("Completed: localized date") })
        assertEquals(1, blocks.count { it.text.endsWith("Captured: localized time") })
        assertEquals(report.completedAt, reference)
    }

    @Test
    fun categoriesOutsideCategoryReportAreNotShownAsUnfinished() {
        val blocks = ReportPdfContentBuilder.build(report(), PdfReportLabels.english())
        val categoryHeadings = blocks.filter { it.style == PdfTextStyle.CATEGORY }

        assertEquals(listOf("Battery"), categoryHeadings.map(PdfTextBlock::text))
        assertEquals(listOf("info"), categoryHeadings.map { it.columns.last() })
    }

    private fun report() =
        batteryReport(
            id = "report-123",
            deviceModel = "Test Device",
            reason = EvidenceReasonCode.PERMISSION_DENIED,
        )
}
