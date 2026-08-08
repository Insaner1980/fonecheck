package com.insaner.fonecheck.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.data.preferences.AppPreferencesRepository
import com.insaner.fonecheck.data.preferences.AppThemeMode
import com.insaner.fonecheck.data.repository.ReportRepository
import com.insaner.fonecheck.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsPermissionSnapshot(
    val camera: Boolean = false,
    val microphone: Boolean = false,
    val phone: Boolean = false,
    val location: Boolean = false,
    val bluetooth: Boolean = false,
)

interface SettingsPermissionProvider {
    fun current(): SettingsPermissionSnapshot
}

class AndroidSettingsPermissionProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : SettingsPermissionProvider {
        override fun current() =
            SettingsPermissionSnapshot(
                camera = granted(Manifest.permission.CAMERA),
                microphone = granted(Manifest.permission.RECORD_AUDIO),
                phone = granted(Manifest.permission.READ_PHONE_STATE),
                location = granted(Manifest.permission.ACCESS_FINE_LOCATION),
                bluetooth =
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                        granted(Manifest.permission.BLUETOOTH_CONNECT),
            )

        private fun granted(permission: String) =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

data class SettingsState(
    val preferences: AppPreferences = AppPreferences(),
    val reportCount: Int = 0,
    val permissions: SettingsPermissionSnapshot = SettingsPermissionSnapshot(),
    val isLoading: Boolean = true,
    val isDeletingReports: Boolean = false,
    val error: String? = null,
    val openOnboarding: Boolean = false,
)

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val preferencesRepository: AppPreferencesRepository,
        private val reportRepository: ReportRepository,
        private val permissionProvider: SettingsPermissionProvider,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _state = MutableStateFlow(SettingsState(permissions = permissionProvider.current()))
        val state: StateFlow<SettingsState> = _state.asStateFlow()

        init {
            viewModelScope.launch {
                combine(
                    preferencesRepository.preferences,
                    reportRepository.observeSummaries(),
                ) { preferences, reports -> preferences to reports.size }
                    .catch { error ->
                        if (error is CancellationException) throw error
                        _state.value = _state.value.copy(isLoading = false, error = SETTINGS_LOAD_FAILED)
                    }.collect { (preferences, reportCount) ->
                        _state.value =
                            _state.value.copy(
                                preferences = preferences,
                                reportCount = reportCount,
                                isLoading = false,
                                error = null,
                            )
                    }
            }
        }

        fun refreshPermissions() {
            _state.value = _state.value.copy(permissions = permissionProvider.current())
        }

        fun setThemeMode(mode: AppThemeMode) {
            updatePreference { preferencesRepository.setThemeMode(mode) }
        }

        fun setTestWarningsEnabled(enabled: Boolean) {
            updatePreference { preferencesRepository.setTestWarningsEnabled(enabled) }
        }

        fun deleteAllReports() {
            if (_state.value.isDeletingReports) return
            _state.value = _state.value.copy(isDeletingReports = true, error = null)
            viewModelScope.launch {
                try {
                    withContext(ioDispatcher) { reportRepository.deleteAll() }
                    _state.value = _state.value.copy(isDeletingReports = false)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    _state.value =
                        _state.value.copy(
                            isDeletingReports = false,
                            error = DELETE_REPORTS_FAILED,
                        )
                }
            }
        }

        fun reopenOnboarding() {
            _state.value = _state.value.copy(openOnboarding = true, error = null)
        }

        fun consumeOpenOnboarding() {
            _state.value = _state.value.copy(openOnboarding = false)
        }

        fun clearError() {
            _state.value = _state.value.copy(error = null)
        }

        private fun updatePreference(update: suspend () -> Unit) {
            viewModelScope.launch {
                try {
                    withContext(ioDispatcher) { update() }
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    _state.value = _state.value.copy(error = SETTINGS_UPDATE_FAILED)
                }
            }
        }

        private companion object {
            const val SETTINGS_LOAD_FAILED = "settings_load_failed"
            const val SETTINGS_UPDATE_FAILED = "settings_update_failed"
            const val DELETE_REPORTS_FAILED = "delete_reports_failed"
        }
    }
