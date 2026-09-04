package com.insaner.fonecheck.ui.screens.vibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.classification.classifyVibrationResult
import com.insaner.fonecheck.ui.components.ButtonRow
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.DisclosureSection
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.ManualResultButtons
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ObservationReasonNote
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.toSemanticTone

@Composable
fun VibrationTestScreen(
    modifier: Modifier = Modifier,
    viewModel: VibrationTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    VibrationLifecycleEffect(onCancelVibration = viewModel::cancelVibration)

    TestScreenContent(modifier = modifier, liveStateUpdatedAtEpochMillis = state.capturedAt.toEpochMilli()) {
        if (state.haptic.readErrors.isNotEmpty()) {
            item {
                Note(
                    text = stringResource(R.string.vibration_capability_read_error),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                )
            }
        }
        if (state.playbackError) {
            item {
                Note(
                    text = stringResource(R.string.vibration_playback_error),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                )
            }
        }
        item {
            val classification = classifyVibrationResult(state.motor.result)
            DisclosureSection(
                label = stringResource(R.string.vibration_motor_title),
                summary = vibrationResultLabel(state.motor.result),
                tone = classification.toSemanticTone(),
                expanded = state.expandedSection == VibrationSection.MOTOR,
                onClick = { viewModel.toggleSection(VibrationSection.MOTOR) },
            ) {
                ObservationReasonNote(
                    classification = classification,
                    valueExplainsNotMeasuredState = state.motor.result == null,
                )
                MotorTestDetails(
                    state = state,
                    onShort = { viewModel.vibrateShort() },
                    onLong = { viewModel.vibrateLong() },
                    onPattern = { viewModel.vibratePattern() },
                    onStop = viewModel::cancelVibration,
                    onConfirmSuccess = { viewModel.reportFelt(true) },
                    onConfirmFailure = { viewModel.reportFelt(false) },
                    onSkip = viewModel::skipMotorConfirmation,
                )
            }
        }

        item {
            DisclosureSection(
                label = stringResource(R.string.vibration_haptic_title),
                summary =
                    stringResource(
                        when {
                            VibrationCapabilityRead.HARDWARE in state.haptic.readErrors ->
                                R.string.status_not_measured
                            state.haptic.hasVibrator -> R.string.conn_supported
                            else -> R.string.conn_not_supported
                        },
                    ),
                tone = SemanticTone.NEUTRAL,
                expanded = state.expandedSection == VibrationSection.HAPTIC,
                onClick = { viewModel.toggleSection(VibrationSection.HAPTIC) },
            ) {
                HapticDetails(state.haptic)
            }
        }
    }
}

@Composable
@Suppress("kotlin:S107") // Explicit motor controls keep each hardware action independently testable.
private fun MotorTestDetails(
    state: VibrationTestState,
    onShort: () -> Unit,
    onLong: () -> Unit,
    onPattern: () -> Unit,
    onStop: () -> Unit,
    onConfirmSuccess: () -> Unit,
    onConfirmFailure: () -> Unit,
    onSkip: () -> Unit,
) {
    val hardwareReadFailed = VibrationCapabilityRead.HARDWARE in state.haptic.readErrors
    val enabled = state.haptic.hasVibrator && !hardwareReadFailed

    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md)) {
        Note(text = stringResource(R.string.vibration_strength_warning))
        ButtonRow { buttonModifier ->
            VibrationPatternButton(
                label = stringResource(R.string.vibration_short),
                isActive = state.isPlaying && state.lastPattern == VibrationPattern.SHORT,
                enabled = enabled,
                onClick = onShort,
                modifier = buttonModifier,
            )
            VibrationPatternButton(
                label = stringResource(R.string.vibration_long),
                isActive = state.isPlaying && state.lastPattern == VibrationPattern.LONG,
                enabled = enabled,
                onClick = onLong,
                modifier = buttonModifier,
            )
            VibrationPatternButton(
                label = stringResource(R.string.vibration_pattern),
                isActive = state.isPlaying && state.lastPattern == VibrationPattern.PATTERN,
                enabled = enabled,
                onClick = onPattern,
                modifier = buttonModifier,
            )
        }
        SecondaryButton(
            label = stringResource(R.string.vibration_stop),
            onClick = onStop,
            enabled = state.isPlaying,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!enabled && !hardwareReadFailed) {
            Note(text = stringResource(R.string.vibration_unavailable))
        }
        Text(
            text = stringResource(R.string.vibration_did_you_feel),
            style = FonecheckTheme.type.rowLabel,
            color = FonecheckTheme.colors.textSecondary,
        )
        ManualResultButtons(
            problemLabel = stringResource(R.string.vibration_no),
            passLabel = stringResource(R.string.vibration_yes),
            onResult = { felt -> if (felt) onConfirmSuccess() else onConfirmFailure() },
            enabled = enabled,
        )
        SecondaryButton(
            label = stringResource(R.string.vibration_skip),
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        )
        Note(text = stringResource(R.string.vibration_accessibility_note))
    }
}

