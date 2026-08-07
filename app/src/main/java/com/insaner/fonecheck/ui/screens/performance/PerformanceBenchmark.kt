package com.insaner.fonecheck.ui.screens.performance

import com.insaner.fonecheck.domain.model.BenchmarkErrorCode
import com.insaner.fonecheck.domain.model.PerformanceBenchmarkResult
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.NanoTimeSource
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.yield

data class PerformanceBenchmarkConfig(
    val cpuIterations: Int = 2_000_000,
    val memoryBufferBytes: Int = 4 * MEBIBYTE,
    val memoryPasses: Int = 8,
) {
    init {
        require(cpuIterations in 1..MAX_CPU_ITERATIONS)
        require(memoryBufferBytes in 1..MAX_MEMORY_BUFFER_BYTES)
        require(memoryPasses in 1..MAX_MEMORY_PASSES)
    }

    private companion object {
        const val MEBIBYTE = 1_048_576
        const val MAX_CPU_ITERATIONS = 10_000_000
        const val MAX_MEMORY_BUFFER_BYTES = 8 * MEBIBYTE
        const val MAX_MEMORY_PASSES = 64
    }
}

fun interface PerformanceBenchmarkRunner {
    suspend fun run(): PerformanceBenchmarkResult
}

fun interface ThermalStatusReader {
    fun read(): ThermalStatusCode
}

fun interface ByteArrayAllocator {
    fun allocate(size: Int): ByteArray
}

class DefaultPerformanceBenchmarkRunner internal constructor(
    private val nanoTimeSource: NanoTimeSource,
    private val thermalStatusReader: ThermalStatusReader,
    private val epochMillisClock: EpochMillisClock,
    private val byteArrayAllocator: ByteArrayAllocator,
    private val config: PerformanceBenchmarkConfig,
) : PerformanceBenchmarkRunner {
    @Inject
    constructor(
        nanoTimeSource: NanoTimeSource,
        thermalStatusReader: ThermalStatusReader,
        epochMillisClock: EpochMillisClock,
    ) : this(
        nanoTimeSource = nanoTimeSource,
        thermalStatusReader = thermalStatusReader,
        epochMillisClock = epochMillisClock,
        byteArrayAllocator = ByteArrayAllocator { ByteArray(it) },
        config = PerformanceBenchmarkConfig(),
    )

    override suspend fun run(): PerformanceBenchmarkResult {
        val thermalBefore = thermalStatusReader.read()
        val cpu = measureCpu()
        val memory = measureMemory()
        val thermalAfter = thermalStatusReader.read()
        benchmarkBlackhole = cpu.checksum xor (memory?.checksum ?: 0L)
        return PerformanceBenchmarkResult(
            cpuOperationsPerSecond = rate(config.cpuIterations.toLong(), cpu.durationNanos),
            memoryMebibytesPerSecond =
                memory?.let {
                    rate(it.bytesProcessed, it.durationNanos).toDouble() / MEBIBYTE
                },
            memoryBytesProcessed = memory?.bytesProcessed ?: 0L,
            durationMillis = (cpu.durationNanos + (memory?.durationNanos ?: 0L)) / NANOS_PER_MILLISECOND,
            thermalBefore = thermalBefore,
            thermalAfter = thermalAfter,
            capturedAt = Instant.ofEpochMilli(epochMillisClock.currentTimeMillis()),
            error = if (memory == null) BenchmarkErrorCode.MEMORY_ALLOCATION_FAILED else null,
        )
    }

    private suspend fun measureCpu(): TimedChecksum {
        val start = nanoTimeSource.nanoTime()
        var value = CPU_SEED
        repeat(config.cpuIterations) { iteration ->
            if (iteration % CHECKPOINT_INTERVAL == 0) {
                currentCoroutineContext().ensureActive()
                yield()
            }
            value = (value xor (value shl 13)) xor (value ushr 7) xor (value shl 17)
        }
        return TimedChecksum(
            durationNanos = elapsedSince(start),
            checksum = value,
        )
    }

    private suspend fun measureMemory(): MemoryMeasurement? =
        try {
            val buffer = byteArrayAllocator.allocate(config.memoryBufferBytes)
            val start = nanoTimeSource.nanoTime()
            var checksum = 0L
            repeat(config.memoryPasses) { pass ->
                for (index in buffer.indices) {
                    if (index % CHECKPOINT_INTERVAL == 0) {
                        currentCoroutineContext().ensureActive()
                        yield()
                    }
                    buffer[index] = (index + pass).toByte()
                }
                for (value in buffer) {
                    checksum += value.toUByte().toLong()
                }
            }
            MemoryMeasurement(
                durationNanos = elapsedSince(start),
                checksum = checksum,
                bytesProcessed = config.memoryBufferBytes.toLong() * config.memoryPasses * 2L,
            )
        } catch (_: OutOfMemoryError) {
            null
        }

    private fun elapsedSince(start: Long): Long =
        (nanoTimeSource.nanoTime() - start).coerceAtLeast(1L)

    private fun rate(
        operations: Long,
        durationNanos: Long,
    ): Long = (operations.toDouble() * NANOS_PER_SECOND / durationNanos).toLong()

    private data class TimedChecksum(
        val durationNanos: Long,
        val checksum: Long,
    )

    private data class MemoryMeasurement(
        val durationNanos: Long,
        val checksum: Long,
        val bytesProcessed: Long,
    )

    private companion object {
        const val MEBIBYTE = 1_048_576.0
        const val NANOS_PER_MILLISECOND = 1_000_000L
        const val NANOS_PER_SECOND = 1_000_000_000L
        const val CHECKPOINT_INTERVAL = 16_384
        const val CPU_SEED = 0x1234_5678_9ABC_DEF0L

        @Volatile
        var benchmarkBlackhole = 0L
    }
}
