package com.insaner.fonecheck.ui.screens.onboarding

import com.insaner.fonecheck.data.preferences.AppPreferencesRepository
import com.insaner.fonecheck.data.preferences.FakeAppPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
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
    fun `pages move within bounds`() {
        val viewModel = OnboardingViewModel(FakeAppPreferencesRepository())

        viewModel.previousPage()
        assertEquals(0, viewModel.state.value.pageIndex)
        repeat(OnboardingPage.entries.size + 2) { viewModel.nextPage() }
        assertEquals(OnboardingPage.entries.lastIndex, viewModel.state.value.pageIndex)
        viewModel.previousPage()
        assertEquals(OnboardingPage.entries.lastIndex - 1, viewModel.state.value.pageIndex)
    }

    @Test
    fun `skip and complete persist completion before finishing`() =
        runTest(dispatcher.scheduler) {
            val preferences = FakeAppPreferencesRepository()
            val viewModel = OnboardingViewModel(preferences)

            viewModel.completeOnboarding()
            assertTrue(viewModel.state.value.isSaving)
            assertFalse(viewModel.state.value.finished)
            advanceUntilIdle()

            assertTrue(preferences.values.value.onboardingComplete)
            assertTrue(viewModel.state.value.finished)
            assertFalse(viewModel.state.value.isSaving)
        }

    @Test
    fun `completion is emitted once even after the event is consumed`() =
        runTest(dispatcher.scheduler) {
            val preferences = FakeAppPreferencesRepository()
            var writes = 0
            val repository =
                object : AppPreferencesRepository by preferences {
                    override suspend fun setOnboardingComplete(complete: Boolean) {
                        writes++
                        preferences.setOnboardingComplete(complete)
                    }
                }
            val viewModel = OnboardingViewModel(repository)

            viewModel.completeOnboarding()
            viewModel.completeOnboarding()
            advanceUntilIdle()
            assertEquals(1, writes)
            assertTrue(viewModel.state.value.finished)

            viewModel.completeOnboarding()
            advanceUntilIdle()
            assertEquals(1, writes)

            viewModel.consumeFinished()
            viewModel.completeOnboarding()
            advanceUntilIdle()
            assertEquals(1, writes)
            assertFalse(viewModel.state.value.finished)
            assertFalse(viewModel.state.value.isSaving)
        }

    @Test
    fun `failed completion remains on the current page and can be retried`() =
        runTest(dispatcher.scheduler) {
            val preferences = FakeAppPreferencesRepository()
            var writes = 0
            val repository =
                object : AppPreferencesRepository by preferences {
                    override suspend fun setOnboardingComplete(complete: Boolean) {
                        if (++writes == 1) throw java.io.IOException("Write failed")
                        preferences.setOnboardingComplete(complete)
                    }
                }
            val viewModel = OnboardingViewModel(repository)
            viewModel.nextPage()

            viewModel.completeOnboarding()
            advanceUntilIdle()
            assertEquals(1, viewModel.state.value.pageIndex)
            assertFalse(preferences.values.value.onboardingComplete)
            assertFalse(viewModel.state.value.finished)
            assertFalse(viewModel.state.value.isSaving)
            assertTrue(viewModel.state.value.saveFailed)

            viewModel.completeOnboarding()
            advanceUntilIdle()
            assertEquals(2, writes)
            assertTrue(preferences.values.value.onboardingComplete)
            assertTrue(viewModel.state.value.finished)
            assertFalse(viewModel.state.value.saveFailed)
        }
}
