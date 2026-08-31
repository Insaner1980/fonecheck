package com.insaner.fonecheck.domain.observation

import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.SimActivityCode
import com.insaner.fonecheck.domain.model.SimFormFactorCode
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimSlotInfo
import com.insaner.fonecheck.domain.model.SimSlotStateCode
import com.insaner.fonecheck.domain.model.ThermalStatusCode

enum class ObservationState {
    PASS,
    FAULT,
    NOTED,
    NOT_MEASURED,
}

enum class ObservationProminence {
    STANDARD,
    PROMINENT,
}

enum class NotMeasuredKind {
    USER_ACTION,
    UNAVAILABLE,
}

enum class ObservationReason(
    val stableCode: String,
    val notMeasuredKind: NotMeasuredKind? = null,
) {
    ROOT_ARTIFACT_PRESENT("root_artifact_present"),
    DEVELOPER_OPTIONS_ENABLED("developer_options_enabled"),
    USB_DEBUGGING_ENABLED("usb_debugging_enabled"),
    SERIAL_RESTRICTED("serial_restricted", NotMeasuredKind.UNAVAILABLE),
    CPU_READING_UNAVAILABLE("cpu_reading_unavailable", NotMeasuredKind.UNAVAILABLE),
    RAM_READING_UNAVAILABLE("ram_reading_unavailable", NotMeasuredKind.UNAVAILABLE),
    GPU_READING_UNAVAILABLE("gpu_reading_unavailable", NotMeasuredKind.UNAVAILABLE),
    DISPLAY_READING_UNAVAILABLE("display_reading_unavailable", NotMeasuredKind.UNAVAILABLE),
    SIM_INVENTORY_UNKNOWN("sim_inventory_unknown", NotMeasuredKind.UNAVAILABLE),
    SIM_NOT_PRESENT("sim_not_present", NotMeasuredKind.USER_ACTION),
    SIM_INACTIVE("sim_inactive", NotMeasuredKind.USER_ACTION),
    SIM_SLOT_UNKNOWN("sim_slot_unknown", NotMeasuredKind.UNAVAILABLE),
    SIM_NETWORK_LOCKED("sim_network_locked"),
    SIM_PIN_REQUIRED("sim_pin_required", NotMeasuredKind.USER_ACTION),
    SIM_PUK_REQUIRED("sim_puk_required", NotMeasuredKind.USER_ACTION),
    SIM_NOT_READY("sim_not_ready", NotMeasuredKind.USER_ACTION),
    SIM_PERMANENTLY_DISABLED("sim_permanently_disabled"),
    SIM_CARD_IO_ERROR("sim_card_io_error"),
    SIM_CARD_RESTRICTED("sim_card_restricted"),
    CAMERA_MEASUREMENT_ERROR("camera_measurement_error", NotMeasuredKind.USER_ACTION),
    SENSOR_MEASUREMENT_ERROR("sensor_measurement_error", NotMeasuredKind.USER_ACTION),
    GPS_DISABLED("gps_disabled", NotMeasuredKind.USER_ACTION),
    GPS_NOT_RUN("gps_not_run", NotMeasuredKind.USER_ACTION),
    GPS_IN_PROGRESS("gps_in_progress", NotMeasuredKind.USER_ACTION),
    GPS_TIMEOUT("gps_timeout", NotMeasuredKind.USER_ACTION),
    GPS_START_FAILED("gps_start_failed", NotMeasuredKind.USER_ACTION),
    BATTERY_HEALTH_UNAVAILABLE("battery_health_unavailable", NotMeasuredKind.UNAVAILABLE),
    BATTERY_OVERHEAT("battery_overheat"),
    BATTERY_DEAD("battery_dead"),
    BATTERY_OVER_VOLTAGE("battery_over_voltage"),
    BATTERY_UNSPECIFIED_FAILURE("battery_unspecified_failure"),
    BATTERY_COLD("battery_cold"),
    BATTERY_TEMPERATURE_UNAVAILABLE("battery_temperature_unavailable", NotMeasuredKind.UNAVAILABLE),
    BATTERY_TEMPERATURE_COLD("battery_temperature_cold"),
    BATTERY_TEMPERATURE_HIGH("battery_temperature_high"),
    BATTERY_TEMPERATURE_CRITICAL("battery_temperature_critical"),
    THERMAL_STATUS_UNAVAILABLE("thermal_status_unavailable", NotMeasuredKind.UNAVAILABLE),
    THERMAL_MANAGEMENT_ACTIVE("thermal_management_active"),
    THERMAL_SEVERE_WITHOUT_APP_LOAD("thermal_severe_without_app_load"),
    BUTTON_TEST_NOT_RUN("button_test_not_run", NotMeasuredKind.USER_ACTION),
    BUTTON_TEST_IN_PROGRESS("button_test_in_progress", NotMeasuredKind.USER_ACTION),
    BUTTON_TEST_TIMEOUT("button_test_timeout", NotMeasuredKind.USER_ACTION),
    BIOMETRIC_NOT_RUN("biometric_not_run", NotMeasuredKind.USER_ACTION),
    BIOMETRIC_IN_PROGRESS("biometric_in_progress", NotMeasuredKind.USER_ACTION),
    BIOMETRIC_NOT_RECOGNIZED("biometric_not_recognized", NotMeasuredKind.USER_ACTION),
    BIOMETRIC_CANCELLED("biometric_cancelled", NotMeasuredKind.USER_ACTION),
    BIOMETRIC_LOCKOUT("biometric_lockout"),
    BIOMETRIC_NOT_ENROLLED("biometric_not_enrolled", NotMeasuredKind.USER_ACTION),
    BIOMETRIC_SECURITY_UPDATE_REQUIRED("biometric_security_update_required", NotMeasuredKind.USER_ACTION),
    BIOMETRIC_UNAVAILABLE("biometric_unavailable", NotMeasuredKind.UNAVAILABLE),
    BIOMETRIC_ERROR("biometric_error", NotMeasuredKind.USER_ACTION),
    PERMISSION_NOT_REQUESTED("permission_not_requested", NotMeasuredKind.USER_ACTION),
    PERMISSION_DENIED("permission_denied", NotMeasuredKind.USER_ACTION),
    PERMISSION_OPEN_SETTINGS("permission_open_settings", NotMeasuredKind.USER_ACTION),
    PERMISSION_PARTIAL("permission_partial", NotMeasuredKind.USER_ACTION),
    HARDWARE_UNAVAILABLE("hardware_unavailable", NotMeasuredKind.UNAVAILABLE),
    VALUE_NOT_EXPOSED("value_not_exposed", NotMeasuredKind.UNAVAILABLE),
    ANDROID_VERSION_UNSUPPORTED("android_version_unsupported", NotMeasuredKind.UNAVAILABLE),
    PLATFORM_RESTRICTION("platform_restriction", NotMeasuredKind.UNAVAILABLE),
    TEST_NOT_RUN("test_not_run", NotMeasuredKind.USER_ACTION),
    MEASUREMENT_IN_PROGRESS("measurement_in_progress", NotMeasuredKind.USER_ACTION),
    TEST_SKIPPED("test_skipped", NotMeasuredKind.USER_ACTION),
    TEST_CANCELLED("test_cancelled", NotMeasuredKind.USER_ACTION),
    MEASUREMENT_TIMEOUT("measurement_timeout", NotMeasuredKind.USER_ACTION),
    MEASUREMENT_ERROR("measurement_error", NotMeasuredKind.USER_ACTION),
    INSUFFICIENT_SPACE("insufficient_space", NotMeasuredKind.USER_ACTION),
    USER_CONFIRMED_DISPLAY_FAILURE("user_confirmed_display_failure"),
    USER_CONFIRMED_AUDIO_FAILURE("user_confirmed_audio_failure"),
    USER_CONFIRMED_CAMERA_FAILURE("user_confirmed_camera_failure"),
    USER_CONFIRMED_VIBRATION_FAILURE("user_confirmed_vibration_failure"),
}

