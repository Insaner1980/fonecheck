package com.insaner.fonecheck.ui.screens.comparison

import androidx.lifecycle.SavedStateHandle
import com.insaner.fonecheck.data.repository.FakeReportRepository
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.data.repository.ReportReadFailure
import com.insaner.fonecheck.testing.testReport
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
            val repository = repositoryWithReports()

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
            val repository = repositoryWithReports(failuresRemaining = 1)
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
        )

    private fun repositoryWithReports(failuresRemaining: Int = 0) =
        FakeReportRepository(getByIdFailuresRemaining = failuresRemaining).apply {
            getByIdOverrides["before"] = ReportLoadResult.Available(report("before", 80))
            getByIdOverrides["after"] = ReportLoadResult.Available(report("after", 86))
        }

    private fun report(
        id: String,
        score: Int,
    ) = testReport(id = id, scoreValue = score)
}
