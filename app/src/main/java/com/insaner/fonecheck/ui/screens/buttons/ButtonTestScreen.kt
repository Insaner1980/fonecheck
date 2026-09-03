package com.insaner.fonecheck.ui.screens.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.classification.classifyButtonTest
import com.insaner.fonecheck.ui.components.ButtonRow
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ObservationReasonNote
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.toSemanticTone

@Composable
fun ButtonTestScreen(
    modifier: Modifier = Modifier,
    viewModel: ButtonTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val liveStateUpdatedAtEpochMillis = remember(state) { System.currentTimeMillis() }
    ButtonLifecycleEffect(onStopTest = viewModel::stopTest)

    TestScreenContent(modifier = modifier, liveStateUpdatedAtEpochMillis = liveStateUpdatedAtEpochMillis) {
        item {
            val classification = classifyButtonTest(state.phase)
            Column {
                SectionHeader(label = stringResource(R.string.button_test_title))
                DataRow(
                    label = stringResource(R.string.button_status_label),
                    value = buttonStatusLabel(state),
                    tone = classification.toSemanticTone(),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    showDivider = classification.reason == null,
                )
                ObservationReasonNote(classification)
                if (classification.reason != null) HairlineRule()
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
        ButtonTestPhase.IDLE -> stringResource(R.string.status_not_measured)
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

internal fun buttonStatusTone(phase: ButtonTestPhase): SemanticTone = classifyButtonTest(phase).toSemanticTone()

internal fun buttonResetAvailable(phase: ButtonTestPhase): Boolean =
    phase == ButtonTestPhase.COMPLETED ||
        phase == ButtonTestPhase.TIMED_OUT ||
        phase == ButtonTestPhase.SKIPPED

@Composable
private fun ButtonTestActions(
    phase: ButtonTestPhase,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    onReset: () -> Unit,
) {
    ButtonRow { buttonModifier ->
        when (phase) {
            ButtonTestPhase.IDLE,
            ButtonTestPhase.COMPLETED,
            ButtonTestPhase.SKIPPED,
            ->
                PrimaryButton(
                    label = stringResource(R.string.button_start_test),
                    onClick = onStart,
                    modifier = buttonModifier,
                )

            ButtonTestPhase.RUNNING -> {
                SecondaryButton(
                    label = stringResource(R.string.button_stop),
                    onClick = onStop,
                    modifier = buttonModifier,
                )
                SecondaryButton(
                    label = stringResource(R.string.button_skip),
                    onClick = onSkip,
                    modifier = buttonModifier,
                )
            }

            ButtonTestPhase.TIMED_OUT ->
                PrimaryButton(
                    label = stringResource(R.string.button_retry),
                    onClick = onRetry,
                    modifier = buttonModifier,
                )
        }
        if (buttonResetAvailable(phase)) {
            SecondaryButton(
                label = stringResource(R.string.button_reset),
                onClick = onReset,
                modifier = buttonModifier,
            )
        }
    }
}

@Composable
private fun buttonDetectionLabel(detected: Boolean): String =
    stringResource(if (detected) R.string.button_detected else R.string.status_not_measured)

private const val REQUIRED_BUTTON_COUNT = 2
