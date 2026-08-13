package com.insaner.fonecheck.ui.screens.vibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DetailInfoRow
import com.insaner.fonecheck.ui.components.SectionBox
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.TestSectionCard
import com.insaner.fonecheck.ui.theme.Blue400
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Red400

@Composable
fun VibrationTestScreen(
    modifier: Modifier = Modifier,
    viewModel: VibrationTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    VibrationLifecycleEffect(onCancelVibration = viewModel::cancelVibration)

    TestScreenContent(modifier = modifier) {
        item {
            TestSectionCard(
                icon = stringResource(R.string.vibration_motor_icon),
                title = stringResource(R.string.vibration_motor_title),
                statusText = vibrationResultLabel(state.motor.result),
                statusColor =
                    when (state.motor.result) {
                        VibrationMotorResult.FELT -> Green400
                        VibrationMotorResult.NOT_FELT -> Red400
                        VibrationMotorResult.SKIPPED,
                        null,
                        -> Neutral500
                    },
                isExpanded = state.expandedSection == VibrationSection.MOTOR,
                onClick = { viewModel.toggleSection(VibrationSection.MOTOR) },
            ) {
                MotorTestDetails(
                    state = state,
                    onShort = viewModel::vibrateShort,
                    onLong = viewModel::vibrateLong,
                    onPattern = viewModel::vibratePattern,
                    onStop = viewModel::cancelVibration,
                    onConfirmSuccess = { viewModel.reportFelt(true) },
                    onConfirmFailure = { viewModel.reportFelt(false) },
                    onSkip = viewModel::skipMotorConfirmation,
                )
            }
        }

        item {
            TestSectionCard(
                icon = stringResource(R.string.vibration_haptic_icon),
                title = stringResource(R.string.vibration_haptic_title),
                statusText =
                    if (state.haptic.hasVibrator) {
                        pluralStringResource(
                            R.plurals.vibration_effect_count,
                            state.haptic.supportedEffectsCount,
                            state.haptic.supportedEffectsCount,
                        )
                    } else {
                        stringResource(R.string.vibration_none)
                    },
                statusColor = if (state.haptic.hasVibrator) Blue400 else Neutral500,
                isExpanded = state.expandedSection == VibrationSection.HAPTIC,
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
    val enabled = state.haptic.hasVibrator
    SectionBox {
        Text(
            text = stringResource(R.string.vibration_strength_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VibrationButton(stringResource(R.string.vibration_short), enabled, onShort, Modifier.weight(1f))
            VibrationButton(stringResource(R.string.vibration_long), enabled, onLong, Modifier.weight(1f))
            VibrationButton(stringResource(R.string.vibration_pattern), enabled, onPattern, Modifier.weight(1f))
        }
        OutlinedButton(
            onClick = onStop,
            enabled = state.isPlaying,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(stringResource(R.string.vibration_stop))
        }
        if (!enabled) {
            Text(
                text = stringResource(R.string.vibration_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.vibration_did_you_feel),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onConfirmSuccess,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.vibration_yes))
            }
            OutlinedButton(
                onClick = onConfirmFailure,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.vibration_no))
            }
        }
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(stringResource(R.string.vibration_skip))
        }
        Text(
            text = stringResource(R.string.vibration_accessibility_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun VibrationButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Blue400),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(label)
    }
}

@Composable
private fun HapticDetails(haptic: HapticCapabilityState) {
    SectionBox {
        DetailInfoRow(
            stringResource(R.string.vibration_has_vibrator),
            if (haptic.hasVibrator) stringResource(R.string.status_yes) else stringResource(R.string.status_no),
            valueColor = if (haptic.hasVibrator) Green400 else Red400,
        )
        DetailInfoRow(
            stringResource(R.string.vibration_amplitude_control),
            if (haptic.hasAmplitudeControl) {
                stringResource(R.string.conn_supported)
            } else {
                stringResource(R.string.conn_not_supported)
            },
            valueColor = if (haptic.hasAmplitudeControl) Green400 else Neutral500,
        )
        DetailInfoRow(
            stringResource(R.string.vibration_effects_supported),
            if (haptic.effectsApiSupported) {
                haptic.supportedEffectsCount.toString()
            } else {
                stringResource(R.string.vibration_api_requires_android_11)
            },
        )
        haptic.supportedEffects.forEach { effect ->
            DetailInfoRow("", vibrationEffectLabel(effect))
        }
        DetailInfoRow(
            stringResource(R.string.vibration_primitives_supported),
            if (haptic.primitivesApiSupported) {
                haptic.supportedPrimitivesCount.toString()
            } else {
                stringResource(R.string.vibration_primitives_require_android_12)
            },
        )
        haptic.supportedPrimitives.forEach { primitive ->
            DetailInfoRow("", vibrationPrimitiveLabel(primitive))
        }
        Text(
            text = stringResource(R.string.vibration_capability_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun vibrationResultLabel(result: VibrationMotorResult?): String =
    stringResource(
        when (result) {
            VibrationMotorResult.FELT -> R.string.vibration_felt
            VibrationMotorResult.NOT_FELT -> R.string.vibration_not_felt
            VibrationMotorResult.SKIPPED -> R.string.vibration_skipped
            null -> R.string.vibration_ready
        },
    )

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
