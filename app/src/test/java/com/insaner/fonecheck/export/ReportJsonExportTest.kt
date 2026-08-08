package com.insaner.fonecheck.export

import com.insaner.fonecheck.data.repository.ReportPayloadCodec
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportJsonExportTest {
    @Test
    fun versionOneExportIsDeterministicAndRoundTripsTypedEvidence() {
        val report = report()

        val first = ReportPayloadCodec.encode(report)
        val second = ReportPayloadCodec.encode(report)

        assertEquals(first, second)
        assertEquals(report, ReportPayloadCodec.decode(first))
        assertTrue(first.contains("\"schemaVersion\":1"))
        assertTrue(first.contains("\"type\":\"int\",\"value\":\"80\""))
    }

    @Test
    fun versionOneFixtureAcceptsUnknownFutureFields() {
        val report = report()
        val fixture = ReportPayloadCodec.encode(report).dropLast(1) + ",\"futureField\":true}"

        assertEquals(report, ReportPayloadCodec.decode(fixture))
    }

    @Test
    fun exportSchemaHasNoSensitiveLocationNetworkCellAudioOrImageFields() {
        val json = ReportPayloadCodec.encode(report())
        val forbiddenKeys =
            listOf(
                "ssid",
                "bssid",
                "cellId",
                "latitude",
                "longitude",
                "audioData",
                "imageData",
            )

        forbiddenKeys.forEach { key -> assertFalse(json.contains("\"$key\"")) }
    }

    private fun report() =
        DiagnosticReport(
            stableId = "report-json",
            kind = ReportKind.FULL_CHECK,
            startedAt = Instant.parse("2026-08-08T10:00:00Z"),
            completedAt = Instant.parse("2026-08-08T10:01:00Z"),
            device = ReportDeviceContext("Finnvek", "Test", "Fonecheck", "test", "16", 36, null),
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
