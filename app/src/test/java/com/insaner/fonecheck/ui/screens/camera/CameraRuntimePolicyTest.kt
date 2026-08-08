package com.insaner.fonecheck.ui.screens.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraRuntimePolicyTest {
    @Test
    fun publicIdsAreClassifiedWithoutClaimingHiddenPhysicalCamerasAsSelectable() {
        val descriptors =
            CameraDescriptorMapper
                .map(
                    publicIds = setOf("0", "1", "2"),
                    readings =
                        listOf(
                            CameraDescriptorReading(
                                "0",
                                CameraFacingCode.REAR,
                                isLogical = true,
                                physicalIds = setOf("2", "3"),
                            ),
                            CameraDescriptorReading("1", CameraFacingCode.FRONT, isLogical = false),
                            CameraDescriptorReading("2", CameraFacingCode.REAR, isLogical = false),
                        ),
                ).associateBy { it.cameraId }

        assertEquals(CameraClassCode.LOGICAL, descriptors.getValue("0").cameraClass)
        assertEquals(setOf("2", "3"), descriptors.getValue("0").physicalCameraIds)
        assertEquals(CameraClassCode.STANDARD, descriptors.getValue("1").cameraClass)
        assertEquals(CameraClassCode.PHYSICAL_SELECTABLE, descriptors.getValue("2").cameraClass)
        assertFalse("3" in descriptors)
    }

    @Test
    fun externalCameraRemainsASelectablePublicCamera() {
        val descriptor =
            CameraDescriptorMapper
                .map(
                    publicIds = setOf("external"),
                    readings =
                        listOf(
                            CameraDescriptorReading("external", CameraFacingCode.EXTERNAL, isLogical = false),
                        ),
                ).single()

        assertEquals(CameraClassCode.EXTERNAL, descriptor.cameraClass)
    }

    @Test
    fun captureGateAcceptsOneSuccessAndIgnoresLateSuccessAfterTimeoutOrTeardown() {
        val gate = CameraCaptureGate()
        val first = gate.begin()
        assertTrue(gate.complete(first))
        assertFalse(gate.complete(first))

        val timedOut = gate.begin()
        gate.cancel(timedOut)
        assertFalse(gate.complete(timedOut))

        val tornDown = gate.begin()
        gate.cancelAll()
        assertFalse(gate.complete(tornDown))
        assertNull(gate.activeToken)
    }
}
