package com.insaner.fonecheck.ui.screens.settings

import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.data.preferences.AppPreferencesRepository
import com.insaner.fonecheck.data.preferences.AppThemeMode
import com.insaner.fonecheck.data.repository.FakeReportRepository
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
            val preferences = FakePreferences()
            val reports = FakeReportRepository().apply { insert(report("one")) }
            val permissions = FakePermissions(SettingsPermissionSnapshot(camera = true))
            val viewModel = SettingsViewModel(preferences, reports, permissions, dispatcher)
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
            val preferences = FakePreferences(AppPreferences(onboardingComplete = true))
            val reports = FakeReportRepository().apply { insert(report("one")) }
            val viewModel = SettingsViewModel(preferences, reports, FakePermissions(), dispatcher)
            advanceUntilIdle()

            viewModel.deleteAllReports()
            advanceUntilIdle()
            assertEquals(0, viewModel.state.value.reportCount)

            viewModel.reopenOnboarding()
            advanceUntilIdle()
            assertFalse(preferences.values.value.onboardingComplete)
            assertTrue(viewModel.state.value.openOnboarding)
            viewModel.consumeOpenOnboarding()
            assertFalse(viewModel.state.value.openOnboarding)
        }

    private class FakePreferences(
        initial: AppPreferences = AppPreferences(),
    ) : AppPreferencesRepository {
        val values = MutableStateFlow(initial)
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

    private class FakePermissions(
        var snapshot: SettingsPermissionSnapshot = SettingsPermissionSnapshot(),
    ) : SettingsPermissionProvider {
        override fun current(): SettingsPermissionSnapshot = snapshot
    }

    private fun report(id: String) =
        DiagnosticReport(
            stableId = id,
            kind = ReportKind.FULL_CHECK,
            startedAt = Instant.parse("2026-08-08T10:00:00Z"),
            completedAt = Instant.parse("2026-08-08T10:01:00Z").plusSeconds(id.length.toLong()),
            device = ReportDeviceContext("Finnvek", "Test", "Fonecheck", "test", "16", 36, null),
            app = ReportAppContext("1.0.0", 1L),
            categories = emptyList(),
            score = ScoreSummary(ScoreVersion.CURRENT, 90, ScoreState.PARTIAL),
            coverage = CoverageSummary(4, 3, 1, 0, 75),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
}