data class ObservationClassification(
    val state: ObservationState,
    val reason: ObservationReason? = null,
    val prominence: ObservationProminence = ObservationProminence.STANDARD,
) {
    init {
        require(state != ObservationState.NOTED || reason != null) { "A noted observation requires a reason." }
        require(state != ObservationState.NOT_MEASURED || reason != null) {
            "A measurement that was not taken requires a reason."
        }
        require(state != ObservationState.NOT_MEASURED || reason?.notMeasuredKind != null) {
            "A measurement that was not taken must say whether the user can change it."
        }
    }

    val notMeasuredKind: NotMeasuredKind?
        get() = reason?.notMeasuredKind
}

fun ObservationClassification.toDiagnosticStatus(informationalPass: Boolean = false): DiagnosticStatus =
    when (state) {
        ObservationState.PASS -> if (informationalPass) DiagnosticStatus.INFO else DiagnosticStatus.PASS
        ObservationState.FAULT -> DiagnosticStatus.FAIL
        ObservationState.NOTED -> DiagnosticStatus.WARNING
        ObservationState.NOT_MEASURED ->
            if (notMeasuredKind == NotMeasuredKind.UNAVAILABLE) {
                DiagnosticStatus.NOT_AVAILABLE
            } else {
                DiagnosticStatus.NOT_TESTED
            }
    }

