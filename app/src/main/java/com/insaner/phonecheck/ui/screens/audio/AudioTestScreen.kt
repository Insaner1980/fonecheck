package com.insaner.phonecheck.ui.screens.audio

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.insaner.phonecheck.R
import com.insaner.phonecheck.ui.theme.Blue400
import com.insaner.phonecheck.ui.theme.Green400
import com.insaner.phonecheck.ui.theme.JetBrainsMono
import com.insaner.phonecheck.ui.theme.Neutral500
import com.insaner.phonecheck.ui.theme.Neutral600
import com.insaner.phonecheck.ui.theme.Neutral700
import com.insaner.phonecheck.ui.theme.Red400
import com.insaner.phonecheck.ui.theme.Yellow400
import kotlinx.coroutines.delay

@Composable
fun AudioTestScreen(
    modifier: Modifier = Modifier,
    viewModel: AudioTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTone()
            viewModel.stopRecording()
            viewModel.stopPlayback()
        }
    }

    Column(
        modifier = modifier
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
        MicrophoneTestCard(state, viewModel)
        HeadphoneJackCard(state, viewModel)
        VolumeButtonCard(state, viewModel)
    }
}

@Composable
private fun SpeakerTestCard(state: AudioTestState, viewModel: AudioTestViewModel) {
    val frequencies = listOf(250, 440, 1000, 4000, 8000)

    AudioCard(title = stringResource(R.string.audio_speaker_title)) {
        Text(
            text = stringResource(R.string.audio_speaker_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
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
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.stopTone() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red400),
            ) {
                Text(stringResource(R.string.audio_stop))
            }
        }
    }
}

@Composable
private fun FrequencyButton(
    frequency: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = if (frequency >= 1000) "${frequency / 1000}k" else "$frequency"
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Blue400 else Neutral700,
            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = "${label}\nHz",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
            ),
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.labelSmall.lineHeight,
        )
    }
}

@Composable
private fun StereoTestCard(state: AudioTestState, viewModel: AudioTestViewModel) {
    AudioCard(title = stringResource(R.string.audio_stereo_title)) {
        Text(
            text = stringResource(R.string.audio_stereo_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val channels = listOf(
                StereoChannel.LEFT to stringResource(R.string.audio_left),
                StereoChannel.BOTH to stringResource(R.string.audio_both),
                StereoChannel.RIGHT to stringResource(R.string.audio_right),
            )
            channels.forEach { (channel, label) ->
                val isActive = state.isPlaying && state.stereoChannel == channel
                Button(
                    onClick = {
                        if (isActive) viewModel.stopTone() else viewModel.playStereoTone(channel)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) Blue400 else Neutral700,
                        contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        if (state.isPlaying) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.stopTone() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red400),
            ) {
                Text(stringResource(R.string.audio_stop))
            }
        }
    }
}

@Composable
private fun EarpieceTestCard(state: AudioTestState, viewModel: AudioTestViewModel) {
    AudioCard(title = stringResource(R.string.audio_earpiece_title)) {
        Text(
            text = stringResource(R.string.audio_earpiece_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Button(
            onClick = {
                if (state.isPlaying) viewModel.stopTone() else viewModel.playEarpieceTone()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isPlaying) Red400 else Blue400,
            ),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                if (state.isPlaying) stringResource(R.string.audio_stop)
                else stringResource(R.string.audio_play_earpiece),
            )
        }
    }
}

@Composable
private fun MicrophoneTestCard(state: AudioTestState, viewModel: AudioTestViewModel) {
    AudioCard(title = stringResource(R.string.audio_microphone_title)) {
        Text(
            text = stringResource(R.string.audio_microphone_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // dB meter
        if (state.isRecording) {
            val animatedDb by animateFloatAsState(
                targetValue = state.decibelLevel / 90f,
                label = "db",
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
                        text = stringResource(R.string.audio_db_value, state.decibelLevel),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = when {
                            state.decibelLevel > 70 -> Red400
                            state.decibelLevel > 40 -> Yellow400
                            else -> Green400
                        },
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { animatedDb },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        state.decibelLevel > 70 -> Red400
                        state.decibelLevel > 40 -> Yellow400
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
                    if (state.isRecording) viewModel.stopRecording()
                    else viewModel.startRecording()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isRecording) Red400 else Blue400,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    if (state.isRecording) stringResource(R.string.audio_stop_recording)
                    else stringResource(R.string.audio_record),
                )
            }

            // Playback button
            Button(
                onClick = {
                    if (state.isPlayingRecording) viewModel.stopPlayback()
                    else viewModel.playRecording()
                },
                modifier = Modifier.weight(1f),
                enabled = state.hasRecordedAudio && !state.isRecording,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isPlayingRecording) Red400 else Green400,
                    disabledContainerColor = Neutral700,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    if (state.isPlayingRecording) stringResource(R.string.audio_stop)
                    else stringResource(R.string.audio_playback),
                    color = if (state.hasRecordedAudio && !state.isRecording)
                        MaterialTheme.colorScheme.onPrimary
                    else Neutral500,
                )
            }
        }
    }
}

@Composable
private fun HeadphoneJackCard(state: AudioTestState, viewModel: AudioTestViewModel) {
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateHeadphoneState()
            delay(1000)
        }
    }

    AudioCard(title = stringResource(R.string.audio_headphone_title)) {
        Text(
            text = stringResource(R.string.audio_headphone_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Row(
            modifier = Modifier
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                val indicatorColor by animateColorAsState(
                    targetValue = if (state.headphonePlugged) Green400 else Neutral600,
                    label = "indicator",
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(indicatorColor),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.headphonePlugged)
                        stringResource(R.string.audio_connected)
                    else stringResource(R.string.audio_disconnected),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (state.headphonePlugged) Green400 else Neutral500,
                )
            }
        }
        if (state.headphonePlugged && state.headphoneType != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.audio_headphone_type),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = state.headphoneType,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun VolumeButtonCard(state: AudioTestState, viewModel: AudioTestViewModel) {
    LaunchedEffect(Unit) {
        viewModel.updateVolumeState()
    }

    AudioCard(title = stringResource(R.string.audio_volume_title)) {
        Text(
            text = stringResource(R.string.audio_volume_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Volume level
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.audio_volume_level),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${state.volumeLevel} / ${state.maxVolume}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

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
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (pressed) Green400 else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
            ),
            color = if (count > 0) Green400 else Neutral500,
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            content()
        }
    }
}
