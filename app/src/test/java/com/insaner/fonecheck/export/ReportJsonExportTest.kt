package com.insaner.fonecheck.export

import com.insaner.fonecheck.data.repository.ReportPayloadCodec
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.testing.batteryReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

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

    @Test
    fun documentedExampleKeepsAndroidBatteryLevelInformational() {
        val report = ReportPayloadCodec.decode(locateDocumentedExample().readText())
        val evidence =
            report.categories
                .single()
                .evidence
                .single()

        assertEquals("battery.level", evidence.checkId.value)
        assertEquals(DiagnosticStatus.INFO, evidence.status)
        assertEquals(DiagnosticStatus.INFO, report.categories.single().aggregateStatus)
        assertEquals(ScoreVersion.CURRENT, report.score.version)
        assertEquals(ScoreState.INCOMPLETE, report.score.state)
        assertNull(report.score.value)
        assertEquals(100, report.coverage.percentage)
    }

    private fun report() = batteryReport(id = "report-json", deviceModel = "Test")

    private fun locateDocumentedExample(): File {
        val workingDirectory = File(requireNotNull(System.getProperty("user.dir")))
        return requireNotNull(
            listOf(
                File(workingDirectory, "docs/report-export-v1.example.json"),
                File(workingDirectory, "../docs/report-export-v1.example.json"),
            ).firstOrNull(File::isFile),
        ) { "Documented report export example was not found from $workingDirectory" }
    }
}
