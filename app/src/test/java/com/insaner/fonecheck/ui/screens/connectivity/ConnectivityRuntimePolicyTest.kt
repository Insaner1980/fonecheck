package com.insaner.fonecheck.ui.screens.connectivity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectivityRuntimePolicyTest {
    @Test
    fun gpsGateRejectsOverlapAndIgnoresLateCallbacksAfterCancellation() {
        val gate = GpsSearchGate(timeoutMillis = 60_000L)
        val token = requireNotNull(gate.start(nowMillis = 1_000L))

        assertNull(gate.start(nowMillis = 2_000L))
        assertEquals(GpsSearchTick.ACTIVE, gate.tick(token, nowMillis = 60_999L))

        gate.cancel(token)

        assertFalse(gate.complete(token))
        assertEquals(GpsSearchTick.IGNORED, gate.tick(token, nowMillis = 61_000L))
    }

    @Test
    fun gpsGateTimesOutAtTheBoundaryAndAllowsANewSearch() {
        val gate = GpsSearchGate(timeoutMillis = 60_000L)
        val first = requireNotNull(gate.start(nowMillis = 1_000L))

        assertEquals(GpsSearchTick.TIMED_OUT, gate.tick(first, nowMillis = 61_000L))
        assertFalse(gate.complete(first))
        val second = requireNotNull(gate.start(nowMillis = 61_001L))
        assertTrue(gate.complete(second))
        assertFalse(gate.complete(second))
    }

    @Test
    fun callbackOwnerUnregistersTheExactCallbackOnce() {
        val removed = mutableListOf<String>()
        val owner = CallbackOwner<String> { removed += it }

        owner.replace("first")
        owner.replace("first")
        owner.replace("second")
        owner.clear()
        owner.clear()

        assertEquals(listOf("first", "second"), removed)
    }

    @Test
    fun bluetoothAccessSeparatesHardwareAbsencePermissionDenialAndPre31Access() {
        assertEquals(
            BluetoothAccessCode.HARDWARE_ABSENT,
            BluetoothAccessPolicy.evaluate(sdkInt = 36, hardwareAvailable = false, permissionGranted = false),
        )
        assertEquals(
            BluetoothAccessCode.PERMISSION_DENIED,
            BluetoothAccessPolicy.evaluate(sdkInt = 31, hardwareAvailable = true, permissionGranted = false),
        )
        assertEquals(
            BluetoothAccessCode.NOT_REQUIRED,
            BluetoothAccessPolicy.evaluate(sdkInt = 30, hardwareAvailable = true, permissionGranted = false),
        )
        assertEquals(
            BluetoothAccessCode.GRANTED,
            BluetoothAccessPolicy.evaluate(sdkInt = 36, hardwareAvailable = true, permissionGranted = true),
        )
    }

    @Test
    fun clearingProtectedGpsDataRemovesEveryPreviousFixValue() {
        val cleared =
            GpsState(
                isAvailable = true,
                isEnabled = true,
                fixStatus = GpsFixStatus.FIXED,
                latitude = 60.1699,
                longitude = 24.9384,
                accuracy = 3.5f,
                altitude = 22.0,
                speed = 1.5f,
                fixTimeMs = 1_000L,
                satelliteCount = 8,
                satellitesUsed = 5,
                satellites = listOf(GpsSatelliteInfo(7, "GPS", 30f, true, 45f, 90f)),
                elapsedSearchMs = 2_000L,
                failure = GpsFailureCode.TIMEOUT,
            ).clearedProtectedFixData()

        assertEquals(GpsState(isAvailable = true, isEnabled = true), cleared)
    }
}
