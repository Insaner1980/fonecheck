package com.insaner.fonecheck.ui.screens.vibration

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// ── State classes ────────────────────────────────────────────────────────────────

data class MotorTestState(
    val lastTestResult: Boolean? = null, // null = not tested, true = felt, false = not felt
)

data class HapticCapabilityState(
    val hasVibrator: Boolean = false,
    val hasAmplitudeControl: Boolean = false,
    val supportedEffectsCount: Int = 0,
    val supportedPrimitivesCount: Int = 0,
    val supportedEffects: List<String> = emptyList(),
    val supportedPrimitives: List<String> = emptyList(),
)

enum class VibrationSection {
    MOTOR,
    HAPTIC,
}

data class VibrationTestState(
    val motor: MotorTestState = MotorTestState(),
    val haptic: HapticCapabilityState = HapticCapabilityState(),
    val expandedSection: VibrationSection? = null,
)

// ── ViewModel ────────────────────────────────────────────────────────────────────

@HiltViewModel
class VibrationTestViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val context: Context get() = getApplication()

        private val vibrator: Vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(VibratorManager::class.java)
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

        private val _state = MutableStateFlow(VibrationTestState())
        val state: StateFlow<VibrationTestState> = _state

        init {
            detectCapabilities()
        }

        fun toggleSection(section: VibrationSection) {
            val current = _state.value.expandedSection
            _state.value =
                _state.value.copy(
                    expandedSection = if (current == section) null else section,
                )
        }

        // ── Motor tests ──────────────────────────────────────────────────────────────

        fun vibrateShort() {
            vibrate(100L)
        }

        fun vibrateLong() {
            vibrate(500L)
        }

        fun vibratePattern() {
            val pattern = longArrayOf(0, 100, 100, 200, 100, 300)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }

        fun reportFelt(felt: Boolean) {
            _state.value =
                _state.value.copy(
                    motor = MotorTestState(lastTestResult = felt),
                )
        }

        // ── Capabilities ─────────────────────────────────────────────────────────────

        private fun detectCapabilities() {
            val hasVibrator = vibrator.hasVibrator()
            val hasAmplitude = vibrator.hasAmplitudeControl()

            val effects = mutableListOf<String>()
            val primitives = mutableListOf<String>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val effectNames =
                    listOf(
                        "Click",
                        "Double Click",
                        "Heavy Click",
                        "Tick",
                    )
                val supportedResults =
                    vibrator.areEffectsSupported(
                        VibrationEffect.EFFECT_CLICK,
                        VibrationEffect.EFFECT_DOUBLE_CLICK,
                        VibrationEffect.EFFECT_HEAVY_CLICK,
                        VibrationEffect.EFFECT_TICK,
                    )
                effectNames.forEachIndexed { index, name ->
                    if (supportedResults[index] == Vibrator.VIBRATION_EFFECT_SUPPORT_YES) {
                        effects.add(name)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val primitiveNames =
                        listOf(
                            "Click",
                            "Thud",
                            "Spin",
                            "Quick Rise",
                            "Slow Rise",
                            "Quick Fall",
                            "Tick",
                            "Low Tick",
                        )
                    val primResults =
                        vibrator.arePrimitivesSupported(
                            VibrationEffect.Composition.PRIMITIVE_CLICK,
                            VibrationEffect.Composition.PRIMITIVE_THUD,
                            VibrationEffect.Composition.PRIMITIVE_SPIN,
                            VibrationEffect.Composition.PRIMITIVE_QUICK_RISE,
                            VibrationEffect.Composition.PRIMITIVE_SLOW_RISE,
                            VibrationEffect.Composition.PRIMITIVE_QUICK_FALL,
                            VibrationEffect.Composition.PRIMITIVE_TICK,
                            VibrationEffect.Composition.PRIMITIVE_LOW_TICK,
                        )
                    primitiveNames.forEachIndexed { index, name ->
                        if (primResults[index]) {
                            primitives.add(name)
                        }
                    }
                }
            }

            _state.value =
                _state.value.copy(
                    haptic =
                        HapticCapabilityState(
                            hasVibrator = hasVibrator,
                            hasAmplitudeControl = hasAmplitude,
                            supportedEffectsCount = effects.size,
                            supportedPrimitivesCount = primitives.size,
                            supportedEffects = effects,
                            supportedPrimitives = primitives,
                        ),
                )
        }

        private fun vibrate(durationMs: Long) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE),
            )
        }
    }
