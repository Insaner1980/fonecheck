package com.insaner.fonecheck.ui.screens.home

import com.insaner.fonecheck.data.repository.FakeReportRepository
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.testing.batteryReport
import com.insaner.fonecheck.testing.testReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
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
    fun `empty repository produces an empty latest full check state`() =
        runTest(dispatcher.scheduler) {
            val viewModel = HomeViewModel(FakeReportRepository())

            advanceUntilIdle()

            assertSame(LatestFullCheckState.Empty, viewModel.latestFullCheck.value)
        }

    @Test
    fun `newer category retest does not replace latest full check`() =
        runTest(dispatcher.scheduler) {
            val repository = FakeReportRepository()
            val full =
                testReport(
                    id = "full",
                    completedAt = Instant.parse("2026-08-11T10:00:00Z"),
                )
            val categoryRetest =
                batteryReport("retest", "Phone").copy(
                    kind = ReportKind.CATEGORY_ONLY,
                    completedAt = Instant.parse("2026-08-11T11:00:00Z"),
                )
            repository.insert(full)
            repository.insert(categoryRetest)

            val viewModel = HomeViewModel(repository)
            advanceUntilIdle()

            val state = viewModel.latestFullCheck.value
            assertTrue(state is LatestFullCheckState.Available)
            assertEquals("full", (state as LatestFullCheckState.Available).report.stableId)
        }

    @Test
    fun `summary collection failure produces an explicit error state`() =
        runTest(dispatcher.scheduler) {
            val repository =
                FakeReportRepository().apply {
                    summariesFlowOverride = flow { error("load_failed") }
                }

            val viewModel = HomeViewModel(repository)
            advanceUntilIdle()

            assertSame(LatestFullCheckState.Error, viewModel.latestFullCheck.value)
        }
}
