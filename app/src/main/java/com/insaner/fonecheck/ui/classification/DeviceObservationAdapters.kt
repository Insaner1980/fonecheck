package com.insaner.fonecheck.ui.classification

import android.os.BatteryManager
import com.insaner.fonecheck.domain.observation.BatteryHealthCode
import com.insaner.fonecheck.domain.observation.BiometricCapabilityOutcome
import com.insaner.fonecheck.domain.observation.BiometricOutcome
import com.insaner.fonecheck.domain.observation.ButtonTestOutcome
import com.insaner.fonecheck.domain.observation.DeviceObservation
import com.insaner.fonecheck.domain.observation.DeviceObservationClassifier
import com.insaner.fonecheck.domain.observation.GpsFixOutcome
import com.insaner.fonecheck.domain.observation.InteractiveCheck
import com.insaner.fonecheck.domain.observation.MeasurementKind
import com.insaner.fonecheck.domain.observation.MeasurementOutcome
import com.insaner.fonecheck.domain.observation.ObservationClassification
import com.insaner.fonecheck.domain.observation.PermissionObservation
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.screens.audio.AudioManualCheck
import com.insaner.fonecheck.ui.screens.biometrics.AuthResult
import com.insaner.fonecheck.ui.screens.biometrics.BiometricAvailability
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestPhase
import com.insaner.fonecheck.ui.screens.connectivity.GpsFailureCode
import com.insaner.fonecheck.ui.screens.connectivity.GpsFixStatus
import com.insaner.fonecheck.ui.screens.vibration.VibrationMotorResult

fun classifyBatteryHealth(androidStatus: Int): ObservationClassification =
    DeviceObservationClassifier.classify(
        DeviceObservation.BatteryHealth(
            when (androidStatus) {
                BatteryManager.BATTERY_HEALTH_UNKNOWN -> BatteryHealthCode.UNKNOWN
                BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealthCode.GOOD
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealthCode.OVERHEAT
                BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealthCode.DEAD
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealthCode.OVER_VOLTAGE
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealthCode.UNSPECIFIED_FAILURE
                BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealthCode.COLD
                else -> BatteryHealthCode.OTHER
            },
        ),
    )

fun classifyPermission(state: PermissionState): ObservationClassification =
    DeviceObservationClassifier.classify(
        DeviceObservation.Permission(
            when (state) {
                PermissionState.NOT_REQUESTED -> PermissionObservation.NOT_REQUESTED
                PermissionState.GRANTED -> PermissionObservation.GRANTED
                PermissionState.DENIED -> PermissionObservation.DENIED
                PermissionState.SETTINGS_RECOVERY -> PermissionObservation.SETTINGS_RECOVERY
                PermissionState.NOT_REQUIRED -> PermissionObservation.NOT_REQUIRED
                PermissionState.HARDWARE_ABSENT -> PermissionObservation.HARDWARE_ABSENT
                PermissionState.PARTIAL -> PermissionObservation.PARTIAL
            },
        ),
    )

fun classifyGpsProvider(enabled: Boolean): ObservationClassification =
    DeviceObservationClassifier.classify(DeviceObservation.GpsProvider(enabled))

fun classifyGpsFix(
    status: GpsFixStatus,
    failure: GpsFailureCode?,
): ObservationClassification =
    DeviceObservationClassifier.classify(
        DeviceObservation.GpsFix(
            when (status) {
                GpsFixStatus.NOT_STARTED -> GpsFixOutcome.NOT_RUN
                GpsFixStatus.SEARCHING -> GpsFixOutcome.IN_PROGRESS
                GpsFixStatus.FIXED -> GpsFixOutcome.FIXED
                GpsFixStatus.FAILED ->
                    when (failure) {
                        GpsFailureCode.TIMEOUT -> GpsFixOutcome.TIMEOUT
                        GpsFailureCode.START_FAILED,
                        null,
                        -> GpsFixOutcome.START_FAILED
                    }
            },
        ),
    )

