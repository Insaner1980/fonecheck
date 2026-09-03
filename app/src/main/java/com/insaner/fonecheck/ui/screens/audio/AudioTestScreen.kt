package com.insaner.fonecheck.ui.screens.audio

import android.content.pm.PackageManager
import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.classification.classifyAudioConfirmation
import com.insaner.fonecheck.ui.components.ButtonRow
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.ManualResultButtons
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.WindowBar
import com.insaner.fonecheck.ui.components.WindowFigure
import com.insaner.fonecheck.ui.components.WindowLabel
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.permissions.rememberPermissionController
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.toSemanticTone
import kotlinx.coroutines.delay

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun AudioTestScreen(
    modifier: Modifier = Modifier,
    viewModel: AudioTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val liveStateUpdatedAtEpochMillis = remember(state) { System.currentTimeMillis() }
    val context = LocalContext.current
    val hasMicrophone =
        remember(context) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        }
    val microphonePermission =
        rememberPermissionController(
            kind = PermissionKind.MICROPHONE,
            hardwareAvailable = hasMicrophone,
        )
    val microphonePermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            microphonePermission.refresh()
        }
    val requestMicrophonePermission = {
        microphonePermission.onRequestLaunched()
        microphonePermissionLauncher.launch(microphonePermission.permissions.toTypedArray())
    }

    LaunchedEffect(microphonePermission.state) {
        if (microphonePermission.state != PermissionState.GRANTED) {
            viewModel.stopRecording()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.stopTone()
        viewModel.stopRecording()
        viewModel.stopPlayback()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTone()
            viewModel.stopRecording()
            viewModel.stopPlayback()
        }
    }

    TestScreenContent(
        modifier =
            modifier
                .onKeyEvent { event ->
                    if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_VOLUME_UP -> {
                                viewModel.onVolumeUp()
                                true
                            }
                            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                                viewModel.onVolumeDown()
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                },
        liveStateUpdatedAtEpochMillis = liveStateUpdatedAtEpochMillis,
    ) {
        // Volume leads. It is the precondition for every test below it: at a low level a working
        // speaker still sounds wrong, and the reader would mark a good device as faulty.
        item { VolumeButtonSection(state, viewModel) }
        item { SpeakerTestSection(state, viewModel) }
        item { StereoTestSection(state, viewModel) }
        item { EarpieceTestSection(state, viewModel) }
        item {
            MicrophoneTestSection(
                state = state,
                viewModel = viewModel,
                permissionState = microphonePermission.state,
                onRequestPermission = requestMicrophonePermission,
                onOpenSettings = microphonePermission::openSettings,
            )
        }
        item { HeadphoneJackSection(state, viewModel) }
    }
}

@Composable
private fun SpeakerTestSection(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
) {
    val frequencies = listOf(250, 440, 1000, 4000, 8000)

    AudioSection(title = stringResource(R.string.audio_speaker_title)) {
        Note(stringResource(R.string.audio_speaker_description))
        ButtonRow { buttonModifier ->
            frequencies.forEach { frequency ->
                val isActive = state.isPlaying && state.currentFrequency == frequency
                ToneSelectionButton(
                    label = frequencyLabel(frequency),
                    isActive = isActive,
                    onClick = {
                        if (isActive) viewModel.stopTone() else viewModel.playTone(frequency)
                    },
                    modifier = buttonModifier,
                )
            }
        }
        ToneStopButton(isPlaying = state.isPlaying, onStop = viewModel::stopTone)
        AudioManualResult(
            check = AudioManualCheck.SPEAKER,
            result = state.manualResults[AudioManualCheck.SPEAKER],
            onResult = { viewModel.recordManualResult(AudioManualCheck.SPEAKER, it) },
        )
    }
}

@Composable
private fun frequencyLabel(frequency: Int): String =
    if (frequency >= 1000) {
        stringResource(R.string.audio_frequency_khz, frequency / 1000)
    } else {
        stringResource(R.string.audio_frequency_hz, frequency)
    }

