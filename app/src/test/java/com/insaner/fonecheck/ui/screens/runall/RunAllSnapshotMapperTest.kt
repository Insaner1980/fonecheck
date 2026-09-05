package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
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
import com.insaner.fonecheck.domain.model.SimActivityCode
import com.insaner.fonecheck.domain.model.SimFormFactorCode
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimSlotInfo
import com.insaner.fonecheck.domain.model.SimSlotStateCode
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
import com.insaner.fonecheck.domain.model.TelephonyHardwareCode
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.localization.evidenceLabelResource
import com.insaner.fonecheck.testing.testDeviceInfo
import com.insaner.fonecheck.testing.testStorageBenchmarkResult
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
import com.insaner.fonecheck.ui.screens.camera.CaptureResult
import com.insaner.fonecheck.ui.screens.connectivity.BluetoothAccessCode
import com.insaner.fonecheck.ui.screens.connectivity.BluetoothState
import com.insaner.fonecheck.ui.screens.connectivity.ConnectivityTestState
import com.insaner.fonecheck.ui.screens.connectivity.GpsFailureCode
import com.insaner.fonecheck.ui.screens.connectivity.GpsFixStatus
import com.insaner.fonecheck.ui.screens.connectivity.GpsState
import com.insaner.fonecheck.ui.screens.connectivity.MobileNetworkState
import com.insaner.fonecheck.ui.screens.connectivity.NfcState
import com.insaner.fonecheck.ui.screens.connectivity.WifiState
import com.insaner.fonecheck.ui.screens.display.DisplayInfoState
import com.insaner.fonecheck.ui.screens.display.DisplayTestState
import com.insaner.fonecheck.ui.screens.performance.BenchmarkPhase
import com.insaner.fonecheck.ui.screens.sensor.GuidedSensorCatalog
import com.insaner.fonecheck.ui.screens.sensor.GuidedSensorCode
import com.insaner.fonecheck.ui.screens.sensor.GuidedSensorStatus
import com.insaner.fonecheck.ui.screens.sensor.InteractiveChallenge
import com.insaner.fonecheck.ui.screens.sensor.SensorTestState
import com.insaner.fonecheck.ui.screens.sensor.SensorType
import com.insaner.fonecheck.ui.screens.storage.AppStorageVolumeInfo
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkErrorCode
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkPhase
import com.insaner.fonecheck.ui.screens.storage.StorageInfo
import com.insaner.fonecheck.ui.screens.storage.StorageTestState
import com.insaner.fonecheck.ui.screens.thermal.ThermalSeverityCode
import com.insaner.fonecheck.ui.screens.thermal.ThermalTestState
import com.insaner.fonecheck.ui.screens.vibration.HapticCapabilityState
import com.insaner.fonecheck.ui.screens.vibration.VibrationCapabilityRead
import com.insaner.fonecheck.ui.screens.vibration.VibrationEffectCode
import com.insaner.fonecheck.ui.screens.vibration.VibrationPrimitiveCode
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

