package com.insaner.fonecheck.ui.screens.buttons

import android.app.Application
import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// ── State classes ────────────────────────────────────────────────────────────────

data class ButtonTestState(
    val volumeUpDetected: Boolean = false,
    val volumeDownDetected: Boolean = false,
    val isTesting: Boolean = false,
    val lastVolume: Int = -1,
)

// ── ViewModel ────────────────────────────────────────────────────────────────────

@HiltViewModel
class ButtonTestViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val context: Context get() = getApplication()
        private val audioManager = context.getSystemService(AudioManager::class.java)

        private val _state = MutableStateFlow(ButtonTestState())
        val state: StateFlow<ButtonTestState> = _state

        fun startTest() {
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            _state.value =
                ButtonTestState(
                    isTesting = true,
                    lastVolume = currentVolume,
                )
        }

        fun checkVolumeChange() {
            if (!_state.value.isTesting) return

            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val lastVolume = _state.value.lastVolume

            if (currentVolume != lastVolume) {
                if (currentVolume > lastVolume) {
                    _state.value =
                        _state.value.copy(
                            volumeUpDetected = true,
                            lastVolume = currentVolume,
                        )
                } else {
                    _state.value =
                        _state.value.copy(
                            volumeDownDetected = true,
                            lastVolume = currentVolume,
                        )
                }
            }
        }

        fun reset() {
            _state.value = ButtonTestState()
        }
    }
