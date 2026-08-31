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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceObservationClassifierTest {
    @Test
    fun `fault is reserved for measured defects and negative user confirmations`() {
        val cases =
            listOf(
                DeviceObservation.BatteryHealth(BatteryHealthCode.DEAD) to ObservationReason.BATTERY_DEAD,
                DeviceObservation.BatteryHealth(BatteryHealthCode.OVER_VOLTAGE) to
                    ObservationReason.BATTERY_OVER_VOLTAGE,
                DeviceObservation.BatteryHealth(BatteryHealthCode.UNSPECIFIED_FAILURE) to
                    ObservationReason.BATTERY_UNSPECIFIED_FAILURE,
                DeviceObservation.SimSlot(SimSlotStateCode.CARD_IO_ERROR) to
                    ObservationReason.SIM_CARD_IO_ERROR,
                DeviceObservation.UserConfirmation(InteractiveCheck.DISPLAY, passed = false) to
                    ObservationReason.USER_CONFIRMED_DISPLAY_FAILURE,
                DeviceObservation.UserConfirmation(InteractiveCheck.SPEAKER, passed = false) to
                    ObservationReason.USER_CONFIRMED_AUDIO_FAILURE,
                DeviceObservation.UserConfirmation(InteractiveCheck.CAMERA, passed = false) to
                    ObservationReason.USER_CONFIRMED_CAMERA_FAILURE,
                DeviceObservation.UserConfirmation(InteractiveCheck.VIBRATION, passed = false) to
                    ObservationReason.USER_CONFIRMED_VIBRATION_FAILURE,
            )

        cases.forEach { (observation, reason) ->
            assertEquals(
                ObservationClassification(ObservationState.FAULT, reason),
                DeviceObservationClassifier.classify(observation),
            )
        }
    }

    @Test
    fun `relevant non-defects are noted with specific reasons`() {
        val cases =
            listOf(
                DeviceObservation.RootArtifact(detected = true) to ObservationReason.ROOT_ARTIFACT_PRESENT,
                DeviceObservation.DeveloperOptions(enabled = true) to ObservationReason.DEVELOPER_OPTIONS_ENABLED,
                DeviceObservation.UsbDebugging(enabled = true) to ObservationReason.USB_DEBUGGING_ENABLED,
                DeviceObservation.SimSlot(SimSlotStateCode.NETWORK_LOCKED) to ObservationReason.SIM_NETWORK_LOCKED,
                DeviceObservation.SimSlot(SimSlotStateCode.PERMANENTLY_DISABLED) to
                    ObservationReason.SIM_PERMANENTLY_DISABLED,
                DeviceObservation.SimSlot(SimSlotStateCode.CARD_RESTRICTED) to
                    ObservationReason.SIM_CARD_RESTRICTED,
                DeviceObservation.BatteryHealth(BatteryHealthCode.OVERHEAT) to ObservationReason.BATTERY_OVERHEAT,
                DeviceObservation.BatteryHealth(BatteryHealthCode.COLD) to ObservationReason.BATTERY_COLD,
                DeviceObservation.BatteryTemperature(46f) to ObservationReason.BATTERY_TEMPERATURE_HIGH,
                DeviceObservation.BatteryTemperature(-1f) to ObservationReason.BATTERY_TEMPERATURE_COLD,
                DeviceObservation.Thermal(ThermalStatusCode.LIGHT) to ObservationReason.THERMAL_MANAGEMENT_ACTIVE,
                DeviceObservation.Thermal(ThermalStatusCode.MODERATE) to ObservationReason.THERMAL_MANAGEMENT_ACTIVE,
                DeviceObservation.Biometric(BiometricOutcome.LOCKED_OUT) to ObservationReason.BIOMETRIC_LOCKOUT,
            )

        cases.forEach { (observation, reason) ->
            assertEquals(
                ObservationClassification(ObservationState.NOTED, reason),
                DeviceObservationClassifier.classify(observation),
            )
        }
    }

    @Test
    fun `unusual heat observations are prominent`() {
        val cases =
            listOf(
                DeviceObservation.BatteryTemperature(50f) to ObservationReason.BATTERY_TEMPERATURE_CRITICAL,
                DeviceObservation.BatteryTemperature(60f) to ObservationReason.BATTERY_TEMPERATURE_CRITICAL,
                DeviceObservation.Thermal(ThermalStatusCode.SEVERE) to
                    ObservationReason.THERMAL_SEVERE_WITHOUT_APP_LOAD,
                DeviceObservation.Thermal(ThermalStatusCode.CRITICAL) to
                    ObservationReason.THERMAL_SEVERE_WITHOUT_APP_LOAD,
                DeviceObservation.Thermal(ThermalStatusCode.EMERGENCY) to
                    ObservationReason.THERMAL_SEVERE_WITHOUT_APP_LOAD,
                DeviceObservation.Thermal(ThermalStatusCode.SHUTDOWN) to
                    ObservationReason.THERMAL_SEVERE_WITHOUT_APP_LOAD,
            )

        cases.forEach { (observation, reason) ->
            assertEquals(
                ObservationClassification(
                    state = ObservationState.NOTED,
                    reason = reason,
                    prominence = ObservationProminence.PROMINENT,
                ),
                DeviceObservationClassifier.classify(observation),
            )
        }
    }

    @Test
    fun `unavailable measurements carry a user-action or unavailable reason`() {
        val cases =
            listOf(
                DeviceObservation.Measurement(MeasurementKind.CPU, MeasurementOutcome.UNAVAILABLE) to
                    ObservationReason.CPU_READING_UNAVAILABLE,
                DeviceObservation.Measurement(MeasurementKind.RAM, MeasurementOutcome.UNAVAILABLE) to
                    ObservationReason.RAM_READING_UNAVAILABLE,
                DeviceObservation.Measurement(MeasurementKind.GPU, MeasurementOutcome.UNAVAILABLE) to
                    ObservationReason.GPU_READING_UNAVAILABLE,
                DeviceObservation.Measurement(MeasurementKind.DISPLAY, MeasurementOutcome.UNAVAILABLE) to
                    ObservationReason.DISPLAY_READING_UNAVAILABLE,
                DeviceObservation.SimInventory(SimInventoryCode.NO_SIM) to ObservationReason.SIM_NOT_PRESENT,
                DeviceObservation.SimInventory(SimInventoryCode.INACTIVE_SIM) to ObservationReason.SIM_INACTIVE,
                DeviceObservation.SimInventory(SimInventoryCode.UNKNOWN) to ObservationReason.SIM_INVENTORY_UNKNOWN,
                DeviceObservation.SimSlot(SimSlotStateCode.PIN_REQUIRED) to ObservationReason.SIM_PIN_REQUIRED,
                DeviceObservation.SimSlot(SimSlotStateCode.PUK_REQUIRED) to ObservationReason.SIM_PUK_REQUIRED,
                DeviceObservation.SimSlot(SimSlotStateCode.NOT_READY) to ObservationReason.SIM_NOT_READY,
                DeviceObservation.SerialNumber(available = false) to ObservationReason.SERIAL_RESTRICTED,
                DeviceObservation.GpsProvider(enabled = false) to ObservationReason.GPS_DISABLED,
                DeviceObservation.GpsFix(GpsFixOutcome.TIMEOUT) to ObservationReason.GPS_TIMEOUT,
                DeviceObservation.GpsFix(GpsFixOutcome.START_FAILED) to ObservationReason.GPS_START_FAILED,
                DeviceObservation.Measurement(MeasurementKind.CAMERA, MeasurementOutcome.ERROR) to
                    ObservationReason.CAMERA_MEASUREMENT_ERROR,
                DeviceObservation.Measurement(MeasurementKind.SENSORS, MeasurementOutcome.ERROR) to
                    ObservationReason.SENSOR_MEASUREMENT_ERROR,
                DeviceObservation.Measurement(MeasurementKind.GENERIC, MeasurementOutcome.IN_PROGRESS) to
                    ObservationReason.MEASUREMENT_IN_PROGRESS,
                DeviceObservation.ButtonTest(ButtonTestOutcome.TIMED_OUT) to ObservationReason.BUTTON_TEST_TIMEOUT,
                DeviceObservation.ButtonTest(ButtonTestOutcome.SKIPPED) to ObservationReason.TEST_SKIPPED,
                DeviceObservation.Biometric(BiometricOutcome.NOT_RECOGNIZED) to
                    ObservationReason.BIOMETRIC_NOT_RECOGNIZED,
                DeviceObservation.Biometric(BiometricOutcome.ERROR) to ObservationReason.BIOMETRIC_ERROR,
                DeviceObservation.Permission(PermissionObservation.DENIED) to ObservationReason.PERMISSION_DENIED,
                DeviceObservation.Permission(PermissionObservation.HARDWARE_ABSENT) to
                    ObservationReason.HARDWARE_UNAVAILABLE,
                DeviceObservation.Thermal(ThermalStatusCode.UNAVAILABLE) to
                    ObservationReason.THERMAL_STATUS_UNAVAILABLE,
                DeviceObservation.BatteryHealth(BatteryHealthCode.UNKNOWN) to
                    ObservationReason.BATTERY_HEALTH_UNAVAILABLE,
            )

        cases.forEach { (observation, reason) ->
            val classification = DeviceObservationClassifier.classify(observation)
            assertEquals(ObservationState.NOT_MEASURED, classification.state)
            assertEquals(reason, classification.reason)
            assertEquals(reason.notMeasuredKind, classification.notMeasuredKind)
        }
    }

    @Test
    fun `successful and normal observations pass without a reason`() {
        val observations =
            listOf(
                DeviceObservation.RootArtifact(detected = false),
                DeviceObservation.DeveloperOptions(enabled = false),
                DeviceObservation.UsbDebugging(enabled = false),
                DeviceObservation.SerialNumber(available = true),
                DeviceObservation.Measurement(MeasurementKind.CPU, MeasurementOutcome.MEASURED),
                DeviceObservation.SimInventory(SimInventoryCode.SINGLE_SIM),
                DeviceObservation.SimSlot(SimSlotStateCode.READY),
                DeviceObservation.SimSlot(SimSlotStateCode.ABSENT),
                DeviceObservation.GpsProvider(enabled = true),
                DeviceObservation.GpsFix(GpsFixOutcome.FIXED),
                DeviceObservation.BatteryHealth(BatteryHealthCode.GOOD),
                DeviceObservation.BatteryTemperature(25f),
                DeviceObservation.Thermal(ThermalStatusCode.NONE),
                DeviceObservation.ButtonTest(ButtonTestOutcome.COMPLETED),
                DeviceObservation.Biometric(BiometricOutcome.SUCCESS),
                DeviceObservation.Permission(PermissionObservation.GRANTED),
                DeviceObservation.UserConfirmation(InteractiveCheck.DISPLAY, passed = true),
            )

        observations.forEach { observation ->
            val classification = DeviceObservationClassifier.classify(observation)
            assertEquals(ObservationState.PASS, classification.state)
            assertNull(classification.reason)
        }
    }

    @Test
    fun `empty second slot on a single sim phone stays unused`() {
        val emptySecondSlot =
            SimSlotInfo(
                slotIndex = 1,
                state = SimSlotStateCode.ABSENT,
                activity = SimActivityCode.INACTIVE,
                formFactor = SimFormFactorCode.UNKNOWN,
                operatorName = null,
                countryIso = null,
                networkType = NetworkGenerationCode.UNKNOWN,
            )

        assertTrue(isUnusedSimSlot(SimInventoryCode.SINGLE_SIM, emptySecondSlot))
        assertFalse(
            isUnusedSimSlot(
                SimInventoryCode.SINGLE_SIM,
                emptySecondSlot.copy(
                    state = SimSlotStateCode.NOT_READY,
                    formFactor = SimFormFactorCode.PHYSICAL,
                ),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `noted classifications require a reason`() {
        ObservationClassification(ObservationState.NOTED)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `not measured classifications require a reason`() {
        ObservationClassification(ObservationState.NOT_MEASURED)
    }

    @Test
    fun `classification has one legacy report adapter`() {
        assertEquals(DiagnosticStatus.PASS, ObservationClassification(ObservationState.PASS).toDiagnosticStatus())
        assertEquals(
            DiagnosticStatus.INFO,
            ObservationClassification(ObservationState.PASS).toDiagnosticStatus(informationalPass = true),
        )
        assertEquals(
            DiagnosticStatus.FAIL,
            ObservationClassification(ObservationState.FAULT, ObservationReason.BATTERY_DEAD).toDiagnosticStatus(),
        )
        assertEquals(
            DiagnosticStatus.WARNING,
            ObservationClassification(ObservationState.NOTED, ObservationReason.BATTERY_OVERHEAT)
                .toDiagnosticStatus(),
        )
        assertEquals(
            DiagnosticStatus.NOT_TESTED,
            ObservationClassification(ObservationState.NOT_MEASURED, ObservationReason.PERMISSION_DENIED)
                .toDiagnosticStatus(),
        )
        assertEquals(
            DiagnosticStatus.NOT_AVAILABLE,
            ObservationClassification(ObservationState.NOT_MEASURED, ObservationReason.HARDWARE_UNAVAILABLE)
                .toDiagnosticStatus(),
        )
        assertEquals(
            EvidenceReasonCode("gps_disabled"),
            ObservationClassification(ObservationState.NOT_MEASURED, ObservationReason.GPS_DISABLED)
                .toEvidenceReasonCode(),
        )
    }
}
