package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.data.repository.FakeReportRepository
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategorySnapshot
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticSnapshotVersion
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportAssembler
import com.insaner.fonecheck.domain.model.ReportAssemblyRequest
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import com.insaner.fonecheck.ui.screens.camera.CameraCaptureAttempt
import com.insaner.fonecheck.ui.screens.camera.CameraCaptureSession
import com.insaner.fonecheck.ui.screens.camera.CameraTestState
import com.insaner.fonecheck.ui.screens.camera.CaptureResult
import com.insaner.fonecheck.ui.screens.display.DisplayPattern
import com.insaner.fonecheck.ui.screens.home.HomeViewModel
import com.insaner.fonecheck.ui.screens.home.LatestFullCheckState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class RunAllTestsViewModelTest {
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
    fun completedStageWaitsWithoutRestartingOrAcceptingLateEvents() {
        val viewModel = runAllViewModel()
        enterFirstInteractiveStage(viewModel)
        val token = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(token))
        viewModel.continueAfterStage(token)
        assertEquals(RunAllStage.DISPLAY, viewModel.state.value.stage)
        viewModel.recordDisplay(token, true)
        val reviewToken = viewModel.state.value.stageToken

        assertTrue(viewModel.state.value.awaitingContinue)
        assertFalse(viewModel.claimStage(reviewToken))
        assertFalse(viewModel.retryStage(reviewToken))
        viewModel.recordDisplay(token, false)
        dispatcher.scheduler.advanceTimeBy(60_000L)
        assertEquals(RunAllStage.DISPLAY, viewModel.state.value.stage)
        assertEquals(true, viewModel.state.value.manualChecks.display)
        viewModel.continueAfterStage(token)
        assertTrue(viewModel.state.value.awaitingContinue)

        viewModel.continueAfterStage(reviewToken)
        viewModel.continueAfterStage(reviewToken)
        assertEquals(RunAllStage.BUTTONS, viewModel.state.value.stage)
        assertFalse(viewModel.state.value.awaitingContinue)
        viewModel.interruptRun(RunAllInterruptionReason.USER_CANCEL)
    }

    @Test
    fun everyAutomaticCategoryRequiresItsOwnContinue() {
        val viewModel = runAllViewModel()
        viewModel.onPreflightAccepted(RunAllSelections(), RunAllHardwareProfile.ALL_AVAILABLE)
        viewModel.onPermissionsResolved(RunAllPermissions())
        val token = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(token))
        viewModel.reportAutomaticIssue(token, DiagnosticCategoryId.STORAGE, RunAllStageOutcome.ERROR)
        viewModel.onAutomaticChecksComplete(token)
        val categories =
            viewModel.state.value.plan.categories
                .filter { it.stage == RunAllStage.AUTOMATIC }

        categories.forEach { category ->
            assertTrue(viewModel.state.value.awaitingContinue)
            assertEquals(category, viewModel.state.value.reviewCategory)
            val reviewToken = viewModel.state.value.stageToken
            dispatcher.scheduler.advanceTimeBy(RunAllTestsViewModel.AUTOMATIC_TIMEOUT_MS * 2)
            assertEquals(category, viewModel.state.value.reviewCategory)
            viewModel.continueAfterStage(reviewToken)
            val next = viewModel.state.value
            viewModel.continueAfterStage(reviewToken)
            assertEquals(next, viewModel.state.value)
        }
        assertEquals(RunAllStage.DISPLAY, viewModel.state.value.stage)
        assertEquals(RunAllStageOutcome.ERROR, viewModel.state.value.automaticIssues[DiagnosticCategoryId.STORAGE])
        viewModel.interruptRun(RunAllInterruptionReason.USER_CANCEL)
    }

    @Test
    fun finalStageRequiresContinueBeforeResultsAndInterruptionRejectsIt() {
        val viewModel = runAllViewModel()
        viewModel.onCategoryRetestRequested(DiagnosticCategoryId.BIOMETRICS, RunAllHardwareProfile.ALL_AVAILABLE)
        viewModel.onPermissionsResolved(RunAllPermissions())
        val token = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(token))
        viewModel.recordBiometricOutcome(token, RunAllStageOutcome.PASSED)
        val reviewToken = viewModel.state.value.stageToken
        assertEquals(RunAllStage.BIOMETRICS, viewModel.state.value.stage)
        assertTrue(viewModel.state.value.awaitingContinue)
        assertEquals(null, viewModel.state.value.report)
        viewModel.interruptRun(RunAllInterruptionReason.BACKGROUND)
        viewModel.continueAfterStage(reviewToken)
        assertEquals(RunAllStage.PREFLIGHT, viewModel.state.value.stage)
    }

    @Test
    fun userResponseIsTimestampedWhenAcceptedAndClearedOnInterruption() {
        var now = 100L
        val viewModel = RunAllTestsViewModel(EpochMillisClock { now }, IdProvider { "test" }, FakeReportRepository())
        enterFirstInteractiveStage(viewModel)
        val token = viewModel.state.value.stageToken
        viewModel.claimStage(token)
        now = 200L
        viewModel.recordDisplay(token, true)
        acknowledgeCompletedStages(viewModel)
        now = 9000L
        viewModel.recordDisplay(token, false)
        acknowledgeCompletedStages(viewModel)
        assertEquals(Instant.ofEpochMilli(200L), viewModel.state.value.manualChecks.completedAt[RunAllStage.DISPLAY])
        viewModel.interruptRun(RunAllInterruptionReason.BACKGROUND)
        assertTrue(
            viewModel.state.value.manualChecks.completedAt
                .isEmpty(),
        )
        assertEquals(RunAllInterruptionReason.BACKGROUND, viewModel.state.value.lastInterruption)
        viewModel.onPreflightAccepted(RunAllSelections(), RunAllHardwareProfile())
        assertEquals(null, viewModel.state.value.lastInterruption)
        viewModel.interruptRun(RunAllInterruptionReason.USER_CANCEL)
    }

    @Test
    fun preflightChoicesAndResolvedPermissionsBuildTheActivePlan() {
        val viewModel = runAllViewModel()
        val selections = RunAllSelections(includeCamera = true, includeStorageBenchmark = false)
        val hardware =
            RunAllHardwareProfile(
                microphoneAvailable = true,
                cameraAvailable = true,
                motionSensorAvailable = false,
                vibratorAvailable = false,
                biometricsAvailable = true,
            )

        assertEquals(RunAllStage.PREFLIGHT, viewModel.state.value.stage)
        viewModel.onPreflightAccepted(selections, hardware)
        assertEquals(RunAllStage.PERMISSIONS, viewModel.state.value.stage)

        viewModel.onPermissionsResolved(
            RunAllPermissions(
                microphone = true,
                camera = false,
                location = false,
                phone = false,
                bluetooth = false,
            ),
        )

        val state = viewModel.state.value
        assertEquals(RunAllStage.AUTOMATIC, state.stage)
        assertEquals(selections, state.selections)
        assertEquals(DiagnosticCatalog.categories, state.plan.categories.map { it.categoryId })
        assertFalse(RunAllStage.CAMERA in state.plan.stages)
        assertFalse(RunAllStage.SENSORS in state.plan.stages)
        assertFalse(RunAllStage.VIBRATION in state.plan.stages)
        assertTrue(RunAllStage.RESULTS in state.plan.stages)
    }

    @Test
    fun completionAdvancesThroughOnlyThePlannedInteractiveStages() {
        val viewModel = runAllViewModel()
        enterFirstInteractiveStage(viewModel)
        assertEquals(RunAllProgress(position = 1, total = 2), viewModel.state.value.progress)

        val displayToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(displayToken))
        viewModel.recordDisplay(displayToken, true)
        acknowledgeCompletedStages(viewModel)
        assertEquals(RunAllStage.BUTTONS, viewModel.state.value.stage)
        assertEquals(RunAllProgress(position = 2, total = 2), viewModel.state.value.progress)

        val buttonsToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(buttonsToken))
        viewModel.skipStage(buttonsToken)
        acknowledgeCompletedStages(viewModel)
        assertEquals(RunAllStage.RESULTS, viewModel.state.value.stage)
    }

    @Test
    fun duplicateAndLateCallbacksCannotAdvanceAnotherStage() {
        val viewModel = runAllViewModel()
        enterFirstInteractiveStage(viewModel)
        val displayToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(displayToken))

        viewModel.recordDisplay(displayToken, true)
        acknowledgeCompletedStages(viewModel)
        val nextStage = viewModel.state.value.stage
        viewModel.recordDisplay(displayToken, false)
        acknowledgeCompletedStages(viewModel)

        assertEquals(RunAllStage.BUTTONS, nextStage)
        assertEquals(nextStage, viewModel.state.value.stage)
        assertEquals(true, viewModel.state.value.manualChecks.display)
    }

    @Test
    fun stageCanBeClaimedOnlyOnceAcrossRecomposition() {
        val viewModel = runAllViewModel()
        enterFirstInteractiveStage(viewModel)
        val token = viewModel.state.value.stageToken

        assertTrue(viewModel.claimStage(token))
        assertFalse(viewModel.claimStage(token))
        assertEquals(RunAllStage.DISPLAY, viewModel.state.value.stage)
    }

    @Test
    fun automaticIssuesAreRecordedOnlyForTheCurrentClaimedStage() {
        val viewModel = runAllViewModel()
        viewModel.onPreflightAccepted(RunAllSelections(), RunAllHardwareProfile.ALL_AVAILABLE)
        viewModel.onPermissionsResolved(RunAllPermissions())
        val automaticToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(automaticToken))

        viewModel.reportAutomaticIssue(
            automaticToken,
            DiagnosticCategoryId.AUDIO,
            RunAllStageOutcome.TIMED_OUT,
        )
        viewModel.onAutomaticChecksComplete(automaticToken)
        acknowledgeCompletedStages(viewModel)
        viewModel.reportAutomaticIssue(
            automaticToken,
            DiagnosticCategoryId.STORAGE,
            RunAllStageOutcome.ERROR,
        )

        assertEquals(
            mapOf(DiagnosticCategoryId.AUDIO to RunAllStageOutcome.TIMED_OUT),
            viewModel.state.value.automaticIssues,
        )
    }

    @Test
    fun successWinsAgainstLateTimeout() {
        val viewModel = runAllViewModel()
        viewModel.onPreflightAccepted(RunAllSelections(), RunAllHardwareProfile())
        viewModel.onPermissionsResolved(RunAllPermissions())
        val token = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(token))

        viewModel.onAutomaticChecksComplete(token)
        acknowledgeCompletedStages(viewModel)
        dispatcher.scheduler.advanceTimeBy(RunAllTestsViewModel.AUTOMATIC_TIMEOUT_MS + 1)

        assertEquals(RunAllStage.DISPLAY, viewModel.state.value.stage)
        assertEquals(RunAllStageOutcome.COMPLETED, viewModel.state.value.stageOutcomes[RunAllStage.AUTOMATIC])
        viewModel.interruptRun(RunAllInterruptionReason.USER_CANCEL)
    }

    @Test
    fun timeoutWinsAgainstLateSuccess() {
        val viewModel = runAllViewModel()
        viewModel.onPreflightAccepted(RunAllSelections(), RunAllHardwareProfile())
        viewModel.onPermissionsResolved(RunAllPermissions())
        val token = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(token))

        dispatcher.scheduler.advanceTimeBy(RunAllTestsViewModel.AUTOMATIC_TIMEOUT_MS + 1)
        viewModel.onAutomaticChecksComplete(token)
        acknowledgeCompletedStages(viewModel)

        assertEquals(RunAllStage.DISPLAY, viewModel.state.value.stage)
        assertEquals(RunAllStageOutcome.TIMED_OUT, viewModel.state.value.stageOutcomes[RunAllStage.AUTOMATIC])
        viewModel.interruptRun(RunAllInterruptionReason.USER_CANCEL)
    }

    @Test
    fun displayWaitsForUserOnEveryPatternAndAcceptsResultAfterLongInspection() {
        val viewModel = runAllViewModel()
        enterFirstInteractiveStage(viewModel)
        val token = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(token))

        DisplayPattern.entries.indices.forEach { index ->
            val beforeWaiting = viewModel.state.value
            dispatcher.scheduler.advanceTimeBy(120_000L)
            dispatcher.scheduler.runCurrent()
            assertEquals(beforeWaiting, viewModel.state.value)
            assertEquals(index, viewModel.state.value.displayColorIndex)
            viewModel.nextDisplayColor(token, DisplayPattern.entries.lastIndex)
        }
        viewModel.recordDisplay(token, true)
        assertEquals(true, viewModel.state.value.manualChecks.display)
        assertEquals(RunAllStageOutcome.PASSED, viewModel.state.value.stageOutcomes[RunAllStage.DISPLAY])
        assertTrue(viewModel.state.value.awaitingContinue)
        assertEquals(RunAllStage.DISPLAY, viewModel.state.value.stage)
        acknowledgeCompletedStages(viewModel)
        assertEquals(RunAllStage.BUTTONS, viewModel.state.value.stage)
        viewModel.interruptRun(RunAllInterruptionReason.USER_CANCEL)
    }

    @Test
    fun displayCanStillBeSkippedOrCancelledAfterLongInspection() {
        listOf(false, true).forEach { cancel ->
            val viewModel = runAllViewModel()
            enterFirstInteractiveStage(viewModel)
            val token = viewModel.state.value.stageToken
            assertTrue(viewModel.claimStage(token))
            dispatcher.scheduler.advanceTimeBy(120_000L)
            dispatcher.scheduler.runCurrent()

            if (cancel) {
                assertTrue(viewModel.interruptRun(RunAllInterruptionReason.USER_CANCEL))
                assertEquals(RunAllStage.PREFLIGHT, viewModel.state.value.stage)
                assertEquals(RunAllRunStatus.NOT_STARTED, viewModel.state.value.runStatus)
            } else {
                viewModel.skipStage(token)
                assertEquals(RunAllStageOutcome.SKIPPED, viewModel.state.value.stageOutcomes[RunAllStage.DISPLAY])
                assertTrue(viewModel.state.value.awaitingContinue)
                acknowledgeCompletedStages(viewModel)
                assertEquals(RunAllStage.BUTTONS, viewModel.state.value.stage)
                viewModel.interruptRun(RunAllInterruptionReason.USER_CANCEL)
            }
            viewModel.recordDisplay(token, true)
            assertEquals(null, viewModel.state.value.manualChecks.display)
        }
    }

    @Test
    fun configurationChangeDiscardsTheIncompleteRun() {
        assertInterruptionDiscardsRun(RunAllInterruptionReason.CONFIGURATION_CHANGE)
    }

    @Test
    fun backgroundingDiscardsTheIncompleteRun() {
        assertInterruptionDiscardsRun(RunAllInterruptionReason.BACKGROUND)
    }

    @Test
    fun cancelledRunRejectsLateReportCompletion() {
        val repository = FakeReportRepository()
        val viewModel =
            RunAllTestsViewModel(
                clock = EpochMillisClock { 100L },
                idProvider = IdProvider { "report" },
                reportRepository = repository,
            )
        enterResults(viewModel)
        val resultsToken = viewModel.state.value.stageToken

        viewModel.interruptRun(RunAllInterruptionReason.USER_CANCEL)
        viewModel.completeReport(resultsToken, deviceContext(), appContext(), completeSnapshots())

        assertEquals(RunAllStage.PREFLIGHT, viewModel.state.value.stage)
        assertEquals(null, viewModel.state.value.report)
        assertTrue(repository.insertAttempts.isEmpty())
    }

    @Test
    fun cameraStageVisitsEveryPublicCameraBeforeAdvancing() {
        val viewModel = runAllViewModel()
        enterCameraStage(viewModel)
        val cameraIds = listOf("rear", "front", "external")

        cameraIds.forEachIndexed { index, cameraId ->
            val token = viewModel.state.value.stageToken
            assertTrue(viewModel.claimStage(token))
            assertTrue(viewModel.prepareCameraStage(token, cameraIds))
            assertEquals(cameraId, viewModel.state.value.currentCameraId)
            viewModel.recordCameraCapture(CaptureResult(640, 480, 100L, CameraCaptureAttempt(1L, cameraId, token)))
            if (index < cameraIds.lastIndex) {
                assertEquals(RunAllStage.CAMERA, viewModel.state.value.stage)
            }
        }

        acknowledgeCompletedStages(viewModel)
        assertEquals(RunAllStage.SENSORS, viewModel.state.value.stage)
        assertTrue(viewModel.state.value.manualChecks.cameraCompleted)
        assertEquals(RunAllStageOutcome.PASSED, viewModel.state.value.stageOutcomes[RunAllStage.CAMERA])
    }

    @Test
    fun publishedCaptureCannotAdvanceARetryOrTheNextCamera() =
        runTest {
            val viewModel = runAllViewModel()
            enterCameraStage(viewModel)
            val state = MutableStateFlow(CameraTestState())
            val camera = CameraCaptureSession(state, EpochMillisClock { 100L }, backgroundScope)
            val token = viewModel.state.value.stageToken
            viewModel.claimStage(token)
            viewModel.prepareCameraStage(token, listOf("rear", "front"))
            val attempt = requireNotNull(camera.begin("rear", token))
            camera.succeed(attempt, 640, 480)
            val published = requireNotNull(state.value.lastCapture)

            camera.cancel()
            viewModel.retryStage(token)
            val retryToken = viewModel.state.value.stageToken
            viewModel.claimStage(retryToken)
            assertFalse(viewModel.recordCameraCapture(published))
            assertEquals("rear", viewModel.state.value.currentCameraId)

            val retry = requireNotNull(camera.begin("rear", retryToken))
            assertFalse(camera.fail(attempt, "late error"))
            camera.succeed(retry, 800, 600)
            val current = requireNotNull(state.value.lastCapture)
            assertTrue(viewModel.recordCameraCapture(current))
            assertEquals("front", viewModel.state.value.currentCameraId)
            viewModel.claimStage(viewModel.state.value.stageToken)
            assertFalse(viewModel.recordCameraCapture(current))
            assertEquals("front", viewModel.state.value.currentCameraId)
            viewModel.interruptRun(RunAllInterruptionReason.USER_CANCEL)
        }

    @Test
    fun cameraErrorAndTimeoutRemainRecoverable() {
        val viewModel = runAllViewModel()
        enterCameraStage(viewModel)
        val firstToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(firstToken))
        assertTrue(viewModel.prepareCameraStage(firstToken, listOf("rear")))

        viewModel.reportStageIssue(firstToken, RunAllStageOutcome.ERROR)
        assertEquals(RunAllStage.CAMERA, viewModel.state.value.stage)
        assertEquals(RunAllStageOutcome.ERROR, viewModel.state.value.stageIssue)

        assertTrue(viewModel.retryStage(firstToken))
        val retryToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(retryToken))
        dispatcher.scheduler.advanceTimeBy(RunAllTestsViewModel.CAMERA_TIMEOUT_MS + 1)

        assertEquals(RunAllStage.CAMERA, viewModel.state.value.stage)
        assertEquals(RunAllStageOutcome.TIMED_OUT, viewModel.state.value.stageIssue)
        viewModel.skipStage(retryToken)
        acknowledgeCompletedStages(viewModel)
        assertEquals(RunAllStage.SENSORS, viewModel.state.value.stage)
        assertEquals(RunAllStageOutcome.SKIPPED, viewModel.state.value.stageOutcomes[RunAllStage.CAMERA])
    }

    @Test
    fun completeReportUsesInjectedIdentityAndTimestamps() =
        runTest {
            val timestamps = listOf(100L, 200L).iterator()
            val repository = FakeReportRepository()
            val viewModel =
                RunAllTestsViewModel(
                    clock = EpochMillisClock { timestamps.next() },
                    idProvider = IdProvider { "report-123" },
                    reportRepository = repository,
                )
            enterResults(viewModel)
            val resultsToken = viewModel.state.value.stageToken

            viewModel.completeReport(resultsToken, deviceContext(), appContext(), completeSnapshots())

            val report = requireNotNull(viewModel.state.value.report)
            assertEquals("report-123", report.stableId)
            assertEquals(ReportKind.FULL_CHECK, report.kind)
            assertEquals(Instant.ofEpochMilli(100L), report.startedAt)
            assertEquals(Instant.ofEpochMilli(200L), report.completedAt)
            assertEquals(deviceContext(), report.device)
            assertEquals(appContext(), report.app)
            assertEquals(DiagnosticCatalog.categories, report.categories.map { it.categoryId })
            dispatcher.scheduler.runCurrent()
            assertEquals(ReportSaveStatus.SAVED, viewModel.state.value.saveStatus)
            assertEquals(listOf(report), repository.insertAttempts)
        }

    @Test
    fun completeReportSurvivesWallClockMovingBeforeStart() =
        runTest {
            val timestamps = listOf(200L, 100L).iterator()
            val repository = FakeReportRepository()
            val viewModel =
                RunAllTestsViewModel(
                    clock = EpochMillisClock { timestamps.next() },
                    idProvider = IdProvider { "clock-adjusted-report" },
                    reportRepository = repository,
                )
            enterResults(viewModel)

            viewModel.completeReport(
                viewModel.state.value.stageToken,
                deviceContext(),
                appContext(),
                completeSnapshots(),
            )

            val report = requireNotNull(viewModel.state.value.report)
            assertEquals(Instant.ofEpochMilli(200L), report.startedAt)
            assertEquals(report.startedAt, report.completedAt)
            dispatcher.scheduler.runCurrent()
            assertEquals(ReportSaveStatus.SAVED, viewModel.state.value.saveStatus)
            assertEquals(listOf(report), repository.insertAttempts)
        }

    @Test
    fun completeReportKeepsTheFirstCompletedReport() =
        runTest {
            var nextId = 1
            var nextTimestamp = 100L
            val repository = FakeReportRepository()
            val viewModel =
                RunAllTestsViewModel(
                    clock = EpochMillisClock { nextTimestamp++ },
                    idProvider = IdProvider { "report-${nextId++}" },
                    reportRepository = repository,
                )
            enterResults(viewModel)
            val resultsToken = viewModel.state.value.stageToken

            viewModel.completeReport(resultsToken, deviceContext(model = "first"), appContext(), completeSnapshots())
            val firstReport = requireNotNull(viewModel.state.value.report)

            viewModel.completeReport(resultsToken, deviceContext(model = "second"), appContext(), completeSnapshots())
            val secondReport = requireNotNull(viewModel.state.value.report)

            assertSame(firstReport, secondReport)
            assertEquals("report-1", secondReport.stableId)
            assertEquals(Instant.ofEpochMilli(100L), secondReport.startedAt)
            assertEquals(Instant.ofEpochMilli(101L), secondReport.completedAt)
            dispatcher.scheduler.runCurrent()
            assertEquals(1, repository.insertAttempts.size)
        }

    @Test
    fun failedSaveRetriesTheSameFrozenReportWithoutRemeasuring() =
        runTest {
            val repository = FakeReportRepository(insertFailuresRemaining = 1)
            val viewModel =
                RunAllTestsViewModel(
                    clock = EpochMillisClock { 100L },
                    idProvider = IdProvider { "report" },
                    reportRepository = repository,
                )
            enterResults(viewModel)

            val frozen = completeReportAndAssertSaveFailed(viewModel)

            viewModel.retryReportSave()
            dispatcher.scheduler.runCurrent()

            assertEquals(ReportSaveStatus.SAVED, viewModel.state.value.saveStatus)
            assertEquals(listOf(frozen, frozen), repository.insertAttempts)
        }

    @Test
    fun savedReportIsAvailableToANewRepositoryReaderAfterCompletion() =
        runTest {
            val repository = FakeReportRepository()
            val viewModel =
                RunAllTestsViewModel(
                    clock = EpochMillisClock { 100L },
                    idProvider = IdProvider { "persisted" },
                    reportRepository = repository,
                )
            enterResults(viewModel)
            viewModel.completeReport(
                viewModel.state.value.stageToken,
                deviceContext(),
                appContext(),
                completeSnapshots(),
            )
            dispatcher.scheduler.runCurrent()

            val loaded = repository.getById("persisted") as ReportLoadResult.Available
            assertEquals(viewModel.state.value.report, loaded.report)
        }

    @Test
    fun categoryRetestPersistsOnlyTheRequestedFreshSnapshot() =
        runTest {
            val timestamps = listOf(100L, 200L).iterator()
            val repository = FakeReportRepository()
            val viewModel =
                RunAllTestsViewModel(
                    clock = EpochMillisClock { timestamps.next() },
                    idProvider = IdProvider { "storage-retest" },
                    reportRepository = repository,
                )

            viewModel.onCategoryRetestRequested(
                categoryId = DiagnosticCategoryId.STORAGE,
                hardware = RunAllHardwareProfile.ALL_AVAILABLE,
            )
            assertEquals(RunAllStage.PERMISSIONS, viewModel.state.value.stage)
            assertEquals(DiagnosticCategoryId.STORAGE, viewModel.state.value.targetCategory)

            viewModel.onPermissionsResolved(RunAllPermissions())
            assertEquals(
                listOf(DiagnosticCategoryId.STORAGE),
                viewModel.state.value.plan.categories
                    .map { it.categoryId },
            )
            val automaticToken = viewModel.state.value.stageToken
            assertTrue(viewModel.claimStage(automaticToken))
            viewModel.onAutomaticChecksComplete(automaticToken)
            acknowledgeCompletedStages(viewModel)
            assertEquals(RunAllStage.RESULTS, viewModel.state.value.stage)

            viewModel.completeReport(
                viewModel.state.value.stageToken,
                deviceContext(),
                appContext(),
                completeSnapshots(),
            )
            dispatcher.scheduler.runCurrent()

            val report = requireNotNull(viewModel.state.value.report)
            assertEquals(ReportKind.CATEGORY_ONLY, report.kind)
            assertEquals(listOf(DiagnosticCategoryId.STORAGE), report.categories.map { it.categoryId })
            assertEquals(ReportSaveStatus.SAVED, viewModel.state.value.saveStatus)
            assertEquals(report, (repository.getById("storage-retest") as ReportLoadResult.Available).report)
        }

    @Test
    fun consecutiveRetestsAndSaveRetryPreserveOriginalAndEachFrozenIdentity() =
        runTest {
            val repository = FakeReportRepository()
            val original =
                ReportAssembler.assemble(
                    ReportAssemblyRequest(
                        "original",
                        ReportKind.FULL_CHECK,
                        Instant.ofEpochMilli(100L),
                        Instant.ofEpochMilli(200L),
                        deviceContext(),
                        appContext(),
                        completeSnapshots(),
                    ),
                )
            repository.insert(original)
            val home = HomeViewModel(repository)
            var now = 300L
            for (id in listOf("retest-b", "retest-c")) {
                val viewModel = RunAllTestsViewModel(EpochMillisClock { now }, IdProvider { id }, repository)
                assertEquals(RunAllTestsState(), viewModel.state.value)
                viewModel.onCategoryRetestRequested(DiagnosticCategoryId.STORAGE, RunAllHardwareProfile.ALL_AVAILABLE)
                viewModel.onPermissionsResolved(RunAllPermissions())
                val token = viewModel.state.value.stageToken
                assertTrue(viewModel.claimStage(token))
                viewModel.onAutomaticChecksComplete(token)
                acknowledgeCompletedStages(viewModel)
                now += 100L
                repository.insertFailuresRemaining = 1
                val frozen = completeReportAndAssertSaveFailed(viewModel)
                assertEquals(id, frozen.stableId)
                assertEquals(ReportKind.CATEGORY_ONLY, frozen.kind)
                assertEquals(listOf(DiagnosticCategoryId.STORAGE), frozen.categories.map { it.categoryId })
                val attemptsBeforeRetry = repository.insertAttempts.size
                viewModel.retryReportSave()
                viewModel.retryReportSave()
                dispatcher.scheduler.runCurrent()
                assertEquals(ReportSaveStatus.SAVED, viewModel.state.value.saveStatus)
                assertSame(frozen, viewModel.state.value.report)
                assertEquals(attemptsBeforeRetry + 1, repository.insertAttempts.size)
                assertSame(frozen, repository.insertAttempts.last())
                assertEquals(frozen, (repository.getById(id) as ReportLoadResult.Available).report)
                assertFalse(viewModel.interruptRun(RunAllInterruptionReason.SCREEN_DISPOSED))
                assertSame(frozen, viewModel.state.value.report)
                assertEquals(original, (repository.getById(original.stableId) as ReportLoadResult.Available).report)
                assertEquals(original, (home.latestFullCheck.value as LatestFullCheckState.Available).report)
                now += 100L
            }
            assertEquals(
                listOf("retest-c", "retest-b", "original"),
                repository.observeSummaries().first().map { it.stableId },
            )
        }

    private fun completeReportAndAssertSaveFailed(viewModel: RunAllTestsViewModel): DiagnosticReport {
        viewModel.completeReport(
            viewModel.state.value.stageToken,
            deviceContext(),
            appContext(),
            completeSnapshots(),
        )
        dispatcher.scheduler.runCurrent()
        val frozen = requireNotNull(viewModel.state.value.report)
        assertEquals(ReportSaveStatus.FAILED, viewModel.state.value.saveStatus)
        return frozen
    }

    private fun completeSnapshots(): List<DiagnosticCategorySnapshot> =
        DiagnosticCatalog.categories.map { categoryId ->
            DiagnosticCategorySnapshot(
                version = DiagnosticSnapshotVersion.CURRENT,
                categoryId = categoryId,
                evidence =
                    listOf(
                        DiagnosticEvidence(
                            categoryId = categoryId,
                            checkId = DiagnosticCheckId(categoryId, "${categoryId.stableId}.complete"),
                            status = DiagnosticStatus.PASS,
                            confidence = Confidence.HIGH,
                            source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                            applicability = Applicability.APPLICABLE,
                            capturedAt = Instant.ofEpochMilli(150L),
                        ),
                    ),
            )
        }

    private fun deviceContext(model: String = "model") =
        ReportDeviceContext(
            manufacturer = "manufacturer",
            model = model,
            brand = "brand",
            product = "product",
            androidRelease = "16",
            apiLevel = 36,
            securityPatch = "2026-08-01",
        )

    private fun appContext() = ReportAppContext(versionName = "1.0.0", versionCode = 1L)

    private fun runAllViewModel() =
        RunAllTestsViewModel(
            clock = EpochMillisClock { 100L },
            idProvider = IdProvider { "report" },
            reportRepository = FakeReportRepository(),
        )

    private fun acknowledgeCompletedStages(viewModel: RunAllTestsViewModel) {
        repeat(
            viewModel.state.value.plan.categories.size
                .coerceAtLeast(1),
        ) {
            if (!viewModel.state.value.awaitingContinue) return
            viewModel.continueAfterStage(viewModel.state.value.stageToken)
        }
        assertFalse(viewModel.state.value.awaitingContinue)
    }

    private fun enterFirstInteractiveStage(viewModel: RunAllTestsViewModel) {
        viewModel.onPreflightAccepted(
            selections = RunAllSelections(includeSpeaker = false, includeCamera = false),
            hardware = RunAllHardwareProfile(),
        )
        viewModel.onPermissionsResolved(RunAllPermissions())
        val automaticToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(automaticToken))
        viewModel.onAutomaticChecksComplete(automaticToken)
        acknowledgeCompletedStages(viewModel)
        assertEquals(RunAllStage.DISPLAY, viewModel.state.value.stage)
    }

    private fun enterResults(viewModel: RunAllTestsViewModel) {
        enterFirstInteractiveStage(viewModel)
        val displayToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(displayToken))
        viewModel.skipStage(displayToken)
        acknowledgeCompletedStages(viewModel)
        val buttonsToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(buttonsToken))
        viewModel.skipStage(buttonsToken)
        acknowledgeCompletedStages(viewModel)
        assertEquals(RunAllStage.RESULTS, viewModel.state.value.stage)
    }

    private fun enterCameraStage(viewModel: RunAllTestsViewModel) {
        viewModel.onPreflightAccepted(
            selections = RunAllSelections(),
            hardware = RunAllHardwareProfile.ALL_AVAILABLE,
        )
        viewModel.onPermissionsResolved(
            RunAllPermissions(
                microphone = true,
                camera = true,
                location = true,
                phone = true,
                bluetooth = true,
            ),
        )
        val automaticToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(automaticToken))
        viewModel.onAutomaticChecksComplete(automaticToken)
        acknowledgeCompletedStages(viewModel)
        val displayToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(displayToken))
        viewModel.skipStage(displayToken)
        acknowledgeCompletedStages(viewModel)
        val audioToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(audioToken))
        viewModel.skipStage(audioToken)
        acknowledgeCompletedStages(viewModel)
        assertEquals(RunAllStage.CAMERA, viewModel.state.value.stage)
    }

    private fun assertInterruptionDiscardsRun(reason: RunAllInterruptionReason) {
        val viewModel = runAllViewModel()
        enterFirstInteractiveStage(viewModel)
        val interruptedToken = viewModel.state.value.stageToken

        assertTrue(viewModel.interruptRun(reason))
        viewModel.recordDisplay(interruptedToken, true)
        acknowledgeCompletedStages(viewModel)

        assertEquals(RunAllStage.PREFLIGHT, viewModel.state.value.stage)
        assertEquals(RunAllRunStatus.NOT_STARTED, viewModel.state.value.runStatus)
        assertEquals(reason, viewModel.state.value.lastInterruption)
        assertEquals(null, viewModel.state.value.report)
        assertEquals(null, viewModel.state.value.manualChecks.display)
    }
}
