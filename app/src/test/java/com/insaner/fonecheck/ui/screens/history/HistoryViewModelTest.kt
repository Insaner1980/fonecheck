package com.insaner.fonecheck.ui.screens.history

import com.insaner.fonecheck.data.repository.FakeReportRepository
import com.insaner.fonecheck.data.repository.SavedReportSummary
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.testing.testReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
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
    fun summariesStayNewestFirstAndReactToInsertAndDelete() =
        runTest(dispatcher.scheduler) {
            val repository = FakeReportRepository()
            repository.insert(report("older", "2026-08-08T10:00:00Z"))
            val viewModel = HistoryViewModel(repository)
            advanceUntilIdle()

            assertEquals(
                listOf("older"),
                viewModel.state.value.reports
                    .map { it.stableId },
            )
            assertFalse(viewModel.state.value.isLoading)

            repository.insert(report("newer", "2026-08-08T11:00:00Z"))
            advanceUntilIdle()
            assertEquals(
                listOf("newer", "older"),
                viewModel.state.value.reports
                    .map { it.stableId },
            )

            viewModel.delete("newer")
            advanceUntilIdle()
            assertEquals(
                listOf("older"),
                viewModel.state.value.reports
                    .map { it.stableId },
            )
        }

    @Test
    fun equalCompletionTimesUseStableIdAsDeterministicTieBreaker() =
        runTest(dispatcher.scheduler) {
            val repository = FakeReportRepository()
            repository.insert(report("a-report", "2026-08-08T10:00:00Z"))
            repository.insert(report("b-report", "2026-08-08T10:00:00Z"))

            val viewModel = HistoryViewModel(repository)
            advanceUntilIdle()

            assertEquals(
                listOf("b-report", "a-report"),
                viewModel.state.value.reports
                    .map(SavedReportSummary::stableId),
            )
        }

    @Test
    fun collectionFailureKeepsTheLastRenderedReportsAndRetryRecovers() =
        runTest(dispatcher.scheduler) {
            val repository = FakeReportRepository()
            repository.insert(report("saved", "2026-08-08T10:00:00Z"))
            val saved = repository.observeSummaries()
            repository.summariesFlowOverride =
                flow {
                    emit(saved.first())
                    error("history_failed")
                }
            val viewModel = HistoryViewModel(repository)
            advanceUntilIdle()

            assertEquals(
                listOf("saved"),
                viewModel.state.value.reports
                    .map { it.stableId },
            )
            assertEquals("history_load_failed", viewModel.state.value.error)

            repository.summariesFlowOverride = saved
            viewModel.retry()
            advanceUntilIdle()
            assertEquals(
                listOf("saved"),
                viewModel.state.value.reports
                    .map { it.stableId },
            )
            assertEquals(null, viewModel.state.value.error)
        }

    @Test
    fun failedDeleteKeepsTheReportAndSurfacesAnError() =
        runTest(dispatcher.scheduler) {
            val repository = FakeReportRepository(deleteFailuresRemaining = 1)
            repository.insert(report("saved", "2026-08-08T10:00:00Z"))
            val viewModel = HistoryViewModel(repository)
            advanceUntilIdle()

            viewModel.delete("saved")
            advanceUntilIdle()

            assertEquals(
                listOf("saved"),
                viewModel.state.value.reports
                    .map { it.stableId },
            )
            assertEquals("history_delete_failed", viewModel.state.value.error)
            assertTrue(
                viewModel.state.value.deletingReportIds
                    .isEmpty(),
            )
        }

    private fun report(
        id: String,
        completedAt: String,
    ) = testReport(
        id = id,
        completedAt = Instant.parse(completedAt),
        scoreState = ScoreState.COMPLETE,
        coverage = CoverageSummary(1, 1, 0, 0, 100),
    )
}
