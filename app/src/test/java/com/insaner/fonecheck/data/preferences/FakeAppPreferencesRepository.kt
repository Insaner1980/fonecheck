package com.insaner.fonecheck.data.preferences

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeAppPreferencesRepository(
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
