package com.insaner.fonecheck.ui.screens.buttons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class ButtonTestPhase {
    IDLE,
    RUNNING,
    COMPLETED,
    TIMED_OUT,
    SKIPPED,
}

data class ButtonTestState(
    val volumeUpDetected: Boolean = false,
    val volumeDownDetected: Boolean = false,
    val phase: ButtonTestPhase = ButtonTestPhase.IDLE,
) {
    val isTesting: Boolean get() = phase == ButtonTestPhase.RUNNING
}

@HiltViewModel
class ButtonTestViewModel
    @Inject
    constructor(
        private val eventSource: VolumeButtonEventSource,
    ) : ViewModel() {
        private val _state = MutableStateFlow(ButtonTestState())
        val state: StateFlow<ButtonTestState> = _state.asStateFlow()

        private var testJob: Job? = null

        fun startTest() {
            testJob?.cancel()
            _state.value = ButtonTestState(phase = ButtonTestPhase.RUNNING)
            testJob =
                viewModelScope.launch {
                    val completed =
                        withTimeoutOrNull(TEST_TIMEOUT_MILLIS) {
                            eventSource.events.first { direction ->
                                recordDirection(direction)
                                _state.value.run { volumeUpDetected && volumeDownDetected }
                            }
                        } != null
                    if (_state.value.phase == ButtonTestPhase.RUNNING) {
                        _state.value =
                            _state.value.copy(
                                phase =
                                    if (completed) {
                                        ButtonTestPhase.COMPLETED
                                    } else {
                                        ButtonTestPhase.TIMED_OUT
                                    },
                            )
                    }
                    testJob = null
                }
        }

        fun retry() {
            startTest()
        }

        fun stopTest() {
            testJob?.cancel()
            testJob = null
            if (_state.value.phase == ButtonTestPhase.RUNNING) {
                _state.value = _state.value.copy(phase = ButtonTestPhase.IDLE)
            }
        }

        fun skip() {
            testJob?.cancel()
            testJob = null
            _state.value = _state.value.copy(phase = ButtonTestPhase.SKIPPED)
        }

        fun reset() {
            testJob?.cancel()
            testJob = null
            _state.value = ButtonTestState()
        }

        override fun onCleared() {
            stopTest()
            super.onCleared()
        }

        private fun recordDirection(direction: VolumeButtonDirection) {
            _state.value =
                when (direction) {
                    VolumeButtonDirection.UP -> _state.value.copy(volumeUpDetected = true)
                    VolumeButtonDirection.DOWN -> _state.value.copy(volumeDownDetected = true)
                }
        }

        companion object {
            const val TEST_TIMEOUT_MILLIS = 15_000L
        }
    }
