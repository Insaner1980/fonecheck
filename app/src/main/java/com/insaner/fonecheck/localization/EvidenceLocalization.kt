package com.insaner.fonecheck.localization

import androidx.annotation.StringRes
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.domain.observation.NotMeasuredKind
import com.insaner.fonecheck.domain.observation.ObservationClassification
import com.insaner.fonecheck.domain.observation.ObservationReason
import com.insaner.fonecheck.domain.observation.ObservationState

@StringRes
fun evidenceReasonStringRes(reason: EvidenceReasonCode): Int? =
    ObservationReason.entries
        .firstOrNull { it.stableCode == reason.value }
        ?.let(::observationReasonStringRes)
        ?: legacyEvidenceReasonStringRes(reason)

internal fun EvidenceReasonCode.isRestatedByNotMeasuredValue(): Boolean =
    this == EvidenceReasonCode.NOT_RUN || value == ObservationReason.TEST_NOT_RUN.stableCode

internal fun shouldShowEvidenceReason(
    status: DiagnosticStatus,
    reason: EvidenceReasonCode,
): Boolean = status != DiagnosticStatus.NOT_TESTED || !reason.isRestatedByNotMeasuredValue()

@StringRes
@Suppress(
    "CyclomaticComplexMethod",
    "kotlin:S1479", // Exhaustive mapping keeps every observation reason compile-time checked.
)
fun observationReasonStringRes(reason: ObservationReason): Int =
    when (reason) {
        ObservationReason.ROOT_ARTIFACT_PRESENT -> R.string.device_root_finding_note
        ObservationReason.DEVELOPER_OPTIONS_ENABLED -> R.string.device_developer_options_note
        ObservationReason.USB_DEBUGGING_ENABLED -> R.string.device_usb_debugging_note
        ObservationReason.SERIAL_RESTRICTED -> R.string.device_serial_restricted_note
        ObservationReason.CPU_READING_UNAVAILABLE -> R.string.observation_reason_cpu_reading_unavailable
        ObservationReason.RAM_READING_UNAVAILABLE -> R.string.observation_reason_ram_reading_unavailable
        ObservationReason.GPU_READING_UNAVAILABLE -> R.string.observation_reason_gpu_reading_unavailable
        ObservationReason.DISPLAY_READING_UNAVAILABLE -> R.string.observation_reason_display_reading_unavailable
        ObservationReason.SIM_INVENTORY_UNKNOWN -> R.string.observation_reason_sim_inventory_unknown
        ObservationReason.SIM_NOT_PRESENT -> R.string.observation_reason_sim_not_present
        ObservationReason.SIM_INACTIVE -> R.string.observation_reason_sim_inactive
        ObservationReason.SIM_SLOT_UNKNOWN -> R.string.observation_reason_sim_slot_unknown
        ObservationReason.SIM_NETWORK_LOCKED -> R.string.observation_reason_sim_network_locked
        ObservationReason.SIM_PIN_REQUIRED -> R.string.observation_reason_sim_pin_required
        ObservationReason.SIM_PUK_REQUIRED -> R.string.observation_reason_sim_puk_required
        ObservationReason.SIM_NOT_READY -> R.string.observation_reason_sim_not_ready
        ObservationReason.SIM_PERMANENTLY_DISABLED -> R.string.observation_reason_sim_permanently_disabled
        ObservationReason.SIM_CARD_IO_ERROR -> R.string.observation_reason_sim_card_io_error
        ObservationReason.SIM_CARD_RESTRICTED -> R.string.observation_reason_sim_card_restricted
        ObservationReason.CAMERA_MEASUREMENT_ERROR -> R.string.observation_reason_camera_measurement_error
        ObservationReason.SENSOR_MEASUREMENT_ERROR -> R.string.observation_reason_sensor_measurement_error
        ObservationReason.GPS_DISABLED -> R.string.observation_reason_gps_disabled
        ObservationReason.GPS_NOT_RUN -> R.string.observation_reason_gps_not_run
        ObservationReason.GPS_IN_PROGRESS -> R.string.observation_reason_gps_in_progress
        ObservationReason.GPS_TIMEOUT -> R.string.observation_reason_gps_timeout
        ObservationReason.GPS_START_FAILED -> R.string.observation_reason_gps_start_failed
        ObservationReason.BATTERY_HEALTH_UNAVAILABLE -> R.string.observation_reason_battery_health_unavailable
        ObservationReason.BATTERY_OVERHEAT -> R.string.observation_reason_battery_overheat
        ObservationReason.BATTERY_DEAD -> R.string.observation_reason_battery_dead
        ObservationReason.BATTERY_OVER_VOLTAGE -> R.string.observation_reason_battery_over_voltage
        ObservationReason.BATTERY_UNSPECIFIED_FAILURE ->
            R.string.observation_reason_battery_unspecified_failure
        ObservationReason.BATTERY_COLD -> R.string.observation_reason_battery_cold
        ObservationReason.BATTERY_TEMPERATURE_UNAVAILABLE ->
            R.string.observation_reason_battery_temperature_unavailable
        ObservationReason.BATTERY_TEMPERATURE_COLD -> R.string.observation_reason_battery_temperature_cold
        ObservationReason.BATTERY_TEMPERATURE_HIGH -> R.string.observation_reason_battery_temperature_high
        ObservationReason.BATTERY_TEMPERATURE_CRITICAL ->
            R.string.observation_reason_battery_temperature_critical
        ObservationReason.THERMAL_STATUS_UNAVAILABLE -> R.string.observation_reason_thermal_status_unavailable
        ObservationReason.THERMAL_MANAGEMENT_ACTIVE -> R.string.observation_reason_thermal_management_active
        ObservationReason.THERMAL_SEVERE_WITHOUT_APP_LOAD ->
            R.string.observation_reason_thermal_severe_without_app_load
        ObservationReason.BUTTON_TEST_NOT_RUN -> R.string.observation_reason_button_test_not_run
        ObservationReason.BUTTON_TEST_IN_PROGRESS -> R.string.observation_reason_button_test_in_progress
        ObservationReason.BUTTON_TEST_TIMEOUT -> R.string.observation_reason_button_test_timeout
        ObservationReason.BIOMETRIC_NOT_RUN -> R.string.observation_reason_biometric_not_run
        ObservationReason.BIOMETRIC_IN_PROGRESS -> R.string.observation_reason_biometric_in_progress
        ObservationReason.BIOMETRIC_NOT_RECOGNIZED -> R.string.observation_reason_biometric_not_recognized
        ObservationReason.BIOMETRIC_CANCELLED -> R.string.observation_reason_biometric_cancelled
        ObservationReason.BIOMETRIC_LOCKOUT -> R.string.observation_reason_biometric_lockout
        ObservationReason.BIOMETRIC_NOT_ENROLLED -> R.string.observation_reason_biometric_not_enrolled
        ObservationReason.BIOMETRIC_SECURITY_UPDATE_REQUIRED ->
            R.string.observation_reason_biometric_security_update_required
        ObservationReason.BIOMETRIC_UNAVAILABLE -> R.string.observation_reason_biometric_unavailable
        ObservationReason.BIOMETRIC_ERROR -> R.string.observation_reason_biometric_error
        ObservationReason.PERMISSION_NOT_REQUESTED -> R.string.observation_reason_permission_not_requested
        ObservationReason.PERMISSION_DENIED -> R.string.observation_reason_permission_denied
        ObservationReason.PERMISSION_OPEN_SETTINGS -> R.string.observation_reason_permission_open_settings
        ObservationReason.PERMISSION_PARTIAL -> R.string.observation_reason_permission_partial
        ObservationReason.HARDWARE_UNAVAILABLE -> R.string.observation_reason_hardware_unavailable
        ObservationReason.VALUE_NOT_EXPOSED -> R.string.observation_reason_value_not_exposed
        ObservationReason.ANDROID_VERSION_UNSUPPORTED -> R.string.observation_reason_android_version_unsupported
        ObservationReason.PLATFORM_RESTRICTION -> R.string.observation_reason_platform_restriction
        ObservationReason.TEST_NOT_RUN -> R.string.observation_reason_test_not_run
        ObservationReason.MEASUREMENT_IN_PROGRESS -> R.string.observation_reason_measurement_in_progress
        ObservationReason.TEST_SKIPPED -> R.string.observation_reason_test_skipped
        ObservationReason.TEST_CANCELLED -> R.string.observation_reason_test_cancelled
        ObservationReason.MEASUREMENT_TIMEOUT -> R.string.observation_reason_measurement_timeout
        ObservationReason.MEASUREMENT_ERROR -> R.string.observation_reason_measurement_error
        ObservationReason.INSUFFICIENT_SPACE -> R.string.observation_reason_insufficient_space
        ObservationReason.USER_CONFIRMED_DISPLAY_FAILURE ->
            R.string.observation_reason_user_confirmed_display_failure
        ObservationReason.USER_CONFIRMED_AUDIO_FAILURE ->
            R.string.observation_reason_user_confirmed_audio_failure
        ObservationReason.USER_CONFIRMED_CAMERA_FAILURE ->
            R.string.observation_reason_user_confirmed_camera_failure
        ObservationReason.USER_CONFIRMED_VIBRATION_FAILURE ->
            R.string.observation_reason_user_confirmed_vibration_failure
    }

