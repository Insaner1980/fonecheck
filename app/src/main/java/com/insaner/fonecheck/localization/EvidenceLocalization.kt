package com.insaner.fonecheck.localization

import androidx.annotation.StringRes
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.ThermalStatusCode

@StringRes
fun evidenceReasonStringRes(reason: EvidenceReasonCode): Int? =
    when (reason) {
        EvidenceReasonCode.PERMISSION_DENIED -> R.string.run_all_permission_missing
        EvidenceReasonCode.NOT_RUN -> R.string.run_all_summary_not_tested
        EvidenceReasonCode.SKIPPED,
        EvidenceReasonCode.CANCELLED,
        -> R.string.run_all_manual_skipped
        EvidenceReasonCode.TIMEOUT -> R.string.conn_gps_failed
        EvidenceReasonCode.INSUFFICIENT_SPACE -> R.string.storage_benchmark_insufficient_space
        EvidenceReasonCode.USER_CONFIRMED_FAILURE -> R.string.run_all_manual_failed
        EvidenceReasonCode.HARDWARE_UNAVAILABLE,
        EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED,
        -> R.string.run_all_status_unavailable
        EvidenceReasonCode.PLATFORM_RESTRICTION -> R.string.button_power_unavailable
        EvidenceReasonCode.BIOMETRIC_LOCKOUT -> R.string.biometric_locked_out
        EvidenceReasonCode.BIOMETRIC_NOT_ENROLLED -> R.string.biometric_none_enrolled
        EvidenceReasonCode.DISABLED -> R.string.status_disabled
        EvidenceReasonCode.DEGRADED -> R.string.run_all_summary_warning
        EvidenceReasonCode.ERROR -> R.string.run_all_summary_fail
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

@StringRes
fun evidenceLabelStringRes(checkId: String): Int? = EVIDENCE_LABEL_RESOURCES[checkId]

@StringRes
fun stableTextStringRes(code: String): Int? = STABLE_TEXT_RESOURCES[code]

fun stableCodeDisplayText(code: String): String =
    code.replace('_', ' ').replaceFirstChar { character -> character.titlecase() }

private val EVIDENCE_LABEL_RESOURCES =
    mapOf(
        "device.identity" to R.string.device_info_title,
        "device.security" to R.string.label_root_artifact,
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
        "camera.capture_dimensions" to R.string.camera_max_resolution,
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
        "observed" to R.string.sensor_orientation_observed,
        "app_cache" to R.string.storage_app_cache,
    )
