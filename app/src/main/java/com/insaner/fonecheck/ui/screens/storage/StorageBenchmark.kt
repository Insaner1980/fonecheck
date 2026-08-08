package com.insaner.fonecheck.ui.screens.storage

import android.content.Context
import android.os.StatFs
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.NanoTimeSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.yield
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.util.zip.CRC32
import javax.inject.Inject

data class StorageBenchmarkConfig(
    val dataSizeBytes: Int = 64 * MEBIBYTE,
    val bufferSizeBytes: Int = 64 * KIBIBYTE,
    val minimumFreeBytesAfterRun: Long = 16L * MEBIBYTE,
) {
    init {
        require(dataSizeBytes in 1..MAX_DATA_SIZE_BYTES)
        require(bufferSizeBytes in 1..MAX_BUFFER_SIZE_BYTES)
        require(minimumFreeBytesAfterRun >= 0L)
    }

    private companion object {
        const val KIBIBYTE = 1_024
        const val MEBIBYTE = 1_048_576
        const val MAX_DATA_SIZE_BYTES = 64 * MEBIBYTE
        const val MAX_BUFFER_SIZE_BYTES = MEBIBYTE
    }
}

enum class StorageBenchmarkErrorCode {
    INSUFFICIENT_SPACE,
    IO_ERROR,
    DATA_MISMATCH,
    CLEANUP_FAILED,
}

data class StorageBenchmarkResult(
    val writeMebibytesPerSecond: Double? = null,
    val readMebibytesPerSecond: Double? = null,
    val bytesWritten: Long = 0L,
    val bytesRead: Long = 0L,
    val checksumCrc32: Long = 0L,
    val durationMillis: Long = 0L,
    val dataSizeBytes: Long,
    val bufferSizeBytes: Int,
    val availableBeforeBytes: Long,
    val cleanupSucceeded: Boolean,
    val capturedAt: Instant,
    val error: StorageBenchmarkErrorCode? = null,
)

fun interface StorageBenchmarkRunner {
    suspend fun run(): StorageBenchmarkResult
}

interface StorageBenchmarkFile {
    fun openOutput(): OutputStream

    fun openInput(): InputStream
}

interface StorageBenchmarkStore {
    fun availableBytes(): Long

    fun createTemporaryFile(): StorageBenchmarkFile

    fun delete(file: StorageBenchmarkFile): Boolean
}

class AndroidStorageBenchmarkStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : StorageBenchmarkStore {
        private val cacheDirectory = context.cacheDir

        override fun availableBytes(): Long = StatFs(cacheDirectory.absolutePath).availableBytes

        override fun createTemporaryFile(): StorageBenchmarkFile =
            AndroidStorageBenchmarkFile(
                File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, cacheDirectory),
            )

        override fun delete(file: StorageBenchmarkFile): Boolean {
            val androidFile = file as? AndroidStorageBenchmarkFile ?: return false
            return !androidFile.file.exists() || androidFile.file.delete()
        }

        private class AndroidStorageBenchmarkFile(
            val file: File,
        ) : StorageBenchmarkFile {
            override fun openOutput(): OutputStream = FileOutputStream(file, false)

            override fun openInput(): InputStream = FileInputStream(file)
        }

        private companion object {
            const val TEMP_FILE_PREFIX = "fonecheck-storage-"
            const val TEMP_FILE_SUFFIX = ".tmp"
        }
    }

