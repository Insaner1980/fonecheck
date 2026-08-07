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
}
