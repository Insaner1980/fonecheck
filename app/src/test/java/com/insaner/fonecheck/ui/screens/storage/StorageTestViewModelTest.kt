package com.insaner.fonecheck.ui.screens.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class StorageTestViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun storageInformationLoadsOffTheMainExecutionPath() =
        runTest(dispatcher.scheduler) {
            val expected = storageInfo()
            val viewModel = viewModel(infoProvider = StorageInfoProvider { expected })

            advanceUntilIdle()

            assertEquals(expected, viewModel.state.value.info)
            assertEquals(false, viewModel.state.value.isInfoLoading)
            assertNull(viewModel.state.value.infoError)
        }

    @Test
    fun completedBenchmarkPublishesRawResult() =
        runTest(dispatcher.scheduler) {
            val expected = benchmarkResult()
            val viewModel = viewModel(runner = StorageBenchmarkRunner { expected })
            advanceUntilIdle()

            viewModel.startBenchmark()
            advanceUntilIdle()

            assertEquals(StorageBenchmarkPhase.COMPLETED, viewModel.state.value.benchmarkPhase)
            assertEquals(expected, viewModel.state.value.benchmarkResult)
        }

    @Test
    fun insufficientSpaceIsNotReportedAsAStorageFailure() =
        runTest(dispatcher.scheduler) {
            val result = benchmarkResult(error = StorageBenchmarkErrorCode.INSUFFICIENT_SPACE)
            val viewModel = viewModel(runner = StorageBenchmarkRunner { result })
            advanceUntilIdle()

            viewModel.startBenchmark()
            advanceUntilIdle()

            assertEquals(StorageBenchmarkPhase.NOT_RUN, viewModel.state.value.benchmarkPhase)
            assertEquals(StorageBenchmarkErrorCode.INSUFFICIENT_SPACE, viewModel.state.value.benchmarkError)
        }

    @Test
    fun runningBenchmarkCanBeSkippedWithoutCancellationRace() =
        runTest(dispatcher.scheduler) {
            val viewModel = viewModel(runner = StorageBenchmarkRunner { awaitCancellation() })
            advanceUntilIdle()
            viewModel.startBenchmark()
            runCurrent()

            viewModel.skipBenchmark()
            runCurrent()

            assertEquals(StorageBenchmarkPhase.SKIPPED, viewModel.state.value.benchmarkPhase)
            assertNull(viewModel.state.value.benchmarkResult)
        }

    private fun viewModel(
        infoProvider: StorageInfoProvider = StorageInfoProvider { storageInfo() },
        runner: StorageBenchmarkRunner = StorageBenchmarkRunner { benchmarkResult() },
    ) = StorageTestViewModel(
        infoProvider = infoProvider,
        benchmarkRunner = runner,
        ioDispatcher = dispatcher,
    )

    private fun storageInfo() =
        StorageInfo(
            totalBytes = 256L * MEBIBYTE,
            usedBytes = 128L * MEBIBYTE,
            availableBytes = 128L * MEBIBYTE,
            usagePercent = 50.0,
            internalStorageAccessible = true,
            appAccessibleVolumes =
                listOf(
                    AppStorageVolumeInfo(
                        isPrimary = true,
                        isRemovable = false,
                        stateCode = "mounted",
                        isMounted = true,
                        totalBytes = 256L * MEBIBYTE,
                        availableBytes = 128L * MEBIBYTE,
                    ),
                ),
            capturedAt = CAPTURED_AT,
        )

    private fun benchmarkResult(error: StorageBenchmarkErrorCode? = null) =
        StorageBenchmarkResult(
            writeMebibytesPerSecond = 100.0.takeIf { error == null },
            readMebibytesPerSecond = 200.0.takeIf { error == null },
            bytesWritten = if (error == null) 64L * MEBIBYTE else 0L,
            bytesRead = if (error == null) 64L * MEBIBYTE else 0L,
            checksumCrc32 = if (error == null) 42L else 0L,
            durationMillis = 1_000L,
            dataSizeBytes = 64L * MEBIBYTE,
            bufferSizeBytes = 64 * 1_024,
            availableBeforeBytes = 256L * MEBIBYTE,
            cleanupSucceeded = true,
            capturedAt = CAPTURED_AT,
            error = error,
        )

    private companion object {
        const val MEBIBYTE = 1_048_576
        val CAPTURED_AT: Instant = Instant.parse("2026-08-08T00:00:00Z")
    }
}
