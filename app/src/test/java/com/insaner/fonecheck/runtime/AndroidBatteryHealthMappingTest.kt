package com.insaner.fonecheck.runtime

import android.os.BatteryManager
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.observation.BatteryHealthCode
import com.insaner.fonecheck.domain.observation.DeviceObservation
import com.insaner.fonecheck.domain.observation.DeviceObservationClassifier
import com.insaner.fonecheck.domain.observation.ObservationClassification
import com.insaner.fonecheck.domain.observation.ObservationProminence
import com.insaner.fonecheck.domain.observation.ObservationReason
import com.insaner.fonecheck.domain.observation.ObservationState
import com.insaner.fonecheck.domain.observation.toDiagnosticStatus
import com.insaner.fonecheck.ui.classification.classifyBatteryHealth
import com.insaner.fonecheck.ui.screens.battery.getHealthLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidBatteryHealthMappingTest {
    @Test
    fun everyAndroidInputRetainsItsSemanticIdentity() {
        cases.forEach { case ->
            assertEquals("Android health ${case.raw}", case.code, batteryHealthCodeFromAndroid(case.raw))
        }
        assertEquals(BatteryHealthCode.entries.toSet(), cases.map { it.code }.toSet())
    }

    @Test
    fun standaloneLabelsRemainUnchanged() {
        cases.forEach { case ->
            assertEquals("Android health ${case.raw}", case.label, getHealthLabel(case.raw))
        }
    }

    @Test
    fun classificationAndDurableStatusRemainUnchanged() {
        cases.forEach { case ->
            val expected = ObservationClassification(case.state, case.reason, ObservationProminence.STANDARD)
            val actual = classifyBatteryHealth(case.raw)
            assertEquals(expected, actual)
            assertEquals(expected, DeviceObservationClassifier.classify(DeviceObservation.BatteryHealth(case.code)))
            val status =
                when (case.state) {
                    ObservationState.PASS -> DiagnosticStatus.PASS
                    ObservationState.FAULT -> DiagnosticStatus.FAIL
                    ObservationState.NOTED -> DiagnosticStatus.WARNING
                    ObservationState.NOT_MEASURED -> DiagnosticStatus.NOT_AVAILABLE
                }
            assertEquals(status, actual.toDiagnosticStatus())
            assertEquals(
                if (case.state == ObservationState.PASS) DiagnosticStatus.INFO else status,
                actual.toDiagnosticStatus(informationalPass = true),
            )
        }
    }

    private data class HealthCase(
        val raw: Int,
        val code: BatteryHealthCode,
        val label: Int,
        val state: ObservationState,
        val reason: ObservationReason?,
    )

    private val cases =
        listOf(
            HealthCase(
                BatteryManager.BATTERY_HEALTH_UNKNOWN,
                BatteryHealthCode.UNKNOWN,
                R.string.batt_health_unknown,
                ObservationState.NOT_MEASURED,
                ObservationReason.BATTERY_HEALTH_UNAVAILABLE,
            ),
            HealthCase(
                BatteryManager.BATTERY_HEALTH_GOOD,
                BatteryHealthCode.GOOD,
                R.string.batt_health_good,
                ObservationState.PASS,
                null,
            ),
            HealthCase(
                BatteryManager.BATTERY_HEALTH_OVERHEAT,
                BatteryHealthCode.OVERHEAT,
                R.string.batt_health_overheat,
                ObservationState.NOTED,
                ObservationReason.BATTERY_OVERHEAT,
            ),
            HealthCase(
                BatteryManager.BATTERY_HEALTH_DEAD,
                BatteryHealthCode.DEAD,
                R.string.batt_health_dead,
                ObservationState.FAULT,
                ObservationReason.BATTERY_DEAD,
            ),
            HealthCase(
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE,
                BatteryHealthCode.OVER_VOLTAGE,
                R.string.batt_health_over_voltage,
                ObservationState.FAULT,
                ObservationReason.BATTERY_OVER_VOLTAGE,
            ),
            HealthCase(
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE,
                BatteryHealthCode.UNSPECIFIED_FAILURE,
                R.string.batt_health_failure,
                ObservationState.FAULT,
                ObservationReason.BATTERY_UNSPECIFIED_FAILURE,
            ),
            HealthCase(
                BatteryManager.BATTERY_HEALTH_COLD,
                BatteryHealthCode.COLD,
                R.string.batt_health_cold,
                ObservationState.NOTED,
                ObservationReason.BATTERY_COLD,
            ),
            HealthCase(
                99,
                BatteryHealthCode.OTHER,
                R.string.batt_health_unknown,
                ObservationState.NOT_MEASURED,
                ObservationReason.BATTERY_HEALTH_UNAVAILABLE,
            ),
            HealthCase(
                -1,
                BatteryHealthCode.OTHER,
                R.string.batt_health_unknown,
                ObservationState.NOT_MEASURED,
                ObservationReason.BATTERY_HEALTH_UNAVAILABLE,
            ),
        )
}
