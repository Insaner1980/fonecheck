package com.insaner.fonecheck.ui.screens.vibration

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

enum class VibrationPattern(
    val durationMillis: Long,
) {
    SHORT(100L),
    LONG(500L),
    PATTERN(800L),
}

enum class VibrationEffectCode {
    CLICK,
    DOUBLE_CLICK,
    HEAVY_CLICK,
    TICK,
}

enum class VibrationPrimitiveCode {
    CLICK,
    THUD,
    SPIN,
    QUICK_RISE,
    SLOW_RISE,
    QUICK_FALL,
    TICK,
    LOW_TICK,
}

data class HapticCapabilityState(
    val hasVibrator: Boolean = false,
    val hasAmplitudeControl: Boolean = false,
    val effectsApiSupported: Boolean = false,
    val supportedEffects: List<VibrationEffectCode> = emptyList(),
    val primitivesApiSupported: Boolean = false,
    val supportedPrimitives: List<VibrationPrimitiveCode> = emptyList(),
) {
    val supportedEffectsCount: Int get() = supportedEffects.size
    val supportedPrimitivesCount: Int get() = supportedPrimitives.size
}

interface VibrationPlatform {
    val capabilities: HapticCapabilityState

    fun play(pattern: VibrationPattern)

    fun cancel()
}

class AndroidVibrationPlatform
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : VibrationPlatform {
        private val vibrator: Vibrator? = resolveVibrator(context)

        override val capabilities: HapticCapabilityState = readCapabilities()

        override fun play(pattern: VibrationPattern) {
            val currentVibrator = vibrator ?: return
            val effect =
                when (pattern) {
                    VibrationPattern.SHORT,
                    VibrationPattern.LONG,
                    ->
                        VibrationEffect.createOneShot(
                            pattern.durationMillis,
                            VibrationEffect.DEFAULT_AMPLITUDE,
                        )
                    VibrationPattern.PATTERN ->
                        VibrationEffect.createWaveform(longArrayOf(0L, 100L, 100L, 200L, 100L, 300L), -1)
                }
            runCatching { currentVibrator.vibrate(effect) }
        }

        override fun cancel() {
            runCatching { vibrator?.cancel() }
        }

        private fun readCapabilities(): HapticCapabilityState {
            val currentVibrator = vibrator ?: return HapticCapabilityState()
            val hasVibrator = runCatching { currentVibrator.hasVibrator() }.getOrDefault(false)
            if (!hasVibrator) return HapticCapabilityState()
            val effectsApiSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            val primitivesApiSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            return HapticCapabilityState(
                hasVibrator = true,
                hasAmplitudeControl = runCatching { currentVibrator.hasAmplitudeControl() }.getOrDefault(false),
                effectsApiSupported = effectsApiSupported,
                supportedEffects =
                    if (effectsApiSupported) {
                        readSupportedEffects(currentVibrator)
                    } else {
                        emptyList()
                    },
                primitivesApiSupported = primitivesApiSupported,
                supportedPrimitives =
                    if (primitivesApiSupported) {
                        readSupportedPrimitives(currentVibrator)
                    } else {
                        emptyList()
                    },
            )
        }

        @RequiresApi(Build.VERSION_CODES.R)
        private fun readSupportedEffects(vibrator: Vibrator): List<VibrationEffectCode> {
            val support =
                runCatching {
                    vibrator.areEffectsSupported(
                        VibrationEffect.EFFECT_CLICK,
                        VibrationEffect.EFFECT_DOUBLE_CLICK,
                        VibrationEffect.EFFECT_HEAVY_CLICK,
                        VibrationEffect.EFFECT_TICK,
                    )
                }.getOrNull()
            return support
                ?.let {
                    VibrationCapabilityPolicy.supportedEffects(
                        results = it,
                        supportedValue = Vibrator.VIBRATION_EFFECT_SUPPORT_YES,
                    )
                }.orEmpty()
        }

        @SuppressLint("InlinedApi")
        @RequiresApi(Build.VERSION_CODES.R)
        private fun readSupportedPrimitives(vibrator: Vibrator): List<VibrationPrimitiveCode> {
            val support =
                runCatching {
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
                }.getOrNull()
            return support?.let(VibrationCapabilityPolicy::supportedPrimitives).orEmpty()
        }

        private fun resolveVibrator(context: Context): Vibrator? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                defaultVibrator(context)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

        @RequiresApi(Build.VERSION_CODES.S)
        private fun defaultVibrator(context: Context): Vibrator? =
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    }
