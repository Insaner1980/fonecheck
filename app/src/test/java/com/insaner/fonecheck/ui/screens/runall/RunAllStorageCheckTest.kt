package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.data.repository.FakeReportRepository
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import com.insaner.fonecheck.testing.testStorageBenchmarkResult
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkErrorCode
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkPhase
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkRunner
import com.insaner.fonecheck.ui.screens.storage.StorageInfo
import com.insaner.fonecheck.ui.screens.storage.StorageInfoProvider
import com.insaner.fonecheck.ui.screens.storage.StorageTestViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class RunAllStorageCheckTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun fullCheckRestartsTheReadCancelledDuringPreflight() = verifyRestart(retest = false)

    @Test
    fun storageRetestRestartsTheReadCancelledDuringPreflight() = verifyRestart(retest = true)

    @Test
    fun cancelledReadFailureCannotCompleteTheActiveRead() = verifyRestart(retest = false, initialFailure = true)

    @Test
    fun deselectedBenchmarkStillReadsInformationButDoesNotRun() =
        runTest(dispatcher.scheduler) {
            var benchmarks = 0
            val storage =
                storageViewModel(
                    runner = StorageBenchmarkRunner {
                        benchmarks++
                        error("Deselected benchmark must not run")
                    },
                )
            val check = async { runAutomaticStorageCheck(storage, includeBenchmark = false) }
            advanceUntilIdle()
            assertNull(check.await())
            assertEquals(storageInfo(), storage.state.value.info)
            assertEquals(StorageBenchmarkPhase.SKIPPED, storage.state.value.benchmarkPhase)
            assertEquals(0, benchmarks)
        }

    @Test
    fun informationFailureRemainsAnError() =
        runTest(dispatcher.scheduler) {
            val storage = storageViewModel(provider = StorageInfoProvider { error("Capture failed") })
            val check = async { runAutomaticStorageCheck(storage, includeBenchmark = false) }
            advanceUntilIdle()
            assertEquals(RunAllStageOutcome.ERROR, check.await())
            assertEquals("storage_info_failed", storage.state.value.infoError)
            assertNull(storage.state.value.info)
        }

    @Test
    fun insufficientSpaceRemainsNotRun() =
        runTest(dispatcher.scheduler) {
            val storage =
                storageViewModel(
                    runner = StorageBenchmarkRunner {
                        testStorageBenchmarkResult(
                            0.0, 0.0, 0L, storageInfo().capturedAt, StorageBenchmarkErrorCode.INSUFFICIENT_SPACE,
                        )
                    },
                )
            val check = async { runAutomaticStorageCheck(storage, includeBenchmark = true) }
            advanceUntilIdle()
            assertNull(check.await())
            assertEquals(StorageBenchmarkPhase.NOT_RUN, storage.state.value.benchmarkPhase)
            assertEquals(StorageBenchmarkErrorCode.INSUFFICIENT_SPACE, storage.state.value.benchmarkError)
        }

    @Test
    fun stageDisposalCancelsTheOwnedBenchmarkWithoutPublishingSuccess() =
        runTest(dispatcher.scheduler) {
            var cancelled = false
            val storage =
                storageViewModel(
                    runner = StorageBenchmarkRunner {
                        try {
                            awaitCancellation()
                        } finally {
                            cancelled = true
                        }
                    },
                )
            val check = async { runAutomaticStorageCheck(storage, includeBenchmark = true) }
            runCurrent()
            assertEquals(StorageBenchmarkPhase.RUNNING, storage.state.value.benchmarkPhase)
            storageOwner(storage).stopStage(RunAllStage.AUTOMATIC)
            check.cancel()
            runCurrent()
            assertTrue(cancelled)
            assertTrue(check.isCancelled)
            assertEquals(StorageBenchmarkPhase.CANCELLED, storage.state.value.benchmarkPhase)
            assertNull(storage.state.value.benchmarkResult)
        }

    @Test
    fun unfinishedInformationTimesOutWithoutStartingTheBenchmark() =
        runTest(dispatcher.scheduler) {
            val storage = storageViewModel(provider = StorageInfoProvider { throw CancellationException() })
            val check = async { runAutomaticStorageCheck(storage, includeBenchmark = true) }
            runCurrent()
            advanceTimeBy(45_001L)
            runCurrent()
            assertEquals(RunAllStageOutcome.TIMED_OUT, check.await())
            assertEquals(StorageBenchmarkPhase.IDLE, storage.state.value.benchmarkPhase)
            assertNull(storage.state.value.info)
            assertNull(storage.state.value.benchmarkResult)
        }

    private fun verifyRestart(
        retest: Boolean,
        initialFailure: Boolean = false,
    ) =
        runTest(dispatcher.scheduler) {
            val executor = Executors.newSingleThreadExecutor()
            val ioDispatcher = executor.asCoroutineDispatcher()
            val initialStarted = CountDownLatch(1)
            val releaseInitial = CountDownLatch(1)
            val activeStarted = CountDownLatch(1)
            val releaseActive = CountDownLatch(1)
            val captures = AtomicInteger()
            val benchmarks = AtomicInteger()
            val expected = storageInfo()
            val storage =
                StorageTestViewModel(
                    StorageInfoProvider {
                        when (captures.incrementAndGet()) {
                            1 -> {
                                initialStarted.countDown()
                                check(releaseInitial.await(5, TimeUnit.SECONDS))
                                if (initialFailure) error("Obsolete capture failed")
                                expected.copy(availableBytes = 0)
                            }
                            2 -> {
                                activeStarted.countDown()
                                check(releaseActive.await(5, TimeUnit.SECONDS))
                                expected
                            }
                            else -> expected
                        }
                    },
                    StorageBenchmarkRunner {
                        benchmarks.incrementAndGet()
                        testStorageBenchmarkResult(100.0, 200.0, expected.availableBytes, expected.capturedAt)
                    },
                    ioDispatcher,
                )
            val owner = storageOwner(storage)
            val session = RunAllTestsViewModel(EpochMillisClock { 0L }, IdProvider { "test" }, FakeReportRepository())
            try {
                runCurrent()
                assertTrue(initialStarted.await(5, TimeUnit.SECONDS))
                assertEquals(RunAllRunStatus.NOT_STARTED, session.state.value.runStatus)
                owner.stopAll()
                assertNull(storage.state.value.info)
                assertTrue(storage.state.value.isInfoLoading)

                if (retest) {
                    session.onCategoryRetestRequested(DiagnosticCategoryId.STORAGE, RunAllHardwareProfile())
                } else {
                    session.onPreflightAccepted(RunAllSelections(includeStorageBenchmark = true), RunAllHardwareProfile())
                }
                owner.markRunStarted()
                session.onPermissionsResolved(RunAllPermissions())
                val token = session.state.value.stageToken
                assertEquals(RunAllStage.AUTOMATIC, session.state.value.stage)
                assertTrue(session.claimStage(token))
                val check = async { runAutomaticStorageCheck(storage, session.state.value.selections.includeStorageBenchmark) }
                runCurrent()
                releaseInitial.countDown()
                assertTrue(
                    "The active run must start a fresh storage-information read",
                    activeStarted.await(5, TimeUnit.SECONDS),
                )
                runCurrent()
                assertFalse(session.claimStage(token))
                assertEquals(0, benchmarks.get())
                assertTrue("An obsolete capture must not complete the active read", storage.state.value.isInfoLoading)
                assertNull(storage.state.value.infoError)
                releaseActive.countDown()
                executor.submit {}.get(5, TimeUnit.SECONDS)
                runCurrent()
                executor.submit {}.get(5, TimeUnit.SECONDS)
                runCurrent()
                assertEquals(expected, storage.state.value.info)
                assertEquals(1, benchmarks.get())
                assertEquals(StorageBenchmarkPhase.COMPLETED, storage.state.value.benchmarkPhase)
                assertTrue(check.isCompleted)
                assertNull(check.await())
                session.onAutomaticChecksComplete(token)
                assertFalse(session.claimStage(token))
            } finally {
                releaseInitial.countDown()
                releaseActive.countDown()
                owner.stopAll()
                session.interruptRun(RunAllInterruptionReason.USER_CANCEL)
                executor.submit {}.get(5, TimeUnit.SECONDS)
                runCurrent()
                ioDispatcher.close()
                assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
            }
        }

    private fun storageOwner(storage: StorageTestViewModel) =
        RunAllResourceOwner(
            stopDeviceInfo = {},
            stopPerformance = {},
            stopSimInfo = {},
            stopMicrophone = {},
            stopGps = {},
            stopStorage = {
                storage.cancelInfoCapture()
                storage.cancelBenchmark()
            },
            stopDisplay = {},
            stopAudio = {},
            stopCamera = {},
            stopSensors = {},
            stopVibration = {},
            stopButtons = {},
            stopBiometrics = {},
            stopThermal = {},
        )

    private fun storageInfo() =
        StorageInfo(
            totalBytes = 512L * 1_048_576,
            usedBytes = 256L * 1_048_576,
            availableBytes = 256L * 1_048_576,
            usagePercent = 50.0,
            internalStorageAccessible = true,
            appAccessibleVolumes = emptyList(),
            capturedAt = Instant.parse("2026-09-08T00:00:00Z"),
        )

    private fun storageViewModel(
        provider: StorageInfoProvider = StorageInfoProvider { storageInfo() },
        runner: StorageBenchmarkRunner = StorageBenchmarkRunner {
            testStorageBenchmarkResult(100.0, 200.0, storageInfo().availableBytes, storageInfo().capturedAt)
        },
    ) = StorageTestViewModel(provider, runner, dispatcher)
}
