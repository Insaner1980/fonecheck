package com.insaner.fonecheck.ui.screens.runall

import android.content.Context
import android.os.BatteryManager
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.CategoryTestResult
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.domain.model.PerformanceInfo
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
import com.insaner.fonecheck.domain.model.TestCategory
import com.insaner.fonecheck.domain.model.TestResult
import com.insaner.fonecheck.domain.model.TestStatus
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.screens.audio.AudioTestState
import com.insaner.fonecheck.ui.screens.battery.BatteryTestState
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestState
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestState
import com.insaner.fonecheck.ui.screens.camera.CameraTestState
import com.insaner.fonecheck.ui.screens.connectivity.ConnectivityTestState
import com.insaner.fonecheck.ui.screens.display.DisplayTestState
import com.insaner.fonecheck.ui.screens.sensor.SensorTestState
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestState

data class DiagnosticSnapshots(
    val device: DeviceInfo,
    val performance: PerformanceInfo,
    val sim: SimTelephonyInfo,
    val display: DisplayTestState,
    val audio: AudioTestState,
    val camera: CameraTestState,
    val sensors: SensorTestState,
    val connectivity: ConnectivityTestState,
    val battery: BatteryTestState,
    val vibration: VibrationTestState,
    val buttons: ButtonTestState,
    val biometrics: BiometricTestState,
)

object RunAllReportBuilder {
    fun build(
        context: Context,
        snapshots: DiagnosticSnapshots,
        manual: ManualCheckResults,
        permissions: RunAllPermissions,
    ): List<CategoryTestResult> =
        diagnosticDestinations.map { destination ->
            when (destination.category) {
                TestCategory.DEVICE -> systemResults(context, snapshots.device)
                TestCategory.PERFORMANCE -> performanceResults(context, snapshots.performance)
                TestCategory.SIM -> simResults(context, snapshots.sim, permissions.phone)
                TestCategory.DISPLAY -> displayResults(context, snapshots.display, manual.display)
                TestCategory.AUDIO ->
                    audioResults(
                        context,
                        snapshots.audio,
                        manual.speaker,
                        permissions.microphone,
                    )
                TestCategory.CAMERA ->
                    cameraResults(
                        context,
                        snapshots.camera,
                        manual.camera,
                        permissions.camera,
                    )
                TestCategory.SENSORS -> sensorResults(context, snapshots.sensors, manual.sensors)
                TestCategory.CONNECTIVITY -> connectivityResults(context, snapshots.connectivity)
                TestCategory.BATTERY -> batteryResults(context, snapshots.battery)
                TestCategory.VIBRATION -> vibrationResults(context, snapshots.vibration, manual.vibration)
                TestCategory.BUTTONS -> buttonResults(context, snapshots.buttons, manual.buttons)
                TestCategory.BIOMETRICS ->
                    biometricResults(
                        context,
                        snapshots.biometrics,
                        manual.biometrics,
                    )
                else -> error("Unsupported diagnostic category: ${destination.category}")
            }
        }

    private fun systemResults(
        context: Context,
        info: DeviceInfo,
    ): CategoryTestResult {
        val securityStatus =
            if (
                info.isRooted || info.developerOptionsEnabled || info.usbDebuggingEnabled
            ) {
                TestStatus.Warning(context.getString(R.string.run_all_summary_warning))
            } else {
                TestStatus.Pass
            }
        return category(
            context = context,
            category = TestCategory.DEVICE,
            results =
                listOf(
                    result(
                        context,
                        TestCategory.DEVICE,
                        "identity",
                        R.string.device_info_title,
                        TestStatus.Pass,
                        "${info.manufacturer} ${info.model}, Android ${info.androidVersion}",
                    ),
                    result(
                        context,
                        TestCategory.DEVICE,
                        "security",
                        R.string.run_all_check_security,
                        securityStatus,
                        context.getString(
                            R.string.run_all_detail_security,
                            yesNo(context, info.isRooted),
                            yesNo(context, info.developerOptionsEnabled),
                            yesNo(context, info.usbDebuggingEnabled),
                        ),
                    ),
                ),
        )
    }

