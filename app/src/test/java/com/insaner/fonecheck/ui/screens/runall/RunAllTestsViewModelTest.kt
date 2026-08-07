package com.insaner.fonecheck.ui.screens.runall

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
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RunAllTestsViewModelTest {
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
        assertFalse(RunAllStage.CAMERA in state.plan.stages)
        assertFalse(RunAllStage.SENSORS in state.plan.stages)
        assertFalse(RunAllStage.VIBRATION in state.plan.stages)
        assertTrue(RunAllStage.RESULTS in state.plan.stages)
    }

    @Test
    fun completionAdvancesThroughOnlyThePlannedInteractiveStages() {
        val viewModel = runAllViewModel()
        viewModel.onPreflightAccepted(
            selections = RunAllSelections(includeSpeaker = false, includeCamera = false),
            hardware = RunAllHardwareProfile(),
        )
        viewModel.onPermissionsResolved(RunAllPermissions())

        viewModel.onAutomaticChecksComplete()
        assertEquals(RunAllStage.DISPLAY, viewModel.state.value.stage)
        assertEquals(RunAllProgress(position = 1, total = 2), viewModel.state.value.progress)

        viewModel.recordDisplay(true)
        assertEquals(RunAllStage.BUTTONS, viewModel.state.value.stage)
        assertEquals(RunAllProgress(position = 2, total = 2), viewModel.state.value.progress)

        viewModel.recordButtons(null)
        assertEquals(RunAllStage.RESULTS, viewModel.state.value.stage)
    }

    @Test
    fun completeReportUsesInjectedIdentityAndTimestamps() =
        runTest {
            val timestamps = listOf(100L, 200L).iterator()
            val viewModel =
                RunAllTestsViewModel(
                    clock = EpochMillisClock { timestamps.next() },
                    idProvider = IdProvider { "report-123" },
                )

            viewModel.completeReport(deviceContext(), appContext(), completeSnapshots())

            val report = requireNotNull(viewModel.state.value.report)
            assertEquals("report-123", report.stableId)
            assertEquals(ReportKind.FULL_CHECK, report.kind)
            assertEquals(Instant.ofEpochMilli(100L), report.startedAt)
            assertEquals(Instant.ofEpochMilli(200L), report.completedAt)
            assertEquals(deviceContext(), report.device)
            assertEquals(appContext(), report.app)
            assertEquals(DiagnosticCatalog.categories, report.categories.map { it.categoryId })
        }

    @Test
    fun completeReportKeepsTheFirstCompletedReport() =
        runTest {
            var nextId = 1
            var nextTimestamp = 100L
            val viewModel =
                RunAllTestsViewModel(
                    clock = EpochMillisClock { nextTimestamp++ },
                    idProvider = IdProvider { "report-${nextId++}" },
                )

            viewModel.completeReport(deviceContext(model = "first"), appContext(), completeSnapshots())
            val firstReport = requireNotNull(viewModel.state.value.report)

            viewModel.completeReport(deviceContext(model = "second"), appContext(), completeSnapshots())
            val secondReport = requireNotNull(viewModel.state.value.report)

            assertSame(firstReport, secondReport)
            assertEquals("report-1", secondReport.stableId)
            assertEquals(Instant.ofEpochMilli(100L), secondReport.startedAt)
            assertEquals(Instant.ofEpochMilli(101L), secondReport.completedAt)
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
        )
}