@StringRes
fun observationStatusStringRes(classification: ObservationClassification): Int =
    when (classification.state) {
        ObservationState.PASS -> R.string.run_all_status_pass
        ObservationState.FAULT -> R.string.run_all_status_fail
        ObservationState.NOTED -> R.string.run_all_status_warning
        ObservationState.NOT_MEASURED ->
            if (classification.notMeasuredKind == NotMeasuredKind.UNAVAILABLE) {
                R.string.run_all_status_unavailable
            } else {
                R.string.status_not_measured
            }
    }

@StringRes
private fun legacyEvidenceReasonStringRes(reason: EvidenceReasonCode): Int? =
    when (reason) {
        EvidenceReasonCode("sensor_response_only") -> R.string.sensor_response_only
        EvidenceReasonCode("sensor_response_unreliable") -> R.string.sensor_response_unreliable
        EvidenceReasonCode("sensor_response_limited_accuracy") -> R.string.sensor_response_limited_accuracy
        EvidenceReasonCode("sensor_response_accuracy_unknown") -> R.string.sensor_response_accuracy_unknown
        EvidenceReasonCode.PERMISSION_DENIED -> R.string.run_all_permission_missing
        EvidenceReasonCode.NOT_RUN -> R.string.run_all_summary_not_tested
        EvidenceReasonCode.SKIPPED,
        EvidenceReasonCode.CANCELLED,
        -> R.string.run_all_manual_skipped
        EvidenceReasonCode.TIMEOUT -> R.string.conn_gps_failed
        EvidenceReasonCode.INSUFFICIENT_SPACE -> R.string.storage_benchmark_insufficient_space
        EvidenceReasonCode.USER_CONFIRMED_FAILURE -> R.string.run_all_manual_failed
        EvidenceReasonCode.HARDWARE_UNAVAILABLE -> R.string.run_all_summary_unavailable
        EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED -> R.string.report_reason_android_version_unsupported
        EvidenceReasonCode.PLATFORM_RESTRICTION -> R.string.button_power_unavailable
        EvidenceReasonCode.BIOMETRIC_LOCKOUT -> R.string.biometric_locked_out
        EvidenceReasonCode.BIOMETRIC_NOT_ENROLLED -> R.string.biometric_none_enrolled
        EvidenceReasonCode.DISABLED -> R.string.status_disabled
        EvidenceReasonCode.DEGRADED -> R.string.run_all_summary_warning
        EvidenceReasonCode.ERROR -> R.string.report_reason_error
        else -> null
    }

