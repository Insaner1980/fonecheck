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
import com.insaner.fonecheck.domain.model.DiagnosticSnapshotVersion
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        assertEquals(RunAllStage.BUTTONS, viewModel.state.value.stage)
        assertEquals(RunAllProgress(position = 2, total = 2), viewModel.state.value.progress)

        val buttonsToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(buttonsToken))
        viewModel.skipStage(buttonsToken)
        assertEquals(RunAllStage.RESULTS, viewModel.state.value.stage)
    }

    @Test
    fun duplicateAndLateCallbacksCannotAdvanceAnotherStage() {
        val viewModel = runAllViewModel()
        enterFirstInteractiveStage(viewModel)
        val displayToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(displayToken))

        viewModel.recordDisplay(displayToken, true)
        val nextStage = viewModel.state.value.stage
        viewModel.recordDisplay(displayToken, false)

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
        enterFirstInteractiveStage(viewModel)
        val token = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(token))

        viewModel.recordDisplay(token, true)
        dispatcher.scheduler.advanceTimeBy(RunAllTestsViewModel.DISPLAY_TIMEOUT_MS + 1)

        assertEquals(RunAllStage.BUTTONS, viewModel.state.value.stage)
        assertEquals(RunAllStageOutcome.PASSED, viewModel.state.value.stageOutcomes[RunAllStage.DISPLAY])
    }

    @Test
    fun timeoutWinsAgainstLateSuccess() {
        val viewModel = runAllViewModel()
        enterFirstInteractiveStage(viewModel)
        val token = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(token))

        dispatcher.scheduler.advanceTimeBy(RunAllTestsViewModel.DISPLAY_TIMEOUT_MS + 1)
        viewModel.recordDisplay(token, true)

        assertEquals(RunAllStage.BUTTONS, viewModel.state.value.stage)
        assertEquals(RunAllStageOutcome.TIMED_OUT, viewModel.state.value.stageOutcomes[RunAllStage.DISPLAY])
        assertEquals(null, viewModel.state.value.manualChecks.display)
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
            viewModel.recordCameraCapture(token)
            if (index < cameraIds.lastIndex) {
                assertEquals(RunAllStage.CAMERA, viewModel.state.value.stage)
            }
        }

        assertEquals(RunAllStage.SENSORS, viewModel.state.value.stage)
        assertTrue(viewModel.state.value.manualChecks.cameraCompleted)
        assertEquals(RunAllStageOutcome.PASSED, viewModel.state.value.stageOutcomes[RunAllStage.CAMERA])
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

            viewModel.completeReport(
                viewModel.state.value.stageToken,
                deviceContext(),
                appContext(),
                completeSnapshots(),
            )
            dispatcher.scheduler.runCurrent()
            val frozen = requireNotNull(viewModel.state.value.report)
            assertEquals(ReportSaveStatus.FAILED, viewModel.state.value.saveStatus)

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

    private fun enterFirstInteractiveStage(viewModel: RunAllTestsViewModel) {
        viewModel.onPreflightAccepted(
            selections = RunAllSelections(includeSpeaker = false, includeCamera = false),
            hardware = RunAllHardwareProfile(),
        )
        viewModel.onPermissionsResolved(RunAllPermissions())
        val automaticToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(automaticToken))
        viewModel.onAutomaticChecksComplete(automaticToken)
        assertEquals(RunAllStage.DISPLAY, viewModel.state.value.stage)
    }

    private fun enterResults(viewModel: RunAllTestsViewModel) {
        enterFirstInteractiveStage(viewModel)
        val displayToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(displayToken))
        viewModel.skipStage(displayToken)
        val buttonsToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(buttonsToken))
        viewModel.skipStage(buttonsToken)
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
        val displayToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(displayToken))
        viewModel.skipStage(displayToken)
        val audioToken = viewModel.state.value.stageToken
        assertTrue(viewModel.claimStage(audioToken))
        viewModel.skipStage(audioToken)
        assertEquals(RunAllStage.CAMERA, viewModel.state.value.stage)
    }

    private fun assertInterruptionDiscardsRun(reason: RunAllInterruptionReason) {
        val viewModel = runAllViewModel()
        enterFirstInteractiveStage(viewModel)
        val interruptedToken = viewModel.state.value.stageToken

        assertTrue(viewModel.interruptRun(reason))
        viewModel.recordDisplay(interruptedToken, true)

        assertEquals(RunAllStage.PREFLIGHT, viewModel.state.value.stage)
        assertEquals(RunAllRunStatus.NOT_STARTED, viewModel.state.value.runStatus)
        assertEquals(reason, viewModel.state.value.lastInterruption)
        assertEquals(null, viewModel.state.value.report)
        assertEquals(null, viewModel.state.value.manualChecks.display)
    }
}
