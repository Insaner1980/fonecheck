package com.insaner.fonecheck.ui.screens.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DetailInfoRow
import com.insaner.fonecheck.ui.components.SectionBox
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.TestSectionCard
import com.insaner.fonecheck.ui.theme.Blue400
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Yellow400

@Composable
fun ButtonTestScreen(
    modifier: Modifier = Modifier,
    viewModel: ButtonTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ButtonLifecycleEffect(onStopTest = viewModel::stopTest)

    TestScreenContent(modifier = modifier) {
        item {
            TestSectionCard(
                icon = stringResource(R.string.button_test_icon),
                title = stringResource(R.string.button_test_title),
                statusText = buttonStatusLabel(state),
                statusColor =
                    when (state.phase) {
                        ButtonTestPhase.COMPLETED -> Green400
                        ButtonTestPhase.TIMED_OUT -> Yellow400
                        ButtonTestPhase.RUNNING -> Blue400
                        ButtonTestPhase.IDLE,
                        ButtonTestPhase.SKIPPED,
                        -> Neutral500
                    },
                isExpanded = true,
                onClick = {},
            ) {
                ButtonTestDetails(
                    state = state,
                    onStart = viewModel::startTest,
                    onStop = viewModel::stopTest,
                    onRetry = viewModel::retry,
                    onSkip = viewModel::skip,
                    onReset = viewModel::reset,
                )
            }
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
                listOf(state.volumeUpDetected, state.volumeDownDetected).count { it },
            )
        ButtonTestPhase.COMPLETED -> stringResource(R.string.button_status_complete)
        ButtonTestPhase.TIMED_OUT -> stringResource(R.string.button_status_timed_out)
        ButtonTestPhase.SKIPPED -> stringResource(R.string.button_status_skipped)
    }

@Composable
private fun ButtonTestDetails(
    state: ButtonTestState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    onReset: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionBox {
            DetailInfoRow(
                stringResource(R.string.button_volume_up),
                buttonDetectionLabel(state.volumeUpDetected),
                valueColor = if (state.volumeUpDetected) Green400 else Neutral500,
            )
        }
        SectionBox {
            DetailInfoRow(
                stringResource(R.string.button_volume_down),
                buttonDetectionLabel(state.volumeDownDetected),
                valueColor = if (state.volumeDownDetected) Green400 else Neutral500,
            )
        }
        SectionBox {
            DetailInfoRow(
                stringResource(R.string.button_power),
                stringResource(R.string.run_all_status_unavailable),
                valueColor = Neutral500,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.button_power_note),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
            )
        }

        if (state.phase == ButtonTestPhase.TIMED_OUT) {
            Text(
                text = stringResource(R.string.button_timeout_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (state.phase) {
                ButtonTestPhase.IDLE,
                ButtonTestPhase.COMPLETED,
                ButtonTestPhase.SKIPPED,
                ->
                    Button(
                        onClick = onStart,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue400),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.button_start_test))
                    }

                ButtonTestPhase.RUNNING -> {
                    OutlinedButton(
                        onClick = onStop,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.button_stop))
                    }
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.button_skip))
                    }
                }

                ButtonTestPhase.TIMED_OUT ->
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue400),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(stringResource(R.string.button_retry))
                    }
            }
            if (state.phase != ButtonTestPhase.RUNNING) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.button_reset))
                }
            }
        }
    }
}

@Composable
private fun buttonDetectionLabel(detected: Boolean): String =
    stringResource(if (detected) R.string.button_detected else R.string.button_not_detected)
