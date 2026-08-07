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
import com.insaner.fonecheck.ui.screens.audio.AudioTestState
import com.insaner.fonecheck.ui.screens.battery.BatteryCurrentDirection
import com.insaner.fonecheck.ui.screens.battery.BatteryTestState
import com.insaner.fonecheck.ui.screens.battery.ManufacturerProfile
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestState
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

object RunAllSnapshotMapper {
    fun map(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        permissions: RunAllPermissions,
        capturedAt: Instant,
    ): List<DiagnosticCategorySnapshot> {
        val evidenceByCategory =
            mapOf(
                DiagnosticCategoryId.DEVICE to deviceEvidence(snapshots),
                DiagnosticCategoryId.PERFORMANCE to performanceEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.SIM to simEvidence(snapshots, permissions, capturedAt),
                DiagnosticCategoryId.DISPLAY to displayEvidence(snapshots, manual, capturedAt),
                DiagnosticCategoryId.AUDIO to audioEvidence(snapshots, manual, permissions, capturedAt),
                DiagnosticCategoryId.CAMERA to cameraEvidence(snapshots, manual, permissions, capturedAt),
                DiagnosticCategoryId.SENSORS to sensorEvidence(snapshots, manual, capturedAt),
                DiagnosticCategoryId.CONNECTIVITY to connectivityEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.BATTERY to batteryEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.THERMAL to thermalEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.STORAGE to storageEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.VIBRATION to vibrationEvidence(snapshots, manual, capturedAt),
                DiagnosticCategoryId.BUTTONS to buttonEvidence(manual, capturedAt),
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
        return listOf(
            evidence(
                categoryId = DiagnosticCategoryId.DEVICE,
                id = "identity",
                status = DiagnosticStatus.INFO,
                value = EvidenceValue.IntValue(device.apiLevel),
                capturedAt = device.capturedAt,
            ),
            evidence(
                categoryId = DiagnosticCategoryId.DEVICE,
                id = "security",
                status =
                    if (device.rootArtifactDetected) {
                        DiagnosticStatus.WARNING
                    } else {
                        DiagnosticStatus.INFO
                    },
                confidence = Confidence.LOW,
                source = EvidenceSource.ESTIMATE,
                reason = EvidenceReasonCode.DEGRADED.takeIf { device.rootArtifactDetected },
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
        )
    }

    private fun performanceEvidence(
        snapshots: DiagnosticSnapshots,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val performance = snapshots.performance
        val benchmark = snapshots.performanceBenchmark
        val hasRamReading = performance.totalRamBytes?.let { it > 0L } == true
        val hasGpuReading = performance.glRenderer != PerformanceInfo.UNAVAILABLE
        return listOf(
            evidence(
                categoryId = DiagnosticCategoryId.PERFORMANCE,
                id = "cpu",
                status = passOrWarning(performance.cpuCores > 0),
                confidence = performance.cpuConfidence,
                reason = EvidenceReasonCode.DEGRADED.takeIf { performance.cpuCores <= 0 },
                value = EvidenceValue.IntValue(performance.cpuCores),
                unit = EvidenceUnitCode("count"),
                capturedAt = capturedAt,
            ),
            evidence(
                categoryId = DiagnosticCategoryId.PERFORMANCE,
                id = "ram",
                status = passOrWarning(hasRamReading),
                confidence = performance.ramConfidence,
                reason = EvidenceReasonCode.DEGRADED.takeIf { !hasRamReading },
                value = EvidenceValue.BooleanValue(hasRamReading),
                capturedAt = capturedAt,
            ),
            evidence(
                categoryId = DiagnosticCategoryId.PERFORMANCE,
                id = "gpu",
                status = passOrWarning(hasGpuReading),
                confidence = performance.gpuConfidence,
                reason = EvidenceReasonCode.DEGRADED.takeIf { !hasGpuReading },
                value = EvidenceValue.BooleanValue(hasGpuReading),
                capturedAt = capturedAt,
            ),
            benchmark?.let {
                evidence(
                    categoryId = DiagnosticCategoryId.PERFORMANCE,
                    id = "cpu_benchmark",
                    status = DiagnosticStatus.INFO,
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
                    status = DiagnosticStatus.INFO,
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
        val inventory =
            when (info.inventory) {
                SimInventoryCode.NO_TELEPHONY ->
                    unavailable(
                        categoryId = DiagnosticCategoryId.SIM,
                        id = "inventory",
                        capturedAt = capturedAt,
                        value = EvidenceValue.StableTextCodeValue("no_telephony"),
                    )

                SimInventoryCode.UNKNOWN ->
                    evidence(
                        categoryId = DiagnosticCategoryId.SIM,
                        id = "inventory",
                        status = DiagnosticStatus.WARNING,
                        confidence = Confidence.LOW,
                        reason = EvidenceReasonCode.DEGRADED,
                        value = EvidenceValue.StableTextCodeValue("unknown"),
                        capturedAt = capturedAt,
                    )

                else ->
                    evidence(
                        categoryId = DiagnosticCategoryId.SIM,
                        id = "inventory",
                        status = DiagnosticStatus.INFO,
                        value = EvidenceValue.StableTextCodeValue(info.inventory.stableCode),
                        capturedAt = capturedAt,
                    )
            }
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
                        status = DiagnosticStatus.INFO,
                        value = EvidenceValue.StableTextCodeValue(info.dataNetworkType.stableCode),
                        capturedAt = capturedAt,
                    )
            }
        return listOf(inventory, network)
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
            evidence(
                categoryId = DiagnosticCategoryId.DISPLAY,
                id = "info",
                status = passOrWarning(hasResolution),
                reason = EvidenceReasonCode.DEGRADED.takeIf { !hasResolution },
                value = EvidenceValue.LongValue(info.widthPx.toLong() * info.heightPx.toLong()),
                unit = EvidenceUnitCode("pixels"),
                capturedAt = capturedAt,
            ),
            manualEvidence(DiagnosticCategoryId.DISPLAY, "visual", manual.display, capturedAt),
        )
    }

    private fun audioEvidence(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        permissions: RunAllPermissions,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val microphone =
            if (!permissions.microphone) {
                notTested(
                    DiagnosticCategoryId.AUDIO,
                    "microphone",
                    capturedAt,
                    EvidenceReasonCode.PERMISSION_DENIED,
                )
            } else {
                val recorded = snapshots.audio.hasRecordedAudio
                if (recorded) {
                    evidence(
                        categoryId = DiagnosticCategoryId.AUDIO,
                        id = "microphone",
                        status = DiagnosticStatus.INFO,
                        value = EvidenceValue.BooleanValue(true),
                        capturedAt = capturedAt,
                    )
                } else {
                    notTested(
                        categoryId = DiagnosticCategoryId.AUDIO,
                        id = "microphone",
                        capturedAt = capturedAt,
                        reason = EvidenceReasonCode.ERROR,
                    )
                }
            }
        return listOf(
            manualEvidence(DiagnosticCategoryId.AUDIO, "speaker", manual.speaker, capturedAt),
            microphone,
            evidence(
                categoryId = DiagnosticCategoryId.AUDIO,
                id = "headphones",
                status = DiagnosticStatus.INFO,
                value = EvidenceValue.BooleanValue(snapshots.audio.headphonePlugged),
                capturedAt = capturedAt,
            ),
        )
    }

    private fun cameraEvidence(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        permissions: RunAllPermissions,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val capture = snapshots.camera.lastCapture
        return listOf(
            presence(
                DiagnosticCategoryId.CAMERA,
                "rear",
                snapshots.camera.rearCapabilities != null,
                capturedAt,
            ),
            presence(
                DiagnosticCategoryId.CAMERA,
                "front",
                snapshots.camera.frontCapabilities != null,
                capturedAt,
            ),
            if (permissions.camera) {
                manualEvidence(DiagnosticCategoryId.CAMERA, "capture", manual.camera, capturedAt)
            } else {
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
                status = DiagnosticStatus.INFO,
                value = EvidenceValue.IntValue(snapshots.camera.cameras.size),
                unit = EvidenceUnitCode("count"),
                capturedAt = capturedAt,
            ),
            evidence(
                categoryId = DiagnosticCategoryId.CAMERA,
                id = "logical_count",
                status = DiagnosticStatus.INFO,
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
                    status = DiagnosticStatus.INFO,
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
                        status = DiagnosticStatus.INFO,
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
                            evidence(
                                categoryId = DiagnosticCategoryId.SENSORS,
                                id = test.code.stableCode,
                                status = DiagnosticStatus.PASS,
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
                        status = DiagnosticStatus.INFO,
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
            add(automaticSensorEvidence("motion", manual.sensors, capturedAt))
        }
    }

    private fun automaticSensorEvidence(
        id: String,
        value: Boolean?,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        when (value) {
            true ->
                evidence(
                    categoryId = DiagnosticCategoryId.SENSORS,
                    id = id,
                    status = DiagnosticStatus.PASS,
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    value = EvidenceValue.BooleanValue(true),
                    capturedAt = capturedAt,
                )

            false ->
                evidence(
                    categoryId = DiagnosticCategoryId.SENSORS,
                    id = id,
                    status = DiagnosticStatus.FAIL,
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    reason = EvidenceReasonCode.DEGRADED,
                    value = EvidenceValue.BooleanValue(false),
                    capturedAt = capturedAt,
                )

            null ->
                notTested(
                    categoryId = DiagnosticCategoryId.SENSORS,
                    id = id,
                    capturedAt = capturedAt,
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
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
                    status = DiagnosticStatus.INFO,
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
                    status = DiagnosticStatus.INFO,
                    value = connectivity.bluetooth.isEnabled?.let(EvidenceValue::BooleanValue),
                    capturedAt = capturedAt,
                )
        }

    private fun gpsEvidence(
        connectivity: ConnectivityTestState,
        capturedAt: Instant,
    ): DiagnosticEvidence {
        val gps = connectivity.gps
        return when {
            !gps.isAvailable -> unavailable(DiagnosticCategoryId.CONNECTIVITY, "gps", capturedAt)
            !gps.isEnabled ->
                evidence(
                    categoryId = DiagnosticCategoryId.CONNECTIVITY,
                    id = "gps",
                    status = DiagnosticStatus.WARNING,
                    reason = EvidenceReasonCode.DISABLED,
                    value = EvidenceValue.BooleanValue(false),
                    capturedAt = capturedAt,
                )

            gps.fixStatus == GpsFixStatus.FIXED ->
                evidence(
                    categoryId = DiagnosticCategoryId.CONNECTIVITY,
                    id = "gps",
                    status = DiagnosticStatus.PASS,
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    value = gps.fixTimeMs?.let(EvidenceValue::LongValue),
                    unit = EvidenceUnitCode("milliseconds"),
                    capturedAt = capturedAt,
                )

            gps.fixStatus == GpsFixStatus.FAILED ->
                if (gps.failure == GpsFailureCode.TIMEOUT) {
                    evidence(
                        categoryId = DiagnosticCategoryId.CONNECTIVITY,
                        id = "gps",
                        status = DiagnosticStatus.WARNING,
                        confidence = Confidence.LOW,
                        source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                        reason = EvidenceReasonCode.TIMEOUT,
                        capturedAt = capturedAt,
                    )
                } else {
                    notTested(
                        categoryId = DiagnosticCategoryId.CONNECTIVITY,
                        id = "gps",
                        capturedAt = capturedAt,
                        reason = EvidenceReasonCode.ERROR,
                        source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    )
                }

            else ->
                notTested(
                    categoryId = DiagnosticCategoryId.CONNECTIVITY,
                    id = "gps",
                    capturedAt = capturedAt,
                    reason = EvidenceReasonCode.NOT_RUN,
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                )
        }
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
                status = DiagnosticStatus.INFO,
                value = EvidenceValue.BooleanValue(enabled),
                capturedAt = capturedAt,
            )
        } else {
            unavailable(DiagnosticCategoryId.CONNECTIVITY, id, capturedAt)
        }

    private fun batteryEvidence(
        snapshots: DiagnosticSnapshots,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val batteryState = snapshots.battery
        val battery = batteryState.basic
        val healthEvidence =
            if (battery.healthStatus == BatteryManager.BATTERY_HEALTH_UNKNOWN) {
                unavailableReading(DiagnosticCategoryId.BATTERY, "health", capturedAt)
            } else {
                val healthStatus =
                    when (battery.healthStatus) {
                        BatteryManager.BATTERY_HEALTH_GOOD -> DiagnosticStatus.PASS
                        BatteryManager.BATTERY_HEALTH_OVERHEAT,
                        BatteryManager.BATTERY_HEALTH_DEAD,
                        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE,
                        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE,
                        -> DiagnosticStatus.FAIL

                        else -> DiagnosticStatus.WARNING
                    }
                evidence(
                    categoryId = DiagnosticCategoryId.BATTERY,
                    id = "health",
                    status = healthStatus,
                    reason = EvidenceReasonCode.DEGRADED.takeIf { healthStatus != DiagnosticStatus.PASS },
                    value = EvidenceValue.StableTextCodeValue(batteryHealthCode(battery.healthStatus)),
                    capturedAt = capturedAt,
                )
            }
        val temperatureEvidence =
            battery.temperatureCelsius?.let { temperature ->
                val status =
                    when {
                        temperature >= 50f -> DiagnosticStatus.FAIL
                        temperature < 0f || temperature > 45f -> DiagnosticStatus.WARNING
                        else -> DiagnosticStatus.PASS
                    }
                evidence(
                    categoryId = DiagnosticCategoryId.BATTERY,
                    id = "temperature",
                    status = status,
                    source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                    reason = EvidenceReasonCode.DEGRADED.takeIf { status != DiagnosticStatus.PASS },
                    value = EvidenceValue.DoubleValue(temperature.toDouble()),
                    unit = EvidenceUnitCode("celsius"),
                    capturedAt = capturedAt,
                )
            } ?: unavailableReading(DiagnosticCategoryId.BATTERY, "temperature", capturedAt)
        val levelEvidence =
            battery.level?.let { level ->
                evidence(
                    categoryId = DiagnosticCategoryId.BATTERY,
                    id = "level",
                    status = DiagnosticStatus.INFO,
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
                    status = DiagnosticStatus.INFO,
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
                        status = DiagnosticStatus.INFO,
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
                        status = DiagnosticStatus.INFO,
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
                status = DiagnosticStatus.INFO,
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
                    evidence(
                        categoryId = DiagnosticCategoryId.BATTERY,
                        id = "cycle_count",
                        status = DiagnosticStatus.NOT_AVAILABLE,
                        confidence = Confidence.UNAVAILABLE,
                        applicability = Applicability.NOT_APPLICABLE,
                        reason = EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED,
                        capturedAt = capturedAt,
                    )

                batteryState.health.cycleCount == null ->
                    unavailableReading(DiagnosticCategoryId.BATTERY, "cycle_count", capturedAt)

                else ->
                    evidence(
                        categoryId = DiagnosticCategoryId.BATTERY,
                        id = "cycle_count",
                        status = DiagnosticStatus.INFO,
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
        val hasVibrator = snapshots.vibration.haptic.hasVibrator
        return listOf(
            presence(DiagnosticCategoryId.VIBRATION, "hardware", hasVibrator, capturedAt),
            if (hasVibrator) {
                manualEvidence(DiagnosticCategoryId.VIBRATION, "motor", manual.vibration, capturedAt)
            } else {
                unavailable(
                    DiagnosticCategoryId.VIBRATION,
                    "motor",
                    capturedAt,
                    source = EvidenceSource.USER_CONFIRMATION,
                )
            },
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
                    evidence(
                        categoryId = DiagnosticCategoryId.THERMAL,
                        id = "status",
                        status = DiagnosticStatus.NOT_AVAILABLE,
                        confidence = Confidence.UNAVAILABLE,
                        applicability = Applicability.NOT_APPLICABLE,
                        reason = EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED,
                        capturedAt = capturedAt,
                    )

                thermal.status == ThermalStatusCode.UNAVAILABLE ->
                    unavailableReading(DiagnosticCategoryId.THERMAL, "status", capturedAt)

                else -> {
                    val diagnosticStatus =
                        when (thermal.status) {
                            ThermalStatusCode.NONE -> DiagnosticStatus.PASS
                            ThermalStatusCode.LIGHT,
                            ThermalStatusCode.MODERATE,
                            -> DiagnosticStatus.WARNING

                            ThermalStatusCode.SEVERE,
                            ThermalStatusCode.CRITICAL,
                            ThermalStatusCode.EMERGENCY,
                            ThermalStatusCode.SHUTDOWN,
                            -> DiagnosticStatus.FAIL

                            ThermalStatusCode.UNAVAILABLE -> error("Unavailable status handled above")
                        }
                    evidence(
                        categoryId = DiagnosticCategoryId.THERMAL,
                        id = "status",
                        status = diagnosticStatus,
                        confidence = thermal.statusConfidence,
                        reason =
                            EvidenceReasonCode.DEGRADED.takeIf {
                                diagnosticStatus != DiagnosticStatus.PASS
                            },
                        value = EvidenceValue.StableTextCodeValue(thermal.status.name.lowercase()),
                        capturedAt = capturedAt,
                    )
                }
            }
        val severityEvidence =
            if (thermal.severity == ThermalSeverityCode.UNAVAILABLE) {
                unavailableReading(DiagnosticCategoryId.THERMAL, "severity", capturedAt)
            } else {
                evidence(
                    categoryId = DiagnosticCategoryId.THERMAL,
                    id = "severity",
                    status = DiagnosticStatus.INFO,
                    confidence = thermal.statusConfidence,
                    source = EvidenceSource.DERIVED,
                    value = EvidenceValue.StableTextCodeValue(thermal.severity.name.lowercase()),
                    capturedAt = capturedAt,
                )
            }
        val headroomEvidence =
            when {
                !thermal.headroomApiSupported ->
                    evidence(
                        categoryId = DiagnosticCategoryId.THERMAL,
                        id = "headroom",
                        status = DiagnosticStatus.NOT_AVAILABLE,
                        confidence = Confidence.UNAVAILABLE,
                        applicability = Applicability.NOT_APPLICABLE,
                        reason = EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED,
                        capturedAt = capturedAt,
                    )

                thermal.headroom == null ->
                    unavailableReading(DiagnosticCategoryId.THERMAL, "headroom", capturedAt)

                else ->
                    evidence(
                        categoryId = DiagnosticCategoryId.THERMAL,
                        id = "headroom",
                        status = DiagnosticStatus.INFO,
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
                    status = DiagnosticStatus.INFO,
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
                            status = DiagnosticStatus.INFO,
                            source = EvidenceSource.DERIVED,
                            value = EvidenceValue.DoubleValue(usage),
                            unit = EvidenceUnitCode("percent"),
                            capturedAt = infoCapturedAt,
                        )
                    } ?: unavailableReading(DiagnosticCategoryId.STORAGE, "usage", infoCapturedAt),
                    evidence(
                        categoryId = DiagnosticCategoryId.STORAGE,
                        id = "internal_access",
                        status = DiagnosticStatus.INFO,
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

        return infoEvidence + storageBenchmarkEvidence(state, capturedAt)
    }

    private fun storageBenchmarkEvidence(
        state: StorageTestState,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val result = state.benchmarkResult
        val ratesAreUsable =
            result?.writeMebibytesPerSecond != null &&
                result.readMebibytesPerSecond != null &&
                (result.error == null || result.error == StorageBenchmarkErrorCode.CLEANUP_FAILED)
        val rateEvidence =
            if (ratesAreUsable) {
                listOf(
                    storageRateEvidence("sequential_write", requireNotNull(result.writeMebibytesPerSecond), result.capturedAt),
                    storageRateEvidence("sequential_read", requireNotNull(result.readMebibytesPerSecond), result.capturedAt),
                )
            } else {
                val reason = storageBenchmarkReason(state)
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
            result?.let {
                listOf(
                    storageLongEvidence("benchmark_data_size", it.dataSizeBytes, "bytes", it.capturedAt),
                    storageLongEvidence("benchmark_available_before", it.availableBeforeBytes, "bytes", it.capturedAt),
                    evidence(
                        categoryId = DiagnosticCategoryId.STORAGE,
                        id = "benchmark_location",
                        status = DiagnosticStatus.INFO,
                        source = EvidenceSource.DERIVED,
                        value = EvidenceValue.StableTextCodeValue("app_cache"),
                        capturedAt = it.capturedAt,
                    ),
                ) +
                    if (it.bytesWritten > 0L) {
                        listOf(
                            evidence(
                                categoryId = DiagnosticCategoryId.STORAGE,
                                id = "benchmark_cleanup",
                                status =
                                    if (it.cleanupSucceeded) {
                                        DiagnosticStatus.INFO
                                    } else {
                                        DiagnosticStatus.WARNING
                                    },
                                source = EvidenceSource.DERIVED,
                                reason =
                                    if (it.cleanupSucceeded) {
                                        null
                                    } else {
                                        EvidenceReasonCode.DEGRADED
                                    },
                                value = EvidenceValue.BooleanValue(it.cleanupSucceeded),
                                capturedAt = it.capturedAt,
                            ),
                        )
                    } else {
                        emptyList()
                    }
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
            status = DiagnosticStatus.INFO,
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
            status = DiagnosticStatus.INFO,
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
            status = DiagnosticStatus.INFO,
            value = EvidenceValue.IntValue(value),
            unit = EvidenceUnitCode("count"),
            capturedAt = capturedAt,
        )

    private fun storageUnavailable(
        id: String,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        evidence(
            categoryId = DiagnosticCategoryId.STORAGE,
            id = id,
            status = DiagnosticStatus.NOT_AVAILABLE,
            confidence = Confidence.UNAVAILABLE,
            reason = EvidenceReasonCode.ERROR,
            capturedAt = capturedAt,
        )

    private fun buttonEvidence(
        manual: ManualCheckResults,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> =
        listOf(
            manualEvidence(DiagnosticCategoryId.BUTTONS, "volume", manual.buttons, capturedAt),
            evidence(
                categoryId = DiagnosticCategoryId.BUTTONS,
                id = "power",
                status = DiagnosticStatus.INFO,
                confidence = Confidence.LOW,
                source = EvidenceSource.ESTIMATE,
                capturedAt = capturedAt,
            ),
        )

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
            if (available) {
                evidence(
                    categoryId = DiagnosticCategoryId.BIOMETRICS,
                    id = "capability",
                    status = DiagnosticStatus.PASS,
                    value = EvidenceValue.StableTextCodeValue(capabilityCode),
                    capturedAt = capturedAt,
                )
            } else {
                unavailable(
                    DiagnosticCategoryId.BIOMETRICS,
                    "capability",
                    capturedAt,
                    EvidenceValue.StableTextCodeValue(capabilityCode),
                )
            },
            if (available) {
                manualEvidence(
                    DiagnosticCategoryId.BIOMETRICS,
                    "authentication",
                    manual.biometrics,
                    capturedAt,
                )
            } else {
                unavailable(
                    DiagnosticCategoryId.BIOMETRICS,
                    "authentication",
                    capturedAt,
                    source = EvidenceSource.USER_CONFIRMATION,
                )
            },
        )
    }

    private fun manualEvidence(
        categoryId: DiagnosticCategoryId,
        id: String,
        value: Boolean?,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        when (value) {
            true ->
                evidence(
                    categoryId = categoryId,
                    id = id,
                    status = DiagnosticStatus.PASS,
                    source = EvidenceSource.USER_CONFIRMATION,
                    value = EvidenceValue.BooleanValue(true),
                    capturedAt = capturedAt,
                )

            false ->
                evidence(
                    categoryId = categoryId,
                    id = id,
                    status = DiagnosticStatus.FAIL,
                    source = EvidenceSource.USER_CONFIRMATION,
                    reason = EvidenceReasonCode.USER_CONFIRMED_FAILURE,
                    value = EvidenceValue.BooleanValue(false),
                    capturedAt = capturedAt,
                )

            null ->
                notTested(
                    categoryId = categoryId,
                    id = id,
                    capturedAt = capturedAt,
                    source = EvidenceSource.USER_CONFIRMATION,
                )
        }

    private fun availability(
        categoryId: DiagnosticCategoryId,
        id: String,
        available: Boolean,
        enabled: Boolean,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        when {
            !available -> unavailable(categoryId, id, capturedAt, EvidenceValue.BooleanValue(false))
            enabled ->
                evidence(
                    categoryId = categoryId,
                    id = id,
                    status = DiagnosticStatus.PASS,
                    value = EvidenceValue.BooleanValue(true),
                    capturedAt = capturedAt,
                )

            else ->
                evidence(
                    categoryId = categoryId,
                    id = id,
                    status = DiagnosticStatus.WARNING,
                    reason = EvidenceReasonCode.DISABLED,
                    value = EvidenceValue.BooleanValue(false),
                    capturedAt = capturedAt,
                )
        }

    private fun presence(
        categoryId: DiagnosticCategoryId,
        id: String,
        available: Boolean,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        if (available) {
            evidence(
                categoryId = categoryId,
                id = id,
                status = DiagnosticStatus.PASS,
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
        evidence(
            categoryId = categoryId,
            id = id,
            status = DiagnosticStatus.NOT_AVAILABLE,
            confidence = Confidence.UNAVAILABLE,
            source = source,
            applicability = Applicability.NOT_APPLICABLE,
            reason = EvidenceReasonCode.HARDWARE_UNAVAILABLE,
            value = value,
            capturedAt = capturedAt,
        )

    private fun unavailableReading(
        categoryId: DiagnosticCategoryId,
        id: String,
        capturedAt: Instant,
    ): DiagnosticEvidence =
        evidence(
            categoryId = categoryId,
            id = id,
            status = DiagnosticStatus.NOT_AVAILABLE,
            confidence = Confidence.UNAVAILABLE,
            reason = EvidenceReasonCode.HARDWARE_UNAVAILABLE,
            capturedAt = capturedAt,
        )

    private fun notTested(
        categoryId: DiagnosticCategoryId,
        id: String,
        capturedAt: Instant,
        reason: EvidenceReasonCode = EvidenceReasonCode.SKIPPED,
        source: EvidenceSource = EvidenceSource.ANDROID_API,
    ): DiagnosticEvidence =
        evidence(
            categoryId = categoryId,
            id = id,
            status = DiagnosticStatus.NOT_TESTED,
            confidence = Confidence.UNAVAILABLE,
            source = source,
            reason = reason,
            capturedAt = capturedAt,
        )

    private fun evidence(
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

    private fun passOrWarning(condition: Boolean): DiagnosticStatus =
        if (condition) DiagnosticStatus.PASS else DiagnosticStatus.WARNING

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
