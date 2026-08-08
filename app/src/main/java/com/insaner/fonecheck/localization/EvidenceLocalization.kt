package com.insaner.fonecheck.localization

import androidx.annotation.StringRes
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.EvidenceReasonCode

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
@Suppress("CyclomaticComplexMethod") // Exhaustive stable-ID to resource lookup.
fun evidenceLabelStringRes(checkId: String): Int? =
    when (checkId) {
        "device.identity" -> R.string.device_info_title
        "device.security" -> R.string.label_root_artifact
        "performance.cpu" -> R.string.perf_cpu_title
        "performance.ram" -> R.string.perf_ram_title
        "performance.gpu" -> R.string.perf_gpu_title
        "performance.cpu_benchmark" -> R.string.perf_benchmark_cpu_rate
        "performance.memory_benchmark" -> R.string.perf_benchmark_memory_rate
        "sim.inventory" -> R.string.sim_telephony_title
        "sim.network" -> R.string.conn_mobile_network_type
        "display.info" -> R.string.display_info_title
        "display.visual" -> R.string.run_all_check_visual_display
        "audio.speaker" -> R.string.run_all_check_speaker
        "audio.microphone" -> R.string.run_all_check_microphone
        "audio.headphones" -> R.string.run_all_check_headphones
        "camera.rear" -> R.string.camera_rear
        "camera.front" -> R.string.camera_front
        "camera.capture" -> R.string.camera_capture_title
        "camera.inventory" -> R.string.camera_inventory_label
        "camera.logical_count" -> R.string.camera_logical_count_label
        "camera.capture_dimensions" -> R.string.camera_max_resolution
        "sensors.inventory" -> R.string.sensor_count
        "sensors.accelerometer" -> R.string.sensor_type_accelerometer
        "sensors.gyroscope" -> R.string.sensor_type_gyroscope
        "sensors.gravity" -> R.string.sensor_type_gravity
        "sensors.proximity" -> R.string.sensor_type_proximity
        "sensors.light" -> R.string.sensor_type_light
        "sensors.magnetometer" -> R.string.sensor_type_magnetometer
        "sensors.barometer" -> R.string.sensor_type_barometer
        "sensors.step" -> R.string.sensor_type_step
        "sensors.orientation" -> R.string.sensor_orientation
        "sensors.motion" -> R.string.run_all_check_motion_sensor
        "connectivity.wifi" -> R.string.conn_wifi_title
        "connectivity.bluetooth" -> R.string.conn_bluetooth_title
        "connectivity.nfc" -> R.string.conn_nfc_title
        "connectivity.nfc_hce" -> R.string.conn_nfc_hce
        "connectivity.gps" -> R.string.conn_gps_title
        "connectivity.mobile" -> R.string.conn_mobile_title
        "battery.health" -> R.string.batt_health_title
        "battery.temperature" -> R.string.batt_temperature
        "battery.level" -> R.string.batt_level
        "battery.current_now" -> R.string.batt_charging_current
        "battery.current_direction" -> R.string.batt_current_direction
        "battery.current_interpretation" -> R.string.batt_current_interpretation
        "battery.current_profile" -> R.string.batt_mfr_profile
        "battery.cycle_count" -> R.string.batt_cycle_count
        "thermal.status" -> R.string.thermal_status_title
        "thermal.severity" -> R.string.thermal_severity_label
        "thermal.headroom" -> R.string.thermal_headroom_title
        "thermal.battery_temperature" -> R.string.thermal_battery_title
        "storage.total" -> R.string.storage_total
        "storage.used" -> R.string.storage_used
        "storage.available" -> R.string.storage_available
        "storage.usage" -> R.string.storage_usage
        "storage.internal_access" -> R.string.storage_internal_access
        "storage.volume_count" -> R.string.storage_volumes_title
        "storage.mounted_volume_count" -> R.string.storage_mounted_state
        "storage.removable_volume_count" -> R.string.storage_removable
        "storage.sequential_write" -> R.string.storage_benchmark_write_rate
        "storage.sequential_read" -> R.string.storage_benchmark_read_rate
        "storage.benchmark_data_size" -> R.string.storage_benchmark_data_size
        "storage.benchmark_available_before" -> R.string.storage_benchmark_available_before
        "storage.benchmark_location" -> R.string.storage_benchmark_location
        "storage.benchmark_cleanup" -> R.string.storage_benchmark_cleanup
        "vibration.hardware" -> R.string.vibration_has_vibrator
        "vibration.amplitude_control" -> R.string.vibration_amplitude_control
        "vibration.effects" -> R.string.vibration_effects_supported
        "vibration.primitives" -> R.string.vibration_primitives_supported
        "vibration.motor" -> R.string.vibration_motor_title
        "buttons.volume" -> R.string.run_all_check_volume_buttons
        "buttons.power" -> R.string.button_power
        "biometrics.capability" -> R.string.biometric_capabilities_title
        "biometrics.fingerprint_hardware" -> R.string.biometric_fingerprint_hardware
        "biometrics.face_hardware" -> R.string.biometric_face_hardware
        "biometrics.strong_capability" -> R.string.biometric_strong
        "biometrics.weak_capability" -> R.string.biometric_weak
        "biometrics.authentication" -> R.string.biometric_test_auth
        else -> null
    }