@StringRes
fun thermalStatusStringRes(status: ThermalStatusCode): Int =
    when (status) {
        ThermalStatusCode.NONE -> R.string.perf_thermal_none
        ThermalStatusCode.LIGHT -> R.string.perf_thermal_light
        ThermalStatusCode.MODERATE -> R.string.perf_thermal_moderate
        ThermalStatusCode.SEVERE -> R.string.perf_thermal_severe
        ThermalStatusCode.CRITICAL -> R.string.perf_thermal_critical
        ThermalStatusCode.EMERGENCY -> R.string.perf_thermal_emergency
        ThermalStatusCode.SHUTDOWN -> R.string.perf_thermal_shutdown
        ThermalStatusCode.UNAVAILABLE -> R.string.device_value_unavailable
    }

data class EvidenceLabelResource(
    @StringRes val stringResId: Int,
    val formatArgument: Int? = null,
)

fun evidenceLabelResource(evidence: DiagnosticEvidence): EvidenceLabelResource? =
    if (evidence.checkId.value == "camera.capture_dimensions" && evidence.value is EvidenceValue.LongValue) {
        EvidenceLabelResource(R.string.camera_last_image_pixel_count)
    } else {
        evidenceLabelResource(evidence.checkId.value)
    }

fun evidenceLabelResource(checkId: String): EvidenceLabelResource? =
    EVIDENCE_LABEL_RESOURCES[checkId]?.let { EvidenceLabelResource(it) }
        ?: SIM_SLOT_STATE_CHECK_ID
            .matchEntire(checkId)
            ?.groupValues
            ?.get(1)
            ?.toIntOrNull()
            ?.takeIf { it < Int.MAX_VALUE }
            ?.let { slotIndex -> EvidenceLabelResource(R.string.sim_slot_title, slotIndex + 1) }

