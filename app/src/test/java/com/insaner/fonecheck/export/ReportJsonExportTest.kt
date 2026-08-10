package com.insaner.fonecheck.export

import com.insaner.fonecheck.data.repository.ReportPayloadCodec
import com.insaner.fonecheck.testing.batteryReport
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

    private fun report() = batteryReport(id = "report-json", deviceModel = "Test")
}
