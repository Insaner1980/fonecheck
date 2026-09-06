package com.insaner.fonecheck.export

import com.insaner.fonecheck.data.repository.ReportPayloadCodec
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportAssembler
import com.insaner.fonecheck.domain.model.ReportAssemblyRequest
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.ui.screens.audio.AudioTestState
import com.insaner.fonecheck.ui.screens.battery.BatteryTestState
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestState
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestState
import com.insaner.fonecheck.ui.screens.camera.CameraTestState
import com.insaner.fonecheck.ui.screens.connectivity.BluetoothAccessCode
import com.insaner.fonecheck.ui.screens.connectivity.BluetoothState
import com.insaner.fonecheck.ui.screens.connectivity.ConnectivityTestState
import com.insaner.fonecheck.ui.screens.display.DisplayTestState
import com.insaner.fonecheck.ui.screens.runall.DiagnosticSnapshots
import com.insaner.fonecheck.ui.screens.runall.ManualCheckResults
import com.insaner.fonecheck.ui.screens.runall.RunAllPermissions
import com.insaner.fonecheck.ui.screens.runall.RunAllSnapshotMapper
import com.insaner.fonecheck.ui.screens.sensor.SensorTestState
import com.insaner.fonecheck.ui.screens.storage.StorageTestState
import com.insaner.fonecheck.ui.screens.thermal.ThermalTestState
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class BluetoothReportPrivacyTest {
    @Test
    fun reportKeepsEnabledBluetoothWithoutAdapterName() {
        val report = bluetoothReport()
        val evidence = report.categories.single().evidence
        val bluetooth = evidence.single { it.checkId.value == BLUETOOTH_CHECK_ID }
        val content =
            listOfNotNull(
                report.stableId,
                report.device.manufacturer,
                report.device.model,
                report.device.brand,
                report.device.product,
                report.device.androidRelease,
                report.device.securityPatch,
                report.app.versionName,
            ) +
                evidence.flatMap { item ->
                    listOfNotNull(
                        item.categoryId.stableId,
                        item.checkId.value,
                        item.reason?.value,
                        item.unit?.value,
                        (item.value as? EvidenceValue.RawTextValue)?.value,
                        (item.value as? EvidenceValue.StableTextCodeValue)?.value,
                    )
                }

        assertFalse("Adapter name leaked into report content", content.any { ADAPTER_NAME in it })
        assertEquals(EvidenceValue.BooleanValue(true), bluetooth.value)
    }

    @Test
    fun payloadAndJsonKeepEnabledBluetoothWithoutAdapterName() {
        val report = bluetoothReport()
        val payload = ReportPayloadCodec.encode(report)
        val json = Json.parseToJsonElement(payload).jsonObject
        val bluetooth =
            json
                .getValue("categories")
                .jsonArray
                .single()
                .jsonObject
                .getValue("evidence")
                .jsonArray
                .single {
                    it.jsonObject
                        .getValue("checkId")
                        .jsonPrimitive.content == BLUETOOTH_CHECK_ID
                }.jsonObject

        assertEquals(REPORT_ID, json.getValue("stableId").jsonPrimitive.content)
        assertFalse("Adapter name leaked into payload/JSON strings", json.stringValues().any { ADAPTER_NAME in it })
        val value = bluetooth.getValue("value").jsonObject
        assertEquals("boolean", value.getValue("type").jsonPrimitive.content)
        assertEquals("true", value.getValue("value").jsonPrimitive.content)
        assertEquals(report, ReportPayloadCodec.decode(payload))
    }

    @Test
    fun pdfContentKeepsEnabledBluetoothWithoutAdapterName() {
        val blocks = ReportPdfContentBuilder.build(bluetoothReport(), PdfReportLabels.english())
        val bluetoothIndex = blocks.indexOfFirst { it.text.startsWith("$BLUETOOTH_CHECK_ID — ") }

        assertTrue(blocks.any { it.text == "Report ID: $REPORT_ID" })
        assertTrue("Bluetooth observation must appear in PDF content", bluetoothIndex >= 0)
        assertFalse("Adapter name leaked into PDF text", blocks.any { ADAPTER_NAME in it.text })
        assertEquals(PdfTextBlock("yes", PdfTextStyle.MONO), blocks[bluetoothIndex + 1])
    }

    private fun bluetoothReport(): DiagnosticReport {
        val capturedAt = Instant.parse("2026-08-08T12:00:00Z")
        val snapshots =
            RunAllSnapshotMapper.map(
                snapshots =
                    DiagnosticSnapshots(
                        device = null,
                        performance = null,
                        sim = null,
                        display = DisplayTestState(),
                        audio = AudioTestState(),
                        camera = CameraTestState(),
                        sensors = SensorTestState(),
                        connectivity =
                            ConnectivityTestState(
                                bluetooth =
                                    BluetoothState(
                                        isAvailable = true,
                                        access = BluetoothAccessCode.GRANTED,
                                        isEnabled = true,
                                        name = ADAPTER_NAME,
                                    ),
                            ),
                        battery = BatteryTestState(),
                        thermal = ThermalTestState(),
                        storage = StorageTestState(),
                        vibration = VibrationTestState(),
                        buttons = ButtonTestState(),
                        biometrics = BiometricTestState(),
                    ),
                manual = ManualCheckResults(),
                permissions = RunAllPermissions(bluetooth = true),
                capturedAt = capturedAt,
            )
        return ReportAssembler.assemble(
            ReportAssemblyRequest(
                stableId = REPORT_ID,
                kind = ReportKind.CATEGORY_ONLY,
                startedAt = capturedAt.minusSeconds(1),
                completedAt = capturedAt,
                device = ReportDeviceContext("Test", "Test", "Test", "Test", "16", 36, null),
                app = ReportAppContext("1.0", 1L),
                snapshots = listOf(snapshots.single { it.categoryId == DiagnosticCategoryId.CONNECTIVITY }),
            ),
        )
    }

    private fun JsonElement.stringValues(): List<String> =
        when (this) {
            is JsonObject -> values.flatMap { it.stringValues() }
            is JsonArray -> flatMap { it.stringValues() }
            is JsonPrimitive -> if (isString) listOf(content) else emptyList()
        }

    private companion object {
        const val ADAPTER_NAME = "fonecheck-private-bt-7c92-sentinel"
        const val BLUETOOTH_CHECK_ID = "connectivity.bluetooth"
        const val REPORT_ID = "bluetooth-privacy-report"
    }
}