    private fun performanceResults(
        context: Context,
        info: PerformanceInfo,
    ): CategoryTestResult =
        category(
            context = context,
            category = TestCategory.PERFORMANCE,
            results =
                listOf(
                    result(
                        context,
                        TestCategory.PERFORMANCE,
                        "cpu",
                        R.string.perf_cpu_title,
                        if (info.cpuCores > 0) TestStatus.Pass else TestStatus.Warning(),
                        context.resources.getQuantityString(
                            R.plurals.run_all_detail_cpu,
                            info.cpuCores,
                            info.cpuCores,
                            info.cpuArchitecture,
                        ),
                        info.cpuConfidence,
                    ),
                    result(
                        context,
                        TestCategory.PERFORMANCE,
                        "ram",
                        R.string.perf_ram_title,
                        if (info.totalRam.isNotBlank()) TestStatus.Pass else TestStatus.Warning(),
                        context.getString(R.string.run_all_detail_ram, info.totalRam, info.availableRam),
                        info.ramConfidence,
                    ),
                    result(
                        context,
                        TestCategory.PERFORMANCE,
                        "gpu",
                        R.string.perf_gpu_title,
                        if (info.glRenderer != "Unknown") TestStatus.Pass else TestStatus.Warning(),
                        context.getString(
                            R.string.run_all_detail_gpu,
                            info.glRenderer,
                            yesNo(context, info.vulkanSupported),
                        ),
                        info.gpuConfidence,
                    ),
                ),
        )

    private fun simResults(
        context: Context,
        info: SimTelephonyInfo,
        hasPermission: Boolean,
    ): CategoryTestResult {
        val slotResults =
            info.simSlots.map { slot ->
                val status =
                    when (slot.status) {
                        "Present" -> TestStatus.Pass
                        "Absent" -> TestStatus.NotAvailable
                        "Card I/O error", "Permanently disabled" -> TestStatus.Fail(slot.status)
                        else -> TestStatus.Warning(slot.status)
                    }
                result(
                    context,
                    TestCategory.SIM,
                    "slot_${slot.slotIndex}",
                    R.string.sim_slot_title,
                    status,
                    context.getString(
                        R.string.run_all_detail_sim,
                        slot.status,
                        slot.operatorName,
                        slot.networkType,
                    ),
                    nameFormatArgs = arrayOf(slot.slotIndex + 1),
                )
            }
        val networkResult =
            result(
                context,
                TestCategory.SIM,
                "network",
                R.string.conn_mobile_network_type,
                if (hasPermission) TestStatus.Info(info.dataNetworkType) else TestStatus.NotTested,
                if (hasPermission) info.dataNetworkType else context.getString(R.string.run_all_permission_missing),
                if (hasPermission) Confidence.HIGH else Confidence.UNAVAILABLE,
            )
        return category(context, TestCategory.SIM, slotResults + networkResult)
    }

    private fun displayResults(
        context: Context,
        state: DisplayTestState,
        manualValue: Boolean?,
    ): CategoryTestResult =
        category(
            context = context,
            category = TestCategory.DISPLAY,
            results =
                listOf(
                    result(
                        context,
                        TestCategory.DISPLAY,
                        "display_info",
                        R.string.display_info_title,
                        if (state.info.widthPx > 0 && state.info.heightPx > 0) {
                            TestStatus.Pass
                        } else {
                            TestStatus.Warning()
                        },
                        context.getString(
                            R.string.run_all_detail_display,
                            state.info.widthPx,
                            state.info.heightPx,
                            state.info.refreshRate,
                        ),
                    ),
                    manualResult(
                        context,
                        TestCategory.DISPLAY,
                        "visual",
                        R.string.run_all_check_visual_display,
                        manualValue,
                    ),
                ),
        )

