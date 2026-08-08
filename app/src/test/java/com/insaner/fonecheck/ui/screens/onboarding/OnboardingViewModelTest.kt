package com.insaner.fonecheck.ui.screens.onboarding

import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.data.preferences.AppPreferencesRepository
import com.insaner.fonecheck.data.preferences.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
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
        val viewModel = OnboardingViewModel(FakePreferences(), dispatcher)

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
            val preferences = FakePreferences()
            val viewModel = OnboardingViewModel(preferences, dispatcher)

            viewModel.completeOnboarding()
            assertTrue(viewModel.state.value.isSaving)
            assertFalse(viewModel.state.value.finished)
            advanceUntilIdle()

            assertTrue(preferences.values.value.onboardingComplete)
            assertTrue(viewModel.state.value.finished)
            assertFalse(viewModel.state.value.isSaving)
        }

    private class FakePreferences : AppPreferencesRepository {
        val values = MutableStateFlow(AppPreferences())
        override val preferences = values

        override suspend fun setThemeMode(mode: AppThemeMode) {
            values.update { it.copy(themeMode = mode) }
        }

        override suspend fun setTestWarningsEnabled(enabled: Boolean) {
            values.update { it.copy(testWarningsEnabled = enabled) }
        }

        override suspend fun setOnboardingComplete(complete: Boolean) {
            values.update { it.copy(onboardingComplete = complete) }
        }
    }
}
