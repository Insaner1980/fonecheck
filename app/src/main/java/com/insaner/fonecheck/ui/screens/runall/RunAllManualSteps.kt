package com.insaner.fonecheck.ui.screens.runall

import androidx.annotation.StringRes
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.SectionBox
import com.insaner.fonecheck.ui.components.StatusBadge
import com.insaner.fonecheck.ui.screens.biometrics.AuthResult
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestState
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestPhase
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestState
import com.insaner.fonecheck.ui.screens.camera.CameraTestState
import com.insaner.fonecheck.ui.screens.display.DisplayPattern
import com.insaner.fonecheck.ui.screens.display.displayPatternBackground
import com.insaner.fonecheck.ui.screens.sensor.SensorTestState
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral400
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400

internal val displayTestPatterns = DisplayPattern.entries

data class PermissionPrompt(
    val state: PermissionState,
    val rationale: String,
    val onRequest: () -> Unit,
    val onOpenSettings: () -> Unit,
)

@Composable
fun FullCheckPreflightScreen(
    selections: RunAllSelections,
    onSelectionsChange: (RunAllSelections) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.run_all_preflight_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.run_all_preflight_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SectionBox {
            PreflightDisclosure(R.string.run_all_preflight_interactions)
            PreflightDisclosure(R.string.run_all_preflight_audio_vibration)
            PreflightDisclosure(R.string.run_all_preflight_storage)
            PreflightDisclosure(R.string.run_all_preflight_permissions)
            PreflightDisclosure(R.string.run_all_preflight_unsupported)
            PreflightDisclosure(R.string.run_all_preflight_local_report)
            PreflightDisclosure(R.string.run_all_preflight_no_network)
        }
        Text(
            text = stringResource(R.string.run_all_preflight_choices_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        PreflightChoiceRow(
            label = stringResource(R.string.run_all_preflight_speaker_option),
            checked = selections.includeSpeaker,
            onCheckedChange = {
                onSelectionsChange(selections.copy(includeSpeaker = it))
            },
        )
        PreflightChoiceRow(
            label = stringResource(R.string.run_all_preflight_microphone_option),
            checked = selections.includeMicrophone,
            onCheckedChange = {
                onSelectionsChange(selections.copy(includeMicrophone = it))
            },
        )
        PreflightChoiceRow(
            label = stringResource(R.string.run_all_preflight_camera_option),
            checked = selections.includeCamera,
            onCheckedChange = {
                onSelectionsChange(selections.copy(includeCamera = it))
            },
        )
        PreflightChoiceRow(
            label = stringResource(R.string.run_all_preflight_storage_option),
            checked = selections.includeStorageBenchmark,
            onCheckedChange = {
                onSelectionsChange(selections.copy(includeStorageBenchmark = it))
            },
        )
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(stringResource(R.string.run_all_preflight_start))
        }
    }
}

@Composable
private fun PreflightDisclosure(@StringRes textResId: Int) {
    Text(
        text = stringResource(textResId),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PreflightChoiceRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                ).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
fun PermissionReviewScreen(
    prompts: List<PermissionPrompt>,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.run_all_preparing_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.run_all_preparing_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        prompts.forEach { prompt ->
            PermissionStatusCard(
                state = prompt.state,
                rationale = prompt.rationale,
                onRequest = prompt.onRequest,
                onOpenSettings = prompt.onOpenSettings,
            )
        }
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(stringResource(R.string.run_all_permissions_continue))
        }
    }
}

@Composable
fun AutomaticCheckScreen(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(56.dp))
        Text(
            text = title,
            modifier = Modifier.padding(top = 24.dp),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            modifier = Modifier.padding(top = 10.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        actionLabel?.let { label ->
            OutlinedButton(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            ) {
                Text(label)
            }
        }
    }
}

@Composable
fun DisplayCheckStep(
    colorIndex: Int,
    progress: RunAllProgress,
    onNextColor: () -> Unit,
    onResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLastColor = colorIndex == displayTestPatterns.lastIndex
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .displayPatternBackground(displayTestPatterns[colorIndex]),
    ) {
        Surface(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ManualProgress(progress)
                Text(
                    text = stringResource(R.string.run_all_display_title),
                    modifier = Modifier.padding(top = 14.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text =
                        if (isLastColor) {
                            stringResource(R.string.run_all_display_question)
                        } else {
                            stringResource(R.string.run_all_display_description)
                        },
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                if (isLastColor) {
                    ConfirmationButtons(
                        positiveText = stringResource(R.string.run_all_looks_good),
                        negativeText = stringResource(R.string.run_all_found_problem),
                        negativeColor = Red400,
                        onPositive = { onResult(true) },
                        onNegative = { onResult(false) },
                    )
                } else {
                    Button(
                        onClick = onNextColor,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.run_all_next_color))
                    }
                }
            }
        }
    }
}

