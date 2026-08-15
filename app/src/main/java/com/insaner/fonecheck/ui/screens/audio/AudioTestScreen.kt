package com.insaner.fonecheck.ui.screens.audio

import android.content.pm.PackageManager
import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.ManualResultButtons
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.StatusBadge
import com.insaner.fonecheck.ui.permissions.rememberPermissionController
import com.insaner.fonecheck.ui.theme.Blue400
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.JetBrainsMono
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Neutral600
import com.insaner.fonecheck.ui.theme.Neutral700
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400
import com.insaner.fonecheck.ui.theme.readableStatusColor
import kotlinx.coroutines.delay

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun AudioTestScreen(
    modifier: Modifier = Modifier,
    viewModel: AudioTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
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

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SpeakerTestCard(state, viewModel)
        StereoTestCard(state, viewModel)
        EarpieceTestCard(state, viewModel)
        MicrophoneTestCard(
            state = state,
            viewModel = viewModel,
            permissionState = microphonePermission.state,
            onRequestPermission = requestMicrophonePermission,
            onOpenSettings = microphonePermission::openSettings,
        )
        HeadphoneJackCard(state, viewModel)
        VolumeButtonCard(state, viewModel)
    }
}

@Composable
private fun SpeakerTestCard(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
) {
    val frequencies = listOf(250, 440, 1000, 4000, 8000)

    AudioCard(title = stringResource(R.string.audio_speaker_title)) {
        AudioDescription(stringResource(R.string.audio_speaker_description))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            frequencies.forEach { freq ->
                val isActive = state.isPlaying && state.currentFrequency == freq
                FrequencyButton(
                    frequency = freq,
                    isActive = isActive,
                    onClick = {
                        if (isActive) viewModel.stopTone() else viewModel.playTone(freq)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (state.isPlaying) {
            StopToneButton(viewModel::stopTone)
        }
        AudioManualResultButtons(
            result = state.manualResults[AudioManualCheck.SPEAKER],
            onResult = { viewModel.recordManualResult(AudioManualCheck.SPEAKER, it) },
        )
    }
}

@Composable
private fun FrequencyButton(
    frequency: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label =
        if (frequency >= 1000) {
            stringResource(R.string.audio_frequency_khz, frequency / 1000)
        } else {
            stringResource(R.string.audio_frequency_hz, frequency)
        }
    ToneSelectionButton(
        onClick = onClick,
        isActive = isActive,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Medium,
                ),
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.labelSmall.lineHeight,
        )
    }
}

@Composable
private fun StereoTestCard(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
) {
    AudioCard(title = stringResource(R.string.audio_stereo_title)) {
        AudioDescription(stringResource(R.string.audio_stereo_description))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val channels =
                listOf(
                    StereoChannel.LEFT to stringResource(R.string.audio_left),
                    StereoChannel.BOTH to stringResource(R.string.audio_both),
                    StereoChannel.RIGHT to stringResource(R.string.audio_right),
                )
            channels.forEach { (channel, label) ->
                val isActive = state.isPlaying && state.stereoChannel == channel
                ToneSelectionButton(
                    onClick = {
                        if (isActive) viewModel.stopTone() else viewModel.playStereoTone(channel)
                    },
                    isActive = isActive,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        if (state.isPlaying) {
            StopToneButton(viewModel::stopTone)
        }
        AudioManualResultButtons(
            result = state.manualResults[AudioManualCheck.STEREO],
            onResult = { viewModel.recordManualResult(AudioManualCheck.STEREO, it) },
        )
    }
}

@Composable
private fun EarpieceTestCard(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
) {
    AudioCard(title = stringResource(R.string.audio_earpiece_title)) {
        AudioDescription(stringResource(R.string.audio_earpiece_description))
        Button(
            onClick = {
                if (state.isPlaying) viewModel.stopTone() else viewModel.playEarpieceTone()
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = if (state.isPlaying) Red400 else Blue400,
                ),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                if (state.isPlaying) {
                    stringResource(R.string.audio_stop)
                } else {
                    stringResource(R.string.audio_play_earpiece)
                },
            )
        }
        AudioManualResultButtons(
            result = state.manualResults[AudioManualCheck.EARPIECE],
            onResult = { viewModel.recordManualResult(AudioManualCheck.EARPIECE, it) },
        )
    }
}

@Composable
@Suppress("kotlin:S3776") // Declarative branches render independent microphone states.
private fun MicrophoneTestCard(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
    permissionState: PermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    AudioCard(title = stringResource(R.string.audio_microphone_title)) {
        AudioDescription(stringResource(R.string.audio_microphone_description))
        PermissionStatusCard(
            state = permissionState,
            rationale = stringResource(R.string.permission_rationale_microphone),
            onRequest = onRequestPermission,
            onOpenSettings = onOpenSettings,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Text(
            text = stringResource(R.string.audio_relative_level_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (state.isRecording) {
            val animatedLevel by animateFloatAsState(
                targetValue = state.relativeInputLevel,
                label = "relative_input_level",
            )
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.audio_level),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.audio_relative_level_value, state.relativeInputLevel * 100f),
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Medium,
                            ),
                        color =
                            readableStatusColor(
                                when {
                                    state.relativeInputLevel > 0.75f -> Red400
                                    state.relativeInputLevel > 0.4f -> Yellow400
                                    else -> Green400
                                },
                            ),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { animatedLevel },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    color =
                        when {
                            state.relativeInputLevel > 0.75f -> Red400
                            state.relativeInputLevel > 0.4f -> Yellow400
                            else -> Green400
                        },
                    trackColor = Neutral700,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Record / Stop button
            Button(
                onClick = {
                    if (state.isRecording) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.startRecording()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = permissionState == PermissionState.GRANTED,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (state.isRecording) Red400 else Blue400,
                    ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    if (state.isRecording) {
                        stringResource(R.string.audio_stop_recording)
                    } else {
                        stringResource(R.string.audio_record)
                    },
                )
            }

            // Playback button
            Button(
                onClick = {
                    if (state.isPlayingRecording) {
                        viewModel.stopPlayback()
                    } else {
                        viewModel.playRecording()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = state.hasRecordedAudio && !state.isRecording,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (state.isPlayingRecording) Red400 else Green400,
                        disabledContainerColor = Neutral700,
                    ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    if (state.isPlayingRecording) {
                        stringResource(R.string.audio_stop)
                    } else {
                        stringResource(R.string.audio_playback)
                    },
                    color =
                        if (state.hasRecordedAudio && !state.isRecording) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            Neutral500
                        },
                )
            }
        }
        if (state.hasRecordedAudio) {
            AudioManualResultButtons(
                result = state.manualResults[AudioManualCheck.PLAYBACK],
                onResult = { viewModel.recordManualResult(AudioManualCheck.PLAYBACK, it) },
            )
        }
    }
}

@Composable
private fun AudioManualResultButtons(
    result: Boolean?,
    onResult: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.audio_manual_confirmation),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ManualResultButtons(
            problemLabel = stringResource(R.string.audio_manual_problem),
            passLabel = stringResource(R.string.audio_manual_pass),
            onResult = onResult,
        )
        result?.let {
            Text(
                text = stringResource(if (it) R.string.audio_manual_passed else R.string.audio_manual_issue_saved),
                style = MaterialTheme.typography.labelMedium,
                color = if (it) Green400 else Red400,
            )
        }
    }
}