fun ObservationClassification.toEvidenceReasonCode(): EvidenceReasonCode? =
    reason?.stableCode?.let(::EvidenceReasonCode)

fun isUnusedSimSlot(
    inventory: SimInventoryCode,
    slot: SimSlotInfo,
): Boolean =
    slot.state == SimSlotStateCode.ABSENT ||
        (
            inventory == SimInventoryCode.SINGLE_SIM &&
                slot.state == SimSlotStateCode.NOT_READY &&
                slot.activity == SimActivityCode.INACTIVE &&
                slot.formFactor == SimFormFactorCode.UNKNOWN &&
                slot.operatorName.isNullOrBlank() &&
                slot.countryIso.isNullOrBlank() &&
                slot.networkType == NetworkGenerationCode.UNKNOWN
        )

enum class MeasurementKind {
    CPU,
    RAM,
    GPU,
    DISPLAY,
    CAMERA,
    SENSORS,
    GENERIC,
}

enum class MeasurementOutcome {
    MEASURED,
    UNAVAILABLE,
    ERROR,
    NOT_RUN,
    IN_PROGRESS,
    SKIPPED,
    CANCELLED,
    TIMED_OUT,
    PERMISSION_DENIED,
    HARDWARE_ABSENT,
    ANDROID_VERSION_UNSUPPORTED,
    PLATFORM_RESTRICTED,
    INSUFFICIENT_SPACE,
}

enum class BatteryHealthCode {
    UNKNOWN,
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED_FAILURE,
    COLD,
    OTHER,
}

enum class InteractiveCheck {
    DISPLAY,
    SPEAKER,
    STEREO,
    EARPIECE,
    AUDIO_PLAYBACK,
    CAMERA,
    VIBRATION,
}

enum class GpsFixOutcome {
    NOT_RUN,
    IN_PROGRESS,
    FIXED,
    TIMEOUT,
    START_FAILED,
}

enum class ButtonTestOutcome {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    TIMED_OUT,
    SKIPPED,
}

enum class BiometricOutcome {
    NOT_RUN,
    IN_PROGRESS,
    SUCCESS,
    NOT_RECOGNIZED,
    CANCELLED,
    LOCKED_OUT,
    NO_ENROLLMENT,
    UNAVAILABLE,
    ERROR,
}

enum class BiometricCapabilityOutcome {
    AVAILABLE,
    NO_HARDWARE,
    HARDWARE_UNAVAILABLE,
    NONE_ENROLLED,
    SECURITY_UPDATE_REQUIRED,
    UNSUPPORTED,
    UNKNOWN,
}

enum class PermissionObservation {
    NOT_REQUESTED,
    GRANTED,
    DENIED,
    SETTINGS_RECOVERY,
    NOT_REQUIRED,
    HARDWARE_ABSENT,
    PARTIAL,
}

sealed interface DeviceObservation {
    data class RootArtifact(
        val detected: Boolean,
    ) : DeviceObservation

    data class DeveloperOptions(
        val enabled: Boolean,
    ) : DeviceObservation