class DefaultStorageBenchmarkRunner internal constructor(
    private val store: StorageBenchmarkStore,
    private val nanoTimeSource: NanoTimeSource,
    private val epochMillisClock: EpochMillisClock,
    private val config: StorageBenchmarkConfig,
) : StorageBenchmarkRunner {
    @Inject
    constructor(
        store: StorageBenchmarkStore,
        nanoTimeSource: NanoTimeSource,
        epochMillisClock: EpochMillisClock,
    ) : this(
        store = store,
        nanoTimeSource = nanoTimeSource,
        epochMillisClock = epochMillisClock,
        config = StorageBenchmarkConfig(),
    )

    override suspend fun run(): StorageBenchmarkResult {
        val availableBytes = store.availableBytes()
        if (
            !StorageRuntimePolicy.hasBenchmarkSpace(
                availableBytes = availableBytes,
                dataSizeBytes = config.dataSizeBytes.toLong(),
                reserveBytes = config.minimumFreeBytesAfterRun,
            )
        ) {
            return emptyResult(
                availableBytes = availableBytes,
                cleanupSucceeded = true,
                error = StorageBenchmarkErrorCode.INSUFFICIENT_SPACE,
            )
        }

        val temporaryFile =
            try {
                store.createTemporaryFile()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                return emptyResult(
                    availableBytes = availableBytes,
                    cleanupSucceeded = true,
                    error = StorageBenchmarkErrorCode.IO_ERROR,
                )
            }

        var cancellation: CancellationException? = null
        val measured =
            try {
                measure(temporaryFile, availableBytes)
            } catch (error: CancellationException) {
                cancellation = error
                null
            } catch (_: Exception) {
                emptyResult(
                    availableBytes = availableBytes,
                    cleanupSucceeded = false,
                    error = StorageBenchmarkErrorCode.IO_ERROR,
                )
            }
        val cleanupSucceeded =
            try {
                store.delete(temporaryFile)
            } catch (_: Exception) {
                false
            }
        cancellation?.let { throw it }

        val result = requireNotNull(measured).copy(cleanupSucceeded = cleanupSucceeded)
        return if (!cleanupSucceeded && result.error == null) {
            result.copy(error = StorageBenchmarkErrorCode.CLEANUP_FAILED)
        } else {
            result
        }
    }

    private suspend fun measure(
        file: StorageBenchmarkFile,
        availableBytes: Long,
    ): StorageBenchmarkResult {
        val buffer = benchmarkBuffer(config.bufferSizeBytes)
        val writeChecksum = CRC32()
        var bytesWritten = 0L
        val writeStart = nanoTimeSource.nanoTime()
        BufferedOutputStream(file.openOutput(), config.bufferSizeBytes).use { output ->
            while (bytesWritten < config.dataSizeBytes) {
                currentCoroutineContext().ensureActive()
                val count = minOf(config.bufferSizeBytes.toLong(), config.dataSizeBytes - bytesWritten).toInt()
                output.write(buffer, 0, count)
                writeChecksum.update(buffer, 0, count)
                bytesWritten += count
                yield()
            }
            output.flush()
        }
        val writeDurationNanos = elapsedSince(writeStart)

        val readChecksum = CRC32()
        var bytesRead = 0L
        val readStart = nanoTimeSource.nanoTime()
        BufferedInputStream(file.openInput(), config.bufferSizeBytes).use { input ->
            while (bytesRead < config.dataSizeBytes) {
                currentCoroutineContext().ensureActive()
                val count =
                    input.read(
                        buffer,
                        0,
                        minOf(config.bufferSizeBytes.toLong(), config.dataSizeBytes - bytesRead).toInt(),
                    )
                if (count < 0) break
                readChecksum.update(buffer, 0, count)
                bytesRead += count
                yield()
            }
        }
        val readDurationNanos = elapsedSince(readStart)
        val dataMatches =
            bytesRead == config.dataSizeBytes.toLong() &&
                readChecksum.value == writeChecksum.value

        return StorageBenchmarkResult(
            writeMebibytesPerSecond = mebibytesPerSecond(bytesWritten, writeDurationNanos),
            readMebibytesPerSecond = mebibytesPerSecond(bytesRead, readDurationNanos),
            bytesWritten = bytesWritten,
            bytesRead = bytesRead,
            checksumCrc32 = readChecksum.value,
            durationMillis = (writeDurationNanos + readDurationNanos) / NANOS_PER_MILLISECOND,
            dataSizeBytes = config.dataSizeBytes.toLong(),
            bufferSizeBytes = config.bufferSizeBytes,
            availableBeforeBytes = availableBytes,
            cleanupSucceeded = false,
            capturedAt = Instant.ofEpochMilli(epochMillisClock.currentTimeMillis()),
            error = StorageBenchmarkErrorCode.DATA_MISMATCH.takeIf { !dataMatches },
        )
    }

    private fun emptyResult(
        availableBytes: Long,
        cleanupSucceeded: Boolean,
        error: StorageBenchmarkErrorCode,
    ) = StorageBenchmarkResult(
        dataSizeBytes = config.dataSizeBytes.toLong(),
        bufferSizeBytes = config.bufferSizeBytes,
        availableBeforeBytes = availableBytes,
        cleanupSucceeded = cleanupSucceeded,
        capturedAt = Instant.ofEpochMilli(epochMillisClock.currentTimeMillis()),
        error = error,
    )

    private fun elapsedSince(startNanos: Long): Long = (nanoTimeSource.nanoTime() - startNanos).coerceAtLeast(1L)

    private fun mebibytesPerSecond(
        bytes: Long,
        durationNanos: Long,
    ): Double = bytes.toDouble() * NANOS_PER_SECOND / durationNanos / MEBIBYTE

    private fun benchmarkBuffer(size: Int): ByteArray =
        ByteArray(size) { index -> ((index * PATTERN_MULTIPLIER + PATTERN_OFFSET) and 0xff).toByte() }

    private companion object {
        const val PATTERN_MULTIPLIER = 31
        const val PATTERN_OFFSET = 17
        const val NANOS_PER_MILLISECOND = 1_000_000L
        const val NANOS_PER_SECOND = 1_000_000_000.0
        const val MEBIBYTE = 1_048_576.0
    }
}