    private fun audioResults(
        context: Context,
        state: AudioTestState,
        speakerResult: Boolean?,
        hasMicrophonePermission: Boolean,
    ): CategoryTestResult =
        category(
            context = context,
            category = TestCategory.AUDIO,
            results =
                listOf(
                    manualResult(
                        context,
                        TestCategory.AUDIO,
                        "speaker",
                        R.string.run_all_check_speaker,
                        speakerResult,
                    ),
                    result(
                        context,
                        TestCategory.AUDIO,
                        "microphone",
                        R.string.run_all_check_microphone,
                        when {
                            !hasMicrophonePermission -> TestStatus.NotTested
                            state.hasRecordedAudio -> TestStatus.Pass
                            else -> TestStatus.Fail(context.getString(R.string.run_all_no_audio_data))
                        },
                        when {
                            !hasMicrophonePermission -> context.getString(R.string.run_all_permission_missing)
                            state.hasRecordedAudio -> context.getString(R.string.run_all_audio_recorded)
                            else -> context.getString(R.string.run_all_no_audio_data)
                        },
                        if (hasMicrophonePermission) Confidence.HIGH else Confidence.UNAVAILABLE,
                    ),
                    result(
                        context,
                        TestCategory.AUDIO,
                        "headphones",
                        R.string.run_all_check_headphones,
                        TestStatus.Info(state.headphoneType ?: context.getString(R.string.status_no)),
                        state.headphoneType ?: context.getString(R.string.run_all_no_headphones),
                    ),
                ),
        )

    private fun cameraResults(
        context: Context,
        state: CameraTestState,
        manualValue: Boolean?,
        hasPermission: Boolean,
    ): CategoryTestResult =
        category(
            context = context,
            category = TestCategory.CAMERA,
            results =
                listOf(
                    result(
                        context,
                        TestCategory.CAMERA,
                        "rear",
                        R.string.camera_rear,
                        if (state.rearCapabilities != null) TestStatus.Pass else TestStatus.NotAvailable,
                        state.rearCapabilities?.maxResolution ?: context.getString(R.string.conn_not_available),
                    ),
                    result(
                        context,
                        TestCategory.CAMERA,
                        "front",
                        R.string.camera_front,
                        if (state.frontCapabilities != null) TestStatus.Pass else TestStatus.NotAvailable,
                        state.frontCapabilities?.maxResolution ?: context.getString(R.string.conn_not_available),
                    ),
                    manualResult(
                        context,
                        TestCategory.CAMERA,
                        "capture",
                        R.string.camera_capture_title,
                        if (hasPermission) manualValue else null,
                    ),
                ),
        )

    private fun sensorResults(
        context: Context,
        state: SensorTestState,
        manualValue: Boolean?,
    ): CategoryTestResult =
        category(
            context = context,
            category = TestCategory.SENSORS,
            results =
                listOf(
                    result(
                        context,
                        TestCategory.SENSORS,
                        "inventory",
                        R.string.sensor_count,
                        if (state.sensorCount > 0) TestStatus.Pass else TestStatus.NotAvailable,
                        context.resources.getQuantityString(
                            R.plurals.run_all_detail_sensor_count,
                            state.sensorCount,
                            state.sensorCount,
                        ),
                    ),
                    manualResult(
                        context,
                        TestCategory.SENSORS,
                        "motion",
                        R.string.run_all_check_motion_sensor,
                        manualValue,
                    ),
                ),
        )

    private fun connectivityResults(
        context: Context,
        state: ConnectivityTestState,
    ): CategoryTestResult =
        category(
            context = context,
            category = TestCategory.CONNECTIVITY,
            results =
                listOf(
                    availabilityResult(
                        context,
                        TestCategory.CONNECTIVITY,
                        "wifi",
                        R.string.conn_wifi_title,
                        state.wifi.isAvailable,
                        state.wifi.isConnected,
                    ),
                    availabilityResult(
                        context,
                        TestCategory.CONNECTIVITY,
                        "bluetooth",
                        R.string.conn_bluetooth_title,
                        state.bluetooth.isAvailable,
                        state.bluetooth.isEnabled,
                    ),
                    availabilityResult(
                        context,
                        TestCategory.CONNECTIVITY,
                        "gps",
                        R.string.conn_gps_title,
                        state.gps.isAvailable,
                        state.gps.isEnabled,
                    ),
                    availabilityResult(
                        context,
                        TestCategory.CONNECTIVITY,
                        "mobile",
                        R.string.conn_mobile_title,
                        state.mobileNetwork.isAvailable,
                        state.mobileNetwork.isConnected,
                    ),
                ),
        )

