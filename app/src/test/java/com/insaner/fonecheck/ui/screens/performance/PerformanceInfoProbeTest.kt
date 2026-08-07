package com.insaner.fonecheck.ui.screens.performance

import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CpuCoreFrequency
import com.insaner.fonecheck.domain.model.PerformanceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceInfoProbeTest {
    @Test
    fun cpuConfidenceIsLowWhenEveryFrequencyIsUnavailable() {
        val frequencies = listOf(CpuCoreFrequency(0, null, null, null))

        assertEquals(Confidence.LOW, PerformanceInfoConfidence.cpu(frequencies))
    }

    @Test
    fun cpuConfidenceIsHighWhenAtLeastOneFrequencyIsAvailable() {
        val frequencies = listOf(CpuCoreFrequency(0, 1_800L, null, 2_800L))

        assertEquals(Confidence.HIGH, PerformanceInfoConfidence.cpu(frequencies))
    }

    @Test
    fun glSessionIsClosedAfterSuccessfulRead() {
        val session = FakeGlInfoSession()

        val info = readGlInfo { session }

        assertEquals("OpenGL ES 3.2", info.version)
        assertEquals("Renderer", info.renderer)
        assertEquals("Vendor", info.vendor)
        assertTrue(session.closed)
    }

    @Test
    fun glSessionIsClosedWhenReadingFails() {
        val session = FakeGlInfoSession(error = IllegalStateException("read failed"))

        val info = readGlInfo { session }

        assertEquals(PerformanceInfo.UNAVAILABLE, info.version)
        assertEquals(PerformanceInfo.UNAVAILABLE, info.renderer)
        assertEquals(PerformanceInfo.UNAVAILABLE, info.vendor)
        assertTrue(session.closed)
    }

    private class FakeGlInfoSession(
        private val error: RuntimeException? = null,
    ) : GlInfoSession {
        var closed = false
            private set

        override fun version(): String {
            error?.let { throw it }
            return "OpenGL ES 3.2"
        }

        override fun renderer(): String = "Renderer"

        override fun vendor(): String = "Vendor"

        override fun close() {
            closed = true
        }
    }
}
