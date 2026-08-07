package com.insaner.fonecheck.ui.screens.thermal

import com.insaner.fonecheck.domain.model.ThermalStatusCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThermalRuntimePolicyTest {
    @Test
    fun statusMappingSeparatesUnsupportedApiFromEveryPublicStatus() {
        assertEquals(
            ThermalStatusCode.UNAVAILABLE,
            ThermalRuntimePolicy.status(sdkInt = 28, rawStatus = 0),
        )
        assertEquals(ThermalStatusCode.NONE, ThermalRuntimePolicy.status(sdkInt = 29, rawStatus = 0))
        assertEquals(ThermalStatusCode.LIGHT, ThermalRuntimePolicy.status(sdkInt = 29, rawStatus = 1))
        assertEquals(ThermalStatusCode.MODERATE, ThermalRuntimePolicy.status(sdkInt = 29, rawStatus = 2))
        assertEquals(ThermalStatusCode.SEVERE, ThermalRuntimePolicy.status(sdkInt = 29, rawStatus = 3))
        assertEquals(ThermalStatusCode.CRITICAL, ThermalRuntimePolicy.status(sdkInt = 29, rawStatus = 4))
        assertEquals(ThermalStatusCode.EMERGENCY, ThermalRuntimePolicy.status(sdkInt = 29, rawStatus = 5))
        assertEquals(ThermalStatusCode.SHUTDOWN, ThermalRuntimePolicy.status(sdkInt = 29, rawStatus = 6))
        assertEquals(ThermalStatusCode.UNAVAILABLE, ThermalRuntimePolicy.status(sdkInt = 29, rawStatus = 99))
    }

    @Test
    fun severityDoesNotClaimThrottlingForUnavailableOrNormalStatus() {
        assertEquals(ThermalSeverityCode.UNAVAILABLE, ThermalRuntimePolicy.severity(ThermalStatusCode.UNAVAILABLE))
        assertEquals(ThermalSeverityCode.NORMAL, ThermalRuntimePolicy.severity(ThermalStatusCode.NONE))
        assertEquals(ThermalSeverityCode.LIGHT, ThermalRuntimePolicy.severity(ThermalStatusCode.LIGHT))
        assertEquals(ThermalSeverityCode.MODERATE, ThermalRuntimePolicy.severity(ThermalStatusCode.MODERATE))
        assertEquals(ThermalSeverityCode.SEVERE, ThermalRuntimePolicy.severity(ThermalStatusCode.SEVERE))
        listOf(
            ThermalStatusCode.CRITICAL,
            ThermalStatusCode.EMERGENCY,
            ThermalStatusCode.SHUTDOWN,
        ).forEach { status ->
            assertEquals(ThermalSeverityCode.CRITICAL, ThermalRuntimePolicy.severity(status))
        }
    }

    @Test
    fun headroomAndBatteryTemperatureRejectUnsupportedOrInvalidReadings() {
        assertNull(ThermalRuntimePolicy.headroom(sdkInt = 29, rawHeadroom = 0.5f))
        assertNull(ThermalRuntimePolicy.headroom(sdkInt = 30, rawHeadroom = Float.NaN))
        assertNull(ThermalRuntimePolicy.headroom(sdkInt = 30, rawHeadroom = Float.POSITIVE_INFINITY))
        assertNull(ThermalRuntimePolicy.headroom(sdkInt = 30, rawHeadroom = -0.1f))
        assertEquals(0.75f, ThermalRuntimePolicy.headroom(sdkInt = 30, rawHeadroom = 0.75f))

        assertNull(ThermalRuntimePolicy.batteryTemperature(rawTenthsCelsius = null))
        assertNull(ThermalRuntimePolicy.batteryTemperature(rawTenthsCelsius = -501))
        assertNull(ThermalRuntimePolicy.batteryTemperature(rawTenthsCelsius = 1_001))
        assertEquals(32.5f, ThermalRuntimePolicy.batteryTemperature(rawTenthsCelsius = 325))
    }
}
