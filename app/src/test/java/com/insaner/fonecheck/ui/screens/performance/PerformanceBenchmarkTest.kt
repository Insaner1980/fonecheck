package com.insaner.fonecheck.ui.screens.performance

import com.insaner.fonecheck.domain.model.BenchmarkErrorCode
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.testing.SequenceNanoTimeSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class PerformanceBenchmarkTest {
    @Test
    fun fixedWorkloadProducesRawRatesAndConditions() =
        runTest {
            val thermalStates = ArrayDeque(listOf(ThermalStatusCode.LIGHT, ThermalStatusCode.MODERATE))
            val runner =
                DefaultPerformanceBenchmarkRunner(
                    nanoTimeSource = SequenceNanoTimeSource(0L, SECOND, SECOND, SECOND * 2),
                    thermalStatusReader = ThermalStatusReader { thermalStates.removeFirst() },
                    epochMillisClock = EpochMillisClock { 1_786_104_000_000L },
                    byteArrayAllocator = ByteArrayAllocator { size -> ByteArray(size) },
                    config =
                        PerformanceBenchmarkConfig(
                            cpuIterations = 8,
                            memoryBufferBytes = 1_048_576,
                            memoryPasses = 2,
                        ),
                )

            val result = runner.run()

            assertEquals(8L, result.cpuOperationsPerSecond)
            assertEquals(4L * 1_048_576, result.memoryBytesProcessed)
            assertEquals(4.0, result.memoryMebibytesPerSecond ?: 0.0, 0.0001)
            assertEquals(2_000L, result.durationMillis)
            assertEquals(ThermalStatusCode.LIGHT, result.thermalBefore)
            assertEquals(ThermalStatusCode.MODERATE, result.thermalAfter)
            assertEquals(Instant.parse("2026-08-07T12:00:00Z"), result.capturedAt)
            assertNull(result.error)
        }

    @Test
    fun memoryAllocationFailureReturnsPartialResult() =
        runTest {
            val runner =
                DefaultPerformanceBenchmarkRunner(
                    nanoTimeSource = SequenceNanoTimeSource(0L, SECOND),
                    thermalStatusReader = ThermalStatusReader { ThermalStatusCode.UNAVAILABLE },
                    epochMillisClock = EpochMillisClock { 0L },
                    byteArrayAllocator = ByteArrayAllocator { throw OutOfMemoryError("test") },
                    config = PerformanceBenchmarkConfig(cpuIterations = 4),
                )

            val result = runner.run()

            assertEquals(4L, result.cpuOperationsPerSecond)
            assertNull(result.memoryMebibytesPerSecond)
            assertEquals(BenchmarkErrorCode.MEMORY_ALLOCATION_FAILED, result.error)
        }

    @Test
    fun workloadConfigurationRejectsUnboundedValues() {
        assertThrows(IllegalArgumentException::class.java) {
            PerformanceBenchmarkConfig(cpuIterations = 50_000_001)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PerformanceBenchmarkConfig(memoryBufferBytes = 64 * 1_048_576)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PerformanceBenchmarkConfig(memoryPasses = 1_001)
        }
    }

    private companion object {
        const val SECOND = 1_000_000_000L
    }
}
