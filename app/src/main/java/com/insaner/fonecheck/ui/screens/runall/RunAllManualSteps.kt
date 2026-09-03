package com.insaner.fonecheck.ui.screens.runall

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.observation.DeviceObservation
import com.insaner.fonecheck.domain.observation.DeviceObservationClassifier
import com.insaner.fonecheck.domain.observation.MeasurementKind
import com.insaner.fonecheck.domain.observation.MeasurementOutcome
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.localization.observationStatusStringRes
import com.insaner.fonecheck.ui.classification.classifyBiometric
import com.insaner.fonecheck.ui.classification.classifyButtonTest
import com.insaner.fonecheck.ui.components.ButtonRow
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.ManualResultButtons
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ObservationReasonNote
import com.insaner.fonecheck.ui.components.PanelToggle
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.ProgressWindow
import com.insaner.fonecheck.ui.components.ScreenLoadingNote
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.screens.biometrics.AuthResult
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestState
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestPhase
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestState
import com.insaner.fonecheck.ui.screens.camera.CameraTestState
import com.insaner.fonecheck.ui.screens.display.DisplayPattern
import com.insaner.fonecheck.ui.screens.display.displayPatternBackground
import com.insaner.fonecheck.ui.screens.sensor.SensorChallengeWindow
import com.insaner.fonecheck.ui.screens.sensor.SensorTestState
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.toSemanticTone

internal val displayTestPatterns = DisplayPattern.entries

data class PermissionPrompt(
    val state: PermissionState,
    val rationale: String,
    val onRequest: () -> Unit,
    val onOpenSettings: () -> Unit,
)

@Composable
private fun ScrollableStepContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FonecheckTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.lg),
        content = content,
    )
}