@Suppress("LargeClass") // Mirrors the canonical mapper's complete category contract in one suite.
class RunAllSnapshotMapperTest {
    @Test
    fun cameraAndUserResponseTimesSurviveLaterAssemblyAndLegacyImageCountsRemainHonest() {
        val cameraAt = Instant.ofEpochMilli(400L)
        val responses =
            mapOf(
                RunAllStage.DISPLAY to Instant.ofEpochMilli(100L),
                RunAllStage.AUDIO to Instant.ofEpochMilli(200L),
                RunAllStage.VIBRATION to Instant.ofEpochMilli(500L),
            )
        val evidence =
            mappedEvidence(
                snapshots =
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        camera = CameraTestState(lastCapture = CaptureResult(1920, 1080, cameraAt.toEpochMilli())),
                        vibration = VibrationTestState(haptic = HapticCapabilityState(hasVibrator = true)),
                    ),
                manual =
                    ManualCheckResults(
                        display = true,
                        speaker = false,
                        vibration = true,
                        cameraCompleted = true,
                        completedAt = responses + (RunAllStage.CAMERA to cameraAt),
                    ),
                permissions = RunAllPermissions(camera = true),
            )
        assertEquals(cameraAt, evidence.getValue("camera.capture").capturedAt)
        assertEquals(cameraAt, evidence.getValue("camera.capture_dimensions").capturedAt)
        assertEquals(responses[RunAllStage.DISPLAY], evidence.getValue("display.visual").capturedAt)
        assertEquals(responses[RunAllStage.AUDIO], evidence.getValue("audio.speaker").capturedAt)
        assertEquals(responses[RunAllStage.VIBRATION], evidence.getValue("vibration.motor").capturedAt)
        val dimensions = evidence.getValue("camera.capture_dimensions")
        assertEquals(EvidenceValue.RawTextValue("1920 × 1080"), dimensions.value)
        assertEquals(
            com.insaner.fonecheck.R.string.camera_last_image_dimensions,
            evidenceLabelResource(dimensions)?.stringResId,
        )
        val legacy = dimensions.copy(value = EvidenceValue.LongValue(2073600L))
        assertEquals(
            com.insaner.fonecheck.R.string.camera_last_image_pixel_count,
            evidenceLabelResource(legacy)?.stringResId,
        )
    }

    @Test
    fun thermalSnapshotPreservesIndividualReadingTimes() {
        val statusAt = Instant.ofEpochMilli(5_000L)
        val readingAt = Instant.ofEpochMilli(1_000L)
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
                                    headroom = 0.5f,
                                    batteryTemperatureCelsius = 30f,
                                    capturedAt = statusAt,
                                    headroomReadAt = readingAt,
                                    batteryTemperatureReadAt = readingAt,
                                ),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.ofEpochMilli(9_000L),
                ).single { it.categoryId == DiagnosticCategoryId.THERMAL }
                .evidence
                .associateBy { it.checkId.value }
        assertEquals(statusAt, evidence.getValue("thermal.status").capturedAt)
        assertEquals(statusAt, evidence.getValue("thermal.severity").capturedAt)
        assertEquals(readingAt, evidence.getValue("thermal.headroom").capturedAt)
        assertEquals(readingAt, evidence.getValue("thermal.battery_temperature").capturedAt)
    }

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
                .filterNot {
                    it.categoryId == DiagnosticCategoryId.DEVICE ||
                        it.categoryId == DiagnosticCategoryId.PERFORMANCE
                }.flatMap { it.evidence }
                .all { it.capturedAt == capturedAt },
        )
        assertTrue(
            snapshots.flatMap { it.evidence }.all { evidence ->
                evidence.checkId.value.startsWith("${evidence.categoryId.stableId}.")
            },
        )
        assertEquals(
            emptyList<String>(),
            snapshots
                .flatMap { it.evidence }
                .map { it.checkId.value }
                .filter { evidenceLabelResource(it) == null },
        )

        val persistedValues =
            snapshots
                .flatMap { it.evidence }
                .mapNotNull { it.value }
                .map { it.testText() }
        assertFalse(sensitiveValues.any { sensitive -> persistedValues.any { sensitive in it } })
    }

    @Test
    fun missingAutomaticProbeDataProducesACompleteReportWithTruthfulReasons() {
        val snapshots =
            diagnosticSnapshotsWithSensitiveConnectivity().copy(
                device = null,
                performance = null,
                sim = null,
                automaticIssues =
                    mapOf(
                        DiagnosticCategoryId.DEVICE to RunAllStageOutcome.TIMED_OUT,
                        DiagnosticCategoryId.PERFORMANCE to RunAllStageOutcome.ERROR,
                        DiagnosticCategoryId.SIM to RunAllStageOutcome.TIMED_OUT,
                        DiagnosticCategoryId.AUDIO to RunAllStageOutcome.TIMED_OUT,
                        DiagnosticCategoryId.STORAGE to RunAllStageOutcome.TIMED_OUT,
                        DiagnosticCategoryId.CONNECTIVITY to RunAllStageOutcome.ERROR,
                    ),
            )
        val results =
            RunAllSnapshotMapper.map(
                snapshots = snapshots,
                manual = ManualCheckResults(),
                permissions = RunAllPermissions(microphone = true),
                capturedAt = Instant.parse("2026-08-07T12:00:30Z"),
            )
        val evidence = results.flatMap { it.evidence }.associateBy { it.checkId.value }

        assertEquals(DiagnosticCatalog.categories, results.map { it.categoryId })
        listOf("device.identity", "device.security", "device.developer_options", "device.usb_debugging")
            .forEach { id ->
                assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue(id).status)
                assertEquals(EvidenceReasonCode("measurement_timeout"), evidence.getValue(id).reason)
            }
        listOf("performance.cpu", "performance.ram", "performance.gpu").forEach { id ->
            assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue(id).status)
            assertEquals(EvidenceReasonCode("measurement_error"), evidence.getValue(id).reason)
        }
        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("sim.inventory").status)
        assertEquals(EvidenceReasonCode("measurement_timeout"), evidence.getValue("sim.inventory").reason)
        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("sim.network").status)
        assertEquals(EvidenceReasonCode.PERMISSION_DENIED, evidence.getValue("sim.network").reason)
        listOf(
            "audio.microphone",
            "storage.total",
            "storage.used",
            "storage.available",
            "storage.usage",
            "storage.internal_access",
            "storage.volume_count",
            "storage.mounted_volume_count",
            "storage.removable_volume_count",
            "storage.sequential_write",
            "storage.sequential_read",
        ).forEach { id ->
            assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue(id).status)
            assertEquals(EvidenceReasonCode("measurement_timeout"), evidence.getValue(id).reason)
        }
        assertConnectivityMeasurementError(evidence)
    }

    @Test
    fun enclosingAutomaticFailureAppliesToConnectivityEvidence() {
        val evidence =
            mappedEvidence(
                snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
                manual =
                    ManualCheckResults(
                        outcomes = mapOf(RunAllStage.AUTOMATIC to RunAllStageOutcome.ERROR),
                    ),
            )

        assertConnectivityMeasurementError(evidence)
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
        assertEquals(EvidenceReasonCode("user_confirmed_audio_failure"), evidence.getValue("audio.speaker").reason)
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
            mappedEvidence(
                snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
                capturedAt = Instant.parse("2026-08-07T12:00:30Z"),
            ).getValue("device.security")

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
            mappedEvidence(
                snapshots = snapshots.copy(device = deviceInfo(rootArtifactDetected = true)),
                capturedAt = Instant.parse("2026-08-07T12:00:30Z"),
            ).getValue("device.security")

        assertEquals(DiagnosticStatus.WARNING, evidence.status)
        assertEquals(Confidence.LOW, evidence.confidence)
        assertEquals(EvidenceReasonCode("root_artifact_present"), evidence.reason)
        assertEquals(EvidenceValue.StableTextCodeValue("known_artifact_detected"), evidence.value)
    }

    @Test
    fun developerOptionsAndUsbDebuggingUseTheSameNotedClassificationAsDeviceDetails() {
        val evidence =
            mappedEvidence(
                snapshots =
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        device =
                            deviceInfo().copy(
                                developerOptionsEnabled = true,
                                usbDebuggingEnabled = true,
                            ),
                    ),
            )

        assertEquals(DiagnosticStatus.WARNING, evidence.getValue("device.developer_options").status)
        assertEquals(
            EvidenceReasonCode("developer_options_enabled"),
            evidence.getValue("device.developer_options").reason,
        )
        assertEquals(DiagnosticStatus.WARNING, evidence.getValue("device.usb_debugging").status)
        assertEquals(
            EvidenceReasonCode("usb_debugging_enabled"),
            evidence.getValue("device.usb_debugging").reason,
        )
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

        assertEquals(DiagnosticStatus.NOT_TESTED, deniedEvidence.getValue("sim.inventory").status)
        assertEquals(EvidenceReasonCode("sim_inactive"), deniedEvidence.getValue("sim.inventory").reason)
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
    fun simUnknownPinAndCardIoStatesKeepMeasurementAndFaultSemanticsSeparate() {
        val unknown =
            mappedEvidence(
                snapshots =
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        sim = simInfo(SimInventoryCode.UNKNOWN),
                    ),
            ).getValue("sim.inventory")
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, unknown.status)
        assertEquals(EvidenceReasonCode("sim_inventory_unknown"), unknown.reason)

        val slots =
            listOf(
                simSlot(slotIndex = 0, state = SimSlotStateCode.PIN_REQUIRED),
                simSlot(slotIndex = 1, state = SimSlotStateCode.CARD_IO_ERROR),
            )
        val slotEvidence =
            mappedEvidence(
                snapshots =
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        sim =
                            simInfo(SimInventoryCode.MULTIPLE_SIM).copy(
                                simSlots = slots,
                                phoneCount = slots.size,
                            ),
                    ),
            )
        assertEquals(DiagnosticStatus.NOT_TESTED, slotEvidence.getValue("sim.slot_0_state").status)
        assertEquals(EvidenceReasonCode("sim_pin_required"), slotEvidence.getValue("sim.slot_0_state").reason)
        assertEquals(DiagnosticStatus.FAIL, slotEvidence.getValue("sim.slot_1_state").status)
        assertEquals(EvidenceReasonCode("sim_card_io_error"), slotEvidence.getValue("sim.slot_1_state").reason)
    }

    @Test
    fun unknownNetworkReadingIsUnavailableEvenWhenPhonePermissionIsGranted() {
        val network =
            mappedEvidence(
                snapshots =
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        sim =
                            simInfo(SimInventoryCode.SINGLE_SIM).copy(
                                dataNetworkType = NetworkGenerationCode.UNKNOWN,
                                phoneStatePermissionGranted = true,
                            ),
                    ),
                permissions = RunAllPermissions(phone = true),
            ).getValue("sim.network")

        assertEquals(DiagnosticStatus.NOT_AVAILABLE, network.status)
        assertEquals(EvidenceReasonCode("value_not_exposed"), network.reason)
        assertEquals(null, network.value)
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
        assertEquals(EvidenceSource.AUTOMATIC_MEASUREMENT, evidence.source)
        assertEquals(EvidenceValue.BooleanValue(true), evidence.value)
    }

    @Test
    fun performanceBenchmarkProducesInformationalRawEvidence() {
        val infoCapturedAt = Instant.parse("2026-08-07T12:00:00Z")
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

        listOf("performance.cpu", "performance.ram", "performance.gpu").forEach { checkId ->
            assertEquals(infoCapturedAt, evidence.getValue(checkId).capturedAt)
        }
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
    fun missingPerformanceBenchmarkUsesCanonicalReasonForItsPhase() {
        mapOf(
            BenchmarkPhase.IDLE to EvidenceReasonCode("test_not_run"),
            BenchmarkPhase.CANCELLED to EvidenceReasonCode("test_cancelled"),
            BenchmarkPhase.ERROR to EvidenceReasonCode("measurement_error"),
        ).forEach { (phase, expectedReason) ->
            val evidence =
                mappedEvidence(
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        performanceBenchmark = null,
                        performanceBenchmarkPhase = phase,
                    ),
                )

            assertEquals(expectedReason, evidence.getValue("performance.cpu_benchmark").reason)
            assertEquals(expectedReason, evidence.getValue("performance.memory_benchmark").reason)
            assertEquals(
                EvidenceSource.AUTOMATIC_MEASUREMENT,
                evidence.getValue("performance.cpu_benchmark").source,
            )
            assertEquals(
                EvidenceSource.AUTOMATIC_MEASUREMENT,
                evidence.getValue("performance.memory_benchmark").source,
            )
        }
    }

    @Test
    fun missingPlatformReadingsAreNotMeasuredAndDoNotPublishBadPlaceholderValues() {
        val evidence =
            mappedEvidence(
                snapshots =
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        performance =
                            performanceInfo().copy(
                                cpuCores = 0,
                                totalRamBytes = null,
                                glRenderer = "",
                            ),
                        display = DisplayTestState(info = DisplayInfoState(widthPx = 0, heightPx = 0)),
                    ),
            )

        mapOf(
            "performance.cpu" to "cpu_reading_unavailable",
            "performance.ram" to "ram_reading_unavailable",
            "performance.gpu" to "gpu_reading_unavailable",
            "display.info" to "display_reading_unavailable",
        ).forEach { (id, reason) ->
            assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.getValue(id).status)
            assertEquals(EvidenceReasonCode(reason), evidence.getValue(id).reason)
            assertEquals(null, evidence.getValue(id).value)
        }
    }

    @Test
    fun sensorSnapshotPreservesUnreliableResponseWithoutAddingCoverageItems() {
        val snapshots = diagnosticSnapshotsWithSensitiveConnectivity()
        val guided =
            GuidedSensorCatalog.create(setOf(SensorType.ACCELEROMETER)).map {
                if (it.code == GuidedSensorCode.ACCELEROMETER) {
                    it.copy(
                        status = GuidedSensorStatus.PASSED,
                        sampleCount = 5,
                        accuracy = com.insaner.fonecheck.ui.screens.sensor.SensorAccuracyCode.UNRELIABLE,
                    )
                } else {
                    it
                }
            }
        val report =
            RunAllSnapshotMapper
                .map(
                    snapshots = snapshots.copy(sensors = SensorTestState(guidedTests = guided, sensorCount = 1)),
                    manual = ManualCheckResults(sensorsCompleted = true),
                    permissions = RunAllPermissions(),
                    capturedAt = Instant.EPOCH,
                ).single { it.categoryId == DiagnosticCategoryId.SENSORS }
        assertEquals(11, report.evidence.size)
        listOf("sensors.accelerometer", "sensors.motion").forEach { id ->
            val item = report.evidence.single { it.checkId.value == id }
            assertEquals(DiagnosticStatus.PASS, item.status)
            assertEquals(Confidence.LOW, item.confidence)
            assertEquals(EvidenceReasonCode("sensor_response_unreliable"), item.reason)
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
                    manual = ManualCheckResults(sensorsCompleted = true),
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
        assertEquals(EvidenceReasonCode("test_skipped"), evidence.getValue("sensors.light").reason)
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.getValue("sensors.gyroscope").status)
        assertEquals(EvidenceSource.DERIVED, evidence.getValue("sensors.orientation").source)
        assertEquals(DiagnosticStatus.INFO, evidence.getValue("sensors.orientation").status)
        assertEquals(EvidenceSource.AUTOMATIC_MEASUREMENT, evidence.getValue("sensors.motion").source)
    }

    @Test
    fun absentMotionHardwareDoesNotReduceSensorCoverage() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(),
                    hardware = RunAllHardwareProfile.ALL_AVAILABLE.copy(motionSensorAvailable = false),
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).single { it.categoryId == DiagnosticCategoryId.SENSORS }
                .evidence
                .associateBy { it.checkId.value }

        listOf("sensors.orientation", "sensors.motion").forEach { checkId ->
            assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.getValue(checkId).status)
            assertEquals(Applicability.NOT_APPLICABLE, evidence.getValue(checkId).applicability)
        }
    }

    @Test
    fun cameraAndSensorApiErrorsAreMeasurementFailuresInsteadOfDeviceFaults() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            camera = CameraTestState(error = "camera_operation_failed"),
                            sensors = SensorTestState(error = "sensor_listener_error"),
                        ),
                    manual = ManualCheckResults(),
                    permissions = RunAllPermissions(camera = true),
                    hardware = RunAllHardwareProfile.ALL_AVAILABLE,
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("camera.capture").status)
        assertEquals(EvidenceReasonCode("camera_measurement_error"), evidence.getValue("camera.capture").reason)
        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("sensors.motion").status)
        assertEquals(EvidenceReasonCode("sensor_measurement_error"), evidence.getValue("sensors.motion").reason)
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
    fun gpsTimeoutIsNotMeasuredInsteadOfAHardwareFailureClaim() {
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

        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.status)
        assertEquals(Confidence.UNAVAILABLE, evidence.confidence)
        assertEquals(EvidenceReasonCode("gps_timeout"), evidence.reason)
    }

    @Test
    fun gpsDisabledAndStartFailureAreNotMeasuredWithActionableReasons() {
        val base = diagnosticSnapshotsWithSensitiveConnectivity()
        val unavailable =
            mappedEvidence(
                snapshots =
                    base.copy(
                        connectivity = ConnectivityTestState(gps = GpsState(isAvailable = false)),
                    ),
            ).getValue("connectivity.gps")
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, unavailable.status)
        assertEquals(Confidence.UNAVAILABLE, unavailable.confidence)
        assertEquals(null, unavailable.value)

        val disabled =
            mappedEvidence(
                snapshots =
                    base.copy(
                        connectivity =
                            ConnectivityTestState(
                                gps = GpsState(isAvailable = true, isEnabled = false),
                            ),
                    ),
            ).getValue("connectivity.gps")
        assertEquals(DiagnosticStatus.NOT_TESTED, disabled.status)
        assertEquals(EvidenceReasonCode("gps_disabled"), disabled.reason)
        assertEquals(EvidenceValue.BooleanValue(false), disabled.value)

        val startFailed =
            mappedEvidence(
                snapshots =
                    base.copy(
                        connectivity =
                            ConnectivityTestState(
                                gps =
                                    GpsState(
                                        isAvailable = true,
                                        isEnabled = true,
                                        fixStatus = GpsFixStatus.FAILED,
                                        failure = GpsFailureCode.START_FAILED,
                                    ),
                            ),
                    ),
            ).getValue("connectivity.gps")
        assertEquals(DiagnosticStatus.NOT_TESTED, startFailed.status)
        assertEquals(EvidenceReasonCode("gps_start_failed"), startFailed.reason)
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
        assertEquals(EvidenceReasonCode("battery_health_unavailable"), evidence.getValue("battery.health").reason)
        assertEquals(
            EvidenceReasonCode("battery_temperature_unavailable"),
            evidence.getValue("battery.temperature").reason,
        )
        assertEquals(EvidenceReasonCode("value_not_exposed"), evidence.getValue("battery.level").reason)
        assertEquals(EvidenceReasonCode("value_not_exposed"), evidence.getValue("battery.current_now").reason)
        assertEquals(
            EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED,
            evidence.getValue("battery.cycle_count").reason,
        )
    }

    @Test
    fun nonFiniteBatteryTemperatureIsUnavailableWithoutAStoredValue() {
        val evidence =
            mappedEvidence(
                snapshots =
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        battery =
                            BatteryTestState(
                                basic = BasicBatteryState(temperatureCelsius = Float.NaN),
                            ),
                    ),
            ).getValue("battery.temperature")

        assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.status)
        assertEquals(Confidence.UNAVAILABLE, evidence.confidence)
        assertEquals(EvidenceReasonCode("battery_temperature_unavailable"), evidence.reason)
        assertEquals(null, evidence.value)
        assertEquals(null, evidence.unit)
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
        assertEquals(DiagnosticStatus.INFO, evidence.getValue("battery.health").status)
    }

    @Test
    fun batteryFaultAndCurrentTemperatureNoteRemainDistinct() {
        val evidence =
            mappedEvidence(
                snapshots =
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        battery =
                            BatteryTestState(
                                basic =
                                    BasicBatteryState(
                                        temperatureCelsius = 50f,
                                        healthStatus = android.os.BatteryManager.BATTERY_HEALTH_GOOD,
                                    ),
                                health =
                                    HealthState(
                                        healthStatusRaw = android.os.BatteryManager.BATTERY_HEALTH_DEAD,
                                    ),
                            ),
                    ),
            )

        assertEquals(DiagnosticStatus.FAIL, evidence.getValue("battery.health").status)
        assertEquals(EvidenceReasonCode("battery_dead"), evidence.getValue("battery.health").reason)
        assertEquals(DiagnosticStatus.WARNING, evidence.getValue("battery.temperature").status)
        assertEquals(
            EvidenceReasonCode("battery_temperature_critical"),
            evidence.getValue("battery.temperature").reason,
        )
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
    fun thermalHeadroomIsInformationalAndSevereStatusIsProminentNoted() {
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

        assertEquals(DiagnosticStatus.WARNING, evidence.getValue("thermal.status").status)
        assertEquals(
            EvidenceReasonCode("thermal_severe_without_app_load"),
            evidence.getValue("thermal.status").reason,
        )
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
        assertFalse(evidence.containsKey("storage.benchmark_cleanup"))
        assertEquals(
            EvidenceValue.StableTextCodeValue("app_cache"),
            evidence.getValue("storage.benchmark_location").value,
        )
    }

    @Test
    fun failedTemporaryFileCleanupDoesNotBecomeDeviceEvidence() {
        val capturedAt = Instant.parse("2026-08-08T12:00:00Z")
        val result =
            storageBenchmarkResult(capturedAt).copy(
                cleanupSucceeded = false,
                error = StorageBenchmarkErrorCode.CLEANUP_FAILED,
            )
        val evidence =
            mappedEvidence(
                snapshots =
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        storage =
                            StorageTestState(
                                benchmarkPhase = StorageBenchmarkPhase.ERROR,
                                benchmarkResult = result,
                                benchmarkError = StorageBenchmarkErrorCode.CLEANUP_FAILED,
                            ),
                    ),
                capturedAt = capturedAt,
            )

        assertEquals(DiagnosticStatus.INFO, evidence.getValue("storage.sequential_write").status)
        assertFalse(evidence.containsKey("storage.benchmark_cleanup"))
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

        assertEquals(DiagnosticStatus.INFO, evidence.getValue("vibration.hardware").status)
        assertEquals(EvidenceValue.BooleanValue(true), evidence.getValue("vibration.amplitude_control").value)
        assertEquals(EvidenceValue.IntValue(2), evidence.getValue("vibration.effects").value)
        assertEquals(EvidenceValue.IntValue(1), evidence.getValue("vibration.primitives").value)
        assertEquals(EvidenceSource.USER_CONFIRMATION, evidence.getValue("vibration.motor").source)
        assertEquals(EvidenceValue.BooleanValue(true), evidence.getValue("vibration.motor").value)
    }

    @Test
    fun vibrationCapabilityReadFailureDoesNotBecomeAbsentOrSupportedEvidence() {
        val capturedAt = Instant.parse("2026-08-08T12:00:00Z")
        val vibration =
            VibrationTestState(
                haptic =
                    HapticCapabilityState(
                        readErrors = setOf(VibrationCapabilityRead.HARDWARE),
                    ),
            )
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots = diagnosticSnapshotsWithSensitiveConnectivity().copy(vibration = vibration),
                    manual = ManualCheckResults(outcomes = mapOf(RunAllStage.VIBRATION to RunAllStageOutcome.ERROR)),
                    permissions = RunAllPermissions(),
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .filter { it.categoryId == DiagnosticCategoryId.VIBRATION }

        assertEquals(
            setOf(
                "vibration.hardware",
                "vibration.amplitude_control",
                "vibration.effects",
                "vibration.primitives",
                "vibration.motor",
            ),
            evidence.mapTo(mutableSetOf()) { it.checkId.value },
        )
        evidence.forEach {
            assertEquals(DiagnosticStatus.NOT_TESTED, it.status)
            assertEquals(EvidenceReasonCode("measurement_error"), it.reason)
            assertEquals(Applicability.APPLICABLE, it.applicability)
        }

        val partialEvidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            vibration =
                                VibrationTestState(
                                    haptic =
                                        HapticCapabilityState(
                                            hasVibrator = true,
                                            effectsApiSupported = true,
                                            primitivesApiSupported = true,
                                            readErrors = setOf(VibrationCapabilityRead.EFFECTS),
                                        ),
                                ),
                        ),
                    manual = ManualCheckResults(vibration = true),
                    permissions = RunAllPermissions(),
                    capturedAt = capturedAt,
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        assertEquals(DiagnosticStatus.INFO, partialEvidence.getValue("vibration.hardware").status)
        assertEquals(DiagnosticStatus.NOT_TESTED, partialEvidence.getValue("vibration.effects").status)
        assertEquals(EvidenceReasonCode("measurement_error"), partialEvidence.getValue("vibration.effects").reason)
        assertEquals(EvidenceValue.IntValue(0), partialEvidence.getValue("vibration.primitives").value)
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
            mappedEvidence(
                snapshots =
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        buttons = ButtonTestState(phase = ButtonTestPhase.TIMED_OUT),
                    ),
            )

        assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue("buttons.volume").status)
        assertEquals(EvidenceReasonCode("button_test_timeout"), evidence.getValue("buttons.volume").reason)
        assertEquals(Confidence.UNAVAILABLE, evidence.getValue("buttons.volume").confidence)
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
                    manual = ManualCheckResults(),
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
        assertEquals(DiagnosticStatus.INFO, evidence.getValue("biometrics.capability").status)
        assertEquals(DiagnosticStatus.INFO, evidence.getValue("biometrics.weak_capability").status)
        assertEquals(DiagnosticStatus.PASS, evidence.getValue("biometrics.authentication").status)
        assertEquals(EvidenceSource.ANDROID_API, evidence.getValue("biometrics.authentication").source)
    }

    @Test
    fun automaticPlatformFactsRemainInformational() {
        val evidence =
            mappedEvidence(
                snapshots =
                    diagnosticSnapshotsWithSensitiveConnectivity().copy(
                        display =
                            DisplayTestState(
                                info = DisplayInfoState(widthPx = 1080, heightPx = 2400),
                            ),
                    ),
            )

        listOf("performance.cpu", "performance.ram", "performance.gpu", "display.info").forEach { id ->
            assertEquals(DiagnosticStatus.INFO, evidence.getValue(id).status)
            assertEquals(EvidenceSource.ANDROID_API, evidence.getValue(id).source)
        }
    }

    @Test
    fun cameraCaptureSourcesDistinguishAutomaticMeasurementFromPermissionState() {
        val snapshots = diagnosticSnapshotsWithSensitiveConnectivity()
        val completed =
            mappedEvidence(
                snapshots =
                    snapshots.copy(
                        camera =
                            CameraTestState(
                                lastCapture =
                                    CaptureResult(
                                        width = 1920,
                                        height = 1080,
                                        timestamp = 0L,
                                    ),
                            ),
                    ),
                manual = ManualCheckResults(cameraCompleted = true),
                permissions = RunAllPermissions(camera = true),
            )
        val denied =
            mappedEvidence(
                snapshots = snapshots,
                permissions = RunAllPermissions(camera = false),
            )

        assertEquals(EvidenceSource.AUTOMATIC_MEASUREMENT, completed.getValue("camera.capture").source)
        assertEquals(
            EvidenceSource.AUTOMATIC_MEASUREMENT,
            completed.getValue("camera.capture_dimensions").source,
        )
        assertEquals(EvidenceSource.ANDROID_API, denied.getValue("camera.capture").source)
    }

    @Test
    fun biometricLockoutDoesNotBecomeSensorFailure() {
        val evidence =
            mappedEvidence(
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
            )

        assertEquals(DiagnosticStatus.WARNING, evidence.getValue("biometrics.authentication").status)
        assertEquals(EvidenceReasonCode.BIOMETRIC_LOCKOUT, evidence.getValue("biometrics.authentication").reason)
    }

    @Test
    fun preflightSkipsRemainDistinctFromPermissionDenialAndAbsentHardware() {
        val capturedAt = Instant.parse("2026-08-08T12:00:00Z")
        val skipped =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            camera =
                                CameraTestState(
                                    lastCapture = CaptureResult(width = 640, height = 480, timestamp = 0L),
                                ),
                            storage =
                                StorageTestState(
                                    benchmarkPhase = StorageBenchmarkPhase.COMPLETED,
                                    benchmarkResult = storageBenchmarkResult(capturedAt),
                                ),
                        ),
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

        assertEquals(EvidenceReasonCode("test_skipped"), skipped.getValue("audio.speaker").reason)
        assertEquals(EvidenceReasonCode("test_skipped"), skipped.getValue("audio.microphone").reason)
        assertEquals(EvidenceSource.USER_CONFIRMATION, skipped.getValue("audio.microphone").source)
        assertEquals(EvidenceReasonCode("test_skipped"), skipped.getValue("camera.capture").reason)
        assertEquals(EvidenceReasonCode("test_skipped"), skipped.getValue("camera.capture_dimensions").reason)
        assertEquals(EvidenceSource.USER_CONFIRMATION, skipped.getValue("camera.capture_dimensions").source)
        assertEquals(EvidenceReasonCode("test_skipped"), skipped.getValue("storage.sequential_write").reason)
        assertEquals(EvidenceSource.USER_CONFIRMATION, skipped.getValue("storage.sequential_write").source)
        assertFalse(skipped.containsKey("storage.benchmark_data_size"))
        assertFalse(skipped.containsKey("storage.benchmark_location"))

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
        assertEquals(EvidenceReasonCode.PERMISSION_DENIED, denied.getValue("camera.capture_dimensions").reason)

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
        assertEquals(Applicability.NOT_APPLICABLE, absent.getValue("camera.capture_dimensions").applicability)
        assertEquals(EvidenceSource.ANDROID_API, absent.getValue("camera.capture").source)
        assertEquals(EvidenceSource.ANDROID_API, absent.getValue("vibration.motor").source)
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

        assertEquals(EvidenceReasonCode("measurement_timeout"), evidence.getValue("display.visual").reason)
        assertEquals(EvidenceReasonCode("test_skipped"), evidence.getValue("audio.speaker").reason)
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.getValue("camera.capture").status)
        assertEquals(EvidenceReasonCode.HARDWARE_UNAVAILABLE, evidence.getValue("camera.capture").reason)
        assertEquals(EvidenceSource.ANDROID_API, evidence.getValue("camera.capture").source)
        assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.getValue("camera.capture_dimensions").status)
        assertEquals(Applicability.NOT_APPLICABLE, evidence.getValue("camera.capture_dimensions").applicability)
    }

    @Test
    fun automaticTimeoutsKeepTheirReasonAndMeasurementSource() {
        val evidence =
            mappedEvidenceForOutcome(
                RunAllStageOutcome.TIMED_OUT,
                RunAllStage.AUDIO,
                RunAllStage.CAMERA,
            )

        listOf("audio.microphone", "camera.capture", "camera.capture_dimensions").forEach { id ->
            assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue(id).status)
            assertEquals(EvidenceReasonCode("measurement_timeout"), evidence.getValue(id).reason)
            assertEquals(EvidenceSource.AUTOMATIC_MEASUREMENT, evidence.getValue(id).source)
        }
    }

    @Test
    fun userSkippedStagesKeepTheirReasonAndUserSource() {
        val evidence =
            RunAllSnapshotMapper
                .map(
                    snapshots =
                        diagnosticSnapshotsWithSensitiveConnectivity().copy(
                            buttons = ButtonTestState(phase = ButtonTestPhase.SKIPPED),
                            biometrics = BiometricTestState(authResult = AuthResult.CANCELLED),
                        ),
                    manual =
                        ManualCheckResults(
                            outcomes =
                                mapOf(
                                    RunAllStage.SENSORS to RunAllStageOutcome.SKIPPED,
                                    RunAllStage.BUTTONS to RunAllStageOutcome.SKIPPED,
                                    RunAllStage.BIOMETRICS to RunAllStageOutcome.SKIPPED,
                                ),
                        ),
                    permissions = RunAllPermissions(),
                    hardware = RunAllHardwareProfile.ALL_AVAILABLE,
                    capturedAt = Instant.parse("2026-08-08T12:00:00Z"),
                ).flatMap { it.evidence }
                .associateBy { it.checkId.value }

        listOf("sensors.motion", "buttons.volume", "biometrics.authentication").forEach { id ->
            assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue(id).status)
            assertEquals(EvidenceReasonCode("test_skipped"), evidence.getValue(id).reason)
            assertEquals(EvidenceSource.USER_CONFIRMATION, evidence.getValue(id).source)
        }
    }

    @Test
    fun unavailableOrchestrationOutcomesAreNonApplicable() {
        val evidence =
            mappedEvidenceForOutcome(
                RunAllStageOutcome.UNAVAILABLE,
                RunAllStage.AUDIO,
                RunAllStage.CAMERA,
                RunAllStage.SENSORS,
                RunAllStage.BIOMETRICS,
            )

        listOf(
            "audio.speaker",
            "audio.microphone",
            "camera.capture",
            "camera.capture_dimensions",
            "sensors.motion",
            "biometrics.authentication",
        ).forEach { id ->
            assertEquals(DiagnosticStatus.NOT_AVAILABLE, evidence.getValue(id).status)
            assertEquals(Applicability.NOT_APPLICABLE, evidence.getValue(id).applicability)
            assertEquals(EvidenceSource.ANDROID_API, evidence.getValue(id).source)
        }
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
    ) = testStorageBenchmarkResult(120.0, 240.0, 128L * MEBIBYTE, capturedAt, error)

    private fun mappedEvidence(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults = ManualCheckResults(),
        permissions: RunAllPermissions = RunAllPermissions(),
        capturedAt: Instant = Instant.parse("2026-08-08T12:00:00Z"),
    ): Map<String, DiagnosticEvidence> =
        RunAllSnapshotMapper
            .map(
                snapshots = snapshots,
                manual = manual,
                permissions = permissions,
                capturedAt = capturedAt,
            ).flatMap { it.evidence }
            .associateBy { it.checkId.value }

    private fun mappedEvidenceForOutcome(
        outcome: RunAllStageOutcome,
        vararg stages: RunAllStage,
    ): Map<String, DiagnosticEvidence> =
        mappedEvidence(
            snapshots = diagnosticSnapshotsWithSensitiveConnectivity(),
            manual = ManualCheckResults(outcomes = stages.associateWith { outcome }),
            permissions =
                RunAllPermissions(
                    microphone = true,
                    camera = true,
                ),
        )

    private fun assertConnectivityMeasurementError(evidence: Map<String, DiagnosticEvidence>) {
        listOf(
            "connectivity.wifi",
            "connectivity.bluetooth",
            "connectivity.nfc",
            "connectivity.nfc_hce",
            "connectivity.gps",
            "connectivity.mobile",
        ).forEach { id ->
            assertEquals(DiagnosticStatus.NOT_TESTED, evidence.getValue(id).status)
            assertEquals(EvidenceReasonCode("measurement_error"), evidence.getValue(id).reason)
        }
    }

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

    private fun simSlot(
        slotIndex: Int,
        state: SimSlotStateCode,
    ) = SimSlotInfo(
        slotIndex = slotIndex,
        state = state,
        activity = SimActivityCode.ACTIVE,
        formFactor = SimFormFactorCode.PHYSICAL,
        operatorName = null,
        countryIso = null,
        networkType = NetworkGenerationCode.UNKNOWN,
    )

    private fun deviceInfo(rootArtifactDetected: Boolean = false) =
        testDeviceInfo(rootArtifactDetected = rootArtifactDetected)

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

    private fun EvidenceValue.testText(): String =
        when (this) {
            is EvidenceValue.BooleanValue -> value.toString()
            is EvidenceValue.IntValue -> value.toString()
            is EvidenceValue.LongValue -> value.toString()
            is EvidenceValue.DecimalValue -> value.toPlainString()
            is EvidenceValue.DoubleValue -> value.toString()
            is EvidenceValue.RawTextValue -> value
            is EvidenceValue.StableTextCodeValue -> value
        }
}
