package com.insaner.fonecheck.export

import android.content.Context
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.localization.evidenceReasonStringRes

private val pdfReasonResources =
    mapOf(
        "sim_not_present" to R.string.pdf_reason_sim_not_present,
        "sim_inactive" to R.string.pdf_reason_sim_inactive,
        "sim_network_locked" to R.string.pdf_reason_sim_network_locked,
        "sim_pin_required" to R.string.pdf_reason_sim_pin_required,
        "sim_puk_required" to R.string.pdf_reason_sim_puk_required,
        "sim_not_ready" to R.string.pdf_reason_sim_not_ready,
        "sim_permanently_disabled" to R.string.pdf_reason_sim_permanently_disabled,
        "sim_card_restricted" to R.string.pdf_reason_sim_card_restricted,
        "camera_measurement_error" to R.string.pdf_reason_camera_measurement_error,
        "sensor_measurement_error" to R.string.pdf_reason_sensor_measurement_error,
        "gps_disabled" to R.string.pdf_reason_gps_disabled,
        "gps_not_run" to R.string.pdf_reason_gps_not_run,
        "gps_in_progress" to R.string.pdf_reason_gps_in_progress,
        "gps_timeout" to R.string.pdf_reason_gps_timeout,
        "gps_start_failed" to R.string.pdf_reason_gps_start_failed,
        "battery_temperature_critical" to R.string.pdf_reason_battery_temperature_critical,
        "thermal_management_active" to R.string.pdf_reason_thermal_management_active,
        "thermal_severe_without_app_load" to R.string.pdf_reason_thermal_severe_without_app_load,
        "button_test_not_run" to R.string.pdf_reason_button_test_not_run,
        "button_test_in_progress" to R.string.pdf_reason_button_test_in_progress,
        "button_test_timeout" to R.string.pdf_reason_button_test_timeout,
        "biometric_not_run" to R.string.pdf_reason_biometric_not_run,
        "biometric_in_progress" to R.string.pdf_reason_biometric_in_progress,
        "biometric_not_recognized" to R.string.pdf_reason_biometric_not_recognized,
        "biometric_lockout" to R.string.pdf_reason_biometric_lockout,
        "biometric_not_enrolled" to R.string.pdf_reason_biometric_not_enrolled,
        "biometric_error" to R.string.pdf_reason_biometric_error,
        "permission_not_requested" to R.string.pdf_reason_permission_not_requested,
        "permission_denied" to R.string.pdf_reason_permission_denied,
        "permission_open_settings" to R.string.pdf_reason_permission_open_settings,
        "permission_partial" to R.string.pdf_reason_permission_partial,
        "test_not_run" to R.string.pdf_reason_test_not_run,
        "measurement_in_progress" to R.string.pdf_reason_measurement_in_progress,
        "test_skipped" to R.string.pdf_reason_test_skipped,
        "measurement_timeout" to R.string.pdf_reason_measurement_timeout,
        "measurement_error" to R.string.pdf_reason_measurement_error,
        "insufficient_space" to R.string.pdf_reason_insufficient_space,
        "user_confirmed_display_failure" to R.string.pdf_user_display,
        "user_confirmed_audio_failure" to R.string.pdf_user_audio,
        "user_confirmed_camera_failure" to R.string.pdf_user_camera,
        "user_confirmed_vibration_failure" to R.string.pdf_user_vibration,
    )

internal fun pdfReasonText(
    context: Context,
    reason: EvidenceReasonCode,
): String? {
    val resource = pdfReasonResources[reason.value] ?: evidenceReasonStringRes(reason)
    return resource?.let(context::getString)
}