@StringRes
fun stableTextStringRes(code: String): Int? = STABLE_TEXT_RESOURCES[code]

fun stableCodeDisplayText(code: String): String =
    code.replace('_', ' ').replaceFirstChar { character -> character.titlecase() }

private val EVIDENCE_LABEL_RESOURCES =
    mapOf(
        "device.identity" to R.string.device_info_title,
        "device.security" to R.string.label_root_artifact,
        "device.developer_options" to R.string.label_developer_options,
        "device.usb_debugging" to R.string.label_usb_debugging,
        "performance.cpu" to R.string.perf_cpu_title,
        "performance.ram" to R.string.perf_ram_title,
        "performance.gpu" to R.string.perf_gpu_title,
        "performance.cpu_benchmark" to R.string.perf_benchmark_cpu_rate,
        "performance.memory_benchmark" to R.string.perf_benchmark_memory_rate,
        "sim.inventory" to R.string.sim_telephony_title,
        "sim.network" to R.string.conn_mobile_network_type,
        "display.info" to R.string.display_info_title,
        "display.visual" to R.string.run_all_check_visual_display,
        "audio.speaker" to R.string.run_all_check_speaker,
        "audio.microphone" to R.string.run_all_check_microphone,
        "audio.headphones" to R.string.run_all_check_headphones,
        "camera.rear" to R.string.camera_rear,
        "camera.front" to R.string.camera_front,
        "camera.capture" to R.string.camera_capture_title,
        "camera.inventory" to R.string.camera_inventory_label,
        "camera.logical_count" to R.string.camera_logical_count_label,
        "camera.capture_dimensions" to R.string.camera_last_image_dimensions,
        "sensors.inventory" to R.string.sensor_count,
        "sensors.accelerometer" to R.string.sensor_type_accelerometer,
        "sensors.gyroscope" to R.string.sensor_type_gyroscope,
        "sensors.gravity" to R.string.sensor_type_gravity,
        "sensors.proximity" to R.string.sensor_type_proximity,
        "sensors.light" to R.string.sensor_type_light,
        "sensors.magnetometer" to R.string.sensor_type_magnetometer,
        "sensors.barometer" to R.string.sensor_type_barometer,
        "sensors.step" to R.string.sensor_type_step,
        "sensors.orientation" to R.string.sensor_orientation,
        "sensors.motion" to R.string.run_all_check_motion_sensor,
        "connectivity.wifi" to R.string.conn_wifi_title,
        "connectivity.bluetooth" to R.string.conn_bluetooth_title,
        "connectivity.nfc" to R.string.conn_nfc_title,
        "connectivity.nfc_hce" to R.string.conn_nfc_hce,
        "connectivity.gps" to R.string.conn_gps_title,
        "connectivity.mobile" to R.string.conn_mobile_title,
        "battery.health" to R.string.batt_health_title,
        "battery.temperature" to R.string.batt_temperature,
        "battery.level" to R.string.batt_level,
        "battery.current_now" to R.string.batt_charging_current,
        "battery.current_direction" to R.string.batt_current_direction,
        "battery.current_interpretation" to R.string.batt_current_interpretation,
        "battery.current_profile" to R.string.batt_mfr_profile,
        "battery.cycle_count" to R.string.batt_cycle_count,
        "thermal.status" to R.string.thermal_status_title,
        "thermal.severity" to R.string.thermal_severity_label,
        "thermal.headroom" to R.string.thermal_headroom_title,
        "thermal.battery_temperature" to R.string.thermal_battery_title,
        "storage.total" to R.string.storage_total,
        "storage.used" to R.string.storage_used,
        "storage.available" to R.string.storage_available,
        "storage.usage" to R.string.storage_usage,
        "storage.internal_access" to R.string.storage_internal_access,
        "storage.volume_count" to R.string.storage_volumes_title,
        "storage.mounted_volume_count" to R.string.storage_mounted_state,
        "storage.removable_volume_count" to R.string.storage_removable,
        "storage.sequential_write" to R.string.storage_benchmark_write_rate,
        "storage.sequential_read" to R.string.storage_benchmark_read_rate,
        "storage.benchmark_data_size" to R.string.storage_benchmark_data_size,
        "storage.benchmark_available_before" to R.string.storage_benchmark_available_before,
        "storage.benchmark_location" to R.string.storage_benchmark_location,
        "storage.benchmark_cleanup" to R.string.storage_benchmark_cleanup,
        "vibration.hardware" to R.string.vibration_has_vibrator,
        "vibration.amplitude_control" to R.string.vibration_amplitude_control,
        "vibration.effects" to R.string.vibration_effects_supported,
        "vibration.primitives" to R.string.vibration_primitives_supported,
        "vibration.motor" to R.string.vibration_motor_title,
        "buttons.volume" to R.string.run_all_check_volume_buttons,
        "buttons.power" to R.string.button_power,
        "biometrics.capability" to R.string.biometric_capabilities_title,
        "biometrics.fingerprint_hardware" to R.string.biometric_fingerprint_hardware,
        "biometrics.face_hardware" to R.string.biometric_face_hardware,
        "biometrics.strong_capability" to R.string.biometric_strong,
        "biometrics.weak_capability" to R.string.biometric_weak,
        "biometrics.authentication" to R.string.biometric_test_auth,
    )