@Composable
private fun StereoTestSection(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
) {
    val channels =
        listOf(
            StereoChannel.LEFT to stringResource(R.string.audio_left),
            StereoChannel.BOTH to stringResource(R.string.audio_both),
            StereoChannel.RIGHT to stringResource(R.string.audio_right),
        )

    AudioSection(title = stringResource(R.string.audio_stereo_title)) {
        Note(stringResource(R.string.audio_stereo_description))
        ButtonRow { buttonModifier ->
            channels.forEach { (channel, label) ->
                val isActive = state.isPlaying && state.stereoChannel == channel
                ToneSelectionButton(
                    label = label,
                    isActive = isActive,
                    onClick = {
                        if (isActive) viewModel.stopTone() else viewModel.playStereoTone(channel)
                    },
                    modifier = buttonModifier,
                )
            }
        }
        ToneStopButton(isPlaying = state.isPlaying, onStop = viewModel::stopTone)
        AudioManualResult(
            check = AudioManualCheck.STEREO,
            result = state.manualResults[AudioManualCheck.STEREO],
            onResult = { viewModel.recordManualResult(AudioManualCheck.STEREO, it) },
        )
    }
}

@Composable
private fun ToneStopButton(
    isPlaying: Boolean,
    onStop: () -> Unit,
) {
    if (isPlaying) {
        SecondaryButton(
            label = stringResource(R.string.audio_stop),
            onClick = onStop,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EarpieceTestSection(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
) {
    AudioSection(title = stringResource(R.string.audio_earpiece_title)) {
        Note(stringResource(R.string.audio_earpiece_description))
        if (state.isPlaying) {
            SecondaryButton(
                label = stringResource(R.string.audio_stop),
                onClick = viewModel::stopTone,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            PrimaryButton(
                label = stringResource(R.string.audio_play_earpiece),
                onClick = viewModel::playEarpieceTone,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AudioManualResult(
            check = AudioManualCheck.EARPIECE,
            result = state.manualResults[AudioManualCheck.EARPIECE],
            onResult = { viewModel.recordManualResult(AudioManualCheck.EARPIECE, it) },
        )
    }
}

@Composable
private fun MicrophoneTestSection(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
    permissionState: PermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    AudioSection(title = stringResource(R.string.audio_microphone_title)) {
        Note(stringResource(R.string.audio_microphone_description))
        PermissionStatusCard(
            state = permissionState,
            rationale = stringResource(R.string.permission_rationale_microphone),
            onRequest = onRequestPermission,
            onOpenSettings = onOpenSettings,
        )
        Note(stringResource(R.string.audio_relative_level_disclaimer))
        if (state.isRecording) {
            DataRow(
                label = stringResource(R.string.audio_level),
                value =
                    stringResource(
                        R.string.audio_relative_level_value,
                        uiNumber(state.relativeInputLevel * 100f),
                    ),
                tone = SemanticTone.NEUTRAL,
            )
        }
        ButtonRow { buttonModifier ->
            if (state.isRecording) {
                SecondaryButton(
                    label = stringResource(R.string.audio_stop_recording),
                    onClick = viewModel::stopRecording,
                    modifier = buttonModifier,
                )
            } else {
                PrimaryButton(
                    label = stringResource(R.string.audio_record),
                    onClick = viewModel::startRecording,
                    modifier = buttonModifier,
                    enabled = permissionState == PermissionState.GRANTED,
                )
            }
            RecordingPlaybackButton(
                isPlayingRecording = state.isPlayingRecording,
                enabled = state.hasRecordedAudio && !state.isRecording,
                onStopPlayback = viewModel::stopPlayback,
                onPlayRecording = viewModel::playRecording,
                modifier = buttonModifier,
            )
        }
        if (state.hasRecordedAudio) {
            AudioManualResult(
                check = AudioManualCheck.PLAYBACK,
                result = state.manualResults[AudioManualCheck.PLAYBACK],
                onResult = { viewModel.recordManualResult(AudioManualCheck.PLAYBACK, it) },
            )
        }
    }
}

@Composable
private fun RecordingPlaybackButton(
    isPlayingRecording: Boolean,
    enabled: Boolean,
    onStopPlayback: () -> Unit,
    onPlayRecording: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SecondaryButton(
        label =
            stringResource(
                if (isPlayingRecording) R.string.audio_stop else R.string.audio_playback,
            ),
        onClick = {
            if (isPlayingRecording) onStopPlayback() else onPlayRecording()
        },
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
private fun AudioManualResult(
    check: AudioManualCheck,
    result: Boolean?,
    onResult: (Boolean) -> Unit,
) {
    Note(stringResource(R.string.audio_manual_confirmation))
    ManualResultButtons(
        problemLabel = stringResource(R.string.audio_manual_problem),
        passLabel = stringResource(R.string.audio_manual_pass),
        onResult = onResult,
    )
    result?.let {
        val classification = classifyAudioConfirmation(check, it)
        StatusText(
            text = stringResource(if (it) R.string.audio_manual_passed else R.string.audio_manual_issue_saved),
            tone = classification.toSemanticTone(),
        )
    }
}

@Composable
private fun HeadphoneJackSection(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
) {
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateHeadphoneState()
            delay(1000)
        }
    }

    AudioSection(title = stringResource(R.string.audio_headphone_title)) {
        Note(stringResource(R.string.audio_headphone_description))
        DataRow(
            label = stringResource(R.string.audio_headphone_status),
            value =
                stringResource(
                    if (state.headphonePlugged) R.string.audio_connected else R.string.audio_disconnected,
                ),
        )
        if (state.headphonePlugged) {
            DataRow(
                label = stringResource(R.string.audio_headphone_type),
                value = state.headphoneType?.let { headphoneTypeLabel(it) },
            )
        }
    }
}

@Composable
private fun headphoneTypeLabel(type: HeadphoneTypeCode): String =
    stringResource(
        when (type) {
            HeadphoneTypeCode.WIRED_HEADSET -> R.string.audio_headphone_wired_headset
            HeadphoneTypeCode.WIRED_HEADPHONES -> R.string.audio_headphone_wired_headphones
            HeadphoneTypeCode.USB_HEADSET -> R.string.audio_headphone_usb_headset
            HeadphoneTypeCode.UNKNOWN -> R.string.sim_value_unknown
        },
    )

@Composable
private fun VolumeButtonSection(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
) {
    LaunchedEffect(Unit) {
        viewModel.updateVolumeState()
    }

    AudioSection(title = stringResource(R.string.audio_volume_title)) {
        VolumeLevelReadout(level = state.volumeLevel, max = state.maxVolume)
        Note(stringResource(R.string.audio_volume_description))
        DataRow(
            label = stringResource(R.string.audio_volume_up),
            value = uiNumber(state.volumeUpCount),
            tone = if (state.volumeUpCount > 0) SemanticTone.PASS else SemanticTone.NEUTRAL,
        )
        DataRow(
            label = stringResource(R.string.audio_volume_down),
            value = uiNumber(state.volumeDownCount),
            tone = if (state.volumeDownCount > 0) SemanticTone.PASS else SemanticTone.NEUTRAL,
        )
        SecondaryButton(
            label = stringResource(R.string.audio_reset),
            onClick = viewModel::resetVolumeTest,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * The system media volume against its own maximum. Bounded by a value Android reports, so it reads
 * as a level rather than two numbers.
 */
@Composable
private fun VolumeLevelReadout(
    level: Int,
    max: Int,
) {
    if (max <= 0) return
    Column {
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        ReadoutWindow {
            WindowLabel(text = stringResource(R.string.audio_volume_level))
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            WindowFigure(value = "${uiNumber(level)} / ${uiNumber(max)}")
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            WindowBar(percentage = (level * PERCENT / max).coerceIn(0, PERCENT))
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
    }
}

private const val PERCENT = 100

@Composable
private fun ToneSelectionButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectionModifier = modifier.semantics { selected = isActive }
    if (isActive) {
        PrimaryButton(
            label = label,
            onClick = onClick,
            modifier = selectionModifier,
        )
    } else {
        SecondaryButton(
            label = label,
            onClick = onClick,
            modifier = selectionModifier,
        )
    }
}

@Composable
private fun AudioSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
    ) {
        SectionHeader(label = title)
        content()
    }
}
