package com.insaner.fonecheck.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    fun resolveDarkTheme(systemDark: Boolean): Boolean =
        when (this) {
            SYSTEM -> systemDark
            LIGHT -> false
            DARK -> true
        }
}

data class AppPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val testWarningsEnabled: Boolean = true,
    val homeIntroductionDismissed: Boolean = false,
)

interface AppPreferencesRepository {
    val preferences: Flow<AppPreferences>

    suspend fun setThemeMode(mode: AppThemeMode)

    suspend fun setTestWarningsEnabled(enabled: Boolean)

    suspend fun setHomeIntroductionDismissed(dismissed: Boolean)
}

class DataStoreAppPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : AppPreferencesRepository {
    override val preferences: Flow<AppPreferences> =
        dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(
                        androidx.datastore.preferences.core
                            .emptyPreferences(),
                    )
                } else {
                    throw error
                }
            }.map { values ->
                AppPreferences(
                    themeMode =
                        values[THEME_MODE_KEY]
                            ?.let { stored -> AppThemeMode.entries.firstOrNull { it.name == stored } }
                            ?: AppThemeMode.SYSTEM,
                    testWarningsEnabled = values[TEST_WARNINGS_KEY] ?: true,
                    homeIntroductionDismissed = values[HOME_INTRODUCTION_DISMISSED_KEY] ?: false,
                )
            }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { it[THEME_MODE_KEY] = mode.name }
    }

    override suspend fun setTestWarningsEnabled(enabled: Boolean) {
        dataStore.edit { it[TEST_WARNINGS_KEY] = enabled }
    }

    override suspend fun setHomeIntroductionDismissed(dismissed: Boolean) {
        dataStore.edit { it[HOME_INTRODUCTION_DISMISSED_KEY] = dismissed }
    }

    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val TEST_WARNINGS_KEY = booleanPreferencesKey("test_warnings_enabled")

        // Keep the released key so completed legacy onboarding also suppresses the new Home introduction.
        private val HOME_INTRODUCTION_DISMISSED_KEY = booleanPreferencesKey("onboarding_complete")
    }
}