fun classifyButtonTest(phase: ButtonTestPhase): ObservationClassification =
    DeviceObservationClassifier.classify(
        DeviceObservation.ButtonTest(
            when (phase) {
                ButtonTestPhase.IDLE -> ButtonTestOutcome.NOT_STARTED
                ButtonTestPhase.RUNNING -> ButtonTestOutcome.IN_PROGRESS
                ButtonTestPhase.COMPLETED -> ButtonTestOutcome.COMPLETED
                ButtonTestPhase.TIMED_OUT -> ButtonTestOutcome.TIMED_OUT
                ButtonTestPhase.SKIPPED -> ButtonTestOutcome.SKIPPED
            },
        ),
    )

fun classifyBiometric(result: AuthResult): ObservationClassification =
    DeviceObservationClassifier.classify(
        DeviceObservation.Biometric(
            when (result) {
                AuthResult.NONE -> BiometricOutcome.NOT_RUN
                AuthResult.IN_PROGRESS -> BiometricOutcome.IN_PROGRESS
                AuthResult.NOT_RECOGNIZED -> BiometricOutcome.NOT_RECOGNIZED
                AuthResult.SUCCESS -> BiometricOutcome.SUCCESS
                AuthResult.CANCELLED -> BiometricOutcome.CANCELLED
                AuthResult.LOCKED_OUT -> BiometricOutcome.LOCKED_OUT
                AuthResult.NO_ENROLLMENT -> BiometricOutcome.NO_ENROLLMENT
                AuthResult.UNAVAILABLE -> BiometricOutcome.UNAVAILABLE
                AuthResult.ERROR -> BiometricOutcome.ERROR
            },
        ),
    )

fun classifyBiometricCapability(status: BiometricAvailability): ObservationClassification =
    DeviceObservationClassifier.classify(
        DeviceObservation.BiometricCapability(
            when (status) {
                BiometricAvailability.AVAILABLE -> BiometricCapabilityOutcome.AVAILABLE
                BiometricAvailability.NO_HARDWARE -> BiometricCapabilityOutcome.NO_HARDWARE
                BiometricAvailability.HARDWARE_UNAVAILABLE -> BiometricCapabilityOutcome.HARDWARE_UNAVAILABLE
                BiometricAvailability.NONE_ENROLLED -> BiometricCapabilityOutcome.NONE_ENROLLED
                BiometricAvailability.SECURITY_UPDATE_REQUIRED ->
                    BiometricCapabilityOutcome.SECURITY_UPDATE_REQUIRED
                BiometricAvailability.UNSUPPORTED -> BiometricCapabilityOutcome.UNSUPPORTED
                BiometricAvailability.UNKNOWN -> BiometricCapabilityOutcome.UNKNOWN
            },
        ),
    )

fun classifyDisplayConfirmation(passed: Boolean): ObservationClassification =
    classifyConfirmation(InteractiveCheck.DISPLAY, passed)

fun classifyAudioConfirmation(
    check: AudioManualCheck,
    passed: Boolean,
): ObservationClassification =
    classifyConfirmation(
        check =
            when (check) {
                AudioManualCheck.SPEAKER -> InteractiveCheck.SPEAKER
                AudioManualCheck.STEREO -> InteractiveCheck.STEREO
                AudioManualCheck.EARPIECE -> InteractiveCheck.EARPIECE
                AudioManualCheck.PLAYBACK -> InteractiveCheck.AUDIO_PLAYBACK
            },
        passed = passed,
    )

fun classifyCameraConfirmation(passed: Boolean): ObservationClassification =
    classifyConfirmation(InteractiveCheck.CAMERA, passed)

fun classifyVibrationResult(result: VibrationMotorResult?): ObservationClassification =
    when (result) {
        VibrationMotorResult.FELT -> classifyConfirmation(InteractiveCheck.VIBRATION, passed = true)
        VibrationMotorResult.NOT_FELT -> classifyConfirmation(InteractiveCheck.VIBRATION, passed = false)
        VibrationMotorResult.SKIPPED ->
            DeviceObservationClassifier.classify(
                DeviceObservation.Measurement(MeasurementKind.GENERIC, MeasurementOutcome.SKIPPED),
            )
        null ->
            DeviceObservationClassifier.classify(
                DeviceObservation.Measurement(MeasurementKind.GENERIC, MeasurementOutcome.NOT_RUN),
            )
    }

private fun classifyConfirmation(
    check: InteractiveCheck,
    passed: Boolean,
): ObservationClassification = DeviceObservationClassifier.classify(DeviceObservation.UserConfirmation(check, passed))