@Composable
fun FullCheckPreflightScreen(
    selections: RunAllSelections,
    onSelectionsChange: (RunAllSelections) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    showWarnings: Boolean = true,
) {
    ScrollableStepContent(modifier) {
        Column {
            SectionHeader(stringResource(R.string.run_all_preflight_title))
            Note(stringResource(R.string.run_all_preflight_description))
        }
        if (showWarnings) {
            PreflightDisclosureGroup(
                title = stringResource(R.string.run_all_preflight_what_happens_title),
                paragraphs =
                    listOf(
                        stringResource(R.string.run_all_preflight_interactions),
                        stringResource(R.string.run_all_preflight_audio_vibration),
                        stringResource(R.string.run_all_preflight_storage),
                    ),
            )
            PreflightDisclosureGroup(
                title = stringResource(R.string.run_all_preflight_permissions_control_title),
                paragraphs =
                    listOf(
                        stringResource(R.string.run_all_preflight_permissions),
                        stringResource(R.string.run_all_preflight_unsupported),
                    ),
            )
            PreflightDisclosureGroup(
                title = stringResource(R.string.run_all_preflight_privacy_title),
                paragraphs =
                    listOf(
                        stringResource(R.string.run_all_preflight_local_report),
                        stringResource(R.string.run_all_preflight_no_network),
                    ),
            )
        }
        Column {
            SectionHeader(stringResource(R.string.run_all_preflight_choices_title))
            PreflightChoiceRow(
                label = stringResource(R.string.run_all_preflight_speaker_option),
                checked = selections.includeSpeaker,
                onCheckedChange = { onSelectionsChange(selections.copy(includeSpeaker = it)) },
            )
            PreflightChoiceRow(
                label = stringResource(R.string.run_all_preflight_microphone_option),
                checked = selections.includeMicrophone,
                onCheckedChange = { onSelectionsChange(selections.copy(includeMicrophone = it)) },
            )
            PreflightChoiceRow(
                label = stringResource(R.string.run_all_preflight_camera_option),
                checked = selections.includeCamera,
                onCheckedChange = { onSelectionsChange(selections.copy(includeCamera = it)) },
            )
            PreflightChoiceRow(
                label = stringResource(R.string.run_all_preflight_storage_option),
                checked = selections.includeStorageBenchmark,
                onCheckedChange = { onSelectionsChange(selections.copy(includeStorageBenchmark = it)) },
            )
        }
        PrimaryButton(
            label = stringResource(R.string.run_all_preflight_start),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PreflightDisclosureGroup(
    title: String,
    paragraphs: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm)) {
        SectionHeader(title)
        paragraphs.forEach { paragraph ->
            Text(
                text = paragraph,
                style = FonecheckTheme.type.rowLabel,
                color = FonecheckTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun PreflightChoiceRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = FonecheckTheme.spacing.minTouchTarget)
                    .toggleable(
                        value = checked,
                        role = Role.Checkbox,
                        onValueChange = onCheckedChange,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PanelToggle(checked = checked)
            Text(
                text = label,
                modifier = Modifier.padding(start = FonecheckTheme.spacing.sm),
                style = FonecheckTheme.type.rowLabel,
                color = FonecheckTheme.colors.textPrimary,
            )
        }
        HairlineRule()
    }
}

@Composable
fun PermissionReviewScreen(
    prompts: List<PermissionPrompt>,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScrollableStepContent(modifier) {
        Column {
            SectionHeader(stringResource(R.string.run_all_preparing_title))
            Note(stringResource(R.string.run_all_preparing_description))
        }
        prompts.forEach { prompt ->
            PermissionStatusCard(
                state = prompt.state,
                rationale = prompt.rationale,
                onRequest = prompt.onRequest,
                onOpenSettings = prompt.onOpenSettings,
            )
        }
        PrimaryButton(
            label = stringResource(R.string.run_all_permissions_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            label = stringResource(R.string.run_all_cancel),
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun CategoryRetestPreflightScreen(
    categoryLabel: String,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScrollableStepContent(modifier) {
        Column {
            SectionHeader(stringResource(R.string.report_retest_title, categoryLabel))
            Note(stringResource(R.string.report_retest_description))
        }
        PrimaryButton(
            label = stringResource(R.string.report_retest_start),
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            label = stringResource(R.string.report_retest_cancel),
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun AutomaticCheckScreen(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    onCancel: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(FonecheckTheme.spacing.lg),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = FonecheckTheme.type.screenTitle,
            color = FonecheckTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        ScreenLoadingNote(description)
        actionLabel?.let { label ->
            SecondaryButton(
                label = label,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        onCancel?.let { cancel ->
            SecondaryButton(
                label = stringResource(R.string.run_all_cancel),
                onClick = cancel,
                modifier = Modifier.fillMaxWidth().padding(top = FonecheckTheme.spacing.sm),
            )
        }
    }
}

@Composable
fun DisplayCheckStep(
    colorIndex: Int,
    progress: RunAllProgress,
    onNextColor: () -> Unit,
    onResult: (Boolean) -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLastColor = colorIndex == displayTestPatterns.lastIndex
    Box(
        modifier = modifier.fillMaxSize().displayPatternBackground(displayTestPatterns[colorIndex]),
    ) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(FonecheckTheme.colors.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(FonecheckTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
        ) {
            ManualProgress(progress)
            Text(
                text = stringResource(R.string.run_all_display_title),
                style = FonecheckTheme.type.screenTitle,
                color = FonecheckTheme.colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Note(
                text =
                    stringResource(
                        if (isLastColor) R.string.run_all_display_question else R.string.run_all_display_description,
                    ),
            )
            if (isLastColor) {
                ManualResultButtons(
                    problemLabel = stringResource(R.string.run_all_found_problem),
                    passLabel = stringResource(R.string.run_all_looks_good),
                    onResult = onResult,
                )
            } else {
                PrimaryButton(
                    label = stringResource(R.string.run_all_next_color),
                    onClick = onNextColor,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ButtonRow { buttonModifier ->
                SecondaryButton(
                    label = stringResource(R.string.run_all_skip),
                    onClick = onSkip,
                    modifier = buttonModifier,
                )
                SecondaryButton(
                    label = stringResource(R.string.run_all_cancel),
                    onClick = onCancel,
                    modifier = buttonModifier,
                )
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
    onSkip: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_audio_title),
        description = stringResource(R.string.run_all_audio_description),
        onCancel = onCancel,
    ) {
        SecondaryButton(
            label = stringResource(R.string.run_all_play_again),
            onClick = onPlayAgain,
            enabled = !isPlaying,
            modifier = Modifier.fillMaxWidth(),
        )
        ManualResultButtons(
            problemLabel = stringResource(R.string.run_all_no_tone),
            passLabel = stringResource(R.string.run_all_heard_tone),
            onResult = onResult,
        )
        SecondaryButton(
            label = stringResource(R.string.run_all_skip),
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
@Suppress("kotlin:S107") // Explicit camera state and action slots keep the step contract type-safe.
fun CameraCheckStep(
    previewView: PreviewView,
    state: CameraTestState,
    progress: RunAllProgress,
    issue: RunAllStageOutcome?,
    cameraPosition: Int,
    cameraTotal: Int,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_camera_title),
        description = stringResource(R.string.run_all_camera_description),
        onCancel = onCancel,
    ) {
        if (cameraTotal > 0) {
            StatusText(
                text =
                    stringResource(
                        R.string.run_all_camera_progress,
                        uiNumber(cameraPosition),
                        uiNumber(cameraTotal),
                    ),
                tone = SemanticTone.NEUTRAL,
            )
        }
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        }
        if (state.lastCapture == null && state.error == null && issue == null) {
            IndeterminateRule()
        }
        val canRetry = state.error != null || issue != null
        if (canRetry) {
            val classification =
                DeviceObservationClassifier.classify(
                    DeviceObservation.Measurement(
                        MeasurementKind.CAMERA,
                        if (issue == RunAllStageOutcome.TIMED_OUT) {
                            MeasurementOutcome.TIMED_OUT
                        } else {
                            MeasurementOutcome.ERROR
                        },
                    ),
                )
            StatusText(
                text = stringResource(observationStatusStringRes(classification)),
                tone = classification.toSemanticTone(),
            )
            ObservationReasonNote(classification)
        }
        RetryAndSkipButtons(
            showRetry = canRetry,
            onRetry = onRetry,
            onSkip = onSkip,
        )
    }
}

@Composable
fun SensorCheckStep(
    state: SensorTestState,
    progress: RunAllProgress,
    onSkip: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_sensor_title),
        description = stringResource(R.string.run_all_sensor_description),
        onCancel = onCancel,
    ) {
        SensorChallengeWindow(state.challenge)
        SecondaryButton(
            label = stringResource(R.string.run_all_skip),
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun VibrationCheckStep(
    progress: RunAllProgress,
    onPlayAgain: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit,
    onResult: (Boolean) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_vibration_title),
        description = stringResource(R.string.run_all_vibration_description),
        onCancel = onCancel,
    ) {
        SecondaryButton(
            label = stringResource(R.string.run_all_play_again),
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            label = stringResource(R.string.vibration_stop),
            onClick = onStop,
            modifier = Modifier.fillMaxWidth(),
        )
        ManualResultButtons(
            problemLabel = stringResource(R.string.run_all_no_vibration),
            passLabel = stringResource(R.string.run_all_felt_vibration),
            onResult = onResult,
        )
        SecondaryButton(
            label = stringResource(R.string.vibration_skip),
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ButtonCheckStep(
    state: ButtonTestState,
    progress: RunAllProgress,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_buttons_title),
        description = stringResource(R.string.run_all_buttons_description),
        onCancel = onCancel,
    ) {
        Column {
            ButtonDetectionRow(
                label = stringResource(R.string.button_volume_up),
                detected = state.volumeUpDetected,
            )
            ButtonDetectionRow(
                label = stringResource(R.string.button_volume_down),
                detected = state.volumeDownDetected,
            )
        }
        if (state.phase == ButtonTestPhase.TIMED_OUT) {
            ObservationReasonNote(classifyButtonTest(state.phase))
            Note(text = stringResource(R.string.button_timeout_hint))
        }
        RetryAndSkipButtons(
            showRetry = state.phase != ButtonTestPhase.RUNNING,
            onRetry = onRetry,
            onSkip = onSkip,
        )
    }
}

@Composable
private fun ButtonDetectionRow(
    label: String,
    detected: Boolean,
) {
    DataRow(
        label = label,
        value = stringResource(if (detected) R.string.button_detected else R.string.status_not_measured),
        tone = if (detected) SemanticTone.PASS else SemanticTone.NEUTRAL,
    )
}

@Composable
fun BiometricCheckStep(
    state: BiometricTestState,
    progress: RunAllProgress,
    onSkip: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManualCheckFrame(
        modifier = modifier,
        progress = progress,
        title = stringResource(R.string.run_all_biometrics_title),
        description = stringResource(R.string.run_all_biometrics_description),
        onCancel = onCancel,
    ) {
        ObservationReasonNote(classifyBiometric(state.authResult))
        if (state.promptActive) {
            IndeterminateRule()
        }
        if (state.authResult == AuthResult.NOT_RECOGNIZED) {
            Note(text = stringResource(R.string.biometric_nonterminal_guidance))
        }
        SecondaryButton(
            label = stringResource(R.string.run_all_skip),
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RetryAndSkipButtons(
    showRetry: Boolean,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.lg),
    ) {
        if (showRetry) {
            PrimaryButton(
                label = stringResource(R.string.button_retry),
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        SecondaryButton(
            label = stringResource(R.string.run_all_skip),
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ManualCheckFrame(
    progress: RunAllProgress,
    title: String,
    description: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ScrollableStepContent(modifier) {
        ManualProgress(progress)
        Column {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = FonecheckTheme.type.screenTitle,
                color = FonecheckTheme.colors.textPrimary,
            )
            Note(text = description)
        }
        content()
        SecondaryButton(
            label = stringResource(R.string.run_all_cancel),
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ManualProgress(progress: RunAllProgress) {
    ProgressWindow(
        label =
            stringResource(
                R.string.run_all_interactive_progress,
                uiNumber(progress.position),
                uiNumber(progress.total),
            ),
        percentage = progress.position * PERCENT / progress.total.coerceAtLeast(1),
    )
}

private const val PERCENT = 100