private val STABLE_TEXT_RESOURCES =
    mapOf(
        "good" to R.string.batt_health_good,
        "overheat" to R.string.batt_health_overheat,
        "dead" to R.string.batt_health_dead,
        "over_voltage" to R.string.batt_health_over_voltage,
        "unspecified_failure" to R.string.batt_health_failure,
        "cold" to R.string.batt_health_cold,
        "charging" to R.string.batt_current_direction_charging,
        "discharging" to R.string.batt_current_direction_discharging,
        "idle" to R.string.batt_current_direction_idle,
        "api_sign" to R.string.batt_current_sign_api,
        "status_sign_normalized" to R.string.batt_current_sign_status,
        "samsung" to R.string.batt_mfr_samsung,
        "oneplus" to R.string.batt_mfr_oneplus,
        "google_pixel" to R.string.batt_mfr_pixel,
        "generic" to R.string.batt_mfr_generic,
        "none" to R.string.perf_thermal_none,
        "normal" to R.string.thermal_severity_normal,
        "light" to R.string.perf_thermal_light,
        "moderate" to R.string.perf_thermal_moderate,
        "severe" to R.string.perf_thermal_severe,
        "critical" to R.string.perf_thermal_critical,
        "emergency" to R.string.perf_thermal_emergency,
        "shutdown" to R.string.perf_thermal_shutdown,
        "strong" to R.string.biometric_strong,
        "weak" to R.string.biometric_weak,
        "unavailable" to R.string.biometric_not_available,
        "available" to R.string.biometric_available,
        "no_hardware" to R.string.biometric_no_hardware,
        "hardware_unavailable" to R.string.biometric_hardware_unavailable,
        "none_enrolled" to R.string.biometric_none_enrolled,
        "security_update_required" to R.string.biometric_security_update_required,
        "unsupported" to R.string.biometric_unsupported,
        "known_artifact_detected" to R.string.device_root_artifact_detected,
        "no_known_artifact_detected" to R.string.device_root_artifact_not_detected,
        "no_telephony" to R.string.sim_inventory_no_telephony,
        "no_sim" to R.string.sim_inventory_no_sim,
        "inactive_sim" to R.string.sim_inventory_inactive,
        "single_sim" to R.string.sim_inventory_single,
        "multiple_sim" to R.string.sim_inventory_multiple,
        "second_generation" to R.string.sim_network_2g,
        "third_generation" to R.string.sim_network_3g,
        "fourth_generation" to R.string.sim_network_4g,
        "fifth_generation" to R.string.sim_network_5g,
        "unknown" to R.string.sim_value_unknown,
        "ready" to R.string.sim_state_ready,
        "absent" to R.string.sim_state_absent,
        "network_locked" to R.string.sim_state_network_locked,
        "pin_required" to R.string.sim_state_pin_required,
        "puk_required" to R.string.sim_state_puk_required,
        "not_ready" to R.string.sim_state_not_ready,
        "permanently_disabled" to R.string.sim_state_permanently_disabled,
        "card_io_error" to R.string.sim_state_card_io_error,
        "card_restricted" to R.string.sim_state_card_restricted,
        "observed" to R.string.sensor_orientation_observed,
        "app_cache" to R.string.storage_app_cache,
    )

private val SIM_SLOT_STATE_CHECK_ID = Regex("""sim\.slot_(\d+)_state""")
