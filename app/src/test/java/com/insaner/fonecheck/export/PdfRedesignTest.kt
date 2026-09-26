package com.insaner.fonecheck.export

import com.insaner.fonecheck.data.repository.ReportPayloadCodec
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.testing.testReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class PdfRedesignTest {
    private val labels = PdfReportLabels.english()

    @Test fun syntheticSampleEquivalentCountsFindingsAndPayloadStaySeparate() {
        // Synthetic evidence, independently matching the supplied sample's counts, not its private payload.
        val sensors =
            category(
                DiagnosticCategoryId.SENSORS,
                List(3) { DiagnosticStatus.PASS } + List(8) { DiagnosticStatus.NOT_TESTED },
            )
        val gps = category(DiagnosticCategoryId.CONNECTIVITY, listOf(DiagnosticStatus.NOT_TESTED))
        val vibration =
            category(DiagnosticCategoryId.VIBRATION, listOf(DiagnosticStatus.FAIL)).let {
                it.copy(
                    evidence =
                        it.evidence.map { item ->
                            item.copy(
                                checkId = DiagnosticCheckId(it.categoryId, "vibration.motor"),
                                source = EvidenceSource.USER_CONFIRMATION,
                                reason = EvidenceReasonCode("user_confirmed_vibration_failure"),
                                value = EvidenceValue.BooleanValue(false),
                            )
                        },
                )
            }
        val rest =
            category(
                DiagnosticCategoryId.DEVICE,
                List(65) { DiagnosticStatus.INFO } + DiagnosticStatus.NOT_AVAILABLE,
            )
        val report =
            testReport(
                categories = listOf(sensors, gps, vibration, rest),
                scoreValue = 88,
                coverage = CoverageSummary(78, 69, 9, 1, 88),
            )
        val before = ReportPayloadCodec.encode(report)
        val blocks = ReportPdfContentBuilder.build(report, labels)
        val summary = blocks.takeWhile { it.text != labels.categories }.joinToString("\n") { it.allText }
        assertTrue(summary.contains("Score: 88 / 100"))
        assertTrue(summary.contains("Score state: partial"))
        assertTrue(summary.contains("Coverage: 88%"))
        assertTrue(summary.contains("Completed / applicable: 69/78"))
        assertTrue(summary.contains("Not measured: 9, Excluded from coverage: 1"))
        assertTrue(summary.contains("Warnings: 0, Failures: 1"))
        assertTrue(summary.contains("The user reported that vibration was not felt"))
        assertFalse(summary.contains("Confidence: user_confirmation"))
        assertEquals(PdfCategoryCounts(3, 11, 8), pdfCategoryCounts(sensors))
        assertEquals(PdfCategoryCounts(0, 1, 1), pdfCategoryCounts(gps))
        assertEquals(79, blocks.count { it.group != null && it.columns.size == 2 })
        assertEquals(before, ReportPayloadCodec.encode(report))
    }

    @Test fun storedScoreAndCoverageAreNeverRecalculatedOrNullCoercedToZero() {
        listOf(100 to ScoreState.COMPLETE, null to ScoreState.INCOMPLETE).forEach { (score, state) ->
            val status = if (score == null) DiagnosticStatus.INFO else DiagnosticStatus.PASS
            val report =
                testReport(
                    categories = listOf(category(DiagnosticCategoryId.DEVICE, listOf(status))),
                    scoreValue = score,
                    scoreState = state,
                    coverage = CoverageSummary(1, 1, 0, 0, 100),
                )
            val text = ReportPdfContentBuilder.build(report, labels).joinToString("\n") { it.allText }
            assertTrue(text.contains("Score: ${score?.let { "$it / 100" } ?: "n/a"}"))
            assertTrue(text.contains("Coverage: 100%"))
            assertFalse(text.contains("Score: 0"))
        }
        val incomplete =
            testReport(
                categories =
                    listOf(
                        category(
                            DiagnosticCategoryId.DEVICE,
                            listOf(DiagnosticStatus.INFO) + List(4) { DiagnosticStatus.NOT_TESTED },
                        ),
                    ),
                scoreValue = null,
                scoreState = ScoreState.INCOMPLETE,
                coverage = CoverageSummary(5, 1, 4, 0, 20),
            )
        val text = ReportPdfContentBuilder.build(incomplete, labels).joinToString("\n") { it.allText }
        assertTrue(text.contains("Score: n/a"))
        assertTrue(text.contains("Coverage: 20%"))
        assertTrue(text.contains("Completed / applicable: 1/5"))
    }

    @Test fun exclusionsAllStatusesAndInformationalFalseAreNotInventedFindings() {
        val items = category(DiagnosticCategoryId.DEVICE, DiagnosticStatus.entries)
        assertEquals(PdfCategoryCounts(4, 5, 1), pdfCategoryCounts(items))
        val excluded =
            items.copy(
                evidence = items.evidence.map { it.copy(applicability = Applicability.NOT_APPLICABLE) },
            )
        assertEquals(PdfCategoryCounts(0, 0, 0), pdfCategoryCounts(excluded))
        val informational = category(DiagnosticCategoryId.DEVICE, listOf(DiagnosticStatus.INFO))
        val text =
            ReportPdfContentBuilder
                .build(
                    testReport(categories = listOf(informational)),
                    labels,
                ).joinToString("\n") {
                    it.allText
                }
        assertTrue(text.contains("Warnings: 0, Failures: 0"))
        assertTrue(text.contains(labels.noFailures))
    }

    @Test fun reducedHistoricalOrderCategoryScopeAndVariableSlotCountsArePreserved() {
        listOf(0, 1, 2, 4).forEach { slots ->
            val sim = category(DiagnosticCategoryId.SIM, List(slots) { DiagnosticStatus.INFO })
            val battery = category(DiagnosticCategoryId.BATTERY, listOf(DiagnosticStatus.PASS))
            val report = testReport(categories = listOf(sim, battery))
            val blocks = ReportPdfContentBuilder.build(report, labels)
            assertEquals(listOf("SIM", "BATTERY"), blocks.filter { it.startsCategory }.map { it.category })
            assertEquals(slots + 1, blocks.count { it.group != null && it.columns.isNotEmpty() })
            val single =
                ReportPdfContentBuilder.build(
                    report.copy(kind = ReportKind.CATEGORY_ONLY, categories = listOf(battery)),
                    labels,
                )
            assertTrue(single.any { it.text == "Scope: battery only." })
            assertEquals(1, single.count { it.startsCategory })
        }
    }

    @Test fun exactDecimalZeroAbsentBytesAndUnknownCodesRetainTheirMeaning() {
        val base = category(DiagnosticCategoryId.DEVICE, listOf(DiagnosticStatus.INFO)).evidence.single()
        val decimal = BigDecimal("-12345678901234567890.123456789")
        assertEquals(
            decimal.toString(),
            ReportPdfContentBuilder.evidenceValue(base.copy(value = EvidenceValue.DecimalValue(decimal)), labels),
        )
        assertEquals("0", ReportPdfContentBuilder.evidenceValue(base.copy(value = EvidenceValue.IntValue(0)), labels))
        assertEquals("n/a", ReportPdfContentBuilder.evidenceValue(base.copy(value = null), labels))
        assertEquals(
            "future code future unit",
            ReportPdfContentBuilder.evidenceValue(
                base.copy(
                    value = EvidenceValue.StableTextCodeValue("future_code"),
                    unit = EvidenceUnitCode("future_unit"),
                ),
                labels,
            ),
        )
        val bytes = base.copy(value = EvidenceValue.LongValue(12345678901), unit = EvidenceUnitCode("bytes"))
        val report =
            testReport(
                categories = listOf(DiagnosticCategoryResult(base.categoryId, DiagnosticStatus.INFO, listOf(bytes))),
            )
        val blocks = ReportPdfContentBuilder.build(report, labels.copy(fileSizeValue = { "12.35 GB" }))
        assertTrue(blocks.any { "12.35 GB" in it.columns })
        assertTrue(blocks.any { it.text == "12345678901 B" })
    }

    private fun category(
        id: DiagnosticCategoryId,
        statuses: List<DiagnosticStatus>,
    ): DiagnosticCategoryResult =
        DiagnosticCategoryResult(
            id,
            statuses.firstOrNull() ?: DiagnosticStatus.NOT_TESTED,
            statuses.mapIndexed { index, status ->
                DiagnosticEvidence(
                    id,
                    DiagnosticCheckId(id, "${id.name.lowercase()}.item_$index"),
                    status,
                    Confidence.HIGH,
                    EvidenceSource.ANDROID_API,
                    Applicability.APPLICABLE,
                    value = EvidenceValue.BooleanValue(false),
                    capturedAt = Instant.EPOCH,
                )
            },
        )
}