    data class UsbDebugging(
        val enabled: Boolean,
    ) : DeviceObservation

    data class SerialNumber(
        val available: Boolean,
    ) : DeviceObservation

    data class Measurement(
        val kind: MeasurementKind,
        val outcome: MeasurementOutcome,
    ) : DeviceObservation

    data class SimInventory(
        val code: SimInventoryCode,
    ) : DeviceObservation

    data class SimSlot(
        val code: SimSlotStateCode,
        val unused: Boolean = false,
    ) : DeviceObservation

    data class UserConfirmation(
        val check: InteractiveCheck,
        val passed: Boolean,
    ) : DeviceObservation

    data class GpsProvider(
        val enabled: Boolean,
    ) : DeviceObservation

    data class GpsFix(
        val outcome: GpsFixOutcome,
    ) : DeviceObservation

    data class BatteryHealth(
        val code: BatteryHealthCode,
    ) : DeviceObservation

    data class BatteryTemperature(
        val celsius: Float?,
    ) : DeviceObservation

    data class Thermal(
        val status: ThermalStatusCode,
    ) : DeviceObservation

    data class ButtonTest(
        val outcome: ButtonTestOutcome,
    ) : DeviceObservation

    data class Biometric(
        val outcome: BiometricOutcome,
    ) : DeviceObservation

    data class BiometricCapability(
        val outcome: BiometricCapabilityOutcome,
    ) : DeviceObservation

    data class Permission(
        val state: PermissionObservation,
    ) : DeviceObservation
}

object DeviceObservationClassifier {
    fun classify(observation: DeviceObservation): ObservationClassification =
        when (observation) {
            is DeviceObservation.RootArtifact ->
                notedWhen(observation.detected, ObservationReason.ROOT_ARTIFACT_PRESENT)
            is DeviceObservation.DeveloperOptions ->
                notedWhen(observation.enabled, ObservationReason.DEVELOPER_OPTIONS_ENABLED)
            is DeviceObservation.UsbDebugging -> notedWhen(observation.enabled, ObservationReason.USB_DEBUGGING_ENABLED)
            is DeviceObservation.SerialNumber ->
                if (observation.available) pass() else notMeasured(ObservationReason.SERIAL_RESTRICTED)
            is DeviceObservation.Measurement -> classifyMeasurement(observation)
            is DeviceObservation.SimInventory -> classifySimInventory(observation.code)
            is DeviceObservation.SimSlot -> classifySimSlot(observation)
            is DeviceObservation.UserConfirmation -> classifyUserConfirmation(observation)
            is DeviceObservation.GpsProvider ->
                if (observation.enabled) pass() else notMeasured(ObservationReason.GPS_DISABLED)
            is DeviceObservation.GpsFix -> classifyGpsFix(observation.outcome)
            is DeviceObservation.BatteryHealth -> classifyBatteryHealth(observation.code)
            is DeviceObservation.BatteryTemperature -> classifyBatteryTemperature(observation.celsius)
            is DeviceObservation.Thermal -> classifyThermal(observation.status)
            is DeviceObservation.ButtonTest -> classifyButtonTest(observation.outcome)
            is DeviceObservation.Biometric -> classifyBiometric(observation.outcome)
            is DeviceObservation.BiometricCapability -> classifyBiometricCapability(observation.outcome)
            is DeviceObservation.Permission -> classifyPermission(observation.state)
        }

