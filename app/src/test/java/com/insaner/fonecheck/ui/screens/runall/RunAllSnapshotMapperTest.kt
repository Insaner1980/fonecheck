package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticSnapshotVersion
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.PerformanceBenchmarkResult
import com.insaner.fonecheck.domain.model.PerformanceInfo
import com.insaner.fonecheck.domain.model.PhoneTypeCode
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
import com.insaner.fonecheck.domain.model.TelephonyHardwareCode
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.ui.screens.audio.AudioTestState
import com.insaner.fonecheck.ui.screens.battery.BatteryTestState
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestState
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestState
import com.insaner.fonecheck.ui.screens.camera.CameraTestState
import com.insaner.fonecheck.ui.screens.connectivity.BluetoothAccessCode
import com.insaner.fonecheck.ui.screens.connectivity.BluetoothState
import com.insaner.fonecheck.ui.screens.connectivity.ConnectivityTestState
import com.insaner.fonecheck.ui.screens.connectivity.GpsFailureCode
import com.insaner.fonecheck.ui.screens.connectivity.GpsFixStatus
import com.insaner.fonecheck.ui.screens.connectivity.GpsState
import com.insaner.fonecheck.ui.screens.connectivity.MobileNetworkState
import com.insaner.fonecheck.ui.screens.connectivity.NfcState
import com.insaner.fonecheck.ui.screens.connectivity.WifiState
import com.insaner.fonecheck.ui.screens.display.DisplayTestState
import com.insaner.fonecheck.ui.screens.sensor.GuidedSensorCatalog
import com.insaner.fonecheck.ui.screens.sensor.GuidedSensorCode
import com.insaner.fonecheck.ui.screens.sensor.GuidedSensorStatus
import com.insaner.fonecheck.ui.screens.sensor.InteractiveChallenge
import com.insaner.fonecheck.ui.screens.sensor.SensorTestState
import com.insaner.fonecheck.ui.screens.sensor.SensorType
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestState
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RunAllSnapshotMapperTest {
    @Test
    fun mapProducesCurrentCatalogOrderedSnapshotsWithoutSensitiveConnectivityValues() {
        val capturedAt = Instant.parse("2026-08-07T12:00:30Z")
        val sensitiveValues =
            listOf(
                "private-ssid",
                "00:11:22:33:44:55",
                "192.0.2.10",
                "60.1699",
                "24.9384",
                "private-operator",
                "private-cell-id",
            )
        val snapshots =
            RunAllSnapshotMapper.map(
                snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
                manual = ManualCheckResults(display = true, speaker = false),
                permissions = RunAllPermissions(),
                capturedAt = capturedAt,
            )

        assertEquals(DiagnosticCatalog.categories, snapshots.map { it.categoryId })
        assertTrue(snapshots.all { it.version == DiagnosticSnapshotVersion.CURRENT })
        assertTrue(snapshots.all { it.evidence.isNotEmpty() })
        assertTrue(
            snapshots
                .filterNot { it.categoryId == DiagnosticCategoryId.DEVICE }
                .flatMap { it.evidence }
                .all { it.capturedAt == capturedAt },
        )
        assertTrue(
            snapshots.flatMap { it.evidence }.all { evidence ->
                evidence.checkId.value.startsWith("${evidence.categoryId.stableId}.")
            },
        )

        val rawTextValues =
            snapshots
                .flatMap { it.evidence }
                .mapNotNull { (it.value as? EvidenceValue.RawTextValue)?.value }
        assertFalse(sensitiveValues.any { sensitive -> rawTextValues.any { sensitive in it } })
    }

    @Test
    fun mapPreservesManualPermissionAndHardwareOutcomesAsTypedEvidence() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
                    manual = ManualCheckResults(display = true, speaker = false),
                    permissions = RunAllPermissions(camera = false),
                    capturedAt = Instant.parse("2026-08-07T12:00:30Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(
            DiagnosticEvidence(
                categoryId = DiagnosticCategoryId.DISPLAY,
                checkId = DiagnosticCheckId(DiagnosticCategoryId.DISPLAY, "display.visual"),
                status = DiagnosticStatus.PASS,
                confidence = Confidence.HIGH,
                source = EvidenceSource.USER_CONFIRMATION,
                applicability = Applicability.APPLICABLE,
                value = EvidenceValue.BooleanValue(true),
                capturedAt = Instant.parse("2026-08-07T12:00:30Z"),
            ),
            evidence["display.visual"],
        )
        assertEquals(DiagnosticStatus.FAIL, evidence.getValue("audio.speaker").status)
        assertEquals(EvidenceReasonCode.USER_CONFIRMED_FAILURE, evidence.getValue("audio.speaker").reason)
        assertEquals(EvidenceValue.BooleanValue(false), evidence.getValue("audio.speaker").value)
        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("camera.capture").status)
        assertEquals(EvidenceReasonCode.PERMISSION_DENIED, evidence.getValue("camera.capture").reason)
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.getValue("vibration.hardware").status)
        assertEquals(Applicability.NOT_APPLICABLE, evidence.getValue("vibration.hardware").applicability)
        assertEquals(EvidenceReasonCode.HARDWARE_UNAVAILABLE, evidence.getValue("vibration.hardware").reason)
    }

    @Test
    fun rootArtifactHeuristicRemainsInformationalWhenNothingIsDetected() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-07T12:00:30Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }
                .getValue("device.security")

        assertEquals(DiagnosticStatus.INFO, evidence.status)
        assertEquals(Confidence.LOW, evidence.confidence)
        assertEquals(EvidenceSource.ESTIMATE, evidence.source)
        assertEquals(EvidenceValue.StableTextCodeValue("no_known_artifact_detected"), evidence.value)
        assertEquals(Instant.parse("2026-08-07T12:00:00Z"), evidence.capturedAt)
    }

    @Test
    fun detectedRootArtifactProducesLowConfidenceWarning() {
        val snapshots = diagnosticSnapshotsWithSensitiveConnectivity()
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = snapshots.copy(device = deviceInfo(rootArtifactDetected = true)),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-07T12:00:30Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }
                .getValue("device.security")

        assertEquals(DiagnosticStatus.WARNING, evidence.status)
        assertEquals(Confidence.LOW, evidence.confidence)
        assertEquals(EvidenceReasonCode.DEGRADED, evidence.reason)
        assertEquals(EvidenceValue.StableTextCodeValue("known_artifact_detected"), evidence.value)
    }

    @Test
    fun simInventoryUsesStableCodesAndDeniedPermissionOnlyReducesCoverage() {
        val capturedAt = Instant.parse("2026-08-07T12:00:30Z")
        val deniedEvidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            sim = simInfo(SimInventoryCode.INACTIVE_SIM),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(phone = false),
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(DiagnosticStatus.INFO, deniedEvidence.getValue("sim.inventory").status)
        assertEquals(
            EvidenceValue.StableTextCodeValue("inactive_sim"),
            deniedEvidence.getValue("sim.inventory").value,
        )
        assertEquals(DiagnosticStatus.NOT_TESTED, deniedEvidence.getValue("sim.network").status)
        assertEquals(EvidenceReasonCode.PERMISSION_DENIED, deniedEvidence.getValue("sim.network").reason)
        assertFalse(deniedEvidence.values.any { it.status == DiagnosticStatus.FAIL })

        val noHardware =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            sim = simInfo(SimInventoryCode.NO_TELEPHONY),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(phone = false),
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, noHardware.getValue("sim.inventory").status)
        assertEquals(Applicability.NOT_APPLICABLE, noHardware.getValue("sim.inventory").applicability)
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, noHardware.getValue("sim.network").status)
    }

    @Test
    fun capturedMicrophoneSamplesRemainInformationalUntilUserConfirmsPlayback() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            audio = AudioTestState(hasRecordedAudio = true),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(microphone = true),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }
                .getValue("audio.microphone")

        assertEquals(DiagnosticStatus.INFO, evidence.status)
        assertEquals(EvidenceValue.BooleanValue(true), evidence.value)
    }

    @Test
    fun performanceBenchmarkProducesInformationalRawEvidence() {
        val benchmarkCapturedAt = Instant.parse("2026-08-07T12:00:10Z")
        val snapshots = diagnosticSnapshotsWithSensitiveConnectivity()
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        snapshots.copy(
                            performanceBenchmark =
                                PerformanceBenchmarkResult(
                                    cpuOperationsPerSecond = 2_000L,
                                    memoryMebibytesPerSecond = 640.5,
                                    memoryBytesProcessed = 64L * 1_048_576,
                                    durationMillis = 600L,
                                    thermalBefore = ThermalStatusCode.NONE,
                                    thermalAfter = ThermalStatusCode.LIGHT,
                                    capturedAt = benchmarkCapturedAt,
                                ),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-07T12:00:30Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        with(evidence.getValue("performance.cpu_benchmark")) {
            assertEquals(DiagnosticStatus.INFO, status)
            assertEquals(Confidence.LOW, confidence)
            assertEquals(EvidenceSource.AUTOMATIC_MEASUREMENT, source)
            assertEquals(EvidenceValue.LongValue(2_000L), value)
            assertEquals("operations_per_second", unit?.value)
            assertEquals(benchmarkCapturedAt, capturedAt)
        }
        with(evidence.getValue("performance.memory_benchmark")) {
            assertEquals(DiagnosticStatus.INFO, status)
            assertEquals(EvidenceValue.DoubleValue(640.5), value)
            assertEquals("mebibytes_per_second", unit?.value)
            assertEquals(benchmarkCapturedAt, capturedAt)
        }
    }

    @Test
    fun sensorEvidenceNamesEveryGuidedSensorAndOnlyPassesCompletedTests() {
        val guidedTests =
            GuidedSensorCatalog
                .create(setOf(SensorType.ACCELEROMETER, SensorType.LIGHT))
                .map { test ->
                    when (test.code) {
                        GuidedSensorCode.ACCELEROMETER ->
                            test.copy(status = GuidedSensorStatus.PASSED, sampleCount = 6)
                        GuidedSensorCode.LIGHT -> test.copy(status = GuidedSensorStatus.SKIPPED)
                        else -> test
                    }
                }
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            sensors =
                                SensorTestState(
                                    guidedTests = guidedTests,
                                    completedChallenges = setOf(InteractiveChallenge.TILT_LEFT),
                                    sensorCount = 2,
                                ),
                        ),
                    manual = ManualCheckResults(sensors = true),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).single { it.categoryId == DiagnosticCategoryId.SENSORS }
                .evidence
                .associateBy { it.checkId.value }

        assertTrue(GuidedSensorCode.entries.all { "sensors.${it.stableCode}" in evidence })
        with(evidence.getValue("sensors.accelerometer")) {
            assertEquals(DiagnosticStatus.PASS, status)
            assertEquals(EvidenceSource.AUTOMATIC_MEASUREMENT, source)
            assertEquals(EvidenceValue.IntValue(6), value)
            assertEquals("samples", unit?.value)
        }
        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("sensors.light").status)
        assertEquals(EvidenceReasonCode.SKIPPED, evidence.getValue("sensors.light").reason)
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.getValue("sensors.gyroscope").status)
        assertEquals(EvidenceSource.DERIVED, evidence.getValue("sensors.orientation").source)
        assertEquals(DiagnosticStatus.INFO, evidence.getValue("sensors.orientation").status)
        assertEquals(EvidenceSource.AUTOMATIC_MEASUREMENT, evidence.getValue("sensors.motion").source)
    }

    @Test
    fun connectivityEvidenceSeparatesPermissionCapabilityAndPhysicalFixWithoutPrivateValues() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            connectivity =
                                ConnectivityTestState(
                                    wifi = WifiState(isAvailable = true, isConnected = true, ssid = "private-ssid"),
                                    bluetooth =
                                        BluetoothState(
                                            isAvailable = true,
                                            access = BluetoothAccessCode.PERMISSION_DENIED,
                                        ),
                                    nfc =
                                        NfcState(
                                            isAvailable = true,
                                            isEnabled = true,
                                            supportsHostCardEmulation = true,
                                        ),
                                    gps =
                                        GpsState(
                                            isAvailable = true,
                                            isEnabled = true,
                                            fixStatus = GpsFixStatus.FIXED,
                                            latitude = 60.1699,
                                            longitude = 24.9384,
                                            fixTimeMs = 3_500L,
                                        ),
                                    mobileNetwork =
                                        MobileNetworkState(
                                            isAvailable = true,
                                            isConnected = true,
                                            operatorName = "private-operator",
                                            cellId = "private-cell-id",
                                        ),
                                ),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).single { it.categoryId == DiagnosticCategoryId.CONNECTIVITY }
                .evidence
                .associateBy { it.checkId.value }

        assertEquals(DiagnosticStatus.INFO, evidence.getValue("connectivity.wifi").status)
        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("connectivity.bluetooth").status)
        assertEquals(EvidenceReasonCode.PERMISSION_DENIED, evidence.getValue("connectivity.bluetooth").reason)
        assertEquals(DiagnosticStatus.INFO, evidence.getValue("connectivity.nfc").status)
        assertEquals(DiagnosticStatus.INFO, evidence.getValue("connectivity.nfc_hce").status)
        assertEquals(DiagnosticStatus.PASS, evidence.getValue("connectivity.gps").status)
        assertEquals(EvidenceSource.AUTOMATIC_MEASUREMENT, evidence.getValue("connectivity.gps").source)
        assertEquals(EvidenceValue.LongValue(3_500L), evidence.getValue("connectivity.gps").value)
        assertEquals(DiagnosticStatus.INFO, evidence.getValue("connectivity.mobile").status)

        val rawValues = evidence.values.mapNotNull { (it.value as? EvidenceValue.RawTextValue)?.value }
        assertFalse(rawValues.any { it.contains("private") || it.contains("60.1699") || it.contains("24.9384") })
    }

    @Test
    fun gpsTimeoutIsADegradedObservationInsteadOfAHardwareFailureClaim() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            connectivity =
                                ConnectivityTestState(
                                    gps =
                                        GpsState(
                                            isAvailable = true,
                                            isEnabled = true,
                                            fixStatus = GpsFixStatus.FAILED,
                                            failure = GpsFailureCode.TIMEOUT,
                                        ),
                                ),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }
                .getValue("connectivity.gps")

        assertEquals(DiagnosticStatus.WARNING, evidence.status)
        assertEquals(Confidence.LOW, evidence.confidence)
        assertEquals(EvidenceReasonCode.TIMEOUT, evidence.reason)
    }

    private fun diagnosticSnapshotsWithSensitiveConnectivity() =
        DiagnosticSnapshots(
            device = deviceInfo(),
            performance = performanceInfo(),
            sim =
                simInfo(SimInventoryCode.NO_SIM),
            display = DisplayTestState(),
            audio = AudioTestState(),
            camera = CameraTestState(),
            sensors = SensorTestState(),
            connectivity =
                ConnectivityTestState(
                    wifi =
                        WifiState(
                            isAvailable = true,
                            isConnected = true,
                            ssid = "private-ssid",
                            ipAddress = "192.0.2.10",
                        ),
                    gps =
                        GpsState(
                            isAvailable = true,
                            isEnabled = true,
                            latitude = 60.1699,
                            longitude = 24.9384,
                        ),
                    mobileNetwork =
                        MobileNetworkState(
                            isAvailable = true,
                            isConnected = true,
                            operatorName = "private-operator",
                            cellId = "private-cell-id",
                        ),
                ),
            battery = BatteryTestState(),
            vibration = VibrationTestState(),
            buttons = ButtonTestState(),
            biometrics = BiometricTestState(),
        )

    private fun simInfo(inventory: SimInventoryCode) =
        SimTelephonyInfo(
            hardware =
                if (inventory == SimInventoryCode.NO_TELEPHONY) {
                    TelephonyHardwareCode.NO_HARDWARE
                } else {
                    TelephonyHardwareCode.AVAILABLE
                },
            inventory = inventory,
            simSlots = emptyList(),
            phoneType = PhoneTypeCode.NONE,
            phoneCount = 0,
            dataNetworkType = NetworkGenerationCode.UNKNOWN,
            phoneStatePermissionGranted = false,
        )

    private fun deviceInfo(rootArtifactDetected: Boolean = false) =
        DeviceInfo(
            model = "model",
            manufacturer = "manufacturer",
            brand = "brand",
            product = "product",
            androidVersion = "16",
            apiLevel = 36,
            securityPatch = "2026-08-01",
            buildNumber = "build",
            kernelVersion = "kernel",
            basebandVersion = "baseband",
            bootloaderVersion = "bootloader",
            widevineLevel = "L1",
            rootArtifactDetected = rootArtifactDetected,
            developerOptionsEnabled = false,
            usbDebuggingEnabled = false,
            capturedAt = Instant.parse("2026-08-07T12:00:00Z"),
        )

    private fun performanceInfo() =
        PerformanceInfo(
            cpuModel = "cpu",
            cpuArchitecture = "arm64",
            cpuCores = 8,
            cpuFrequencies = emptyList(),
            cpuConfidence = Confidence.HIGH,
            totalRamBytes = 8L * 1_073_741_824,
            availableRamBytes = 4L * 1_073_741_824,
            ramConfidence = Confidence.HIGH,
            glEsVersion = "3.2",
            glRenderer = "gpu",
            glVendor = "vendor",
            vulkanFeatureDeclared = true,
            gpuConfidence = Confidence.HIGH,
            capturedAt = Instant.parse("2026-08-07T12:00:00Z"),
        )
}