@Composable
private fun VibrationPatternButton(
    label: String,
    isActive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonModifier = modifier.semantics { selected = isActive }
    if (isActive) {
        PrimaryButton(
            label = label,
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
        )
    } else {
        SecondaryButton(
            label = label,
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
        )
    }
}

@Composable
private fun HapticDetails(haptic: HapticCapabilityState) {
    val readFailedLabel = stringResource(R.string.vibration_read_failed)
    val hardwareReadFailed = VibrationCapabilityRead.HARDWARE in haptic.readErrors
    Column {
        DataRow(
            label = stringResource(R.string.vibration_has_vibrator),
            value = if (hardwareReadFailed) null else yesNoLabel(haptic.hasVibrator),
            unavailableLabel = readFailedLabel,
        )
        DataRow(
            label = stringResource(R.string.vibration_amplitude_control),
            value =
                if (hardwareReadFailed || VibrationCapabilityRead.AMPLITUDE_CONTROL in haptic.readErrors) {
                    null
                } else {
                    stringResource(
                        if (haptic.hasAmplitudeControl) {
                            R.string.conn_supported
                        } else {
                            R.string.conn_not_supported
                        },
                    )
                },
            unavailableLabel = readFailedLabel,
        )
        HapticCapabilityList(
            label = stringResource(R.string.vibration_effects_supported),
            apiSupported = haptic.effectsApiSupported,
            readFailed = hardwareReadFailed || VibrationCapabilityRead.EFFECTS in haptic.readErrors,
            values = haptic.supportedEffects.map { vibrationEffectLabel(it) },
        )
        HapticCapabilityList(
            label = stringResource(R.string.vibration_primitives_supported),
            apiSupported = haptic.primitivesApiSupported,
            readFailed = hardwareReadFailed || VibrationCapabilityRead.PRIMITIVES in haptic.readErrors,
            values = haptic.supportedPrimitives.map { vibrationPrimitiveLabel(it) },
        )
        Note(text = stringResource(R.string.vibration_capability_note))
    }
}

@Composable
private fun HapticCapabilityList(
    label: String,
    apiSupported: Boolean,
    readFailed: Boolean,
    values: List<String>,
) {
    val emptyLabel = stringResource(R.string.vibration_none)
    LongValueRow(
        label = label,
        value =
            when {
                readFailed -> null
                apiSupported -> values.joinToString().ifEmpty { emptyLabel }
                else -> null
            },
        unavailableLabel =
            stringResource(
                if (readFailed) {
                    R.string.vibration_read_failed
                } else {
                    R.string.vibration_api_requires_android_11
                },
            ),
    )
}

@Composable
private fun vibrationResultLabel(result: VibrationMotorResult?): String =
    stringResource(
        when (result) {
            VibrationMotorResult.FELT -> R.string.vibration_felt
            VibrationMotorResult.NOT_FELT -> R.string.vibration_not_felt
            VibrationMotorResult.SKIPPED -> R.string.vibration_skipped
            null -> R.string.status_not_measured
        },
    )

internal fun vibrationResultTone(result: VibrationMotorResult?): SemanticTone =
    classifyVibrationResult(result).toSemanticTone()

@Composable
private fun vibrationEffectLabel(effect: VibrationEffectCode): String =
    stringResource(
        when (effect) {
            VibrationEffectCode.CLICK -> R.string.vibration_effect_click
            VibrationEffectCode.DOUBLE_CLICK -> R.string.vibration_effect_double_click
            VibrationEffectCode.HEAVY_CLICK -> R.string.vibration_effect_heavy_click
            VibrationEffectCode.TICK -> R.string.vibration_effect_tick
        },
    )

@Composable
private fun vibrationPrimitiveLabel(primitive: VibrationPrimitiveCode): String =
    stringResource(
        when (primitive) {
            VibrationPrimitiveCode.CLICK -> R.string.vibration_primitive_click
            VibrationPrimitiveCode.THUD -> R.string.vibration_primitive_thud
            VibrationPrimitiveCode.SPIN -> R.string.vibration_primitive_spin
            VibrationPrimitiveCode.QUICK_RISE -> R.string.vibration_primitive_quick_rise
            VibrationPrimitiveCode.SLOW_RISE -> R.string.vibration_primitive_slow_rise
            VibrationPrimitiveCode.QUICK_FALL -> R.string.vibration_primitive_quick_fall
            VibrationPrimitiveCode.TICK -> R.string.vibration_primitive_tick
            VibrationPrimitiveCode.LOW_TICK -> R.string.vibration_primitive_low_tick
        },
    )

@Composable
private fun yesNoLabel(value: Boolean): String = stringResource(if (value) R.string.status_yes else R.string.status_no)