    private fun batteryResults(
        context: Context,
        state: BatteryTestState,
    ): CategoryTestResult {
        val healthStatus =
            when (state.basic.healthStatus) {
                BatteryManager.BATTERY_HEALTH_GOOD -> TestStatus.Pass
                BatteryManager.BATTERY_HEALTH_OVERHEAT,
                BatteryManager.BATTERY_HEALTH_DEAD,
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE,
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE,
                -> TestStatus.Fail(context.getString(state.health.healthStatusLabel))
                else -> TestStatus.Warning(context.getString(state.health.healthStatusLabel))
            }
        val temperatureStatus =
            when {
                state.basic.temperatureCelsius >= 50f -> TestStatus.Fail()
                state.basic.temperatureCelsius < 0f || state.basic.temperatureCelsius > 45f -> TestStatus.Warning()
                else -> TestStatus.Pass
            }
        return category(
            context = context,
            category = TestCategory.BATTERY,
            results =
                listOf(
                    result(
                        context,
                        TestCategory.BATTERY,
                        "health",
                        R.string.batt_health_title,
                        healthStatus,
                        context.getString(state.health.healthStatusLabel),
                    ),
                    result(
                        context,
                        TestCategory.BATTERY,
                        "temperature",
                        R.string.batt_temperature,
                        temperatureStatus,
                        context.getString(
                            R.string.run_all_detail_temperature,
                            state.basic.temperatureCelsius,
                        ),
                    ),
                    result(
                        context,
                        TestCategory.BATTERY,
                        "level",
                        R.string.batt_level,
                        TestStatus.Info("${state.basic.level}%"),
                        "${state.basic.level}%",
                    ),
                ),
        )
    }

    private fun vibrationResults(
        context: Context,
        state: VibrationTestState,
        manualValue: Boolean?,
    ): CategoryTestResult =
        category(
            context = context,
            category = TestCategory.VIBRATION,
            results =
                listOf(
                    result(
                        context,
                        TestCategory.VIBRATION,
                        "hardware",
                        R.string.vibration_has_vibrator,
                        if (state.haptic.hasVibrator) TestStatus.Pass else TestStatus.NotAvailable,
                        context.resources.getQuantityString(
                            R.plurals.run_all_detail_haptics,
                            state.haptic.supportedEffectsCount,
                            yesNo(context, state.haptic.hasAmplitudeControl),
                            state.haptic.supportedEffectsCount,
                        ),
                    ),
                    manualResult(
                        context,
                        TestCategory.VIBRATION,
                        "motor",
                        R.string.vibration_motor_title,
                        if (state.haptic.hasVibrator) manualValue else null,
                    ),
                ),
        )

    private fun buttonResults(
        context: Context,
        state: ButtonTestState,
        manualValue: Boolean?,
    ): CategoryTestResult =
        category(
            context = context,
            category = TestCategory.BUTTONS,
            results =
                listOf(
                    manualResult(
                        context,
                        TestCategory.BUTTONS,
                        "volume",
                        R.string.run_all_check_volume_buttons,
                        manualValue,
                        context.getString(
                            R.string.run_all_detail_buttons,
                            detected(context, state.volumeUpDetected),
                            detected(context, state.volumeDownDetected),
                        ),
                    ),
                    result(
                        context,
                        TestCategory.BUTTONS,
                        "power",
                        R.string.button_power,
                        TestStatus.Info(context.getString(R.string.button_power_note)),
                        context.getString(R.string.button_power_note),
                        Confidence.LOW,
                    ),
                ),
        )

