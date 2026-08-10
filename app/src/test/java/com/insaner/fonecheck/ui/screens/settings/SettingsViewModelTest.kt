package com.insaner.fonecheck.ui.screens.settings

import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.data.preferences.AppThemeMode
import com.insaner.fonecheck.data.preferences.FakeAppPreferencesRepository
import com.insaner.fonecheck.data.repository.FakeReportRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
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
    fun preferencesReportCountAndPermissionsStayReactive() =
        runTest(dispatcher.scheduler) {
            val preferences = FakeAppPreferencesRepository()
            val reports = FakeReportRepository().apply { insert(report("one")) }
            val permissions = FakePermissions(SettingsPermissionSnapshot(camera = true))
            val viewModel = SettingsViewModel(preferences, reports, permissions)
            advanceUntilIdle()

            assertEquals(1, viewModel.state.value.reportCount)
            assertTrue(viewModel.state.value.permissions.camera)

            viewModel.setThemeMode(AppThemeMode.DARK)
            viewModel.setTestWarningsEnabled(false)
            reports.insert(report("two"))
            permissions.snapshot = SettingsPermissionSnapshot(microphone = true)
            viewModel.refreshPermissions()
            advanceUntilIdle()

            assertEquals(AppThemeMode.DARK, viewModel.state.value.preferences.themeMode)
            assertFalse(viewModel.state.value.preferences.testWarningsEnabled)
            assertEquals(2, viewModel.state.value.reportCount)
            assertTrue(viewModel.state.value.permissions.microphone)
        }

    @Test
    fun deleteAllAndReopenOnboardingRequireExplicitCommands() =
        runTest(dispatcher.scheduler) {
            val preferences = FakeAppPreferencesRepository(AppPreferences(onboardingComplete = true))
            val reports = FakeReportRepository().apply { insert(report("one")) }
            val viewModel = SettingsViewModel(preferences, reports, FakePermissions())
            advanceUntilIdle()

            viewModel.deleteAllReports()
            advanceUntilIdle()
            assertEquals(0, viewModel.state.value.reportCount)

            viewModel.reopenOnboarding()
            advanceUntilIdle()
            assertTrue(preferences.values.value.onboardingComplete)
            assertTrue(viewModel.state.value.openOnboarding)
            viewModel.consumeOpenOnboarding()
            assertFalse(viewModel.state.value.openOnboarding)
        }

    private class FakePermissions(
        var snapshot: SettingsPermissionSnapshot = SettingsPermissionSnapshot(),
    ) : SettingsPermissionProvider {
        override fun current(): SettingsPermissionSnapshot = snapshot
    }

    private fun report(id: String) =
        testReport(
            id = id,
            completedAt = Instant.parse("2026-08-08T10:01:00Z").plusSeconds(id.length.toLong()),
        )
}
