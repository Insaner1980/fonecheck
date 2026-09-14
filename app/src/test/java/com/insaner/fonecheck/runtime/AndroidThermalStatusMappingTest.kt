package com.insaner.fonecheck.runtime

import android.os.PowerManager
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidThermalStatusMappingTest {
    @Test
    fun mapsEveryKnownAndroidThermalStatus() {
        val cases =
            listOf(
                PowerManager.THERMAL_STATUS_NONE to ThermalStatusCode.NONE,
                PowerManager.THERMAL_STATUS_LIGHT to ThermalStatusCode.LIGHT,
                PowerManager.THERMAL_STATUS_MODERATE to ThermalStatusCode.MODERATE,
                PowerManager.THERMAL_STATUS_SEVERE to ThermalStatusCode.SEVERE,
                PowerManager.THERMAL_STATUS_CRITICAL to ThermalStatusCode.CRITICAL,
                PowerManager.THERMAL_STATUS_EMERGENCY to ThermalStatusCode.EMERGENCY,
                PowerManager.THERMAL_STATUS_SHUTDOWN to ThermalStatusCode.SHUTDOWN,
            )

        cases.forEach { (rawStatus, expected) ->
            assertEquals("Android thermal status $rawStatus", expected, thermalStatusCodeFromAndroid(rawStatus))
        }
    }

    @Test
    fun unknownAndroidThermalStatusIsUnavailable() {
        assertEquals(ThermalStatusCode.UNAVAILABLE, thermalStatusCodeFromAndroid(99))
        assertEquals(ThermalStatusCode.UNAVAILABLE, thermalStatusCodeFromAndroid(-1))
    }
}
