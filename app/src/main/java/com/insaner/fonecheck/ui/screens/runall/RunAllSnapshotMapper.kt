package com.insaner.fonecheck.ui.screens.runall

import android.os.BatteryManager
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
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
import java.time.Instant

object RunAllSnapshotMapper {
    fun map(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        permissions: RunAllPermissions,
        capturedAt: Instant,
    ): List<DiagnosticCategorySnapshot> {
        val evidenceByCategory =
            mapOf(
                DiagnosticCategoryId.DEVICE to deviceEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.PERFORMANCE to performanceEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.SIM to simEvidence(snapshots, permissions, capturedAt),
                DiagnosticCategoryId.DISPLAY to displayEvidence(snapshots, manual, capturedAt),
                DiagnosticCategoryId.AUDIO to audioEvidence(snapshots, manual, permissions, capturedAt),
                DiagnosticCategoryId.CAMERA to cameraEvidence(snapshots, manual, permissions, capturedAt),
                DiagnosticCategoryId.SENSORS to sensorEvidence(snapshots, manual, capturedAt),
                DiagnosticCategoryId.CONNECTIVITY to connectivityEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.BATTERY to batteryEvidence(snapshots, capturedAt),
                DiagnosticCategoryId.THERMAL to
                    listOf(notTested(DiagnosticCategoryId.THERMAL, "snapshot", capturedAt)),
                DiagnosticCategoryId.STORAGE to
                    listOf(notTested(DiagnosticCategoryId.STORAGE, "snapshot", capturedAt)),
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

    private fun deviceEvidence(
        snapshots: DiagnosticSnapshots,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val device = snapshots.device
        val securityDegraded =
            device.isRooted || device.developerOptionsEnabled || device.usbDebuggingEnabled
        return listOf(
            evidence(
                categoryId = DiagnosticCategoryId.DEVICE,
                id = "identity",
                status = DiagnosticStatus.INFO,
                value = EvidenceValue.IntValue(device.apiLevel),
                capturedAt = capturedAt,
            ),
            evidence(
                categoryId = DiagnosticCategoryId.DEVICE,
                id = "security",
                status = if (securityDegraded) DiagnosticStatus.WARNING else DiagnosticStatus.PASS,
                reason = EvidenceReasonCode.DEGRADED.takeIf { securityDegraded },
                value = EvidenceValue.BooleanValue(!securityDegraded),
                capturedAt = capturedAt,
            ),
        )
    }

    private fun performanceEvidence(
        snapshots: DiagnosticSnapshots,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val performance = snapshots.performance
        val hasRamReading = performance.totalRam.isNotBlank()
        val hasGpuReading = performance.glRenderer != "Unknown"
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
        )
    }

    private fun simEvidence(
        snapshots: DiagnosticSnapshots,
        permissions: RunAllPermissions,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val slotCount = snapshots.sim.simSlots.size
        val inventory =
            if (slotCount == 0) {
                unavailable(DiagnosticCategoryId.SIM, "inventory", capturedAt, EvidenceValue.IntValue(0))
            } else {
                evidence(
                    categoryId = DiagnosticCategoryId.SIM,
                    id = "inventory",
                    status = DiagnosticStatus.PASS,
                    value = EvidenceValue.IntValue(slotCount),
                    unit = EvidenceUnitCode("count"),
                    capturedAt = capturedAt,
                )
            }
        val network =
            if (permissions.phone) {
                evidence(
                    categoryId = DiagnosticCategoryId.SIM,
                    id = "network",
                    status = DiagnosticStatus.INFO,
                    capturedAt = capturedAt,
                )
            } else {
                notTested(
                    categoryId = DiagnosticCategoryId.SIM,
                    id = "network",
                    capturedAt = capturedAt,
                    reason = EvidenceReasonCode.PERMISSION_DENIED,
                )
            }
        return listOf(inventory, network)
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
                evidence(
                    categoryId = DiagnosticCategoryId.AUDIO,
                    id = "microphone",
                    status = if (recorded) DiagnosticStatus.PASS else DiagnosticStatus.FAIL,
                    reason = EvidenceReasonCode.ERROR.takeIf { !recorded },
                    value = EvidenceValue.BooleanValue(recorded),
                    capturedAt = capturedAt,
                )
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
    ): List<DiagnosticEvidence> =
        listOf(
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
        )

    private fun sensorEvidence(
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> =
        listOf(
            if (snapshots.sensors.sensorCount > 0) {
                evidence(
                    categoryId = DiagnosticCategoryId.SENSORS,
                    id = "inventory",
                    status = DiagnosticStatus.PASS,
                    value = EvidenceValue.IntValue(snapshots.sensors.sensorCount),
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
            manualEvidence(DiagnosticCategoryId.SENSORS, "motion", manual.sensors, capturedAt),
        )

    private fun connectivityEvidence(
        snapshots: DiagnosticSnapshots,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val connectivity = snapshots.connectivity
        return listOf(
            availability(
                DiagnosticCategoryId.CONNECTIVITY,
                "wifi",
                connectivity.wifi.isAvailable,
                connectivity.wifi.isConnected,
                capturedAt,
            ),
            availability(
                DiagnosticCategoryId.CONNECTIVITY,
                "bluetooth",
                connectivity.bluetooth.isAvailable,
                connectivity.bluetooth.isEnabled,
                capturedAt,
            ),
            availability(
                DiagnosticCategoryId.CONNECTIVITY,
                "gps",
                connectivity.gps.isAvailable,
                connectivity.gps.isEnabled,
                capturedAt,
            ),
            availability(
                DiagnosticCategoryId.CONNECTIVITY,
                "mobile",
                connectivity.mobileNetwork.isAvailable,
                connectivity.mobileNetwork.isConnected,
                capturedAt,
            ),
        )
    }

    private fun batteryEvidence(
        snapshots: DiagnosticSnapshots,
        capturedAt: Instant,
    ): List<DiagnosticEvidence> {
        val battery = snapshots.battery.basic
        val healthCode = batteryHealthCode(battery.healthStatus)
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
        val temperatureStatus =
            when {
                battery.temperatureCelsius >= 50f -> DiagnosticStatus.FAIL
                battery.temperatureCelsius < 0f || battery.temperatureCelsius > 45f -> DiagnosticStatus.WARNING
                else -> DiagnosticStatus.PASS
            }
        return listOf(
            evidence(
                categoryId = DiagnosticCategoryId.BATTERY,
                id = "health",
                status = healthStatus,
                reason = EvidenceReasonCode.DEGRADED.takeIf { healthStatus != DiagnosticStatus.PASS },
                value = EvidenceValue.StableTextCodeValue(healthCode),
                capturedAt = capturedAt,
            ),
            evidence(
                categoryId = DiagnosticCategoryId.BATTERY,
                id = "temperature",
                status = temperatureStatus,
                reason = EvidenceReasonCode.DEGRADED.takeIf { temperatureStatus != DiagnosticStatus.PASS },
                value = EvidenceValue.DoubleValue(battery.temperatureCelsius.toDouble()),
                unit = EvidenceUnitCode("celsius"),
                capturedAt = capturedAt,
            ),
            evidence(
                categoryId = DiagnosticCategoryId.BATTERY,
                id = "level",
                status = DiagnosticStatus.INFO,
                value = EvidenceValue.IntValue(battery.level),
                unit = EvidenceUnitCode("percent"),
                capturedAt = capturedAt,
            ),
        )
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
