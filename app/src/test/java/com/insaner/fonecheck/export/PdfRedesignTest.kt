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
        assertTrue(summary.contains("Score: 88 / 100\npartial"))
        assertTrue(summary.contains("Coverage: 88%, Completed / applicable: 69/78"))
        assertTrue(summary.contains("Failures: 1, Warnings: 0, Not measured: 9, Excluded from coverage: 1"))
        assertTrue(summary.contains("The user reported that vibration was not felt"))
        // The findings name what was not measured instead of only counting it.
        assertTrue(summary.contains("Sensors — Not measured: sensors.item_3, sensors.item_4"))
        assertTrue(summary.contains("Connectivity — Not measured: connectivity.item_0"))
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
        assertTrue(text.contains("Failures: 0, Warnings: 0"))
        assertTrue(text.contains(labels.noFailures))
    }

    @Test fun reducedHistoricalOrderCategoryScopeAndVariableSlotCountsArePreserved() {
        listOf(0, 1, 2, 4).forEach { slots ->
            val sim = category(DiagnosticCategoryId.SIM, List(slots) { DiagnosticStatus.INFO })
            val battery = category(DiagnosticCategoryId.BATTERY, listOf(DiagnosticStatus.PASS))
            val report = testReport(categories = listOf(sim, battery))
            val blocks = ReportPdfContentBuilder.build(report, labels)
            // Hardware tests come before device information; each keeps its saved order.
            assertEquals(listOf("BATTERY", "SIM"), blocks.filter { it.startsCategory }.map { it.category })
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
        // Exact bytes stay in the JSON export; the page carries the rounded size only.
        assertFalse(blocks.any { it.allText.contains("12345678901") })
    }

    @Test fun partlyMeasuredCategoryIsPartialAndInformationalRowsCarryNoVerdict() {
        val sensors =
            category(DiagnosticCategoryId.SENSORS, listOf(DiagnosticStatus.PASS, DiagnosticStatus.NOT_TESTED))
                .copy(aggregateStatus = DiagnosticStatus.NOT_TESTED)
        val device =
            category(DiagnosticCategoryId.DEVICE, listOf(DiagnosticStatus.INFO, DiagnosticStatus.NOT_AVAILABLE)).let {
                val unread = it.evidence.last().copy(value = null)
                it.copy(evidence = listOf(it.evidence.first(), unread))
            }
        val blocks = ReportPdfContentBuilder.build(testReport(categories = listOf(sensors, device)), labels)
        // Nothing was read from an unavailable observation, so it prints no confidence and no time.
        val unavailableDetail = blocks[blocks.indexOfFirst { it.text == "device.item_1" && it.group != null } + 1]
        assertEquals("android_api", unavailableDetail.text)

        val heading = blocks.single { it.startsCategory && it.category == "SENSORS" }
        assertEquals(PdfMark.PARTIAL, heading.mark)
        assertEquals(labels.partial, heading.columns.last())
        val tableRow = blocks.single { it.text == "Sensors" && it.style == PdfTextStyle.BODY }
        assertEquals(listOf("1/2", labels.partial), tableRow.columns)

        val rows = blocks.filter { it.group != null && it.columns.size == 2 }.associateBy { it.text }
        assertEquals(PdfMark.PASS, rows.getValue("sensors.item_0").mark)
        assertEquals(PdfMark.NOT_MEASURED, rows.getValue("sensors.item_1").mark)
        assertEquals(null, rows.getValue("device.item_0").mark)
        assertEquals("", rows.getValue("device.item_0").columns.last())
        assertTrue(blocks.any { it.legend.map { entry -> entry.first }.containsAll(PdfMark.entries) })
    }

    @Test fun measuredValuesUseScreenPrecisionAndManualAnswersReadAsAnswers() {
        val base = category(DiagnosticCategoryId.BATTERY, listOf(DiagnosticStatus.INFO)).evidence.single()

        fun value(
            value: EvidenceValue,
            unit: String,
        ) = ReportPdfContentBuilder.evidenceValue(base.copy(value = value, unit = EvidenceUnitCode(unit)), labels)
        assertEquals("31.3%", value(EvidenceValue.DoubleValue(31.339), "percent"))
        assertEquals("70%", value(EvidenceValue.IntValue(70), "percent"))
        assertEquals("0.52", value(EvidenceValue.DoubleValue(0.516), "ratio"))
        assertEquals("29.7 °C", value(EvidenceValue.DoubleValue(29.74), "celsius"))
        assertEquals("679.7 mA", value(EvidenceValue.DoubleValue(679.687), "milliamperes"))
        val manual = base.copy(source = EvidenceSource.USER_CONFIRMATION)
        assertEquals(
            "problem reported",
            ReportPdfContentBuilder.evidenceValue(manual.copy(value = EvidenceValue.BooleanValue(false)), labels),
        )
        assertEquals(
            "confirmed working",
            ReportPdfContentBuilder.evidenceValue(manual.copy(value = EvidenceValue.BooleanValue(true)), labels),
        )
        assertEquals("no", ReportPdfContentBuilder.evidenceValue(base, labels))
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
