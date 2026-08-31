package com.insaner.fonecheck.ui.screens.runall

import android.os.BatteryManager
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategorySnapshot
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticSnapshotVersion
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.PerformanceBenchmarkResult
import com.insaner.fonecheck.domain.model.PerformanceInfo
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.domain.observation.DeviceObservation
import com.insaner.fonecheck.domain.observation.DeviceObservationClassifier
import com.insaner.fonecheck.domain.observation.InteractiveCheck
import com.insaner.fonecheck.domain.observation.MeasurementKind
import com.insaner.fonecheck.domain.observation.MeasurementOutcome
import com.insaner.fonecheck.domain.observation.ObservationClassification
import com.insaner.fonecheck.domain.observation.ObservationReason
import com.insaner.fonecheck.domain.observation.ObservationState
import com.insaner.fonecheck.domain.observation.isUnusedSimSlot
import com.insaner.fonecheck.domain.observation.toDiagnosticStatus
import com.insaner.fonecheck.domain.observation.toEvidenceReasonCode
import com.insaner.fonecheck.ui.classification.classifyBatteryHealth
import com.insaner.fonecheck.ui.classification.classifyBiometric
import com.insaner.fonecheck.ui.classification.classifyBiometricCapability
import com.insaner.fonecheck.ui.classification.classifyButtonTest
import com.insaner.fonecheck.ui.classification.classifyGpsFix
import com.insaner.fonecheck.ui.screens.audio.AudioTestState
import com.insaner.fonecheck.ui.screens.battery.BatteryCurrentDirection
import com.insaner.fonecheck.ui.screens.battery.BatteryTestState
import com.insaner.fonecheck.ui.screens.battery.ManufacturerProfile
import com.insaner.fonecheck.ui.screens.biometrics.AuthResult
import com.insaner.fonecheck.ui.screens.biometrics.BiometricAvailability
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestState
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestPhase
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestState
import com.insaner.fonecheck.ui.screens.camera.CameraClassCode
import com.insaner.fonecheck.ui.screens.camera.CameraTestState
import com.insaner.fonecheck.ui.screens.connectivity.BluetoothAccessCode
import com.insaner.fonecheck.ui.screens.connectivity.ConnectivityTestState
import com.insaner.fonecheck.ui.screens.connectivity.GpsFailureCode
import com.insaner.fonecheck.ui.screens.connectivity.GpsFixStatus
import com.insaner.fonecheck.ui.screens.display.DisplayTestState
import com.insaner.fonecheck.ui.screens.sensor.GuidedSensorStatus
import com.insaner.fonecheck.ui.screens.sensor.InteractiveChallenge
import com.insaner.fonecheck.ui.screens.sensor.SensorTestState
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkErrorCode
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkPhase
import com.insaner.fonecheck.ui.screens.storage.StorageTestState
import com.insaner.fonecheck.ui.screens.thermal.ThermalSeverityCode
import com.insaner.fonecheck.ui.screens.thermal.ThermalTestState
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestState
import java.time.Instant

data class DiagnosticSnapshots(
    val device: DeviceInfo,
    val performance: PerformanceInfo,
    val performanceBenchmark: PerformanceBenchmarkResult? = null,
    val sim: SimTelephonyInfo,
    val display: DisplayTestState,
    val audio: AudioTestState,
    val camera: CameraTestState,
    val sensors: SensorTestState,
    val connectivity: ConnectivityTestState,
    val battery: BatteryTestState,
    val thermal: ThermalTestState,
    val storage: StorageTestState,
    val vibration: VibrationTestState,
    val buttons: ButtonTestState,
    val biometrics: BiometricTestState,
)

@Suppress("LargeClass", "TooManyFunctions") // One canonical mapper owns every diagnostic category.
object RunAllSnapshotMapper {
    fun map(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        permissions: RunAllPermissions,
        selections: RunAllSelections = RunAllSelections(),
        hardware: RunAllHardwareProfile = RunAllHardwareProfile.ALL_AVAILABLE,
        capturedAt: Instant,
    ): List<DiagnosticCategorySnapshot> {
        val evidenceByCategory =
            mapOf(
                DiagnosticCategoryId.DEVICE to deviceEvidence(snapshots),
                DiagnosticCategoryId.PERFORMANCE to performanceEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.SIM to simEvidence(snapshots, permissions, capturedAt),
                DiagnosticCategoryId.DISPLAY to displayEvidence(snapshots, manual, capturedAt),
                DiagnosticCategoryId.AUDIO to
                    audioEvidence(snapshots, manual, permissions, selections, hardware, capturedAt),
                DiagnosticCategoryId.CAMERA to
                    cameraEvidence(snapshots, manual, permissions, selections, hardware, capturedAt),
                DiagnosticCategoryId.SENSORS to sensorEvidence(snapshots, manual, capturedAt),
                DiagnosticCategoryId.CONNECTIVITY to connectivityEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.BATTERY to batteryEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.THERMAL to thermalEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.STORAGE to storageEvidence(snapshots, selections, capturedAt),
                DiagnosticCategoryId.VIBRATION to vibrationEvidence(snapshots, manual, capturedAt),
                DiagnosticCategoryId.BUTTONS to buttonEvidence(snapshots.buttons, manual, capturedAt),
                DiagnosticCategoryId.BIOMETRICS to biometricEvidence(snapshots, manual, capturedAt),
            )

        return DiagnosticCatalog.categories.map { categoryId ->
            DiagnosticCategorySnapshot(
                version = DiagnosticSnapshotVersion.CURRENT,
                categoryId = categoryId,
                evidence = evidenceByCategory.getValue(categoryId),
            )
        }
    }

