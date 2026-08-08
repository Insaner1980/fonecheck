package com.insaner.fonecheck.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreAppPreferencesRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()
    private val scopes = mutableListOf<CoroutineScope>()

    @After
    fun tearDown() {
        scopes.forEach(CoroutineScope::cancel)
    }

    @Test
    fun defaultsAndUpdatesRoundTripThroughPreferencesDataStore() =
        runTest {
            val defaults = repository(File(temporaryFolder.root, "defaults.preferences_pb"))
            assertEquals(AppPreferences(), defaults.preferences.first())

            val theme = repository(File(temporaryFolder.root, "theme.preferences_pb"))
            theme.setThemeMode(AppThemeMode.DARK)
            assertEquals(AppThemeMode.DARK, theme.preferences.first().themeMode)

            val warnings = repository(File(temporaryFolder.root, "warnings.preferences_pb"))
            warnings.setTestWarningsEnabled(false)
            assertFalse(warnings.preferences.first().testWarningsEnabled)

            val onboarding = repository(File(temporaryFolder.root, "onboarding.preferences_pb"))
            onboarding.setOnboardingComplete(true)
            assertTrue(onboarding.preferences.first().onboardingComplete)
        }

    @Test
    fun unknownStoredThemeFallsBackToSystem() =
        runTest {
            val dataStore =
                PreferenceDataStoreFactory.createWithPath(scope = dataStoreScope()) {
                    File(temporaryFolder.root, "unknown.preferences_pb").absolutePath.toPath()
                }
            dataStore.edit { preferences ->
                preferences[DataStoreAppPreferencesRepository.THEME_MODE_KEY] = "future_theme"
            }

            val repository = DataStoreAppPreferencesRepository(dataStore)

            assertEquals(AppThemeMode.SYSTEM, repository.preferences.first().themeMode)
        }

    @Test
    fun themeModeResolvesAgainstSystemOnlyWhenRequested() {
        assertTrue(AppThemeMode.SYSTEM.resolveDarkTheme(systemDark = true))
        assertFalse(AppThemeMode.SYSTEM.resolveDarkTheme(systemDark = false))
        assertFalse(AppThemeMode.LIGHT.resolveDarkTheme(systemDark = true))
        assertTrue(AppThemeMode.DARK.resolveDarkTheme(systemDark = false))
    }

    private fun kotlinx.coroutines.test.TestScope.repository(file: File) =
        DataStoreAppPreferencesRepository(
            PreferenceDataStoreFactory.createWithPath(scope = dataStoreScope()) {
                file.absolutePath.toPath()
            },
        )

    private fun dataStoreScope() = CoroutineScope(SupervisorJob() + Dispatchers.IO).also(scopes::add)
}
