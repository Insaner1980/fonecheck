package com.insaner.fonecheck.ui.screens.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone

@Composable
fun ButtonTestScreen(
    modifier: Modifier = Modifier,
    viewModel: ButtonTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ButtonLifecycleEffect(onStopTest = viewModel::stopTest)

    TestScreenContent(modifier = modifier) {
        item {
            Column {
                SectionHeader(label = stringResource(R.string.button_test_title))
                DataRow(
                    label = stringResource(R.string.button_status_label),
                    value = buttonStatusLabel(state),
                    tone = buttonStatusTone(state.phase),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                DataRow(
                    label = stringResource(R.string.button_volume_up),
                    value = buttonDetectionLabel(state.volumeUpDetected),
                    tone = if (state.volumeUpDetected) SemanticTone.PASS else SemanticTone.NEUTRAL,
                )
                DataRow(
                    label = stringResource(R.string.button_volume_down),
                    value = buttonDetectionLabel(state.volumeDownDetected),
                    tone = if (state.volumeDownDetected) SemanticTone.PASS else SemanticTone.NEUTRAL,
                )
                DataRow(
                    label = stringResource(R.string.button_power),
                    value = null,
                    unavailableLabel = stringResource(R.string.button_power_unavailable),
                )
                Note(text = stringResource(R.string.button_power_note))
            }
        }

        if (state.phase == ButtonTestPhase.TIMED_OUT) {
            item {
                Note(text = stringResource(R.string.button_timeout_hint))
            }
        }

        item {
            ButtonTestActions(
                phase = state.phase,
                onStart = viewModel::startTest,
                onStop = viewModel::stopTest,
                onRetry = viewModel::retry,
                onSkip = viewModel::skip,
                onReset = viewModel::reset,
            )
        }
    }
}

@Composable
private fun buttonStatusLabel(state: ButtonTestState): String =
    when (state.phase) {
        ButtonTestPhase.IDLE -> stringResource(R.string.button_status_ready)
        ButtonTestPhase.RUNNING ->
            stringResource(
                R.string.button_status_progress,
                uiNumber(listOf(state.volumeUpDetected, state.volumeDownDetected).count { it }),
                uiNumber(REQUIRED_BUTTON_COUNT),
            )
        ButtonTestPhase.COMPLETED -> stringResource(R.string.button_status_complete)
        ButtonTestPhase.TIMED_OUT -> stringResource(R.string.button_status_timed_out)
        ButtonTestPhase.SKIPPED -> stringResource(R.string.button_status_skipped)
    }

internal fun buttonStatusTone(phase: ButtonTestPhase): SemanticTone =
    when (phase) {
        ButtonTestPhase.COMPLETED -> SemanticTone.PASS
        ButtonTestPhase.TIMED_OUT -> SemanticTone.ATTENTION
        ButtonTestPhase.IDLE,
        ButtonTestPhase.RUNNING,
        ButtonTestPhase.SKIPPED,
        -> SemanticTone.NEUTRAL
    }

@Composable
private fun ButtonTestActions(
    phase: ButtonTestPhase,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
    ) {
        when (phase) {
            ButtonTestPhase.IDLE,
            ButtonTestPhase.COMPLETED,
            ButtonTestPhase.SKIPPED,
            ->
                PrimaryButton(
                    label = stringResource(R.string.button_start_test),
                    onClick = onStart,
                    modifier = Modifier.weight(1f),
                )

            ButtonTestPhase.RUNNING -> {
                SecondaryButton(
                    label = stringResource(R.string.button_stop),
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                )
                SecondaryButton(
                    label = stringResource(R.string.button_skip),
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                )
            }

            ButtonTestPhase.TIMED_OUT ->
                PrimaryButton(
                    label = stringResource(R.string.button_retry),
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                )
        }
        if (phase != ButtonTestPhase.RUNNING) {
            SecondaryButton(
                label = stringResource(R.string.button_reset),
                onClick = onReset,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun buttonDetectionLabel(detected: Boolean): String =
    stringResource(if (detected) R.string.button_detected else R.string.button_not_detected)

private const val REQUIRED_BUTTON_COUNT = 2