    private fun classifyMeasurement(observation: DeviceObservation.Measurement): ObservationClassification =
        when (observation.outcome) {
            MeasurementOutcome.MEASURED -> pass()
            MeasurementOutcome.UNAVAILABLE ->
                notMeasured(
                    when (observation.kind) {
                        MeasurementKind.CPU -> ObservationReason.CPU_READING_UNAVAILABLE
                        MeasurementKind.RAM -> ObservationReason.RAM_READING_UNAVAILABLE
                        MeasurementKind.GPU -> ObservationReason.GPU_READING_UNAVAILABLE
                        MeasurementKind.DISPLAY -> ObservationReason.DISPLAY_READING_UNAVAILABLE
                        else -> ObservationReason.VALUE_NOT_EXPOSED
                    },
                )
            MeasurementOutcome.ERROR ->
                notMeasured(
                    when (observation.kind) {
                        MeasurementKind.CAMERA -> ObservationReason.CAMERA_MEASUREMENT_ERROR
                        MeasurementKind.SENSORS -> ObservationReason.SENSOR_MEASUREMENT_ERROR
                        else -> ObservationReason.MEASUREMENT_ERROR
                    },
                )
            MeasurementOutcome.NOT_RUN -> notMeasured(ObservationReason.TEST_NOT_RUN)
            MeasurementOutcome.IN_PROGRESS -> notMeasured(ObservationReason.MEASUREMENT_IN_PROGRESS)
            MeasurementOutcome.SKIPPED -> notMeasured(ObservationReason.TEST_SKIPPED)
            MeasurementOutcome.CANCELLED -> notMeasured(ObservationReason.TEST_CANCELLED)
            MeasurementOutcome.TIMED_OUT -> notMeasured(ObservationReason.MEASUREMENT_TIMEOUT)
            MeasurementOutcome.PERMISSION_DENIED -> notMeasured(ObservationReason.PERMISSION_DENIED)
            MeasurementOutcome.HARDWARE_ABSENT -> notMeasured(ObservationReason.HARDWARE_UNAVAILABLE)
            MeasurementOutcome.ANDROID_VERSION_UNSUPPORTED ->
                notMeasured(ObservationReason.ANDROID_VERSION_UNSUPPORTED)
            MeasurementOutcome.PLATFORM_RESTRICTED -> notMeasured(ObservationReason.PLATFORM_RESTRICTION)
            MeasurementOutcome.INSUFFICIENT_SPACE -> notMeasured(ObservationReason.INSUFFICIENT_SPACE)
        }

    private fun classifySimInventory(code: SimInventoryCode): ObservationClassification =
        when (code) {
            SimInventoryCode.NO_TELEPHONY -> notMeasured(ObservationReason.HARDWARE_UNAVAILABLE)
            SimInventoryCode.NO_SIM -> notMeasured(ObservationReason.SIM_NOT_PRESENT)
            SimInventoryCode.INACTIVE_SIM -> notMeasured(ObservationReason.SIM_INACTIVE)
            SimInventoryCode.SINGLE_SIM,
            SimInventoryCode.MULTIPLE_SIM,
            -> pass()
            SimInventoryCode.UNKNOWN -> notMeasured(ObservationReason.SIM_INVENTORY_UNKNOWN)
        }

    private fun classifySimSlot(observation: DeviceObservation.SimSlot): ObservationClassification {
        if (observation.unused) return pass()
        return when (observation.code) {
            SimSlotStateCode.READY -> pass()
            SimSlotStateCode.ABSENT -> pass()
            SimSlotStateCode.NETWORK_LOCKED -> noted(ObservationReason.SIM_NETWORK_LOCKED)
            SimSlotStateCode.PIN_REQUIRED -> notMeasured(ObservationReason.SIM_PIN_REQUIRED)
            SimSlotStateCode.PUK_REQUIRED -> notMeasured(ObservationReason.SIM_PUK_REQUIRED)
            SimSlotStateCode.NOT_READY -> notMeasured(ObservationReason.SIM_NOT_READY)
            SimSlotStateCode.PERMANENTLY_DISABLED -> noted(ObservationReason.SIM_PERMANENTLY_DISABLED)
            SimSlotStateCode.CARD_IO_ERROR -> fault(ObservationReason.SIM_CARD_IO_ERROR)
            SimSlotStateCode.CARD_RESTRICTED -> noted(ObservationReason.SIM_CARD_RESTRICTED)
            SimSlotStateCode.UNKNOWN -> notMeasured(ObservationReason.SIM_SLOT_UNKNOWN)
        }
    }

