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
import com.insaner.fonecheck.ui.screens.battery.BasicBatteryState
import com.insaner.fonecheck.ui.screens.battery.BatteryCurrentDirection
import com.insaner.fonecheck.ui.screens.battery.BatteryTestState
import com.insaner.fonecheck.ui.screens.battery.ChargingState
import com.insaner.fonecheck.ui.screens.battery.HealthState
import com.insaner.fonecheck.ui.screens.battery.ManufacturerProfile
import com.insaner.fonecheck.ui.screens.battery.ManufacturerState
import com.insaner.fonecheck.ui.screens.biometrics.AuthResult
import com.insaner.fonecheck.ui.screens.biometrics.BiometricAvailability
import com.insaner.fonecheck.ui.screens.biometrics.BiometricCapability
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestState
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestPhase
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
import com.insaner.fonecheck.ui.screens.storage.AppStorageVolumeInfo
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkErrorCode
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkPhase
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkResult
import com.insaner.fonecheck.ui.screens.storage.StorageInfo
import com.insaner.fonecheck.ui.screens.storage.StorageTestState
import com.insaner.fonecheck.ui.screens.thermal.ThermalSeverityCode
import com.insaner.fonecheck.ui.screens.thermal.ThermalTestState
import com.insaner.fonecheck.ui.screens.vibration.HapticCapabilityState
import com.insaner.fonecheck.ui.screens.vibration.VibrationEffectCode
import com.insaner.fonecheck.ui.screens.vibration.VibrationPrimitiveCode
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

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

    @Test
    fun missingBatteryMeasurementsAreUnavailableInsteadOfZeroMeasurements() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity().copy(battery = BatteryTestState()),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        listOf("battery.health", "battery.temperature", "battery.level", "battery.current_now").forEach { id ->
            assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.getValue(id).status)
            assertEquals(Confidence.UNAVAILABLE, evidence.getValue(id).confidence)
            assertEquals(null, evidence.getValue(id).value)
        }
        assertEquals(
            EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED,
            evidence.getValue("battery.cycle_count").reason,
        )
    }

    @Test
    fun batterySnapshotPreservesCurrentConfidenceSignCaveatCycleCountAndOemProfile() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            battery =
                                BatteryTestState(
                                    basic =
                                        BasicBatteryState(
                                            level = 75,
                                            voltageMv = 4_100,
                                            temperatureCelsius = 32.5f,
                                            healthStatus = android.os.BatteryManager.BATTERY_HEALTH_GOOD,
                                        ),
                                    charging =
                                        ChargingState(
                                            chargingCurrentMa = 3_000.0,
                                            currentDirection = BatteryCurrentDirection.CHARGING,
                                            currentSignNormalized = true,
                                            chargingCurrentConfidence = Confidence.LOW,
                                        ),
                                    health =
                                        HealthState(
                                            healthStatusRaw = android.os.BatteryManager.BATTERY_HEALTH_GOOD,
                                            cycleCount = 240,
                                            cycleCountSupported = true,
                                            cycleCountConfidence = Confidence.HIGH,
                                        ),
                                    manufacturer =
                                        ManufacturerState(
                                            profile = ManufacturerProfile.SAMSUNG,
                                            manufacturerName = "Samsung",
                                        ),
                                ),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(EvidenceValue.DoubleValue(3_000.0), evidence.getValue("battery.current_now").value)
        assertEquals(Confidence.LOW, evidence.getValue("battery.current_now").confidence)
        assertEquals(
            EvidenceValue.StableTextCodeValue("status_sign_normalized"),
            evidence.getValue("battery.current_interpretation").value,
        )
        assertEquals(
            EvidenceValue.StableTextCodeValue("samsung"),
            evidence.getValue("battery.current_profile").value,
        )
        assertEquals(EvidenceValue.IntValue(240), evidence.getValue("battery.cycle_count").value)
        assertEquals(Confidence.HIGH, evidence.getValue("battery.cycle_count").confidence)
    }

    @Test
    fun unsupportedThermalApiIsDifferentFromANormalThermalObservation() {
        val unsupported =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, unsupported.getValue("thermal.status").status)
        assertEquals(
            EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED,
            unsupported.getValue("thermal.status").reason,
        )

        val normal =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            thermal =
                                ThermalTestState(
                                    statusApiSupported = true,
                                    status = ThermalStatusCode.NONE,
                                    severity = ThermalSeverityCode.NORMAL,
                                    statusConfidence = Confidence.HIGH,
                                    batteryTemperatureCelsius = 32.0f,
                                    batteryTemperatureConfidence = Confidence.HIGH,
                                ),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(DiagnosticStatus.PASS, normal.getValue("thermal.status").status)
        assertEquals(EvidenceValue.StableTextCodeValue("none"), normal.getValue("thermal.status").value)
        assertEquals(
            EvidenceValue.DoubleValue(32.0),
            normal.getValue("thermal.battery_temperature").value,
        )
    }

    @Test
    fun thermalHeadroomIsInformationalAndSevereStatusIsARealFailure() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            thermal =
                                ThermalTestState(
                                    statusApiSupported = true,
                                    headroomApiSupported = true,
                                    status = ThermalStatusCode.SEVERE,
                                    severity = ThermalSeverityCode.SEVERE,
                                    statusConfidence = Confidence.HIGH,
                                    headroom = 1.1f,
                                    headroomConfidence = Confidence.LOW,
                                ),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(DiagnosticStatus.FAIL, evidence.getValue("thermal.status").status)
        assertEquals(EvidenceReasonCode.DEGRADED, evidence.getValue("thermal.status").reason)
        assertEquals(DiagnosticStatus.INFO, evidence.getValue("thermal.headroom").status)
        assertEquals(Confidence.LOW, evidence.getValue("thermal.headroom").confidence)
        assertEquals(EvidenceValue.DoubleValue(1.1), evidence.getValue("thermal.headroom").value)
        assertEquals(
            EvidenceValue.StableTextCodeValue("severe"),
            evidence.getValue("thermal.severity").value,
        )
    }

    @Test
    fun storageSnapshotContainsInformationRawRatesAndTestConditions() {
        val capturedAt = Instant.parse("2026-08-08T12:00:00Z")
        val storageState =
            StorageTestState(
                info =
                    StorageInfo(
                        totalBytes = 256L * MEBIBYTE,
                        usedBytes = 192L * MEBIBYTE,
                        availableBytes = 64L * MEBIBYTE,
                        usagePercent = 75.0,
                        internalStorageAccessible = true,
                        appAccessibleVolumes =
                            listOf(
                                AppStorageVolumeInfo(
                                    isPrimary = true,
                                    isRemovable = false,
                                    stateCode = "mounted",
                                    isMounted = true,
                                    totalBytes = 256L * MEBIBYTE,
                                    availableBytes = 64L * MEBIBYTE,
                                ),
                            ),
                        capturedAt = capturedAt,
                    ),
                isInfoLoading = false,
                benchmarkPhase = StorageBenchmarkPhase.COMPLETED,
                benchmarkResult = storageBenchmarkResult(capturedAt),
            )
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity().copy(storage = storageState),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(EvidenceValue.LongValue(256L * MEBIBYTE), evidence.getValue("storage.total").value)
        assertEquals(EvidenceValue.DoubleValue(75.0), evidence.getValue("storage.usage").value)
        assertEquals(DiagnosticStatus.INFO, evidence.getValue("storage.sequential_write").status)
        assertEquals(Confidence.LOW, evidence.getValue("storage.sequential_write").confidence)
        assertEquals(EvidenceValue.DoubleValue(120.0), evidence.getValue("storage.sequential_write").value)
        assertEquals(EvidenceValue.DoubleValue(240.0), evidence.getValue("storage.sequential_read").value)
        assertEquals(EvidenceValue.BooleanValue(true), evidence.getValue("storage.benchmark_cleanup").value)
        assertEquals(
            EvidenceValue.StableTextCodeValue("app_cache"),
            evidence.getValue("storage.benchmark_location").value,
        )
    }

    @Test
    fun insufficientStorageSpaceProducesNotTestedEvidenceInsteadOfFailure() {
        val capturedAt = Instant.parse("2026-08-08T12:00:00Z")
        val state =
            StorageTestState(
                isInfoLoading = false,
                benchmarkPhase = StorageBenchmarkPhase.NOT_RUN,
                benchmarkResult =
                    storageBenchmarkResult(
                        capturedAt = capturedAt,
                        error = StorageBenchmarkErrorCode.INSUFFICIENT_SPACE,
                    ),
                benchmarkError = StorageBenchmarkErrorCode.INSUFFICIENT_SPACE,
            )
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity().copy(storage = state),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("storage.sequential_write").status)
        assertEquals(EvidenceReasonCode.INSUFFICIENT_SPACE, evidence.getValue("storage.sequential_write").reason)
        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("storage.sequential_read").status)
    }

    @Test
    fun vibrationApiSupportAndPhysicalConfirmationRemainSeparateEvidence() {
        val capturedAt = Instant.parse("2026-08-08T12:00:00Z")
        val vibration =
            VibrationTestState(
                haptic =
                    HapticCapabilityState(
                        hasVibrator = true,
                        hasAmplitudeControl = true,
                        effectsApiSupported = true,
                        supportedEffects = listOf(VibrationEffectCode.CLICK, VibrationEffectCode.TICK),
                        primitivesApiSupported = true,
                        supportedPrimitives = listOf(VibrationPrimitiveCode.CLICK),
                    ),
            )
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity().copy(vibration = vibration),
                    manual = ManualCheckResults(vibration = true),
                    permissions = RunAllPermissions(),
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(DiagnosticStatus.PASS, evidence.getValue("vibration.hardware").status)
        assertEquals(EvidenceValue.BooleanValue(true), evidence.getValue("vibration.amplitude_control").value)
        assertEquals(EvidenceValue.IntValue(2), evidence.getValue("vibration.effects").value)
        assertEquals(EvidenceValue.IntValue(1), evidence.getValue("vibration.primitives").value)
        assertEquals(EvidenceSource.USER_CONFIRMATION, evidence.getValue("vibration.motor").source)
        assertEquals(EvidenceValue.BooleanValue(true), evidence.getValue("vibration.motor").value)
    }

    @Test
    fun buttonKeyEventsAndPowerButtonBoundaryRemainSeparateEvidence() {
        val capturedAt = Instant.parse("2026-08-08T12:00:00Z")
        val buttons =
            ButtonTestState(
                volumeUpDetected = true,
                volumeDownDetected = true,
                phase = ButtonTestPhase.COMPLETED,
            )
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity().copy(buttons = buttons),
                    manual = ManualCheckResults(buttons = true),
                    permissions = RunAllPermissions(),
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(DiagnosticStatus.PASS, evidence.getValue("buttons.volume").status)
        assertEquals(EvidenceSource.AUTOMATIC_MEASUREMENT, evidence.getValue("buttons.volume").source)
        assertEquals(EvidenceValue.BooleanValue(true), evidence.getValue("buttons.volume").value)
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.getValue("buttons.power").status)
        assertEquals(Applicability.NOT_APPLICABLE, evidence.getValue("buttons.power").applicability)
        assertEquals(EvidenceReasonCode.PLATFORM_RESTRICTION, evidence.getValue("buttons.power").reason)
    }

    @Test
    fun buttonTimeoutIsNotReportedAsPhysicalFailure() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            buttons = ButtonTestState(phase = ButtonTestPhase.TIMED_OUT),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("buttons.volume").status)
        assertEquals(EvidenceReasonCode.TIMEOUT, evidence.getValue("buttons.volume").reason)
    }

    @Test
    fun biometricHardwareCapabilityAndSuccessfulPromptRemainSeparateEvidence() {
        val biometrics =
            BiometricTestState(
                capability =
                    BiometricCapability(
                        fingerprintHardware = true,
                        faceHardware = false,
                        strongStatus = BiometricAvailability.NONE_ENROLLED,
                        weakStatus = BiometricAvailability.AVAILABLE,
                    ),
                authResult = AuthResult.SUCCESS,
            )
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity().copy(biometrics = biometrics),
                    manual = ManualCheckResults(biometrics = true),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(EvidenceValue.BooleanValue(true), evidence.getValue("biometrics.fingerprint_hardware").value)
        assertEquals(EvidenceValue.BooleanValue(false), evidence.getValue("biometrics.face_hardware").value)
        assertEquals(
            EvidenceValue.StableTextCodeValue("none_enrolled"),
            evidence.getValue("biometrics.strong_capability").value,
        )
        assertEquals(DiagnosticStatus.PASS, evidence.getValue("biometrics.authentication").status)
        assertEquals(EvidenceSource.ANDROID_API, evidence.getValue("biometrics.authentication").source)
    }

    @Test
    fun biometricLockoutDoesNotBecomeSensorFailure() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            biometrics =
                                BiometricTestState(
                                    capability =
                                        BiometricCapability(
                                            fingerprintHardware = true,
                                            weakStatus = BiometricAvailability.AVAILABLE,
                                        ),
                                    authResult = AuthResult.LOCKED_OUT,
                                ),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("biometrics.authentication").status)
        assertEquals(EvidenceReasonCode.BIOMETRIC_LOCKOUT, evidence.getValue("biometrics.authentication").reason)
    }

    @Test
    fun preflightSkipsRemainDistinctFromPermissionDenialAndAbsentHardware() {
        val capturedAt = Instant.parse("2026-08-08T12:00:00Z")
        val skipped =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    selections =
                        RunAllSelections(
                            includeSpeaker = false,
                            includeMicrophone = false,
                            includeCamera = false,
                            includeStorageBenchmark = false,
                        ),
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(EvidenceReasonCode.SKIPPED, skipped.getValue("audio.speaker").reason)
        assertEquals(EvidenceReasonCode.SKIPPED, skipped.getValue("audio.microphone").reason)
        assertEquals(EvidenceReasonCode.SKIPPED, skipped.getValue("camera.capture").reason)
        assertEquals(EvidenceReasonCode.SKIPPED, skipped.getValue("storage.sequential_write").reason)

        val denied =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    selections = RunAllSelections(),
                    hardware = RunAllHardwareProfile.ALL_AVAILABLE,
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }
        assertEquals(EvidenceReasonCode.PERMISSION_DENIED, denied.getValue("audio.microphone").reason)
        assertEquals(EvidenceReasonCode.PERMISSION_DENIED, denied.getValue("camera.capture").reason)

        val absent =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    selections = RunAllSelections(),
                    hardware = RunAllHardwareProfile(),
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }
        assertEquals(EvidenceReasonCode.HARDWARE_UNAVAILABLE, absent.getValue("audio.microphone").reason)
        assertEquals(Applicability.NOT_APPLICABLE, absent.getValue("camera.capture").applicability)
    }

    @Test
    fun orchestrationOutcomesPreserveTimeoutSkipAndUnavailableReasons() {
        val capturedAt = Instant.parse("2026-08-08T12:00:00Z")
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
                    manual =
                        ManualCheckResults(
                            outcomes =
                                mapOf(
                                    RunAllStage.DISPLAY to RunAllStageOutcome.TIMED_OUT,
                                    RunAllStage.AUDIO to RunAllStageOutcome.SKIPPED,
                                    RunAllStage.CAMERA to RunAllStageOutcome.UNAVAILABLE,
                                ),
                        ),
                    permissions =
                        RunAllPermissions(
                            microphone = true,
                            camera = true,
                            location = true,
                            phone = true,
                            bluetooth = true,
                        ),
                    hardware = RunAllHardwareProfile.ALL_AVAILABLE,
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(EvidenceReasonCode.TIMEOUT, evidence.getValue("display.visual").reason)
        assertEquals(EvidenceReasonCode.SKIPPED, evidence.getValue("audio.speaker").reason)
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.getValue("camera.capture").status)
        assertEquals(EvidenceReasonCode.HARDWARE_UNAVAILABLE, evidence.getValue("camera.capture").reason)
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
            thermal = ThermalTestState(),
            storage = StorageTestState(),
            vibration = VibrationTestState(),
            buttons = ButtonTestState(),
            biometrics = BiometricTestState(),
        )

    private fun storageBenchmarkResult(
        capturedAt: Instant,
        error: StorageBenchmarkErrorCode? = null,
    ) = StorageBenchmarkResult(
        writeMebibytesPerSecond = 120.0.takeIf { error == null },
        readMebibytesPerSecond = 240.0.takeIf { error == null },
        bytesWritten = if (error == null) 64L * MEBIBYTE else 0L,
        bytesRead = if (error == null) 64L * MEBIBYTE else 0L,
        checksumCrc32 = if (error == null) 42L else 0L,
        durationMillis = 1_000L,
        dataSizeBytes = 64L * MEBIBYTE,
        bufferSizeBytes = 64 * 1_024,
        availableBeforeBytes = 128L * MEBIBYTE,
        cleanupSucceeded = true,
        capturedAt = capturedAt,
        error = error,
    )

    private companion object {
        const val MEBIBYTE = 1_048_576
    }

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
