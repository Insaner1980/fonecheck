package com.insaner.fonecheck.export

import com.insaner.fonecheck.data.repository.ReportPayloadCodec
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
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
import com.insaner.fonecheck.domain.model.presentationReason
import com.insaner.fonecheck.testing.testReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ReportEvidenceTest {
    @Test
    fun sensorLimitationsSurviveStorageJsonAndPdfContent() {
        val evidence =
            DiagnosticEvidence(
                DiagnosticCategoryId.SENSORS,
                DiagnosticCheckId(DiagnosticCategoryId.SENSORS, "sensors.accelerometer"),
                DiagnosticStatus.PASS,
                Confidence.LOW,
                EvidenceSource.AUTOMATIC_MEASUREMENT,
                Applicability.APPLICABLE,
                EvidenceReasonCode("sensor_response_unreliable"),
                EvidenceValue.IntValue(5),
                EvidenceUnitCode("samples"),
                Instant.EPOCH,
            )
        val report =
            testReport(
                kind = ReportKind.CATEGORY_ONLY,
                categories =
                    listOf(
                        DiagnosticCategoryResult(DiagnosticCategoryId.SENSORS, DiagnosticStatus.PASS, listOf(evidence)),
                    ),
            )
        val decoded = ReportPayloadCodec.decode(ReportPayloadCodec.encode(report))
        assertEquals(report, decoded)
        val text = ReportPdfContentBuilder.build(decoded, PdfReportLabels.english()).joinToString("\n") { it.text }
        assertTrue(text.contains("Confidence: low"))
        assertTrue(text.contains("Reason: sensor response unreliable"))
        val legacy = evidence.copy(reason = null, confidence = Confidence.HIGH)
        assertEquals("sensor_response_accuracy_unknown", legacy.presentationReason()?.value)
        assertNull(legacy.reason)
        val legacyReport = report.copy(categories = listOf(report.categories.single().copy(evidence = listOf(legacy))))
        val legacyText =
            ReportPdfContentBuilder
                .build(
                    legacyReport,
                    PdfReportLabels.english(),
                ).joinToString("\n") { it.text }
        assertTrue(legacyText.contains("Confidence: low"))
        assertTrue(legacyText.contains("Reason: sensor response accuracy unknown"))
        assertEquals(
            Confidence.HIGH,
            legacyReport.categories
                .single()
                .evidence
                .single()
                .confidence,
        )
    }

    @Test
    fun thermalReadingTimesSurviveStorageJsonAndPdfContent() {
        val evidence =
            listOf("status" to 5_000L, "headroom" to 1_000L, "battery_temperature" to 1_000L).map { (id, time) ->
                DiagnosticEvidence(
                    categoryId = DiagnosticCategoryId.THERMAL,
                    checkId = DiagnosticCheckId(DiagnosticCategoryId.THERMAL, "thermal.$id"),
                    status = DiagnosticStatus.INFO,
                    confidence = Confidence.HIGH,
                    source = EvidenceSource.ANDROID_API,
                    applicability = Applicability.APPLICABLE,
                    capturedAt = Instant.ofEpochMilli(time),
                )
            }
        val report =
            testReport(
                kind = ReportKind.CATEGORY_ONLY,
                categories =
                    listOf(
                        DiagnosticCategoryResult(DiagnosticCategoryId.THERMAL, DiagnosticStatus.INFO, evidence),
                    ),
            )
        val decoded = ReportPayloadCodec.decode(ReportPayloadCodec.encode(report))
        assertEquals(report, decoded)
        val blocks = ReportPdfContentBuilder.build(decoded, PdfReportLabels.english())
        assertEquals(2, blocks.count { it.text == "Read or received: 1970-01-01T00:00:01Z" })
        assertEquals(1, blocks.count { it.text == "Read or received: 1970-01-01T00:00:05Z" })
    }
}
