package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.data.repository.FakeReportRepository
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategorySnapshot
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.PerformanceBenchmarkResult
import com.insaner.fonecheck.domain.model.PerformanceInfo
import com.insaner.fonecheck.domain.model.PhoneTypeCode
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
import com.insaner.fonecheck.domain.model.TelephonyHardwareCode
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import com.insaner.fonecheck.testing.testDeviceInfo
import com.insaner.fonecheck.testing.testReport
import com.insaner.fonecheck.testing.testStorageBenchmarkResult
import com.insaner.fonecheck.ui.screens.audio.AudioTestState
import com.insaner.fonecheck.ui.screens.battery.BatteryTestState
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestState
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestState
import com.insaner.fonecheck.ui.screens.camera.CameraTestState
import com.insaner.fonecheck.ui.screens.connectivity.ConnectivityTestState
import com.insaner.fonecheck.ui.screens.deviceinfo.DeviceInfoProvider
import com.insaner.fonecheck.ui.screens.deviceinfo.DeviceInfoViewModel
import com.insaner.fonecheck.ui.screens.display.DisplayTestState
import com.insaner.fonecheck.ui.screens.performance.PerformanceBenchmarkRunner
import com.insaner.fonecheck.ui.screens.performance.PerformanceInfoProvider
import com.insaner.fonecheck.ui.screens.performance.PerformanceInfoViewModel
import com.insaner.fonecheck.ui.screens.sensor.SensorTestState
import com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyProvider
import com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyViewModel
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkErrorCode
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkPhase
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkRunner
import com.insaner.fonecheck.ui.screens.storage.StorageInfo
import com.insaner.fonecheck.ui.screens.storage.StorageInfoProvider
import com.insaner.fonecheck.ui.screens.storage.StorageTestViewModel
import com.insaner.fonecheck.ui.screens.thermal.ThermalTestState
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class RunAllAutomaticChecksTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun claimedEntryStartsOnceAndAcceptsStorageThroughTheRealMapperAndFrozenReport() =
        runTest(dispatcher.scheduler) {
            val gate = CompletableDeferred<Unit>()
            val f =
                Fixture(
                    StorageBenchmarkRunner {
                        gate.await()
                        benchmark()
                    },
                )
            f.enter(DiagnosticCategoryId.STORAGE)
            val token = f.run.state.value.stageToken
            val execution = async { f.execute(token) }
            runCurrent()
            assertEquals(listOf("storage-info", "storage-benchmark"), f.starts)
            f.execute(token)
            f.execute(token - 1)
            assertEquals(1, f.starts.count { it == "storage-benchmark" })
            gate.complete(Unit)
            runCurrent()
            execution.await()
            assertEquals(RunAllStage.RESULTS, f.run.state.value.stage)
            assertEquals(RunAllStageOutcome.COMPLETED, f.run.state.value.stageOutcomes[RunAllStage.AUTOMATIC])
            assertEquals(benchmark(), f.storage.state.value.benchmarkResult)
            val snapshots = f.snapshots()
            val source = testReport()
            val resultToken = f.run.state.value.stageToken
            f.run.completeReport(resultToken, source.device, source.app, snapshots)
            runCurrent()
            val report = requireNotNull(f.run.state.value.report)
            assertEquals(listOf(DiagnosticCategoryId.STORAGE), report.categories.map { it.categoryId })
            assertEquals(
                snapshots
                    .single {
                        it.categoryId == DiagnosticCategoryId.STORAGE
                    }.evidence,
                report.categories.single().evidence,
            )
            f.execute(token)
            f.run.completeReport(resultToken, source.device, source.app, snapshots)
            assertSame(report, f.run.state.value.report)
            assertEquals(ReportSaveStatus.SAVED, f.run.state.value.saveStatus)
        }

    @Test
    fun fullRunRestartsPreflightReadsKeepsOrderAndHonorsDeselection() =
        runTest(dispatcher.scheduler) {
            val f = Fixture()
            // Constructor reads are pending when the production preflight cleanup cancels them.
            f.enter(selections = RunAllSelections(includeMicrophone = false, includeStorageBenchmark = false))
            val execution = async { f.execute() }
            runCurrent()
            execution.await()
            assertEquals(
                listOf(
                    "device",
                    "performance-info",
                    "sim",
                    "performance-benchmark",
                    "storage-info",
                    "headphones",
                    "connectivity",
                ),
                f.starts,
            )
            assertEquals(StorageBenchmarkPhase.SKIPPED, f.storage.state.value.benchmarkPhase)
            assertEquals(RunAllStage.DISPLAY, f.run.state.value.stage)
            assertTrue(
                f.run.state.value.automaticIssues
                    .isEmpty(),
            )
            f.run.interruptRun(RunAllInterruptionReason.USER_CANCEL)
        }

    @Test
    fun interruptionCancelsOwnedWorkAndLateSuccessCannotResumeOrOverwriteNewRun() =
        runTest(dispatcher.scheduler) {
            val late = CompletableDeferred<Unit>()
            val fresh = CompletableDeferred<Unit>()
            var calls = 0
            var cancelled = 0
            val f =
                Fixture(
                    StorageBenchmarkRunner {
                        if (++calls == 1) {
                            try {
                                awaitCancellation()
                            } finally {
                                cancelled++
                                withContext(NonCancellable) { late.await() }
                            }
                        } else {
                            fresh.await()
                        }
                        benchmark()
                    },
                )
            f.enter(DiagnosticCategoryId.STORAGE)
            val oldToken = f.run.state.value.stageToken
            val old = async { f.execute() }
            runCurrent()
            f.owner.stopAll()
            f.run.interruptRun(RunAllInterruptionReason.BACKGROUND)
            runCurrent()
            assertTrue(old.isCancelled)
            assertEquals(1, cancelled)
            assertNull(f.run.state.value.report)
            assertEquals(RunAllStage.PREFLIGHT, f.run.state.value.stage)
            late.complete(Unit)
            runCurrent()
            assertNull(f.storage.state.value.benchmarkResult)
            f.enter(DiagnosticCategoryId.STORAGE)
            val current = async { f.execute() }
            runCurrent()
            f.run.onAutomaticChecksComplete(oldToken)
            f.run.reportAutomaticIssue(oldToken, DiagnosticCategoryId.STORAGE, RunAllStageOutcome.ERROR)
            assertEquals(RunAllStage.AUTOMATIC, f.run.state.value.stage)
            assertTrue(
                f.run.state.value.automaticIssues
                    .isEmpty(),
            )
            fresh.complete(Unit)
            runCurrent()
            current.await()
            assertEquals(benchmark(), f.storage.state.value.benchmarkResult)
        }

    @Test
    fun cancelledOldBenchmarkFailureCannotPoisonNewAutomaticStage() =
        runTest(dispatcher.scheduler) {
            val late = CompletableDeferred<Unit>()
            val fresh = CompletableDeferred<Unit>()
            var calls = 0
            val f =
                Fixture(
                    StorageBenchmarkRunner {
                        if (++calls == 1) {
                            withContext(NonCancellable) { late.await() }
                            error("synthetic obsolete I/O failure")
                        }
                        fresh.await()
                        benchmark()
                    },
                )
            try {
                f.enter(DiagnosticCategoryId.STORAGE)
                val old = async { f.execute() }
                runCurrent()
                f.owner.stopAll()
                f.run.interruptRun(RunAllInterruptionReason.BACKGROUND)
                runCurrent()
                assertTrue(old.isCancelled)
                f.enter(DiagnosticCategoryId.STORAGE)
                val current = async { f.execute() }
                runCurrent()
                assertEquals("A new automatic stage must start its own benchmark", 2, calls)
                late.complete(Unit)
                runCurrent()
                assertEquals(StorageBenchmarkPhase.RUNNING, f.storage.state.value.benchmarkPhase)
                assertNull(f.storage.state.value.benchmarkError)
                assertEquals(RunAllStage.AUTOMATIC, f.run.state.value.stage)
                fresh.complete(Unit)
                runCurrent()
                current.await()
                assertTrue(
                    f.run.state.value.automaticIssues
                        .isEmpty(),
                )
                assertEquals(benchmark(), f.storage.state.value.benchmarkResult)
            } finally {
                late.complete(Unit)
                fresh.complete(Unit)
                f.owner.stopAll()
                f.run.interruptRun(RunAllInterruptionReason.USER_CANCEL)
                runCurrent()
            }
        }

    @Test
    fun cancellingCallerAloneStopsIndependentCategoryWork() =
        runTest(dispatcher.scheduler) {
            var stopped = false
            val f =
                Fixture(
                    StorageBenchmarkRunner {
                        try {
                            awaitCancellation()
                        } finally {
                            stopped = true
                        }
                    },
                )
            f.enter(DiagnosticCategoryId.STORAGE)
            val execution = async { f.execute() }
            runCurrent()
            execution.cancel()
            runCurrent()
            assertTrue(stopped)
            assertTrue(execution.isCancelled)
            assertFalse(
                f.run.state.value.stageOutcomes
                    .containsKey(RunAllStage.AUTOMATIC),
            )
            f.run.interruptRun(RunAllInterruptionReason.SCREEN_DISPOSED)
        }

    @Test
    fun storageOperationTimeoutRemainsAnIssueAndReleasesWork() =
        runTest(dispatcher.scheduler) {
            var stopped = false
            val f =
                Fixture(
                    StorageBenchmarkRunner {
                        try {
                            awaitCancellation()
                        } finally {
                            stopped = true
                        }
                    },
                )
            f.enter(DiagnosticCategoryId.STORAGE)
            val execution = async { f.execute() }
            runCurrent()
            advanceTimeBy(45_001L)
            runCurrent()
            execution.await()
            assertTrue(stopped)
            assertEquals(RunAllStageOutcome.TIMED_OUT, f.run.state.value.automaticIssues[DiagnosticCategoryId.STORAGE])
            assertEquals(RunAllStage.RESULTS, f.run.state.value.stage)
            assertNull(f.storage.state.value.benchmarkResult)
            val reading =
                f.snapshots().single { it.categoryId == DiagnosticCategoryId.STORAGE }.evidence.single {
                    it.checkId.value ==
                        "storage.sequential_write"
                }
            assertEquals(DiagnosticStatus.NOT_TESTED, reading.status)
            assertEquals(EvidenceReasonCode("measurement_timeout"), reading.reason)
        }

    @Test
    fun enclosingStateMachineDeadlineCancelsPendingExecutionWithoutNormalCompletion() =
        runTest(dispatcher.scheduler) {
            // Advance the state-machine clock independently to model a delayed execution dispatcher.
            val deadlineDispatcher = StandardTestDispatcher(TestCoroutineScheduler())
            Dispatchers.setMain(deadlineDispatcher)
            val f = Fixture(StorageBenchmarkRunner { awaitCancellation() })
            f.enter(DiagnosticCategoryId.STORAGE)
            val execution = async { f.execute() }
            runCurrent()
            deadlineDispatcher.scheduler.runCurrent()
            deadlineDispatcher.scheduler.advanceTimeBy(RunAllTestsViewModel.AUTOMATIC_TIMEOUT_MS)
            deadlineDispatcher.scheduler.runCurrent()
            runCurrent()
            deadlineDispatcher.scheduler.runCurrent()
            assertTrue(execution.isCancelled)
            assertEquals(RunAllStageOutcome.TIMED_OUT, f.run.state.value.stageOutcomes[RunAllStage.AUTOMATIC])
            assertEquals(RunAllStage.RESULTS, f.run.state.value.stage)
            assertNull(f.storage.state.value.benchmarkResult)
            val reading =
                f.snapshots().single { it.categoryId == DiagnosticCategoryId.STORAGE }.evidence.single {
                    it.checkId.value ==
                        "storage.sequential_write"
                }
            assertEquals(DiagnosticStatus.NOT_TESTED, reading.status)
            assertEquals(EvidenceReasonCode("measurement_timeout"), reading.reason)
        }

    @Test
    fun expectedStorageErrorAndUnavailableBenchmarkRemainDistinct() =
        runTest(dispatcher.scheduler) {
            for (error in listOf(StorageBenchmarkErrorCode.IO_ERROR, StorageBenchmarkErrorCode.INSUFFICIENT_SPACE)) {
                val f = Fixture(StorageBenchmarkRunner { benchmark().copy(error = error) })
                f.enter(DiagnosticCategoryId.STORAGE)
                val execution = async { f.execute() }
                runCurrent()
                execution.await()
                assertEquals(RunAllStage.RESULTS, f.run.state.value.stage)
                assertEquals(
                    if (error == StorageBenchmarkErrorCode.IO_ERROR) RunAllStageOutcome.ERROR else null,
                    f.run.state.value.automaticIssues[DiagnosticCategoryId.STORAGE],
                )
                assertEquals(
                    if (error ==
                        StorageBenchmarkErrorCode.IO_ERROR
                    ) {
                        StorageBenchmarkPhase.ERROR
                    } else {
                        StorageBenchmarkPhase.NOT_RUN
                    },
                    f.storage.state.value.benchmarkPhase,
                )
            }
        }

    @Test
    fun microphonePermissionAndOperationErrorsRetainTheirExistingMeaning() =
        runTest(dispatcher.scheduler) {
            val f = Fixture()
            f.enter(DiagnosticCategoryId.AUDIO)
            f.execute()
            assertFalse(f.starts.contains("microphone"))
            f.run.interruptRun(RunAllInterruptionReason.USER_CANCEL)
            f.enter(DiagnosticCategoryId.AUDIO, permissions = RunAllPermissions(microphone = true))
            f.recordingError = true
            f.execute()
            assertEquals(RunAllStageOutcome.ERROR, f.run.state.value.automaticIssues[DiagnosticCategoryId.AUDIO])
            assertEquals(RunAllStage.AUDIO, f.run.state.value.stage)
            f.run.interruptRun(RunAllInterruptionReason.USER_CANCEL)
        }

    @Test
    fun manualOnlyRetestDoesNotClaimOrStartAutomaticWork() =
        runTest(dispatcher.scheduler) {
            val f = Fixture()
            f.enter(DiagnosticCategoryId.DISPLAY)
            val token = f.run.state.value.stageToken
            f.execute(token)
            assertEquals(RunAllStage.DISPLAY, f.run.state.value.stage)
            assertTrue(f.starts.isEmpty())
            assertTrue(f.run.claimStage(token))
            f.run.interruptRun(RunAllInterruptionReason.USER_CANCEL)
        }

    @Test
    fun obsoleteCleanupCannotStopNewExecution() =
        runTest(dispatcher.scheduler) {
            val f = Fixture()
            val old = Job()
            val fresh = Job()
            f.owner.beginAutomatic(old)
            f.owner.beginAutomatic(fresh)
            f.owner.endAutomatic(old)
            assertTrue(old.isCancelled)
            assertTrue(fresh.isActive)
            f.owner.stopAll()
            assertTrue(fresh.isCancelled)
            f.owner.stopAll()
        }

    private inner class Fixture(
        runner: StorageBenchmarkRunner = StorageBenchmarkRunner { benchmark() },
    ) {
        val starts = mutableListOf<String>()
        var recordingError = false
        val run =
            RunAllTestsViewModel(
                EpochMillisClock { 1000L },
                IdProvider { "synthetic-automatic" },
                FakeReportRepository(),
            )
        val device =
            DeviceInfoViewModel(
                DeviceInfoProvider {
                    starts += "device"
                    testDeviceInfo()
                },
                dispatcher,
            )
        val performance =
            PerformanceInfoViewModel(
                PerformanceInfoProvider {
                    starts += "performance-info"
                    PerformanceInfo(
                        "synthetic",
                        "synthetic",
                        2,
                        emptyList(),
                        Confidence.HIGH,
                        100L,
                        50L,
                        Confidence.HIGH,
                        "synthetic",
                        "synthetic",
                        "synthetic",
                        false,
                        Confidence.HIGH,
                        Instant.EPOCH,
                    )
                },
                PerformanceBenchmarkRunner {
                    starts += "performance-benchmark"
                    PerformanceBenchmarkResult(
                        100,
                        100.0,
                        100,
                        1,
                        ThermalStatusCode.NONE,
                        ThermalStatusCode.NONE,
                        Instant.EPOCH,
                    )
                },
                dispatcher,
            )
        val sim =
            SimTelephonyViewModel(
                SimTelephonyProvider {
                    starts += "sim"
                    SimTelephonyInfo(
                        TelephonyHardwareCode.NO_HARDWARE,
                        SimInventoryCode.NO_TELEPHONY,
                        emptyList(),
                        PhoneTypeCode.NONE,
                        0,
                        NetworkGenerationCode.UNKNOWN,
                        false,
                    )
                },
                dispatcher,
            )
        val storage =
            StorageTestViewModel(
                StorageInfoProvider {
                    starts += "storage-info"
                    info()
                },
                StorageBenchmarkRunner {
                    starts += "storage-benchmark"
                    runner.run()
                },
                dispatcher,
            )
        val owner =
            RunAllResourceOwner(
                stopDeviceInfo = device::cancelCapture,
                stopPerformance = {
                    performance.cancelInfoCapture()
                    performance.cancelBenchmark()
                },
                stopSimInfo = sim::cancelCapture,
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

        fun enter(
            category: DiagnosticCategoryId? = null,
            selections: RunAllSelections = RunAllSelections(),
            permissions: RunAllPermissions = RunAllPermissions(),
        ) {
            owner.stopAll()
            if (category == null) {
                run.onPreflightAccepted(selections, RunAllHardwareProfile.ALL_AVAILABLE)
            } else {
                run.onCategoryRetestRequested(category, RunAllHardwareProfile.ALL_AVAILABLE)
            }
            owner.markRunStarted()
            run.onPermissionsResolved(permissions)
        }

        suspend fun execute(token: Long = run.state.value.stageToken) =
            runAutomaticChecks(
                token,
                run,
                owner,
                device,
                performance,
                sim,
                storage,
                audioState = { AudioTestState() },
                updateHeadphones = { starts += "headphones" },
                startRecording = {
                    starts += "microphone"
                    if (recordingError) error("synthetic unavailable microphone")
                },
                cancelRecording = {},
                refreshConnectivity = { starts += "connectivity" },
            )

        fun snapshots(): List<DiagnosticCategorySnapshot> {
            val state = run.state.value
            return RunAllSnapshotMapper.map(
                DiagnosticSnapshots(
                    device.state.value.info,
                    performance.state.value.info,
                    sim = sim.state.value.info,
                    automaticIssues = state.automaticIssues,
                    display = DisplayTestState(),
                    audio = AudioTestState(),
                    camera = CameraTestState(),
                    sensors = SensorTestState(),
                    connectivity = ConnectivityTestState(),
                    battery = BatteryTestState(),
                    thermal = ThermalTestState(),
                    storage = storage.state.value,
                    vibration = VibrationTestState(),
                    buttons = ButtonTestState(),
                    biometrics = BiometricTestState(),
                ),
                state.manualChecks,
                state.permissions,
                state.selections,
                state.hardware,
                Instant.ofEpochMilli(1000L),
            )
        }
    }

    private fun info() =
        StorageInfo(512L * 1_048_576, 256L * 1_048_576, 256L * 1_048_576, 50.0, true, emptyList(), Instant.EPOCH)

    private fun benchmark() = testStorageBenchmarkResult(100.0, 200.0, info().availableBytes, Instant.EPOCH)
}