    private fun classifyUserConfirmation(observation: DeviceObservation.UserConfirmation): ObservationClassification {
        if (observation.passed) return pass()
        val reason =
            when (observation.check) {
                InteractiveCheck.DISPLAY -> ObservationReason.USER_CONFIRMED_DISPLAY_FAILURE
                InteractiveCheck.SPEAKER,
                InteractiveCheck.STEREO,
                InteractiveCheck.EARPIECE,
                InteractiveCheck.AUDIO_PLAYBACK,
                -> ObservationReason.USER_CONFIRMED_AUDIO_FAILURE
                InteractiveCheck.CAMERA -> ObservationReason.USER_CONFIRMED_CAMERA_FAILURE
                InteractiveCheck.VIBRATION -> ObservationReason.USER_CONFIRMED_VIBRATION_FAILURE
            }
        return fault(reason)
    }

    private fun classifyGpsFix(outcome: GpsFixOutcome): ObservationClassification =
        when (outcome) {
            GpsFixOutcome.NOT_RUN -> notMeasured(ObservationReason.GPS_NOT_RUN)
            GpsFixOutcome.IN_PROGRESS -> notMeasured(ObservationReason.GPS_IN_PROGRESS)
            GpsFixOutcome.FIXED -> pass()
            GpsFixOutcome.TIMEOUT -> notMeasured(ObservationReason.GPS_TIMEOUT)
            GpsFixOutcome.START_FAILED -> notMeasured(ObservationReason.GPS_START_FAILED)
        }

    private fun classifyBatteryHealth(code: BatteryHealthCode): ObservationClassification =
        when (code) {
            BatteryHealthCode.UNKNOWN,
            BatteryHealthCode.OTHER,
            -> notMeasured(ObservationReason.BATTERY_HEALTH_UNAVAILABLE)
            BatteryHealthCode.GOOD -> pass()
            BatteryHealthCode.OVERHEAT -> noted(ObservationReason.BATTERY_OVERHEAT)
            BatteryHealthCode.DEAD -> fault(ObservationReason.BATTERY_DEAD)
            BatteryHealthCode.OVER_VOLTAGE -> fault(ObservationReason.BATTERY_OVER_VOLTAGE)
            BatteryHealthCode.UNSPECIFIED_FAILURE -> fault(ObservationReason.BATTERY_UNSPECIFIED_FAILURE)
            BatteryHealthCode.COLD -> noted(ObservationReason.BATTERY_COLD)
        }

    private fun classifyBatteryTemperature(celsius: Float?): ObservationClassification =
        when {
            celsius == null || !celsius.isFinite() -> notMeasured(ObservationReason.BATTERY_TEMPERATURE_UNAVAILABLE)
            celsius >= 50f ->
                noted(
                    reason = ObservationReason.BATTERY_TEMPERATURE_CRITICAL,
                    prominence = ObservationProminence.PROMINENT,
                )
            celsius < 0f -> noted(ObservationReason.BATTERY_TEMPERATURE_COLD)
            celsius > 45f -> noted(ObservationReason.BATTERY_TEMPERATURE_HIGH)
            else -> pass()
        }

    private fun classifyThermal(status: ThermalStatusCode): ObservationClassification =
        when (status) {
            ThermalStatusCode.NONE -> pass()
            ThermalStatusCode.LIGHT,
            ThermalStatusCode.MODERATE,
            -> noted(ObservationReason.THERMAL_MANAGEMENT_ACTIVE)
            ThermalStatusCode.SEVERE,
            ThermalStatusCode.CRITICAL,
            ThermalStatusCode.EMERGENCY,
            ThermalStatusCode.SHUTDOWN,
            ->
                noted(
                    reason = ObservationReason.THERMAL_SEVERE_WITHOUT_APP_LOAD,
                    prominence = ObservationProminence.PROMINENT,
                )
            ThermalStatusCode.UNAVAILABLE -> notMeasured(ObservationReason.THERMAL_STATUS_UNAVAILABLE)
        }

    private fun classifyButtonTest(outcome: ButtonTestOutcome): ObservationClassification =
        when (outcome) {
            ButtonTestOutcome.NOT_STARTED -> notMeasured(ObservationReason.BUTTON_TEST_NOT_RUN)
            ButtonTestOutcome.IN_PROGRESS -> notMeasured(ObservationReason.BUTTON_TEST_IN_PROGRESS)
            ButtonTestOutcome.COMPLETED -> pass()
            ButtonTestOutcome.TIMED_OUT -> notMeasured(ObservationReason.BUTTON_TEST_TIMEOUT)
            ButtonTestOutcome.SKIPPED -> notMeasured(ObservationReason.TEST_SKIPPED)
        }

