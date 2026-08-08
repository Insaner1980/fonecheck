package com.insaner.fonecheck.ui.screens.export

import androidx.lifecycle.SavedStateHandle
import com.insaner.fonecheck.data.repository.FakeReportRepository
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.data.repository.ReportReadFailure
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.export.ExportedReport
import com.insaner.fonecheck.export.ReportExporter
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportExportViewModelTest {
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
    fun availableReportExportsPdfAndEmitsOneShareRequest() =
        runTest(dispatcher.scheduler) {
            val report = report()
            val repository = FakeReportRepository(getByIdOverride = ReportLoadResult.Available(report))
            val exporter = FakeExporter()
            val viewModel = viewModel(repository, exporter)
            advanceUntilIdle()

            viewModel.exportPdf()
            advanceUntilIdle()

            assertEquals(listOf(report), exporter.reports)
            assertEquals(exporter.result, (viewModel.state.value as ReportExportState.Ready).shareRequest)
            viewModel.consumeShareRequest()
            assertNull((viewModel.state.value as ReportExportState.Ready).shareRequest)
        }

    @Test
    fun exportFailureRemainsRetryableWithoutReloadingTheReport() =
        runTest(dispatcher.scheduler) {
            val repository = FakeReportRepository(getByIdOverride = ReportLoadResult.Available(report()))
            val exporter = FakeExporter(failuresRemaining = 1)
            val viewModel = viewModel(repository, exporter)
            advanceUntilIdle()

            viewModel.exportPdf()
            advanceUntilIdle()
            assertEquals("pdf_export_failed", (viewModel.state.value as ReportExportState.Ready).error)

            viewModel.exportPdf()
            advanceUntilIdle()
            assertEquals(exporter.result, (viewModel.state.value as ReportExportState.Ready).shareRequest)
        }

    @Test
    fun missingAndUnreadableReportsRemainDistinct() =
        runTest(dispatcher.scheduler) {
            val repository = FakeReportRepository(getByIdOverride = ReportLoadResult.NotFound)
            val viewModel = viewModel(repository, FakeExporter())
            advanceUntilIdle()
            assertEquals(ReportExportState.NotFound, viewModel.state.value)

            repository.getByIdOverride =
                ReportLoadResult.Unavailable("saved-report", ReportReadFailure.CORRUPT_DATA)
            viewModel.retryLoad()
            advanceUntilIdle()
            assertEquals(
                ReportExportState.Unavailable(ReportReadFailure.CORRUPT_DATA),
                viewModel.state.value,
            )
        }

    private fun viewModel(
        repository: FakeReportRepository,
        exporter: ReportExporter,
    ) =
        ReportExportViewModel(
            savedStateHandle = SavedStateHandle(mapOf("reportId" to "saved-report")),
            reportRepository = repository,
            reportExporter = exporter,
            ioDispatcher = dispatcher,
        )

    private class FakeExporter(
        var failuresRemaining: Int = 0,
    ) : ReportExporter {
        val reports = mutableListOf<DiagnosticReport>()
        val result = ExportedReport("content://report.pdf", "application/pdf", "report.pdf")

        override suspend fun exportPdf(report: DiagnosticReport): ExportedReport {
            reports += report
            if (failuresRemaining > 0) {
                failuresRemaining -= 1
                error("failed")
            }
            return result
        }
    }

    private fun report() =
        DiagnosticReport(
            stableId = "saved-report",
            kind = ReportKind.FULL_CHECK,
            startedAt = Instant.parse("2026-08-08T10:00:00Z"),
            completedAt = Instant.parse("2026-08-08T10:01:00Z"),
            device = ReportDeviceContext("Finnvek", "Test", "Fonecheck", "test", "16", 36, null),
            app = ReportAppContext("1.0.0", 1L),
            categories = emptyList(),
            score = ScoreSummary(ScoreVersion.CURRENT, 90, ScoreState.PARTIAL),
            coverage = CoverageSummary(4, 3, 1, 0, 75),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
}
