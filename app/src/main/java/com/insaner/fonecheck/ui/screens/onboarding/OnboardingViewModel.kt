package com.insaner.fonecheck.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.preferences.AppPreferencesRepository
import com.insaner.fonecheck.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class OnboardingPage(
    @StringRes val titleResId: Int,
    @StringRes val bodyResId: Int,
) {
    WELCOME(R.string.onboarding_welcome_title, R.string.onboarding_welcome_body),
    TESTING(R.string.onboarding_testing_title, R.string.onboarding_testing_body),
    PRIVACY(R.string.onboarding_privacy_title, R.string.onboarding_privacy_body),
    PERMISSIONS(R.string.onboarding_permissions_title, R.string.onboarding_permissions_body),
    REPORTS(R.string.onboarding_reports_title, R.string.onboarding_reports_body),
    READY(R.string.onboarding_ready_title, R.string.onboarding_ready_body),
}

data class OnboardingState(
    val pageIndex: Int = 0,
    val isSaving: Boolean = false,
    val finished: Boolean = false,
    val saveFailed: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val preferencesRepository: AppPreferencesRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _state = MutableStateFlow(OnboardingState())
        val state: StateFlow<OnboardingState> = _state.asStateFlow()

        fun nextPage() {
            _state.value =
                _state.value.copy(
                    pageIndex = (_state.value.pageIndex + 1).coerceAtMost(OnboardingPage.entries.lastIndex),
                )
        }

        fun previousPage() {
            _state.value = _state.value.copy(pageIndex = (_state.value.pageIndex - 1).coerceAtLeast(0))
        }

        fun completeOnboarding() {
            if (_state.value.isSaving) return
            _state.value = _state.value.copy(isSaving = true, saveFailed = false)
            viewModelScope.launch {
                try {
                    withContext(ioDispatcher) { preferencesRepository.setOnboardingComplete(true) }
                    _state.value = _state.value.copy(isSaving = false, finished = true)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    _state.value = _state.value.copy(isSaving = false, saveFailed = true)
                }
            }
        }

        fun consumeFinished() {
            _state.value = _state.value.copy(finished = false)
        }
    }
