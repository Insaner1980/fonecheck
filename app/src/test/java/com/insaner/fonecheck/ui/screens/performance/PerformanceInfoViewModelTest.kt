package com.insaner.fonecheck.ui.screens.performance

import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.PerformanceBenchmarkResult
import com.insaner.fonecheck.domain.model.PerformanceInfo
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class PerformanceInfoViewModelTest {
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
    fun obsoleteInfoFailureCannotFinishReplacementCapture() {
        val ioScheduler = TestCoroutineScheduler()
        val viewModel =
            PerformanceInfoViewModel(
                performanceInfoProvider = PerformanceInfoProvider { error("capture failed") },
                benchmarkRunner = PerformanceBenchmarkRunner { benchmarkResult() },
                ioDispatcher = StandardTestDispatcher(ioScheduler),
            )
        dispatcher.scheduler.runCurrent()
        ioScheduler.runCurrent()

        // Replace the read before its failure is delivered back to Main.
        viewModel.refreshInfo()
        dispatcher.scheduler.runCurrent()

        assertEquals(PerformanceInfoState(isInfoLoading = true), viewModel.state.value)
        viewModel.cancelInfoCapture()
        ioScheduler.runCurrent()
        dispatcher.scheduler.runCurrent()
    }

    @Test
    fun benchmarkTimeoutProducesRecoverableErrorState() =
        runTest(dispatcher.scheduler) {
            val viewModel =
                viewModel(
                    benchmarkRunner =
                        PerformanceBenchmarkRunner {
                            delay(6_000L)
                            benchmarkResult()
                        },
                )
            advanceUntilIdle()

            viewModel.startBenchmark()
            advanceTimeBy(5_001L)
            runCurrent()

            assertEquals(BenchmarkPhase.ERROR, viewModel.state.value.benchmarkPhase)
            assertEquals("benchmark_timeout", viewModel.state.value.benchmarkError)
            assertNull(viewModel.state.value.benchmarkResult)
        }

    @Test
    fun runningBenchmarkCanBeCancelled() =
        runTest(dispatcher.scheduler) {
            val viewModel =
                viewModel(
                    benchmarkRunner = PerformanceBenchmarkRunner { awaitCancellation() },
                )
            advanceUntilIdle()

            viewModel.startBenchmark()
            runCurrent()
            assertEquals(BenchmarkPhase.RUNNING, viewModel.state.value.benchmarkPhase)

            viewModel.cancelBenchmark()
            runCurrent()

            assertEquals(BenchmarkPhase.CANCELLED, viewModel.state.value.benchmarkPhase)
            assertNull(viewModel.state.value.benchmarkResult)
        }

    @Test
    fun cancelledBenchmarkCannotOverwriteItsReplacement() =
        runTest(dispatcher.scheduler) {
            for (failAfterCancellation in listOf(false, true)) {
                val oldCompletion = CompletableDeferred<Unit>()
                val newCompletion = CompletableDeferred<Unit>()
                var calls = 0
                val viewModel =
                    viewModel(
                        PerformanceBenchmarkRunner {
                            if (++calls == 1) {
                                withContext(NonCancellable) { oldCompletion.await() }
                                if (failAfterCancellation) error("Obsolete benchmark failure")
                            } else {
                                newCompletion.await()
                            }
                            benchmarkResult()
                        },
                    )
                advanceUntilIdle()
                viewModel.startBenchmark()
                runCurrent()
                viewModel.cancelBenchmark()
                viewModel.startBenchmark()
                runCurrent()

                oldCompletion.complete(Unit)
                runCurrent()
                assertEquals(2, calls)
                assertEquals(BenchmarkPhase.RUNNING, viewModel.state.value.benchmarkPhase)
                assertNull(viewModel.state.value.benchmarkError)
                assertNull(viewModel.state.value.benchmarkResult)

                newCompletion.complete(Unit)
                runCurrent()
                assertEquals(BenchmarkPhase.COMPLETED, viewModel.state.value.benchmarkPhase)
                assertEquals(benchmarkResult(), viewModel.state.value.benchmarkResult)
                assertNull(viewModel.state.value.benchmarkError)
            }
        }

    @Test
    fun completedBenchmarkIsPublishedAsInformationalRawResult() =
        runTest(dispatcher.scheduler) {
            val expected = benchmarkResult()
            val viewModel = viewModel(PerformanceBenchmarkRunner { expected })
            advanceUntilIdle()

            viewModel.startBenchmark()
            advanceUntilIdle()

            assertEquals(BenchmarkPhase.COMPLETED, viewModel.state.value.benchmarkPhase)
            assertEquals(expected, viewModel.state.value.benchmarkResult)
            assertNull(viewModel.state.value.benchmarkError)
        }

    @Test
    fun queuedInformationCaptureCanBeCancelledBeforeItStarts() =
        runTest(dispatcher.scheduler) {
            var captureCount = 0
            val viewModel =
                PerformanceInfoViewModel(
                    performanceInfoProvider =
                        PerformanceInfoProvider {
                            captureCount += 1
                            performanceInfo()
                        },
                    benchmarkRunner = PerformanceBenchmarkRunner { benchmarkResult() },
                    ioDispatcher = dispatcher,
                )
            viewModel.cancelInfoCapture()
            advanceUntilIdle()

            assertEquals(0, captureCount)
            assertNull(viewModel.state.value.info)
        }

    private fun viewModel(benchmarkRunner: PerformanceBenchmarkRunner) =
        PerformanceInfoViewModel(
            performanceInfoProvider = PerformanceInfoProvider(::performanceInfo),
            benchmarkRunner = benchmarkRunner,
            ioDispatcher = dispatcher,
        )

    private fun performanceInfo() =
        PerformanceInfo(
            cpuModel = "CPU",
            cpuArchitecture = "arm64",
            cpuCores = 8,
            cpuFrequencies = emptyList(),
            cpuConfidence = Confidence.LOW,
            totalRamBytes = 8L * 1_073_741_824,
            availableRamBytes = 4L * 1_073_741_824,
            ramConfidence = Confidence.HIGH,
            glEsVersion = "OpenGL ES 3.2",
            glRenderer = "GPU",
            glVendor = "Vendor",
            vulkanFeatureDeclared = true,
            gpuConfidence = Confidence.HIGH,
            capturedAt = Instant.parse("2026-08-07T12:00:00Z"),
        )

    private fun benchmarkResult() =
        PerformanceBenchmarkResult(
            cpuOperationsPerSecond = 1_000,
            memoryMebibytesPerSecond = 500.0,
            memoryBytesProcessed = 64L * 1_048_576,
            durationMillis = 500,
            thermalBefore = ThermalStatusCode.NONE,
            thermalAfter = ThermalStatusCode.LIGHT,
            capturedAt = Instant.parse("2026-08-07T12:00:01Z"),
        )
}