@Composable
fun AudioCheckStep(
    isPlaying: Boolean,
    progress: RunAllProgress,
    onPlayAgain: () -> Unit,
    onResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_audio_title),
        description = stringResource(R.string.run_all_audio_description),
    ) {
        OutlinedButton(
            onClick = onPlayAgain,
            enabled = !isPlaying,
        ) {
            Text(stringResource(R.string.run_all_play_again))
        }
        Spacer(modifier = Modifier.height(20.dp))
        ConfirmationButtons(
            positiveText = stringResource(R.string.run_all_heard_tone),
            negativeText = stringResource(R.string.run_all_no_tone),
            onPositive = { onResult(true) },
            onNegative = { onResult(false) },
        )
    }
}

@Composable
fun CameraCheckStep(
    previewView: PreviewView,
    state: CameraTestState,
    progress: RunAllProgress,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_camera_title),
        description = stringResource(R.string.run_all_camera_description),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(260.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            shape = RoundedCornerShape(20.dp),
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (state.lastCapture == null && state.error == null) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun SensorCheckStep(
    state: SensorTestState,
    progress: RunAllProgress,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_sensor_title),
        description = stringResource(R.string.run_all_sensor_description),
    ) {
        LinearProgressIndicator(
            progress = { state.challenge.progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onSkip) {
            Text(stringResource(R.string.run_all_skip))
        }
    }
}

@Composable
fun VibrationCheckStep(
    progress: RunAllProgress,
    onPlayAgain: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit,
    onResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_vibration_title),
        description = stringResource(R.string.run_all_vibration_description),
    ) {
        OutlinedButton(onClick = onPlayAgain) {
            Text(stringResource(R.string.run_all_play_again))
        }
        OutlinedButton(
            onClick = onStop,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(stringResource(R.string.vibration_stop))
        }
        Spacer(modifier = Modifier.height(20.dp))
        ConfirmationButtons(
            positiveText = stringResource(R.string.run_all_felt_vibration),
            negativeText = stringResource(R.string.run_all_no_vibration),
            onPositive = { onResult(true) },
            onNegative = { onResult(false) },
        )
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(stringResource(R.string.vibration_skip))
        }
    }
}

@Composable
fun ButtonCheckStep(
    state: ButtonTestState,
    progress: RunAllProgress,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_buttons_title),
        description = stringResource(R.string.run_all_buttons_description),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatusBadge(
                text = stringResource(R.string.button_volume_up),
                color = if (state.volumeUpDetected) Green400 else Neutral400,
            )
            StatusBadge(
                text = stringResource(R.string.button_volume_down),
                color = if (state.volumeDownDetected) Green400 else Neutral400,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (state.phase == ButtonTestPhase.TIMED_OUT) {
            Text(
                text = stringResource(R.string.button_timeout_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (state.phase != ButtonTestPhase.RUNNING) {
            Button(onClick = onRetry) {
                Text(stringResource(R.string.button_retry))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedButton(onClick = onSkip) {
            Text(stringResource(R.string.run_all_skip))
        }
    }
}

@Composable
fun BiometricCheckStep(
    state: BiometricTestState,
    progress: RunAllProgress,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_biometrics_title),
        description = stringResource(R.string.run_all_biometrics_description),
    ) {
        if (state.promptActive) {
            CircularProgressIndicator()
        }
        if (state.authResult == AuthResult.NOT_RECOGNIZED) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.biometric_nonterminal_guidance),
                color = Yellow400,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onSkip) {
            Text(stringResource(R.string.run_all_skip))
        }
    }
}

@Composable
private fun ManualCheckFrame(
    progress: RunAllProgress,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ManualProgress(progress)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            modifier = Modifier.padding(top = 10.dp, bottom = 28.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        content()
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ManualProgress(progress: RunAllProgress) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text =
                stringResource(
                    R.string.run_all_interactive_progress,
                    progress.position,
                    progress.total,
                ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { progress.position.toFloat() / progress.total.coerceAtLeast(1) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ConfirmationButtons(
    positiveText: String,
    negativeText: String,
    onPositive: () -> Unit,
    onNegative: () -> Unit,
    negativeColor: Color = Yellow400,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(
            onClick = onPositive,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(positiveText)
        }
        OutlinedButton(
            onClick = onNegative,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = negativeColor),
            border = BorderStroke(1.dp, negativeColor.copy(alpha = 0.72f)),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(text = negativeText)
        }
    }
}