@Composable
private fun HeadphoneJackCard(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
) {
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateHeadphoneState()
            delay(1000)
        }
    }

    AudioCard(title = stringResource(R.string.audio_headphone_title)) {
        AudioDescription(stringResource(R.string.audio_headphone_description))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.audio_headphone_status),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatusBadge(
                text =
                    if (state.headphonePlugged) {
                        stringResource(R.string.audio_connected)
                    } else {
                        stringResource(R.string.audio_disconnected)
                    },
                color = if (state.headphonePlugged) Green400 else Neutral500,
            )
        }
        if (state.headphonePlugged && state.headphoneType != null) {
            InfoRow(stringResource(R.string.audio_headphone_type), headphoneTypeLabel(state.headphoneType))
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
private fun VolumeButtonCard(
    state: AudioTestState,
    viewModel: AudioTestViewModel,
) {
    LaunchedEffect(Unit) {
        viewModel.updateVolumeState()
    }

    AudioCard(title = stringResource(R.string.audio_volume_title)) {
        AudioDescription(stringResource(R.string.audio_volume_description))

        // Volume level
        InfoRow(
            stringResource(R.string.audio_volume_level),
            "${state.volumeLevel} / ${state.maxVolume}",
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Volume up/down indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VolumeIndicator(
                label = stringResource(R.string.audio_volume_up),
                pressed = state.volumeUpPressed,
                count = state.volumeUpCount,
                modifier = Modifier.weight(1f),
            )
            VolumeIndicator(
                label = stringResource(R.string.audio_volume_down),
                pressed = state.volumeDownPressed,
                count = state.volumeDownCount,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { viewModel.resetVolumeTest() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.audio_reset))
        }
    }
}

@Composable
private fun AudioDescription(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}

@Composable
private fun StopToneButton(onClick: () -> Unit) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red400),
        ) {
            Text(stringResource(R.string.audio_stop))
        }
    }
}

@Composable
private fun ToneSelectionButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (isActive) Blue400 else Neutral700,
                contentColor =
                    if (isActive) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            ),
        shape = RoundedCornerShape(8.dp),
        content = content,
    )
}

@Composable
private fun VolumeIndicator(
    label: String,
    pressed: Boolean,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue = if (pressed) Green400 else Neutral600,
        label = "border",
    )
    val bgColor by animateColorAsState(
        targetValue = if (pressed) Green400.copy(alpha = 0.15f) else Neutral700.copy(alpha = 0.5f),
        label = "bg",
    )

    Column(
        modifier =
            modifier
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .background(bgColor, RoundedCornerShape(12.dp))
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color =
                if (pressed) {
                    readableStatusColor(Green400)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$count",
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                ),
            color = if (count > 0) readableStatusColor(Green400) else Neutral500,
        )
    }
}

@Composable
private fun AudioCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            content()
        }
    }
}