@StringRes
@Suppress("CyclomaticComplexMethod") // Exhaustive persisted-code to resource lookup.
fun stableTextStringRes(code: String): Int? =
    when (code) {
        "good" -> R.string.batt_health_good
        "overheat" -> R.string.batt_health_overheat
        "dead" -> R.string.batt_health_dead
        "over_voltage" -> R.string.batt_health_over_voltage
        "unspecified_failure" -> R.string.batt_health_failure
        "cold" -> R.string.batt_health_cold
        "charging" -> R.string.batt_current_direction_charging
        "discharging" -> R.string.batt_current_direction_discharging
        "idle" -> R.string.batt_current_direction_idle
        "api_sign" -> R.string.batt_current_sign_api
        "status_sign_normalized" -> R.string.batt_current_sign_status
        "samsung" -> R.string.batt_mfr_samsung
        "oneplus" -> R.string.batt_mfr_oneplus
        "google_pixel" -> R.string.batt_mfr_pixel
        "generic" -> R.string.batt_mfr_generic
        "none" -> R.string.perf_thermal_none
        "normal" -> R.string.thermal_severity_normal
        "light" -> R.string.perf_thermal_light
        "moderate" -> R.string.perf_thermal_moderate
        "severe" -> R.string.perf_thermal_severe
        "critical" -> R.string.perf_thermal_critical
        "emergency" -> R.string.perf_thermal_emergency
        "shutdown" -> R.string.perf_thermal_shutdown
        "strong" -> R.string.biometric_strong
        "weak" -> R.string.biometric_weak
        "unavailable" -> R.string.biometric_not_available
        "available" -> R.string.biometric_available
        "no_hardware" -> R.string.biometric_no_hardware
        "hardware_unavailable" -> R.string.biometric_hardware_unavailable
        "none_enrolled" -> R.string.biometric_none_enrolled
        "security_update_required" -> R.string.biometric_security_update_required
        "unsupported" -> R.string.biometric_unsupported
        "known_artifact_detected" -> R.string.device_root_artifact_detected
        "no_known_artifact_detected" -> R.string.device_root_artifact_not_detected
        "no_telephony" -> R.string.sim_inventory_no_telephony
        "no_sim" -> R.string.sim_inventory_no_sim
        "inactive_sim" -> R.string.sim_inventory_inactive
        "single_sim" -> R.string.sim_inventory_single
        "multiple_sim" -> R.string.sim_inventory_multiple
        "second_generation" -> R.string.sim_network_2g
        "third_generation" -> R.string.sim_network_3g
        "fourth_generation" -> R.string.sim_network_4g
        "fifth_generation" -> R.string.sim_network_5g
        "unknown" -> R.string.sim_value_unknown
        "observed" -> R.string.sensor_orientation_observed
        "app_cache" -> R.string.storage_app_cache
        else -> null
    }

fun stableCodeDisplayText(code: String): String =
    code.replace('_', ' ').replaceFirstChar { character -> character.titlecase() }