    private fun biometricResults(
        context: Context,
        state: BiometricTestState,
        manualValue: Boolean?,
    ): CategoryTestResult {
        val available = state.capability.strongAvailable || state.capability.weakAvailable
        return category(
            context = context,
            category = TestCategory.BIOMETRICS,
            results =
                listOf(
                    result(
                        context,
                        TestCategory.BIOMETRICS,
                        "capability",
                        R.string.biometric_capabilities_title,
                        if (available) TestStatus.Pass else TestStatus.NotAvailable,
                        when {
                            state.capability.strongAvailable -> context.getString(R.string.biometric_strong)
                            state.capability.weakAvailable -> context.getString(R.string.biometric_weak)
                            else -> context.getString(R.string.biometric_not_available)
                        },
                    ),
                    manualResult(
                        context,
                        TestCategory.BIOMETRICS,
                        "authentication",
                        R.string.biometric_test_auth,
                        if (available) manualValue else null,
                    ),
                ),
        )
    }

    private fun availabilityResult(
        context: Context,
        category: TestCategory,
        id: String,
        nameResId: Int,
        available: Boolean,
        enabled: Boolean,
    ): TestResult =
        result(
            context,
            category,
            id,
            nameResId,
            when {
                !available -> TestStatus.NotAvailable
                enabled -> TestStatus.Pass
                else -> TestStatus.Warning(context.getString(R.string.status_disabled))
            },
            when {
                !available -> context.getString(R.string.conn_not_available)
                enabled -> context.getString(R.string.status_enabled)
                else -> context.getString(R.string.status_disabled)
            },
        )

    private fun manualResult(
        context: Context,
        category: TestCategory,
        id: String,
        nameResId: Int,
        value: Boolean?,
        detailOverride: String? = null,
    ): TestResult =
        result(
            context,
            category,
            id,
            nameResId,
            when (value) {
                true -> TestStatus.Pass
                false -> TestStatus.Fail(context.getString(R.string.run_all_manual_failed))
                null -> TestStatus.NotTested
            },
            detailOverride ?: when (value) {
                true -> context.getString(R.string.run_all_manual_confirmed)
                false -> context.getString(R.string.run_all_manual_failed)
                null -> context.getString(R.string.run_all_manual_skipped)
            },
            if (value == null) Confidence.UNAVAILABLE else Confidence.HIGH,
        )

    private fun category(
        context: Context,
        category: TestCategory,
        results: List<TestResult>,
    ): CategoryTestResult {
        val status = aggregateStatus(results)
        val summary =
            context.getString(
                when (status) {
                    TestStatus.Pass, is TestStatus.Info -> R.string.run_all_summary_pass
                    is TestStatus.Warning -> R.string.run_all_summary_warning
                    is TestStatus.Fail -> R.string.run_all_summary_fail
                    TestStatus.NotAvailable -> R.string.run_all_summary_unavailable
                    TestStatus.NotTested -> R.string.run_all_summary_not_tested
                },
            )
        return CategoryTestResult(category, status, summary, results)
    }

    private fun aggregateStatus(results: List<TestResult>): TestStatus =
        when {
            results.any { it.status is TestStatus.Fail } -> TestStatus.Fail()
            results.any { it.status is TestStatus.Warning } -> TestStatus.Warning()
            results.all { it.status == TestStatus.NotAvailable } -> TestStatus.NotAvailable
            results.all { it.status == TestStatus.NotTested } -> TestStatus.NotTested
            results.any { it.status == TestStatus.NotTested } -> TestStatus.Warning()
            else -> TestStatus.Pass
        }

    private fun result(
        context: Context,
        category: TestCategory,
        id: String,
        nameResId: Int,
        status: TestStatus,
        detail: String,
        confidence: Confidence = Confidence.HIGH,
        nameFormatArgs: Array<out Any> = emptyArray(),
    ): TestResult =
        TestResult(
            id = "${category.name.lowercase()}_$id",
            name = context.getString(nameResId, *nameFormatArgs),
            status = status,
            detail = detail,
            confidence = confidence,
            timestamp = System.currentTimeMillis(),
        )

    private fun yesNo(
        context: Context,
        value: Boolean,
    ): String =
        context.getString(
            if (value) R.string.status_yes else R.string.status_no,
        )

    private fun detected(
        context: Context,
        value: Boolean,
    ): String =
        context.getString(
            if (value) R.string.button_detected else R.string.button_not_detected,
        )
}