    private fun deviceEvidence(snapshots: DiagnosticSnapshots): List<DiagnosticEvidence> {
        val device = snapshots.device
        val rootClassification =
            DeviceObservationClassifier.classify(DeviceObservation.RootArtifact(device.rootArtifactDetected))
        return listOf(
            evidence(
                categoryId = DiagnosticCategoryId.DEVICE,
                id = "identity",
                value = EvidenceValue.IntValue(device.apiLevel),
                capturedAt = device.capturedAt,
            ),
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.DEVICE,
                id = "security",
                classification = rootClassification,
                informationalPass = true,
                confidence = Confidence.LOW,
                source = EvidenceSource.ESTIMATE,
                value =
                    EvidenceValue.StableTextCodeValue(
                        if (device.rootArtifactDetected) {
                            "known_artifact_detected"
                        } else {
                            "no_known_artifact_detected"
                        },
                    ),
                capturedAt = device.capturedAt,
            ),
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.DEVICE,
                id = "developer_options",
                classification =
                    DeviceObservationClassifier.classify(
                        DeviceObservation.DeveloperOptions(device.developerOptionsEnabled),
                    ),
                informationalPass = true,
                value = EvidenceValue.BooleanValue(device.developerOptionsEnabled),
                capturedAt = device.capturedAt,
            ),
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.DEVICE,
                id = "usb_debugging",
                classification =
                    DeviceObservationClassifier.classify(
                        DeviceObservation.UsbDebugging(device.usbDebuggingEnabled),
                    ),
                informationalPass = true,
                value = EvidenceValue.BooleanValue(device.usbDebuggingEnabled),
                capturedAt = device.capturedAt,
            ),
        )
    }

    private fun performanceEvidence(
        snapshots: DiagnosticSnapshots,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val performance = snapshots.performance
        val benchmark = snapshots.performanceBenchmark
        val hasRamReading = performance.totalRamBytes?.let { it > 0L } == true
        val hasGpuReading = performance.glRenderer != PerformanceInfo.UNAVAILABLE && performance.glRenderer.isNotBlank()
        return listOf(
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.PERFORMANCE,
                id = "cpu",
                classification =
                    classifyMeasurement(
                        MeasurementKind.CPU,
                        available = performance.cpuCores > 0,
                    ),
                informationalPass = true,
                confidence = performance.cpuConfidence,
                value = performance.cpuCores.takeIf { it > 0 }?.let(EvidenceValue::IntValue),
                unit = EvidenceUnitCode("count"),
                capturedAt = capturedAt,
            ),
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.PERFORMANCE,
                id = "ram",
                classification = classifyMeasurement(MeasurementKind.RAM, hasRamReading),
                informationalPass = true,
                confidence = performance.ramConfidence,
                value = EvidenceValue.BooleanValue(true).takeIf { hasRamReading },
                capturedAt = capturedAt,
            ),
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.PERFORMANCE,
                id = "gpu",
                classification = classifyMeasurement(MeasurementKind.GPU, hasGpuReading),
                informationalPass = true,
                confidence = performance.gpuConfidence,
                value = EvidenceValue.BooleanValue(true).takeIf { hasGpuReading },
                capturedAt = capturedAt,
            ),
            benchmark?.let {
                evidence(
                    categoryId = DiagnosticCategoryId.PERFORMANCE,
                    id = "cpu_benchmark",
                    confidence = Confidence.LOW,
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    value = EvidenceValue.LongValue(it.cpuOperationsPerSecond),
                    unit = EvidenceUnitCode("operations_per_second"),
                    capturedAt = it.capturedAt,
                )
            } ?: notTested(
                categoryId = DiagnosticCategoryId.PERFORMANCE,
                id = "cpu_benchmark",
                capturedAt = capturedAt,
                reason = EvidenceReasonCode.ERROR,
            ),
            benchmark?.memoryMebibytesPerSecond?.let { throughput ->
                evidence(
                    categoryId = DiagnosticCategoryId.PERFORMANCE,
                    id = "memory_benchmark",
                    confidence = Confidence.LOW,
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    value = EvidenceValue.DoubleValue(throughput),
                    unit = EvidenceUnitCode("mebibytes_per_second"),
                    capturedAt = benchmark.capturedAt,
                )
            } ?: notTested(
                categoryId = DiagnosticCategoryId.PERFORMANCE,
                id = "memory_benchmark",
                capturedAt = benchmark?.capturedAt ?: capturedAt,
                reason = EvidenceReasonCode.ERROR,
            ),
        )
    }

    private fun simEvidence(
        snapshots: DiagnosticSnapshots,
        permissions: RunAllPermissions,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val info = snapshots.sim
        val inventoryClassification =
            DeviceObservationClassifier.classify(DeviceObservation.SimInventory(info.inventory))
        val inventory =
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.SIM,
                id = "inventory",
                classification = inventoryClassification,
                informationalPass = true,
                confidence = if (info.inventory == SimInventoryCode.UNKNOWN) Confidence.LOW else Confidence.HIGH,
                applicability =
                    if (info.inventory == SimInventoryCode.NO_TELEPHONY) {
                        Applicability.NOT_APPLICABLE
                    } else {
                        Applicability.APPLICABLE
                    },
                value = EvidenceValue.StableTextCodeValue(info.inventory.stableCode),
                capturedAt = capturedAt,
            )
        val network =
            when {
                info.inventory == SimInventoryCode.NO_TELEPHONY ->
                    unavailable(DiagnosticCategoryId.SIM, "network", capturedAt)

                !permissions.phone || !info.phoneStatePermissionGranted ->
                    notTested(
                        categoryId = DiagnosticCategoryId.SIM,
                        id = "network",
                        capturedAt = capturedAt,
                        reason = EvidenceReasonCode.PERMISSION_DENIED,
                    )

                else ->
                    evidence(
                        categoryId = DiagnosticCategoryId.SIM,
                        id = "network",
                        value = EvidenceValue.StableTextCodeValue(info.dataNetworkType.stableCode),
                        capturedAt = capturedAt,
                    )
            }
        val slotEvidence =
            info.simSlots.map { slot ->
                val classification =
                    DeviceObservationClassifier.classify(
                        DeviceObservation.SimSlot(
                            code = slot.state,
                            unused = isUnusedSimSlot(info.inventory, slot),
                        ),
                    )
                classifiedEvidence(
                    categoryId = DiagnosticCategoryId.SIM,
                    id = "slot_${slot.slotIndex}_state",
                    classification = classification,
                    informationalPass = true,
                    value = EvidenceValue.StableTextCodeValue(slot.state.name.lowercase()),
                    capturedAt = capturedAt,
                )
            }
        return listOf(inventory, network) + slotEvidence
    }

    private val SimInventoryCode.stableCode: String
        get() =
            when (this) {
                SimInventoryCode.NO_TELEPHONY -> "no_telephony"
                SimInventoryCode.NO_SIM -> "no_sim"
                SimInventoryCode.INACTIVE_SIM -> "inactive_sim"
                SimInventoryCode.SINGLE_SIM -> "single_sim"
                SimInventoryCode.MULTIPLE_SIM -> "multiple_sim"
                SimInventoryCode.UNKNOWN -> "unknown"
            }

    private val NetworkGenerationCode.stableCode: String
        get() =
            when (this) {
                NetworkGenerationCode.SECOND_GENERATION -> "second_generation"
                NetworkGenerationCode.THIRD_GENERATION -> "third_generation"
                NetworkGenerationCode.FOURTH_GENERATION -> "fourth_generation"
                NetworkGenerationCode.FIFTH_GENERATION -> "fifth_generation"
                NetworkGenerationCode.UNKNOWN -> "unknown"
            }

    private fun displayEvidence(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val info = snapshots.display.info
        val hasResolution = info.widthPx > 0 && info.heightPx > 0
        return listOf(
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.DISPLAY,
                id = "info",
                classification = classifyMeasurement(MeasurementKind.DISPLAY, hasResolution),
                informationalPass = true,
                value =
                    if (hasResolution) {
                        EvidenceValue.LongValue(info.widthPx.toLong() * info.heightPx.toLong())
                    } else {
                        null
                    },
                unit = EvidenceUnitCode("pixels").takeIf { hasResolution },
                capturedAt = capturedAt,
            ),
            confirmationEvidence(
                DiagnosticCategoryId.DISPLAY,
                "visual",
                manual.display,
                InteractiveCheck.DISPLAY,
                manual.outcomes[RunAllStage.DISPLAY],
                capturedAt,
            ),
        )
    }

    private fun audioEvidence(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        permissions: RunAllPermissions,
        selections: RunAllSelections,
        hardware: RunAllHardwareProfile,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val microphone =
            when {
                !selections.includeMicrophone ->
                    notTested(
                        DiagnosticCategoryId.AUDIO,
                        "microphone",
                        capturedAt,
                        EvidenceReasonCode.SKIPPED,
                    )
                !hardware.microphoneAvailable ->
                    unavailable(
                        DiagnosticCategoryId.AUDIO,
                        "microphone",
                        capturedAt,
                    )
                !permissions.microphone ->
                    notTested(
                        DiagnosticCategoryId.AUDIO,
                        "microphone",
                        capturedAt,
                        EvidenceReasonCode.PERMISSION_DENIED,
                    )
                snapshots.audio.hasRecordedAudio ->
                    evidence(
                        categoryId = DiagnosticCategoryId.AUDIO,
                        id = "microphone",
                        value = EvidenceValue.BooleanValue(true),
                        capturedAt = capturedAt,
                    )
                else ->
                    notTested(
                        categoryId = DiagnosticCategoryId.AUDIO,
                        id = "microphone",
                        capturedAt = capturedAt,
                        reason = EvidenceReasonCode.ERROR,
                    )
            }
        return listOf(
            if (selections.includeSpeaker) {
                confirmationEvidence(
                    DiagnosticCategoryId.AUDIO,
                    "speaker",
                    manual.speaker,
                    InteractiveCheck.SPEAKER,
                    manual.outcomes[RunAllStage.AUDIO],
                    capturedAt,
                )
            } else {
                notTested(
                    DiagnosticCategoryId.AUDIO,
                    "speaker",
                    capturedAt,
                    EvidenceReasonCode.SKIPPED,
                    EvidenceSource.USER_CONFIRMATION,
                )
            },
            microphone,
            evidence(
                categoryId = DiagnosticCategoryId.AUDIO,
                id = "headphones",
                value = EvidenceValue.BooleanValue(snapshots.audio.headphonePlugged),
                capturedAt = capturedAt,
            ),
        )
    }

    private fun cameraEvidence(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        permissions: RunAllPermissions,
        selections: RunAllSelections,
        hardware: RunAllHardwareProfile,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val capture = snapshots.camera.lastCapture
        return listOf(
            capabilityPresence(
                DiagnosticCategoryId.CAMERA,
                "rear",
                snapshots.camera.rearCapabilities != null,
                capturedAt,
            ),
            capabilityPresence(
                DiagnosticCategoryId.CAMERA,
                "front",
                snapshots.camera.frontCapabilities != null,
                capturedAt,
            ),
            when {
                !selections.includeCamera ->
                    notTested(
                        DiagnosticCategoryId.CAMERA,
                        "capture",
                        capturedAt,
                        EvidenceReasonCode.SKIPPED,
                        EvidenceSource.USER_CONFIRMATION,
                    )
                !hardware.cameraAvailable ->
                    unavailable(
                        DiagnosticCategoryId.CAMERA,
                        "capture",
                        capturedAt,
                    )
                permissions.camera ->
                    cameraCaptureEvidence(
                        completed = manual.cameraCompleted,
                        hasError = snapshots.camera.error != null,
                        outcome = manual.outcomes[RunAllStage.CAMERA],
                        capturedAt = capturedAt,
                    )
                else ->
                    notTested(
                        DiagnosticCategoryId.CAMERA,
                        "capture",
                        capturedAt,
                        EvidenceReasonCode.PERMISSION_DENIED,
                        EvidenceSource.USER_CONFIRMATION,
                    )
            },
            evidence(
                categoryId = DiagnosticCategoryId.CAMERA,
                id = "inventory",
                value = EvidenceValue.IntValue(snapshots.camera.cameras.size),
                unit = EvidenceUnitCode("count"),
                capturedAt = capturedAt,
            ),
            evidence(
                categoryId = DiagnosticCategoryId.CAMERA,
                id = "logical_count",
                value =
                    EvidenceValue.IntValue(
                        snapshots.camera.cameras.count { it.cameraClass == CameraClassCode.LOGICAL },
                    ),
                unit = EvidenceUnitCode("count"),
                capturedAt = capturedAt,
            ),
            capture?.let {
                evidence(
                    categoryId = DiagnosticCategoryId.CAMERA,
                    id = "capture_dimensions",
                    value = EvidenceValue.LongValue(it.width.toLong() * it.height.toLong()),
                    unit = EvidenceUnitCode("pixels"),
                    capturedAt = Instant.ofEpochMilli(it.timestamp),
                )
            } ?: notTested(
                categoryId = DiagnosticCategoryId.CAMERA,
                id = "capture_dimensions",
                capturedAt = capturedAt,
            ),
        )
    }

    private fun cameraCaptureEvidence(
        completed: Boolean,
        hasError: Boolean,
        outcome: RunAllStageOutcome?,
        capturedAt: Instant,
    ): DiagnosticEvidence {
        val classification =
            when {
                completed ->
                    DeviceObservationClassifier.classify(
                        DeviceObservation.UserConfirmation(InteractiveCheck.CAMERA, passed = true),
                    )
                hasError || outcome == RunAllStageOutcome.ERROR ->
                    classifyMeasurement(MeasurementKind.CAMERA, MeasurementOutcome.ERROR)
                outcome == RunAllStageOutcome.TIMED_OUT ->
                    classifyMeasurement(MeasurementKind.CAMERA, MeasurementOutcome.TIMED_OUT)
                outcome == RunAllStageOutcome.SKIPPED ->
                    classifyMeasurement(MeasurementKind.CAMERA, MeasurementOutcome.SKIPPED)
                outcome == RunAllStageOutcome.UNAVAILABLE ->
                    classifyMeasurement(MeasurementKind.CAMERA, MeasurementOutcome.HARDWARE_ABSENT)
                else -> classifyMeasurement(MeasurementKind.CAMERA, MeasurementOutcome.NOT_RUN)
            }
        return classifiedEvidence(
            categoryId = DiagnosticCategoryId.CAMERA,
            id = "capture",
            classification = classification,
            source =
                if (!completed && outcome == RunAllStageOutcome.UNAVAILABLE) {
                    EvidenceSource.ANDROID_API
                } else {
                    EvidenceSource.USER_CONFIRMATION
                },
            value = EvidenceValue.BooleanValue(true).takeIf { completed },
            capturedAt = capturedAt,
        )
    }

    private fun sensorEvidence(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val sensorState = snapshots.sensors
        val orientationChallenges =
            setOf(
                InteractiveChallenge.TILT_LEFT,
                InteractiveChallenge.TILT_RIGHT,
                InteractiveChallenge.FACE_DOWN,
                InteractiveChallenge.FACE_UP,
            )
        return buildList {
            add(
                if (sensorState.sensorCount > 0) {
                    evidence(
                        categoryId = DiagnosticCategoryId.SENSORS,
                        id = "inventory",
                        value = EvidenceValue.IntValue(sensorState.sensorCount),
                        unit = EvidenceUnitCode("count"),
                        capturedAt = capturedAt,
                    )
                } else {
                    unavailable(
                        DiagnosticCategoryId.SENSORS,
                        "inventory",
                        capturedAt,
                        EvidenceValue.IntValue(0),
                    )
                },
            )
            sensorState.guidedTests.forEach { test ->
                add(
                    when (test.status) {
                        GuidedSensorStatus.PASSED ->
                            classifiedEvidence(
                                categoryId = DiagnosticCategoryId.SENSORS,
                                id = test.code.stableCode,
                                classification =
                                    classifyMeasurement(MeasurementKind.SENSORS, MeasurementOutcome.MEASURED),
                                source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                                value = EvidenceValue.IntValue(test.sampleCount),
                                unit = EvidenceUnitCode("samples"),
                                capturedAt = capturedAt,
                            )

                        GuidedSensorStatus.NOT_AVAILABLE ->
                            unavailable(
                                categoryId = DiagnosticCategoryId.SENSORS,
                                id = test.code.stableCode,
                                capturedAt = capturedAt,
                            )

                        GuidedSensorStatus.NOT_TESTED ->
                            notTested(
                                categoryId = DiagnosticCategoryId.SENSORS,
                                id = test.code.stableCode,
                                capturedAt = capturedAt,
                                reason = EvidenceReasonCode.NOT_RUN,
                                source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                            )

                        GuidedSensorStatus.SAMPLING ->
                            notTested(
                                categoryId = DiagnosticCategoryId.SENSORS,
                                id = test.code.stableCode,
                                capturedAt = capturedAt,
                                reason = EvidenceReasonCode.CANCELLED,
                                source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                            )

                        GuidedSensorStatus.SKIPPED ->
                            notTested(
                                categoryId = DiagnosticCategoryId.SENSORS,
                                id = test.code.stableCode,
                                capturedAt = capturedAt,
                                reason = EvidenceReasonCode.SKIPPED,
                                source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                            )
                    },
                )
            }
            add(
                if (sensorState.completedChallenges.any(orientationChallenges::contains)) {
                    evidence(
                        categoryId = DiagnosticCategoryId.SENSORS,
                        id = "orientation",
                        confidence = Confidence.LOW,
                        source = EvidenceSource.DERIVED,
                        value = EvidenceValue.StableTextCodeValue("observed"),
                        capturedAt = capturedAt,
                    )
                } else {
                    notTested(
                        categoryId = DiagnosticCategoryId.SENSORS,
                        id = "orientation",
                        capturedAt = capturedAt,
                        source = EvidenceSource.DERIVED,
                    )
                },
            )
            add(
                automaticSensorEvidence(
                    "motion",
                    completed = manual.sensorsCompleted,
                    hasError = sensorState.error != null,
                    manual.outcomes[RunAllStage.SENSORS],
                    capturedAt,
                ),
            )
        }
    }

    private fun automaticSensorEvidence(
        id: String,
        completed: Boolean,
        hasError: Boolean,
        outcome: RunAllStageOutcome?,
        capturedAt: Instant,
    ): DiagnosticEvidence {
        val measurementOutcome =
            when {
                completed -> MeasurementOutcome.MEASURED
                hasError || outcome == RunAllStageOutcome.ERROR -> MeasurementOutcome.ERROR
                outcome == RunAllStageOutcome.TIMED_OUT -> MeasurementOutcome.TIMED_OUT
                outcome == RunAllStageOutcome.SKIPPED -> MeasurementOutcome.SKIPPED
                outcome == RunAllStageOutcome.UNAVAILABLE -> MeasurementOutcome.HARDWARE_ABSENT
                else -> MeasurementOutcome.NOT_RUN
            }
        return classifiedEvidence(
            categoryId = DiagnosticCategoryId.SENSORS,
            id = id,
            classification = classifyMeasurement(MeasurementKind.SENSORS, measurementOutcome),
            source = EvidenceSource.AUTOMATIC_MEASUREMENT,
            value = EvidenceValue.BooleanValue(true).takeIf { completed },
            capturedAt = capturedAt,
        )
    }

    private fun connectivityEvidence(
        snapshots: DiagnosticSnapshots,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val connectivity = snapshots.connectivity
        return listOf(
            capabilityStateEvidence(
                id = "wifi",
                available = connectivity.wifi.isAvailable,
                enabled = connectivity.wifi.isConnected,
                capturedAt = capturedAt,
            ),
            bluetoothEvidence(connectivity, capturedAt),
            capabilityStateEvidence(
                id = "nfc",
                available = connectivity.nfc.isAvailable,
                enabled = connectivity.nfc.isEnabled,
                capturedAt = capturedAt,
            ),
            if (connectivity.nfc.isAvailable) {
                evidence(
                    categoryId = DiagnosticCategoryId.CONNECTIVITY,
                    id = "nfc_hce",
                    value = EvidenceValue.BooleanValue(connectivity.nfc.supportsHostCardEmulation),
                    capturedAt = capturedAt,
                )
            } else {
                unavailable(DiagnosticCategoryId.CONNECTIVITY, "nfc_hce", capturedAt)
            },
            gpsEvidence(connectivity, capturedAt),
            capabilityStateEvidence(
                id = "mobile",
                available = connectivity.mobileNetwork.isAvailable,
                enabled = connectivity.mobileNetwork.isConnected,
                capturedAt = capturedAt,
            ),
        )
    }

    private fun bluetoothEvidence(
        connectivity: ConnectivityTestState,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        when (connectivity.bluetooth.access) {
            BluetoothAccessCode.HARDWARE_ABSENT ->
                unavailable(DiagnosticCategoryId.CONNECTIVITY, "bluetooth", capturedAt)

            BluetoothAccessCode.PERMISSION_DENIED ->
                notTested(
                    categoryId = DiagnosticCategoryId.CONNECTIVITY,
                    id = "bluetooth",
                    capturedAt = capturedAt,
                    reason = EvidenceReasonCode.PERMISSION_DENIED,
                )

            BluetoothAccessCode.NOT_REQUIRED,
            BluetoothAccessCode.GRANTED,
            ->
                evidence(
                    categoryId = DiagnosticCategoryId.CONNECTIVITY,
                    id = "bluetooth",
                    value = connectivity.bluetooth.isEnabled?.let(EvidenceValue::BooleanValue),
                    capturedAt = capturedAt,
                )
        }

    private fun gpsEvidence(
        connectivity: ConnectivityTestState,
        capturedAt: Instant,
    ): DiagnosticEvidence {
        val gps = connectivity.gps
        val classification =
            when {
                !gps.isAvailable ->
                    classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.HARDWARE_ABSENT)
                !gps.isEnabled ->
                    DeviceObservationClassifier.classify(DeviceObservation.GpsProvider(enabled = false))
                else -> classifyGpsFix(gps.fixStatus, gps.failure)
            }
        return classifiedEvidence(
            categoryId = DiagnosticCategoryId.CONNECTIVITY,
            id = "gps",
            classification = classification,
            confidence = if (gps.fixStatus == GpsFixStatus.FAILED) Confidence.LOW else Confidence.HIGH,
            source = EvidenceSource.AUTOMATIC_MEASUREMENT,
            applicability = if (gps.isAvailable) Applicability.APPLICABLE else Applicability.NOT_APPLICABLE,
            value =
                when {
                    !gps.isEnabled -> EvidenceValue.BooleanValue(false)
                    gps.fixStatus == GpsFixStatus.FIXED -> gps.fixTimeMs?.let(EvidenceValue::LongValue)
                    else -> null
                },
            unit = EvidenceUnitCode("milliseconds").takeIf { gps.fixStatus == GpsFixStatus.FIXED },
            capturedAt = capturedAt,
        )
    }

    private fun capabilityStateEvidence(
        id: String,
        available: Boolean,
        enabled: Boolean,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        if (available) {
            evidence(
                categoryId = DiagnosticCategoryId.CONNECTIVITY,
                id = id,
                value = EvidenceValue.BooleanValue(enabled),
                capturedAt = capturedAt,
            )
        } else {
            unavailable(DiagnosticCategoryId.CONNECTIVITY, id, capturedAt)
        }

    @Suppress("LongMethod", "kotlin:S3776") // Keeps the battery category's evidence mapping atomic.
    private fun batteryEvidence(
        snapshots: DiagnosticSnapshots,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val batteryState = snapshots.battery
        val battery = batteryState.basic
        val healthStatus = batteryState.health.healthStatusRaw
        val healthClassification = classifyBatteryHealth(healthStatus)
        val healthEvidence =
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.BATTERY,
                id = "health",
                classification = healthClassification,
                informationalPass = true,
                confidence =
                    if (healthClassification.state == ObservationState.NOT_MEASURED) {
                        Confidence.UNAVAILABLE
                    } else {
                        Confidence.HIGH
                    },
                value =
                    batteryHealthCode(healthStatus)
                        .takeIf { healthClassification.state != ObservationState.NOT_MEASURED }
                        ?.let(EvidenceValue::StableTextCodeValue),
                capturedAt = capturedAt,
            )
        val temperatureClassification =
            DeviceObservationClassifier.classify(DeviceObservation.BatteryTemperature(battery.temperatureCelsius))
        val temperatureEvidence =
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.BATTERY,
                id = "temperature",
                classification = temperatureClassification,
                confidence =
                    if (battery.temperatureCelsius == null) Confidence.UNAVAILABLE else Confidence.HIGH,
                source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                value = battery.temperatureCelsius?.toDouble()?.let(EvidenceValue::DoubleValue),
                unit = EvidenceUnitCode("celsius").takeIf { battery.temperatureCelsius != null },
                capturedAt = capturedAt,
            )
        val levelEvidence =
            battery.level?.let { level ->
                evidence(
                    categoryId = DiagnosticCategoryId.BATTERY,
                    id = "level",
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    value = EvidenceValue.IntValue(level),
                    unit = EvidenceUnitCode("percent"),
                    capturedAt = capturedAt,
                )
            } ?: unavailableReading(DiagnosticCategoryId.BATTERY, "level", capturedAt)
        val currentEvidence =
            batteryState.charging.chargingCurrentMa?.let { currentMa ->
                evidence(
                    categoryId = DiagnosticCategoryId.BATTERY,
                    id = "current_now",
                    confidence = batteryState.charging.chargingCurrentConfidence,
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    value = EvidenceValue.DoubleValue(currentMa),
                    unit = EvidenceUnitCode("milliamperes"),
                    capturedAt = capturedAt,
                )
            } ?: unavailableReading(DiagnosticCategoryId.BATTERY, "current_now", capturedAt)
        val interpretationEvidence =
            if (batteryState.charging.chargingCurrentMa != null) {
                listOf(
                    evidence(
                        categoryId = DiagnosticCategoryId.BATTERY,
                        id = "current_direction",
                        confidence = Confidence.LOW,
                        source = EvidenceSource.DERIVED,
                        value =
                            EvidenceValue.StableTextCodeValue(
                                when (batteryState.charging.currentDirection) {
                                    BatteryCurrentDirection.CHARGING -> "charging"
                                    BatteryCurrentDirection.DISCHARGING -> "discharging"
                                    BatteryCurrentDirection.IDLE -> "idle"
                                },
                            ),
                        capturedAt = capturedAt,
                    ),
                    evidence(
                        categoryId = DiagnosticCategoryId.BATTERY,
                        id = "current_interpretation",
                        confidence = Confidence.LOW,
                        source = EvidenceSource.DERIVED,
                        value =
                            EvidenceValue.StableTextCodeValue(
                                if (batteryState.charging.currentSignNormalized) {
                                    "status_sign_normalized"
                                } else {
                                    "api_sign"
                                },
                            ),
                        capturedAt = capturedAt,
                    ),
                )
            } else {
                emptyList()
            }
        val profileEvidence =
            evidence(
                categoryId = DiagnosticCategoryId.BATTERY,
                id = "current_profile",
                confidence = batteryState.manufacturer.profileConfidence,
                source = EvidenceSource.DERIVED,
                value =
                    EvidenceValue.StableTextCodeValue(
                        when (batteryState.manufacturer.profile) {
                            ManufacturerProfile.SAMSUNG -> "samsung"
                            ManufacturerProfile.ONEPLUS -> "oneplus"
                            ManufacturerProfile.GOOGLE_PIXEL -> "google_pixel"
                            ManufacturerProfile.GENERIC -> "generic"
                        },
                    ),
                capturedAt = capturedAt,
            )
        val cycleCountEvidence =
            when {
                !batteryState.health.cycleCountSupported ->
                    classifiedEvidence(
                        categoryId = DiagnosticCategoryId.BATTERY,
                        id = "cycle_count",
                        classification =
                            classifyMeasurement(
                                MeasurementKind.GENERIC,
                                MeasurementOutcome.ANDROID_VERSION_UNSUPPORTED,
                            ),
                        confidence = Confidence.UNAVAILABLE,
                        applicability = Applicability.NOT_APPLICABLE,
                        capturedAt = capturedAt,
                    )

                batteryState.health.cycleCount == null ->
                    unavailableReading(DiagnosticCategoryId.BATTERY, "cycle_count", capturedAt)

                else ->
                    evidence(
                        categoryId = DiagnosticCategoryId.BATTERY,
                        id = "cycle_count",
                        confidence = batteryState.health.cycleCountConfidence,
                        value = EvidenceValue.IntValue(batteryState.health.cycleCount),
                        capturedAt = capturedAt,
                    )
            }
        return listOf(
            healthEvidence,
            temperatureEvidence,
            levelEvidence,
            currentEvidence,
        ) + interpretationEvidence + profileEvidence + cycleCountEvidence
    }

    private fun vibrationEvidence(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val haptic = snapshots.vibration.haptic
        val hasVibrator = haptic.hasVibrator
        return listOf(
            capabilityPresence(DiagnosticCategoryId.VIBRATION, "hardware", hasVibrator, capturedAt),
            if (hasVibrator) {
                evidence(
                    categoryId = DiagnosticCategoryId.VIBRATION,
                    id = "amplitude_control",
                    value = EvidenceValue.BooleanValue(haptic.hasAmplitudeControl),
                    capturedAt = capturedAt,
                )
            } else {
                unavailable(DiagnosticCategoryId.VIBRATION, "amplitude_control", capturedAt)
            },
            vibrationCapabilityEvidence(
                id = "effects",
                hasVibrator = hasVibrator,
                apiSupported = haptic.effectsApiSupported,
                count = haptic.supportedEffectsCount,
                capturedAt = capturedAt,
            ),
            vibrationCapabilityEvidence(
                id = "primitives",
                hasVibrator = hasVibrator,
                apiSupported = haptic.primitivesApiSupported,
                count = haptic.supportedPrimitivesCount,
                capturedAt = capturedAt,
            ),
            if (hasVibrator) {
                confirmationEvidence(
                    DiagnosticCategoryId.VIBRATION,
                    "motor",
                    manual.vibration,
                    InteractiveCheck.VIBRATION,
                    manual.outcomes[RunAllStage.VIBRATION],
                    capturedAt,
                )
            } else {
                unavailable(
                    DiagnosticCategoryId.VIBRATION,
                    "motor",
                    capturedAt,
                )
            },
        )
    }

    private fun vibrationCapabilityEvidence(
        id: String,
        hasVibrator: Boolean,
        apiSupported: Boolean,
        count: Int,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        when {
            !hasVibrator -> unavailable(DiagnosticCategoryId.VIBRATION, id, capturedAt)
            !apiSupported ->
                classifiedEvidence(
                    categoryId = DiagnosticCategoryId.VIBRATION,
                    id = id,
                    classification =
                        classifyMeasurement(
                            MeasurementKind.GENERIC,
                            MeasurementOutcome.ANDROID_VERSION_UNSUPPORTED,
                        ),
                    confidence = Confidence.UNAVAILABLE,
                    applicability = Applicability.NOT_APPLICABLE,
                    capturedAt = capturedAt,
                )
            else ->
                evidence(
                    categoryId = DiagnosticCategoryId.VIBRATION,
                    id = id,
                    value = EvidenceValue.IntValue(count),
                    unit = EvidenceUnitCode("count"),
                    capturedAt = capturedAt,
                )
        }

    private fun thermalEvidence(
        snapshots: DiagnosticSnapshots,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val thermal = snapshots.thermal
        val statusEvidence =
            when {
                !thermal.statusApiSupported ->
                    classifiedEvidence(
                        categoryId = DiagnosticCategoryId.THERMAL,
                        id = "status",
                        classification =
                            classifyMeasurement(
                                MeasurementKind.GENERIC,
                                MeasurementOutcome.ANDROID_VERSION_UNSUPPORTED,
                            ),
                        confidence = Confidence.UNAVAILABLE,
                        applicability = Applicability.NOT_APPLICABLE,
                        capturedAt = capturedAt,
                    )

                thermal.status == ThermalStatusCode.UNAVAILABLE ->
                    classifiedEvidence(
                        categoryId = DiagnosticCategoryId.THERMAL,
                        id = "status",
                        classification =
                            DeviceObservationClassifier.classify(DeviceObservation.Thermal(thermal.status)),
                        confidence = Confidence.UNAVAILABLE,
                        capturedAt = capturedAt,
                    )

                else ->
                    classifiedEvidence(
                        categoryId = DiagnosticCategoryId.THERMAL,
                        id = "status",
                        classification =
                            DeviceObservationClassifier.classify(DeviceObservation.Thermal(thermal.status)),
                        confidence = thermal.statusConfidence,
                        value = EvidenceValue.StableTextCodeValue(thermal.status.name.lowercase()),
                        capturedAt = capturedAt,
                    )
            }
        val severityEvidence =
            if (thermal.severity == ThermalSeverityCode.UNAVAILABLE) {
                unavailableReading(DiagnosticCategoryId.THERMAL, "severity", capturedAt)
            } else {
                evidence(
                    categoryId = DiagnosticCategoryId.THERMAL,
                    id = "severity",
                    confidence = thermal.statusConfidence,
                    source = EvidenceSource.DERIVED,
                    value = EvidenceValue.StableTextCodeValue(thermal.severity.name.lowercase()),
                    capturedAt = capturedAt,
                )
            }
        val headroomEvidence =
            when {
                !thermal.headroomApiSupported ->
                    classifiedEvidence(
                        categoryId = DiagnosticCategoryId.THERMAL,
                        id = "headroom",
                        classification =
                            classifyMeasurement(
                                MeasurementKind.GENERIC,
                                MeasurementOutcome.ANDROID_VERSION_UNSUPPORTED,
                            ),
                        confidence = Confidence.UNAVAILABLE,
                        applicability = Applicability.NOT_APPLICABLE,
                        capturedAt = capturedAt,
                    )

                thermal.headroom == null ->
                    unavailableReading(DiagnosticCategoryId.THERMAL, "headroom", capturedAt)

                else ->
                    evidence(
                        categoryId = DiagnosticCategoryId.THERMAL,
                        id = "headroom",
                        confidence = thermal.headroomConfidence,
                        source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                        value = EvidenceValue.DoubleValue(thermal.headroom.toString().toDouble()),
                        unit = EvidenceUnitCode("ratio"),
                        capturedAt = capturedAt,
                    )
            }
        val batteryTemperatureEvidence =
            thermal.batteryTemperatureCelsius?.let { temperature ->
                evidence(
                    categoryId = DiagnosticCategoryId.THERMAL,
                    id = "battery_temperature",
                    confidence = thermal.batteryTemperatureConfidence,
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    value = EvidenceValue.DoubleValue(temperature.toDouble()),
                    unit = EvidenceUnitCode("celsius"),
                    capturedAt = capturedAt,
                )
            } ?: unavailableReading(DiagnosticCategoryId.THERMAL, "battery_temperature", capturedAt)

        return listOf(statusEvidence, severityEvidence, headroomEvidence, batteryTemperatureEvidence)
    }

    private fun storageEvidence(
        snapshots: DiagnosticSnapshots,
        selections: RunAllSelections,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val state = snapshots.storage
        val infoEvidence =
            state.info?.let { info ->
                val infoCapturedAt = info.capturedAt
                listOf(
                    storageLongEvidence("total", info.totalBytes, "bytes", infoCapturedAt),
                    storageLongEvidence("used", info.usedBytes, "bytes", infoCapturedAt),
                    storageLongEvidence("available", info.availableBytes, "bytes", infoCapturedAt),
                    info.usagePercent?.let { usage ->
                        evidence(
                            categoryId = DiagnosticCategoryId.STORAGE,
                            id = "usage",
                            source = EvidenceSource.DERIVED,
                            value = EvidenceValue.DoubleValue(usage),
                            unit = EvidenceUnitCode("percent"),
                            capturedAt = infoCapturedAt,
                        )
                    } ?: unavailableReading(DiagnosticCategoryId.STORAGE, "usage", infoCapturedAt),
                    evidence(
                        categoryId = DiagnosticCategoryId.STORAGE,
                        id = "internal_access",
                        value = EvidenceValue.BooleanValue(info.internalStorageAccessible),
                        capturedAt = infoCapturedAt,
                    ),
                    storageCountEvidence("volume_count", info.appAccessibleVolumes.size, infoCapturedAt),
                    storageCountEvidence(
                        "mounted_volume_count",
                        info.appAccessibleVolumes.count { it.isMounted },
                        infoCapturedAt,
                    ),
                    storageCountEvidence(
                        "removable_volume_count",
                        info.appAccessibleVolumes.count { it.isRemovable },
                        infoCapturedAt,
                    ),
                )
            } ?: listOf(
                storageUnavailable("total", capturedAt),
                storageUnavailable("used", capturedAt),
                storageUnavailable("available", capturedAt),
                storageUnavailable("usage", capturedAt),
                storageUnavailable("internal_access", capturedAt),
                storageUnavailable("volume_count", capturedAt),
                storageUnavailable("mounted_volume_count", capturedAt),
                storageUnavailable("removable_volume_count", capturedAt),
            )

        return infoEvidence + storageBenchmarkEvidence(state, selections.includeStorageBenchmark, capturedAt)
    }

    @Suppress("kotlin:S3776") // Benchmark validity and failure reasons form one evidence decision.
    private fun storageBenchmarkEvidence(
        state: StorageTestState,
        included: Boolean,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val result = state.benchmarkResult
        val writeRate = result?.writeMebibytesPerSecond
        val readRate = result?.readMebibytesPerSecond
        val benchmarkCapturedAt = result?.capturedAt
        val resultError = result?.error
        val hasRates = writeRate != null && readRate != null
        val allowsRates = resultError == null || resultError == StorageBenchmarkErrorCode.CLEANUP_FAILED
        val rateEvidence =
            if (hasRates && benchmarkCapturedAt != null && allowsRates) {
                listOf(
                    storageRateEvidence(
                        "sequential_write",
                        writeRate,
                        benchmarkCapturedAt,
                    ),
                    storageRateEvidence(
                        "sequential_read",
                        readRate,
                        benchmarkCapturedAt,
                    ),
                )
            } else {
                val reason =
                    if (included) {
                        storageBenchmarkReason(state)
                    } else {
                        EvidenceReasonCode.SKIPPED
                    }
                listOf(
                    notTested(
                        categoryId = DiagnosticCategoryId.STORAGE,
                        id = "sequential_write",
                        capturedAt = capturedAt,
                        reason = reason,
                        source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    ),
                    notTested(
                        categoryId = DiagnosticCategoryId.STORAGE,
                        id = "sequential_read",
                        capturedAt = capturedAt,
                        reason = reason,
                        source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    ),
                )
            }
        val conditions =
            result
                ?.let {
                    listOf(
                        storageLongEvidence("benchmark_data_size", it.dataSizeBytes, "bytes", it.capturedAt),
                        storageLongEvidence(
                            "benchmark_available_before",
                            it.availableBeforeBytes,
                            "bytes",
                            it.capturedAt,
                        ),
                        evidence(
                            categoryId = DiagnosticCategoryId.STORAGE,
                            id = "benchmark_location",
                            source = EvidenceSource.DERIVED,
                            value = EvidenceValue.StableTextCodeValue("app_cache"),
                            capturedAt = it.capturedAt,
                        ),
                    )
                }.orEmpty()
        return rateEvidence + conditions
    }

    private fun storageBenchmarkReason(state: StorageTestState): EvidenceReasonCode =
        when {
            state.benchmarkError == StorageBenchmarkErrorCode.INSUFFICIENT_SPACE ->
                EvidenceReasonCode.INSUFFICIENT_SPACE
            state.benchmarkPhase == StorageBenchmarkPhase.SKIPPED -> EvidenceReasonCode.SKIPPED
            state.benchmarkPhase == StorageBenchmarkPhase.CANCELLED -> EvidenceReasonCode.CANCELLED
            state.benchmarkPhase == StorageBenchmarkPhase.ERROR -> EvidenceReasonCode.ERROR
            else -> EvidenceReasonCode.NOT_RUN
        }

    private fun storageRateEvidence(
        id: String,
        value: Double,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        evidence(
            categoryId = DiagnosticCategoryId.STORAGE,
            id = id,
            confidence = Confidence.LOW,
            source = EvidenceSource.AUTOMATIC_MEASUREMENT,
            value = EvidenceValue.DoubleValue(value),
            unit = EvidenceUnitCode("mebibytes_per_second"),
            capturedAt = capturedAt,
        )

    private fun storageLongEvidence(
        id: String,
        value: Long,
        unit: String,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        evidence(
            categoryId = DiagnosticCategoryId.STORAGE,
            id = id,
            value = EvidenceValue.LongValue(value),
            unit = EvidenceUnitCode(unit),
            capturedAt = capturedAt,
        )

    private fun storageCountEvidence(
        id: String,
        value: Int,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        evidence(
            categoryId = DiagnosticCategoryId.STORAGE,
            id = id,
            value = EvidenceValue.IntValue(value),
            unit = EvidenceUnitCode("count"),
            capturedAt = capturedAt,
        )

    private fun storageUnavailable(
        id: String,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        classifiedEvidence(
            categoryId = DiagnosticCategoryId.STORAGE,
            id = id,
            classification = classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.ERROR),
            confidence = Confidence.UNAVAILABLE,
            capturedAt = capturedAt,
        )

    private fun buttonEvidence(
        state: ButtonTestState,
        manual: ManualCheckResults,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val volumeClassification =
            if (manual.buttons == true) {
                DeviceObservationClassifier.classify(
                    DeviceObservation.ButtonTest(com.insaner.fonecheck.domain.observation.ButtonTestOutcome.COMPLETED),
                )
            } else {
                classifyButtonTest(state.phase)
            }
        val volumeEvidence =
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.BUTTONS,
                id = "volume",
                classification = volumeClassification,
                source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                value = EvidenceValue.BooleanValue(true).takeIf { volumeClassification.state == ObservationState.PASS },
                capturedAt = capturedAt,
            )
        return listOf(
            volumeEvidence,
            classifiedEvidence(
                categoryId = DiagnosticCategoryId.BUTTONS,
                id = "power",
                classification =
                    classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.PLATFORM_RESTRICTED),
                confidence = Confidence.UNAVAILABLE,
                source = EvidenceSource.ANDROID_API,
                applicability = Applicability.NOT_APPLICABLE,
                capturedAt = capturedAt,
            ),
        )
    }

    private fun biometricEvidence(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val capability = snapshots.biometrics.capability
        val available = capability.strongAvailable || capability.weakAvailable
        val capabilityCode =
            when {
                capability.strongAvailable -> "strong"
                capability.weakAvailable -> "weak"
                else -> "unavailable"
            }
        return listOf(
            evidence(
                categoryId = DiagnosticCategoryId.BIOMETRICS,
                id = "fingerprint_hardware",
                value = EvidenceValue.BooleanValue(capability.fingerprintHardware),
                capturedAt = capturedAt,
            ),
            evidence(
                categoryId = DiagnosticCategoryId.BIOMETRICS,
                id = "face_hardware",
                value = EvidenceValue.BooleanValue(capability.faceHardware),
                capturedAt = capturedAt,
            ),
            biometricCapabilityEvidence("strong_capability", capability.strongStatus, capturedAt),
            biometricCapabilityEvidence("weak_capability", capability.weakStatus, capturedAt),
            if (available) {
                classifiedEvidence(
                    categoryId = DiagnosticCategoryId.BIOMETRICS,
                    id = "capability",
                    classification = classifyBiometricCapability(BiometricAvailability.AVAILABLE),
                    informationalPass = true,
                    value = EvidenceValue.StableTextCodeValue(capabilityCode),
                    capturedAt = capturedAt,
                )
            } else {
                classifiedEvidence(
                    categoryId = DiagnosticCategoryId.BIOMETRICS,
                    id = "capability",
                    classification = classifyBiometricCapability(capability.weakStatus),
                    confidence =
                        if (capability.weakStatus == BiometricAvailability.UNKNOWN) {
                            Confidence.LOW
                        } else {
                            Confidence.HIGH
                        },
                    applicability =
                        if (capability.weakStatus == BiometricAvailability.NO_HARDWARE) {
                            Applicability.NOT_APPLICABLE
                        } else {
                            Applicability.APPLICABLE
                        },
                    value = EvidenceValue.StableTextCodeValue(capabilityCode),
                    capturedAt = capturedAt,
                )
            },
            biometricAuthenticationEvidence(
                snapshots.biometrics,
                manual.outcomes[RunAllStage.BIOMETRICS],
                capturedAt,
            ),
        )
    }

    private fun biometricCapabilityEvidence(
        id: String,
        status: BiometricAvailability,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        classifiedEvidence(
            categoryId = DiagnosticCategoryId.BIOMETRICS,
            id = id,
            classification = classifyBiometricCapability(status),
            informationalPass = true,
            confidence =
                if (status == BiometricAvailability.UNKNOWN) Confidence.LOW else Confidence.HIGH,
            applicability =
                if (status == BiometricAvailability.NO_HARDWARE) {
                    Applicability.NOT_APPLICABLE
                } else {
                    Applicability.APPLICABLE
                },
            value = EvidenceValue.StableTextCodeValue(status.stableCode),
            capturedAt = capturedAt,
        )

    private fun biometricAuthenticationEvidence(
        state: BiometricTestState,
        outcome: RunAllStageOutcome?,
        capturedAt: Instant,
    ): DiagnosticEvidence {
        val classification =
            when {
                state.authResult != AuthResult.NONE -> classifyBiometric(state.authResult)
                outcome == RunAllStageOutcome.UNAVAILABLE ->
                    classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.HARDWARE_ABSENT)
                outcome == RunAllStageOutcome.SKIPPED ->
                    classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.SKIPPED)
                outcome == RunAllStageOutcome.TIMED_OUT ->
                    classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.TIMED_OUT)
                outcome == RunAllStageOutcome.ERROR ->
                    classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.ERROR)
                else -> classifyBiometric(AuthResult.NONE)
            }
        return classifiedEvidence(
            categoryId = DiagnosticCategoryId.BIOMETRICS,
            id = "authentication",
            classification = classification,
            source = EvidenceSource.ANDROID_API,
            value = EvidenceValue.BooleanValue(true).takeIf { state.authResult == AuthResult.SUCCESS },
            capturedAt = capturedAt,
        )
    }

    private fun confirmationEvidence(
        categoryId: DiagnosticCategoryId,
        id: String,
        value: Boolean?,
        check: InteractiveCheck,
        outcome: RunAllStageOutcome?,
        capturedAt: Instant,
    ): DiagnosticEvidence {
        val classification =
            value?.let {
                DeviceObservationClassifier.classify(DeviceObservation.UserConfirmation(check, it))
            } ?: classifyMeasurement(
                kind = MeasurementKind.GENERIC,
                outcome =
                    when (outcome) {
                        RunAllStageOutcome.SKIPPED -> MeasurementOutcome.SKIPPED
                        RunAllStageOutcome.TIMED_OUT -> MeasurementOutcome.TIMED_OUT
                        RunAllStageOutcome.ERROR -> MeasurementOutcome.ERROR
                        RunAllStageOutcome.UNAVAILABLE -> MeasurementOutcome.HARDWARE_ABSENT
                        else -> MeasurementOutcome.NOT_RUN
                    },
            )
        return classifiedEvidence(
            categoryId = categoryId,
            id = id,
            classification = classification,
            source = EvidenceSource.USER_CONFIRMATION,
            value = value?.let(EvidenceValue::BooleanValue),
            capturedAt = capturedAt,
        )
    }

    private fun capabilityPresence(
        categoryId: DiagnosticCategoryId,
        id: String,
        available: Boolean,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        if (available) {
            evidence(
                categoryId = categoryId,
                id = id,
                value = EvidenceValue.BooleanValue(true),
                capturedAt = capturedAt,
            )
        } else {
            unavailable(categoryId, id, capturedAt, EvidenceValue.BooleanValue(false))
        }

    private fun unavailable(
        categoryId: DiagnosticCategoryId,
        id: String,
        capturedAt: Instant,
        value: EvidenceValue? = null,
        source: EvidenceSource = EvidenceSource.ANDROID_API,
    ): DiagnosticEvidence =
        classifiedEvidence(
            categoryId = categoryId,
            id = id,
            classification =
                classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.HARDWARE_ABSENT),
            confidence = Confidence.UNAVAILABLE,
            source = source,
            applicability = Applicability.NOT_APPLICABLE,
            value = value,
            capturedAt = capturedAt,
        )

    private fun unavailableReading(
        categoryId: DiagnosticCategoryId,
        id: String,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        classifiedEvidence(
            categoryId = categoryId,
            id = id,
            classification = classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.UNAVAILABLE),
            confidence = Confidence.UNAVAILABLE,
            capturedAt = capturedAt,
        )

    private fun notTested(
        categoryId: DiagnosticCategoryId,
        id: String,
        capturedAt: Instant,
        reason: EvidenceReasonCode = EvidenceReasonCode.SKIPPED,
        source: EvidenceSource = EvidenceSource.ANDROID_API,
    ): DiagnosticEvidence =
        classifiedEvidence(
            categoryId = categoryId,
            id = id,
            classification = classificationForLegacyReason(reason),
            confidence = Confidence.UNAVAILABLE,
            source = source,
            capturedAt = capturedAt,
        )

    private fun classificationForLegacyReason(reason: EvidenceReasonCode): ObservationClassification =
        when (reason) {
            EvidenceReasonCode.PERMISSION_DENIED ->
                classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.PERMISSION_DENIED)
            EvidenceReasonCode.NOT_RUN ->
                classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.NOT_RUN)
            EvidenceReasonCode.SKIPPED ->
                classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.SKIPPED)
            EvidenceReasonCode.CANCELLED ->
                classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.CANCELLED)
            EvidenceReasonCode.TIMEOUT ->
                classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.TIMED_OUT)
            EvidenceReasonCode.INSUFFICIENT_SPACE ->
                classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.INSUFFICIENT_SPACE)
            EvidenceReasonCode.HARDWARE_UNAVAILABLE ->
                classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.HARDWARE_ABSENT)
            EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED ->
                classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.ANDROID_VERSION_UNSUPPORTED)
            EvidenceReasonCode.PLATFORM_RESTRICTION ->
                classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.PLATFORM_RESTRICTED)
            EvidenceReasonCode.BIOMETRIC_LOCKOUT -> classifyBiometric(AuthResult.LOCKED_OUT)
            EvidenceReasonCode.BIOMETRIC_NOT_ENROLLED -> classifyBiometric(AuthResult.NO_ENROLLMENT)
            else -> classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.ERROR)
        }

    @Suppress("kotlin:S107") // Central factory exposes the complete immutable evidence schema.
    private fun classifiedEvidence(
        categoryId: DiagnosticCategoryId,
        id: String,
        classification: ObservationClassification,
        informationalPass: Boolean = false,
        confidence: Confidence = Confidence.HIGH,
        source: EvidenceSource = EvidenceSource.ANDROID_API,
        applicability: Applicability = Applicability.APPLICABLE,
        value: EvidenceValue? = null,
        unit: EvidenceUnitCode? = null,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        rawEvidence(
            categoryId = categoryId,
            id = id,
            status = classification.toDiagnosticStatus(informationalPass),
            confidence = confidence,
            source = source,
            applicability = applicability,
            reason = classification.toEvidenceReasonCode(),
            value = value,
            unit = unit,
            capturedAt = capturedAt,
        )

    private fun classifyMeasurement(
        kind: MeasurementKind,
        available: Boolean,
    ): ObservationClassification =
        classifyMeasurement(
            kind,
            if (available) MeasurementOutcome.MEASURED else MeasurementOutcome.UNAVAILABLE,
        )

    private fun classifyMeasurement(
        kind: MeasurementKind,
        outcome: MeasurementOutcome,
    ): ObservationClassification = DeviceObservationClassifier.classify(DeviceObservation.Measurement(kind, outcome))

    @Suppress("kotlin:S107") // Central factory exposes the complete immutable evidence schema.
    private fun evidence(
        categoryId: DiagnosticCategoryId,
        id: String,
        confidence: Confidence = Confidence.HIGH,
        source: EvidenceSource = EvidenceSource.ANDROID_API,
        applicability: Applicability = Applicability.APPLICABLE,
        value: EvidenceValue? = null,
        unit: EvidenceUnitCode? = null,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        classifiedEvidence(
            categoryId = categoryId,
            id = id,
            classification = classifyMeasurement(MeasurementKind.GENERIC, MeasurementOutcome.MEASURED),
            informationalPass = true,
            confidence = confidence,
            source = source,
            applicability = applicability,
            value = value,
            unit = unit,
            capturedAt = capturedAt,
        )

    @Suppress("kotlin:S107") // Central factory exposes the complete immutable evidence schema.
    private fun rawEvidence(
        categoryId: DiagnosticCategoryId,
        id: String,
        status: DiagnosticStatus,
        confidence: Confidence = Confidence.HIGH,
        source: EvidenceSource = EvidenceSource.ANDROID_API,
        applicability: Applicability = Applicability.APPLICABLE,
        reason: EvidenceReasonCode? = null,
        value: EvidenceValue? = null,
        unit: EvidenceUnitCode? = null,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        DiagnosticEvidence(
            categoryId = categoryId,
            checkId = DiagnosticCheckId(categoryId, "${categoryId.stableId}.$id"),
            status = status,
            confidence = confidence,
            source = source,
            applicability = applicability,
            reason = reason,
            value = value,
            unit = unit,
            capturedAt = capturedAt,
        )

    private fun batteryHealthCode(healthStatus: Int): String =
        when (healthStatus) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "over_voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "unspecified_failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "cold"
            else -> "unknown"
        }
}
