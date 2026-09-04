package com.insaner.fonecheck.ui.screens.vibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class VibrationMotorResult {
    FELT,
    NOT_FELT,
    SKIPPED,
}

data class MotorTestState(
    val result: VibrationMotorResult? = null,
)

enum class VibrationSection {
    MOTOR,
    HAPTIC,
}

data class VibrationTestState(
    val motor: MotorTestState = MotorTestState(),
    val haptic: HapticCapabilityState = HapticCapabilityState(),
    val expandedSection: VibrationSection? = null,
    val isPlaying: Boolean = false,
    val lastPattern: VibrationPattern? = null,
    val playbackError: Boolean = false,
)

@HiltViewModel
class VibrationTestViewModel
    @Inject
    constructor(
        private val platform: VibrationPlatform,
    ) : ViewModel() {
        private val _state = MutableStateFlow(VibrationTestState(haptic = platform.capabilities))
        val state: StateFlow<VibrationTestState> = _state.asStateFlow()

        private var ownsVibration = false
        private var completionJob: Job? = null

        fun toggleSection(section: VibrationSection) {
            val current = _state.value.expandedSection
            _state.value =
                _state.value.copy(
                    expandedSection = if (current == section) null else section,
                )
        }

        fun vibrateShort(): Boolean = play(VibrationPattern.SHORT)

        fun vibrateLong(): Boolean = play(VibrationPattern.LONG)

        fun vibratePattern(): Boolean = play(VibrationPattern.PATTERN)

        fun cancelVibration() {
            completionJob?.cancel()
            completionJob = null
            if (!ownsVibration) return
            ownsVibration = false
            platform.cancel()
            _state.value = _state.value.copy(isPlaying = false)
        }

        fun reportFelt(felt: Boolean) {
            cancelVibration()
            _state.value =
                _state.value.copy(
                    motor =
                        MotorTestState(
                            result = if (felt) VibrationMotorResult.FELT else VibrationMotorResult.NOT_FELT,
                        ),
                    playbackError = false,
                )
        }

        fun skipMotorConfirmation() {
            cancelVibration()
            _state.value =
                _state.value.copy(
                    motor = MotorTestState(result = VibrationMotorResult.SKIPPED),
                    playbackError = false,
                )
        }

        override fun onCleared() {
            cancelVibration()
        }

        private fun play(pattern: VibrationPattern): Boolean {
            if (!_state.value.haptic.hasVibrator) return false
            cancelVibration()
            if (!platform.play(pattern)) {
                platform.cancel()
                _state.value =
                    _state.value.copy(
                        isPlaying = false,
                        lastPattern = pattern,
                        playbackError = true,
                    )
                return false
            }
            ownsVibration = true
            _state.value =
                _state.value.copy(
                    isPlaying = true,
                    lastPattern = pattern,
                    playbackError = false,
                )
            completionJob =
                viewModelScope.launch {
                    delay(pattern.durationMillis)
                    completionJob = null
                    ownsVibration = false
                    _state.value = _state.value.copy(isPlaying = false)
                }
            return true
        }
    }
