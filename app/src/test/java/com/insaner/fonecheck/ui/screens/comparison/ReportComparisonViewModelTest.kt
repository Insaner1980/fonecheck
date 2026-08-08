package com.insaner.fonecheck.ui.screens.comparison

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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ReportComparisonViewModelTest {
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
    fun twoAvailableReportsAreComparedInRequestedOrder() =
        runTest(dispatcher.scheduler) {
            val repository = FakeReportRepository()
            repository.getByIdOverrides["before"] = ReportLoadResult.Available(report("before", 80))
            repository.getByIdOverrides["after"] = ReportLoadResult.Available(report("after", 86))

            val viewModel = viewModel(repository)
            advanceUntilIdle()

            val state = viewModel.state.value as ReportComparisonState.Content
            assertEquals("before", state.comparison.beforeId)
            assertEquals("after", state.comparison.afterId)
        }

    @Test
    fun missingAndUnavailableReportsRemainDistinct() =
        runTest(dispatcher.scheduler) {
            val repository = FakeReportRepository()
            repository.getByIdOverrides["before"] = ReportLoadResult.NotFound
            repository.getByIdOverrides["after"] = ReportLoadResult.Available(report("after", 86))
            val viewModel = viewModel(repository)
            advanceUntilIdle()
            assertEquals(ReportComparisonState.NotFound, viewModel.state.value)

            repository.getByIdOverrides["before"] =
                ReportLoadResult.Unavailable("before", ReportReadFailure.CORRUPT_DATA)
            viewModel.retry()
            advanceUntilIdle()
            assertEquals(
                ReportComparisonState.Unavailable(setOf(ReportReadFailure.CORRUPT_DATA)),
                viewModel.state.value,
            )
        }

    @Test
    fun repositoryFailureCanBeRetried() =
        runTest(dispatcher.scheduler) {
            val repository = FakeReportRepository(getByIdFailuresRemaining = 1)
            repository.getByIdOverrides["before"] = ReportLoadResult.Available(report("before", 80))
            repository.getByIdOverrides["after"] = ReportLoadResult.Available(report("after", 86))
            val viewModel = viewModel(repository)
            advanceUntilIdle()
            assertEquals(ReportComparisonState.Error, viewModel.state.value)

            viewModel.retry()
            advanceUntilIdle()
            assert(viewModel.state.value is ReportComparisonState.Content)
        }

    private fun viewModel(repository: FakeReportRepository) =
        ReportComparisonViewModel(
            savedStateHandle =
                SavedStateHandle(
                    mapOf("firstReportId" to "before", "secondReportId" to "after"),
                ),
            reportRepository = repository,
            ioDispatcher = dispatcher,
        )

    private fun report(
        id: String,
        score: Int,
    ) = DiagnosticReport(
        stableId = id,
        kind = ReportKind.FULL_CHECK,
        startedAt = Instant.parse("2026-08-08T10:00:00Z"),
        completedAt = Instant.parse("2026-08-08T10:01:00Z"),
        device = ReportDeviceContext("Finnvek", "Test", "Fonecheck", "test", "16", 36, null),
        app = ReportAppContext("1.0.0", 1L),
        categories = emptyList(),
        score = ScoreSummary(ScoreVersion.CURRENT, score, ScoreState.PARTIAL),
        coverage = CoverageSummary(4, 3, 1, 0, 75),
        schemaVersion = ReportSchemaVersion.CURRENT,
    )
}
