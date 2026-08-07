package com.insaner.fonecheck.ui.screens.deviceinfo

import com.insaner.fonecheck.domain.model.DeviceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceInfoProbeTest {
    @Test
    fun textNormalizationUsesStableUnavailableValue() {
        assertEquals("Pixel 9", DeviceValueNormalizer.text("  Pixel 9  "))
        assertEquals(DeviceInfo.UNAVAILABLE, DeviceValueNormalizer.text(null))
        assertEquals(DeviceInfo.UNAVAILABLE, DeviceValueNormalizer.text("  "))
        assertEquals(DeviceInfo.UNAVAILABLE, DeviceValueNormalizer.text("unknown"))
        assertEquals(DeviceInfo.UNAVAILABLE, DeviceValueNormalizer.text("UNKNOWN"))
    }

    @Test
    fun securityPatchAcceptsOnlyIsoCalendarDates() {
        assertEquals("2026-08-05", DeviceValueNormalizer.securityPatch(" 2026-08-05 "))
        assertEquals(DeviceInfo.UNAVAILABLE, DeviceValueNormalizer.securityPatch("2026-02-30"))
        assertEquals(DeviceInfo.UNAVAILABLE, DeviceValueNormalizer.securityPatch("August 2026"))
    }

    @Test
    fun rootHeuristicChecksOnlyItsBoundedArtifactList() {
        val checkedPaths = mutableListOf<String>()

        val detected =
            RootArtifactHeuristic.detect { path ->
                checkedPaths += path
                path == "/system/bin/su"
            }

        assertTrue(detected)
        assertTrue(checkedPaths.all { it in RootArtifactHeuristic.knownPaths })
        assertTrue(checkedPaths.size <= RootArtifactHeuristic.knownPaths.size)
    }

    @Test
    fun missingOrUnreadableRootArtifactsDoNotProduceDetection() {
        assertFalse(RootArtifactHeuristic.detect { false })
        assertFalse(RootArtifactHeuristic.detect { throw SecurityException("blocked") })
    }

    @Test
    fun drmSessionIsClosedAfterSuccessfulRead() {
        val session = FakeDrmSession(level = " L1 ")

        val level = readDrmSecurityLevel { session }

        assertEquals("L1", level)
        assertTrue(session.closed)
    }

    @Test
    fun drmSessionIsClosedWhenReadingFails() {
        val session = FakeDrmSession(error = IllegalStateException("unavailable"))

        val level = readDrmSecurityLevel { session }

        assertEquals(DeviceInfo.UNAVAILABLE, level)
        assertTrue(session.closed)
    }

    private class FakeDrmSession(
        private val level: String = "",
        private val error: RuntimeException? = null,
    ) : DrmPropertySession {
        var closed = false
            private set

        override fun securityLevel(): String {
            error?.let { throw it }
            return level
        }

        override fun close() {
            closed = true
        }
    }
}