    private fun classifyBiometric(outcome: BiometricOutcome): ObservationClassification =
        when (outcome) {
            BiometricOutcome.NOT_RUN -> notMeasured(ObservationReason.BIOMETRIC_NOT_RUN)
            BiometricOutcome.IN_PROGRESS -> notMeasured(ObservationReason.BIOMETRIC_IN_PROGRESS)
            BiometricOutcome.SUCCESS -> pass()
            BiometricOutcome.NOT_RECOGNIZED -> notMeasured(ObservationReason.BIOMETRIC_NOT_RECOGNIZED)
            BiometricOutcome.CANCELLED -> notMeasured(ObservationReason.BIOMETRIC_CANCELLED)
            BiometricOutcome.LOCKED_OUT -> noted(ObservationReason.BIOMETRIC_LOCKOUT)
            BiometricOutcome.NO_ENROLLMENT -> notMeasured(ObservationReason.BIOMETRIC_NOT_ENROLLED)
            BiometricOutcome.UNAVAILABLE -> notMeasured(ObservationReason.BIOMETRIC_UNAVAILABLE)
            BiometricOutcome.ERROR -> notMeasured(ObservationReason.BIOMETRIC_ERROR)
        }

    private fun classifyBiometricCapability(outcome: BiometricCapabilityOutcome): ObservationClassification =
        when (outcome) {
            BiometricCapabilityOutcome.AVAILABLE -> pass()
            BiometricCapabilityOutcome.NO_HARDWARE -> notMeasured(ObservationReason.HARDWARE_UNAVAILABLE)
            BiometricCapabilityOutcome.HARDWARE_UNAVAILABLE ->
                notMeasured(ObservationReason.BIOMETRIC_UNAVAILABLE)
            BiometricCapabilityOutcome.NONE_ENROLLED ->
                notMeasured(ObservationReason.BIOMETRIC_NOT_ENROLLED)
            BiometricCapabilityOutcome.SECURITY_UPDATE_REQUIRED ->
                notMeasured(ObservationReason.BIOMETRIC_SECURITY_UPDATE_REQUIRED)
            BiometricCapabilityOutcome.UNSUPPORTED ->
                notMeasured(ObservationReason.ANDROID_VERSION_UNSUPPORTED)
            BiometricCapabilityOutcome.UNKNOWN -> notMeasured(ObservationReason.VALUE_NOT_EXPOSED)
        }

    private fun classifyPermission(state: PermissionObservation): ObservationClassification =
        when (state) {
            PermissionObservation.NOT_REQUESTED -> notMeasured(ObservationReason.PERMISSION_NOT_REQUESTED)
            PermissionObservation.GRANTED,
            PermissionObservation.NOT_REQUIRED,
            -> pass()
            PermissionObservation.DENIED -> notMeasured(ObservationReason.PERMISSION_DENIED)
            PermissionObservation.SETTINGS_RECOVERY -> notMeasured(ObservationReason.PERMISSION_OPEN_SETTINGS)
            PermissionObservation.HARDWARE_ABSENT -> notMeasured(ObservationReason.HARDWARE_UNAVAILABLE)
            PermissionObservation.PARTIAL -> notMeasured(ObservationReason.PERMISSION_PARTIAL)
        }

    private fun notedWhen(
        condition: Boolean,
        reason: ObservationReason,
    ): ObservationClassification = if (condition) noted(reason) else pass()

    private fun pass(): ObservationClassification = ObservationClassification(ObservationState.PASS)

    private fun fault(reason: ObservationReason): ObservationClassification =
        ObservationClassification(ObservationState.FAULT, reason)

    private fun noted(
        reason: ObservationReason,
        prominence: ObservationProminence = ObservationProminence.STANDARD,
    ): ObservationClassification = ObservationClassification(ObservationState.NOTED, reason, prominence)

    private fun notMeasured(reason: ObservationReason): ObservationClassification =
        ObservationClassification(ObservationState.NOT_MEASURED, reason)
}
