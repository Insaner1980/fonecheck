package com.insaner.fonecheck.ui.screens.storage

import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.NanoTimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

class StorageBenchmarkTest {
    @Test
    fun fixedWorkloadProducesRawRatesChecksumAndCleansUp() =
        runTest {
            val store = InMemoryStorageBenchmarkStore(availableBytes = 12)
            val runner = runner(store, times = longArrayOf(0L, SECOND, SECOND, 3L * SECOND))

            val result = runner.run()

            assertNull(result.error)
            assertEquals(8L, result.bytesWritten)
            assertEquals(8L, result.bytesRead)
            assertEquals(8.0 / MEBIBYTE, result.writeMebibytesPerSecond ?: 0.0, 0.0000001)
            assertEquals(4.0 / MEBIBYTE, result.readMebibytesPerSecond ?: 0.0, 0.0000001)
            assertEquals(3_000L, result.durationMillis)
            assertEquals(8L, result.dataSizeBytes)
            assertEquals(12L, result.availableBeforeBytes)
            assertNotEquals(0L, result.checksumCrc32)
            assertTrue(result.cleanupSucceeded)
            assertTrue(store.deleted)
            assertEquals(Instant.parse("2026-08-08T00:00:00Z"), result.capturedAt)
        }

    @Test
    fun insufficientFreeSpaceDoesNotCreateTemporaryFile() =
        runTest {
            val store = InMemoryStorageBenchmarkStore(availableBytes = 11)

            val result = runner(store).run()

            assertEquals(StorageBenchmarkErrorCode.INSUFFICIENT_SPACE, result.error)
            assertEquals(0L, result.bytesWritten)
            assertFalse(store.created)
            assertFalse(store.deleted)
        }

    @Test
    fun readFailureStillDeletesTemporaryFile() =
        runTest {
            val store =
                InMemoryStorageBenchmarkStore(
                    availableBytes = 12,
                    readFailure = IOException("read failed"),
                )

            val result = runner(store, times = longArrayOf(0L, SECOND)).run()

            assertEquals(StorageBenchmarkErrorCode.IO_ERROR, result.error)
            assertTrue(result.cleanupSucceeded)
            assertTrue(store.deleted)
        }

    @Test
    fun cancellationStillDeletesTemporaryFile() =
        runTest {
            val store =
                InMemoryStorageBenchmarkStore(
                    availableBytes = 12,
                    writeFailure = CancellationException("cancelled"),
                )
            var cancelled = false

            try {
                runner(store, times = longArrayOf(0L)).run()
            } catch (_: CancellationException) {
                cancelled = true
            }

            assertTrue(cancelled)
            assertTrue(store.deleted)
        }

    @Test
    fun corruptedReadIsReportedAndTemporaryFileIsDeleted() =
        runTest {
            val store =
                InMemoryStorageBenchmarkStore(
                    availableBytes = 12,
                    corruptRead = true,
                )

            val result = runner(store, times = longArrayOf(0L, SECOND, SECOND, 2L * SECOND)).run()

            assertEquals(StorageBenchmarkErrorCode.DATA_MISMATCH, result.error)
            assertTrue(result.cleanupSucceeded)
            assertTrue(store.deleted)
        }

    @Test
    fun cleanupFailureIsReportedWithoutClaimingSuccess() =
        runTest {
            val store =
                InMemoryStorageBenchmarkStore(
                    availableBytes = 12,
                    deleteSucceeds = false,
                )

            val result = runner(store, times = longArrayOf(0L, SECOND, SECOND, 2L * SECOND)).run()

            assertEquals(StorageBenchmarkErrorCode.CLEANUP_FAILED, result.error)
            assertFalse(result.cleanupSucceeded)
        }

    private fun runner(
        store: StorageBenchmarkStore,
        times: LongArray = longArrayOf(),
    ) = DefaultStorageBenchmarkRunner(
        store = store,
        nanoTimeSource = SequenceNanoTimeSource(*times),
        epochMillisClock = EpochMillisClock { Instant.parse("2026-08-08T00:00:00Z").toEpochMilli() },
        config =
            StorageBenchmarkConfig(
                dataSizeBytes = 8,
                bufferSizeBytes = 4,
                minimumFreeBytesAfterRun = 4,
            ),
    )

    private class InMemoryStorageBenchmarkStore(
        private val availableBytes: Long,
        private val writeFailure: Exception? = null,
        private val readFailure: Exception? = null,
        private val corruptRead: Boolean = false,
        private val deleteSucceeds: Boolean = true,
    ) : StorageBenchmarkStore {
        private var bytes = ByteArray(0)
        var created = false
        var deleted = false

        override fun availableBytes(): Long = availableBytes

        override fun createTemporaryFile(): StorageBenchmarkFile {
            created = true
            return object : StorageBenchmarkFile {
                override fun openOutput(): OutputStream {
                    writeFailure?.let { throw it }
                    return object : ByteArrayOutputStream() {
                        override fun close() {
                            bytes = toByteArray()
                            super.close()
                        }
                    }
                }

                override fun openInput(): InputStream {
                    readFailure?.let { throw it }
                    val readable = bytes.copyOf()
                    if (corruptRead && readable.isNotEmpty()) readable[0] = (readable[0] + 1).toByte()
                    return ByteArrayInputStream(readable)
                }
            }
        }

        override fun delete(file: StorageBenchmarkFile): Boolean {
            deleted = true
            return deleteSucceeds
        }
    }

    private class SequenceNanoTimeSource(
        vararg values: Long,
    ) : NanoTimeSource {
        private val values = ArrayDeque(values.toList())

        override fun nanoTime(): Long = values.removeFirst()
    }

    private companion object {
        const val SECOND = 1_000_000_000L
        const val MEBIBYTE = 1_048_576.0
    }
}
