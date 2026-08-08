package com.insaner.fonecheck.ui.screens.report

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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportDetailViewModelTest {
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
    fun availableReportIsLoadedFromTheImmutableRepositorySnapshot() =
        runTest(dispatcher.scheduler) {
            val report = report()
            val repository = FakeReportRepository(getByIdOverride = ReportLoadResult.Available(report))

            val viewModel = viewModel(repository)
            advanceUntilIdle()

            assertEquals(ReportDetailState.Content(report), viewModel.state.value)
        }

    @Test
    fun unavailableAndMissingReportsRemainDistinct() =
        runTest(dispatcher.scheduler) {
            val repository =
                FakeReportRepository(
                    getByIdOverride =
                        ReportLoadResult.Unavailable(
                            reportId = "saved-report",
                            reason = ReportReadFailure.CORRUPT_DATA,
                        ),
                )
            val unavailable = viewModel(repository)
            advanceUntilIdle()
            assertEquals(
                ReportDetailState.Unavailable(ReportReadFailure.CORRUPT_DATA),
                unavailable.state.value,
            )

            repository.getByIdOverride = ReportLoadResult.NotFound
            unavailable.retry()
            advanceUntilIdle()
            assertEquals(ReportDetailState.NotFound, unavailable.state.value)
        }

    @Test
    fun repositoryFailureCanBeRetriedWithoutKeepingAnErrorSnapshot() =
        runTest(dispatcher.scheduler) {
            val report = report()
            val repository =
                FakeReportRepository(
                    getByIdFailuresRemaining = 1,
                    getByIdOverride = ReportLoadResult.Available(report),
                )
            val viewModel = viewModel(repository)
            advanceUntilIdle()
            assertEquals(ReportDetailState.Error, viewModel.state.value)

            viewModel.retry()
            advanceUntilIdle()
            assertEquals(ReportDetailState.Content(report), viewModel.state.value)
        }

    private fun viewModel(repository: FakeReportRepository) =
        ReportDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("reportId" to "saved-report")),
            reportRepository = repository,
            ioDispatcher = dispatcher,
        )

    private fun report() =
        DiagnosticReport(
            stableId = "saved-report",
            kind = ReportKind.FULL_CHECK,
            startedAt = Instant.parse("2026-08-08T10:00:00Z"),
            completedAt = Instant.parse("2026-08-08T10:01:00Z"),
            device = ReportDeviceContext("Finnvek", "Test Device", "Fonecheck", "test", "16", 36, null),
            app = ReportAppContext("1.0.0", 1L),
            categories = emptyList(),
            score = ScoreSummary(ScoreVersion.CURRENT, null, ScoreState.INCOMPLETE),
            coverage = CoverageSummary(0, 0, 0, 0, 0),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
}
