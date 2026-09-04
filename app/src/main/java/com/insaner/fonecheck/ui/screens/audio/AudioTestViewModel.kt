package com.insaner.fonecheck.ui.screens.audio

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.di.IoDispatcher
import com.insaner.fonecheck.ui.screens.buttons.VolumeButtonDirection
import com.insaner.fonecheck.ui.screens.buttons.VolumeButtonEventSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.sin

enum class AudioTestType {
    SPEAKER,
    STEREO,
    EARPIECE,
    MICROPHONE,
    HEADPHONE_JACK,
    VOLUME_BUTTONS,
}

enum class AudioManualCheck {
    SPEAKER,
    STEREO,
    EARPIECE,
    PLAYBACK,
}

enum class AudioOperationError {
    OUTPUT_UNAVAILABLE,
    RECORDING_UNAVAILABLE,
}

data class AudioTestState(
    val isPlaying: Boolean = false,
    val isRecording: Boolean = false,
    val currentFrequency: Int = 440,
    val stereoChannel: StereoChannel = StereoChannel.BOTH,
    val relativeInputLevel: Float = 0f,
    val hasRecordedAudio: Boolean = false,
    val isPlayingRecording: Boolean = false,
    val earpieceAvailable: Boolean? = null,
    val headphonePlugged: Boolean = false,
    val headphoneType: HeadphoneTypeCode? = null,
    val volumeLevel: Int = 0,
    val maxVolume: Int = 15,
    val volumeStream: Int = AudioManager.STREAM_MUSIC,
    val volumeUpPressed: Boolean = false,
    val volumeDownPressed: Boolean = false,
    val volumeUpCount: Int = 0,
    val volumeDownCount: Int = 0,
    val manualResults: Map<AudioManualCheck, Boolean> = emptyMap(),
    val error: AudioOperationError? = null,
)

internal fun AudioTestState.recordVolumeButton(direction: VolumeButtonDirection): AudioTestState =
    when (direction) {
        VolumeButtonDirection.UP -> copy(volumeUpPressed = true, volumeUpCount = volumeUpCount + 1)
        VolumeButtonDirection.DOWN -> copy(volumeDownPressed = true, volumeDownCount = volumeDownCount + 1)
    }

enum class StereoChannel { LEFT, RIGHT, BOTH }

enum class HeadphoneTypeCode {
    WIRED_HEADSET,
    WIRED_HEADPHONES,
    USB_HEADSET,
    UNKNOWN,
}

@HiltViewModel
class AudioTestViewModel
    @Inject
    constructor(
        application: Application,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        volumeButtonEventSource: VolumeButtonEventSource,
    ) : AndroidViewModel(application) {
        private val audioManager = application.getSystemService(AudioManager::class.java)

        private val _state = MutableStateFlow(AudioTestState())
        val state: StateFlow<AudioTestState> = _state

        private var toneJob: Job? = null
        private var recordJob: Job? = null
        private var playbackJob: Job? = null
        private var recordedData: ShortArray? = null
        private val toneOwner = AudioResourceOwner<AudioTrack>(::stopAndRelease)
        private val playbackOwner = AudioResourceOwner<AudioTrack>(::stopAndRelease)
        private val recordOwner = AudioResourceOwner<AudioRecord>(::stopAndRelease)
        private val routeController = AndroidAudioRouteController(audioManager)
        private val routeOwner = AudioResourceOwner<AudioRouteSession>(AudioRouteSession::close)
        private val toneGate = AudioOperationGate()
        private val recordGate = AudioOperationGate()
        private val playbackGate = AudioOperationGate()

        private val sampleRate = 44100

        init {
            viewModelScope.launch {
                volumeButtonEventSource.events.collect { direction ->
                    onVolumeButton(direction)
                }
            }
        }

        fun updateHeadphoneState() {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val headphone =
                devices.firstOrNull { device ->
                    device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        device.type == AudioDeviceInfo.TYPE_USB_HEADSET
                }
            _state.update {
                it.copy(
                    earpieceAvailable =
                        devices.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE },
                    headphonePlugged = headphone != null,
                    headphoneType = headphone?.let { headphoneTypeCode(it.type) },
                )
            }
        }

        fun playTone(
            frequencyHz: Int,
            streamType: Int = AudioManager.STREAM_MUSIC,
        ) {
            stopTone()
            stopPlayback()
            _state.update { it.copy(error = null) }
            val route =
                if (streamType == AudioManager.STREAM_VOICE_CALL) {
                    AudioOutputRoute.EARPIECE
                } else {
                    AudioOutputRoute.MEDIA
                }
            val routeSession = openRoute(route) ?: return
            val operationToken = toneGate.start()
            _state.update { it.copy(isPlaying = true, currentFrequency = frequencyHz) }

            toneJob =
                viewModelScope.launch(ioDispatcher) {
                    playGeneratedTone(
                        routeSession = routeSession,
                        frequencyHz = frequencyHz,
                        channelMask = AudioFormat.CHANNEL_OUT_MONO,
                        usage =
                            if (streamType == AudioManager.STREAM_VOICE_CALL) {
                                AudioAttributes.USAGE_VOICE_COMMUNICATION
                            } else {
                                AudioAttributes.USAGE_MEDIA
                            },
                        contentType =
                            if (streamType == AudioManager.STREAM_VOICE_CALL) {
                                AudioAttributes.CONTENT_TYPE_SPEECH
                            } else {
                                AudioAttributes.CONTENT_TYPE_MUSIC
                            },
                        operationToken = operationToken,
                    )
                }
        }

        fun playStereoTone(channel: StereoChannel) {
            stopTone()
            stopPlayback()
            _state.update { it.copy(error = null) }
            val routeSession = openRoute(AudioOutputRoute.MEDIA) ?: return
            val operationToken = toneGate.start()
            _state.update { it.copy(isPlaying = true, stereoChannel = channel) }
            val frequencyHz = 440

            toneJob =
                viewModelScope.launch(ioDispatcher) {
                    playGeneratedTone(
                        routeSession = routeSession,
                        frequencyHz = frequencyHz,
                        channelMask = AudioFormat.CHANNEL_OUT_STEREO,
                        usage = AudioAttributes.USAGE_MEDIA,
                        contentType = AudioAttributes.CONTENT_TYPE_MUSIC,
                        stereoChannel = channel,
                        operationToken = operationToken,
                    )
                }
        }

        private fun CoroutineScope.playGeneratedTone(
            routeSession: AudioRouteSession,
            frequencyHz: Int,
            channelMask: Int,
            usage: Int,
            contentType: Int,
            operationToken: Long,
            stereoChannel: StereoChannel? = null,
        ) {
            var track: AudioTrack? = null
            try {
                val created =
                    createAudioTrack(channelMask, usage, contentType)
                        ?: run {
                            if (toneGate.isCurrent(operationToken)) {
                                _state.update { it.copy(error = AudioOperationError.OUTPUT_UNAVAILABLE) }
                            }
                            return
                        }
                if (!toneGate.isCurrent(operationToken)) {
                    stopAndRelease(created.first)
                    return
                }
                track = created.first
                toneOwner.replace(created.first)
                created.first.play()
                val buffer = ShortArray(created.second / 2)
                var phase = 0.0
                val phaseIncrement = 2.0 * PI * frequencyHz / sampleRate
                while (isActive) {
                    phase = fillToneBuffer(buffer, phase, phaseIncrement, stereoChannel)
                    if (created.first.write(buffer, 0, buffer.size) <= 0) {
                        if (toneGate.isCurrent(operationToken)) {
                            _state.update { it.copy(error = AudioOperationError.OUTPUT_UNAVAILABLE) }
                        }
                        break
                    }
                }
            } catch (_: IllegalStateException) {
                if (toneGate.isCurrent(operationToken)) {
                    _state.update { it.copy(error = AudioOperationError.OUTPUT_UNAVAILABLE) }
                }
            } finally {
                track?.let(toneOwner::release)
                routeOwner.release(routeSession)
                if (toneGate.isCurrent(operationToken)) {
                    _state.update { it.copy(isPlaying = false) }
                }
            }
        }

        private fun fillToneBuffer(
            buffer: ShortArray,
            initialPhase: Double,
            phaseIncrement: Double,
            stereoChannel: StereoChannel?,
        ): Double {
            var phase = initialPhase
            if (stereoChannel == null) {
                for (index in buffer.indices) {
                    buffer[index] = (sin(phase) * Short.MAX_VALUE).toInt().toShort()
                    phase += phaseIncrement
                }
                return phase
            }
            var index = 0
            while (index < buffer.size - 1) {
                val sample = (sin(phase) * Short.MAX_VALUE).toInt().toShort()
                buffer[index] = if (stereoChannel == StereoChannel.RIGHT) 0 else sample
                buffer[index + 1] = if (stereoChannel == StereoChannel.LEFT) 0 else sample
                phase += phaseIncrement
                index += 2
            }
            return phase
        }

        fun playEarpieceTone() {
            playTone(1000, AudioManager.STREAM_VOICE_CALL)
        }

        fun stopTone() {
            toneGate.cancel()
            toneJob?.cancel()
            toneJob = null
            toneOwner.release()
            routeOwner.release()
            _state.update { it.copy(isPlaying = false) }
        }

        @Suppress("MissingPermission")
        fun startRecording(maxDurationMs: Long = DEFAULT_RECORDING_DURATION_MS) {
            val permissionGranted =
                ContextCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.RECORD_AUDIO,
                ) == PackageManager.PERMISSION_GRANTED
            val hasMicrophone =
                getApplication<Application>()
                    .packageManager
                    .hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
            if (!AudioRecordingPolicy.canStart(hasMicrophone, permissionGranted, _state.value.isRecording)) return
            cancelRecording()
            _state.update {
                it.copy(
                    hasRecordedAudio = false,
                    relativeInputLevel = 0f,
                    error = null,
                )
            }
            val bufferSize =
                AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                )
            if (bufferSize <= 0) {
                _state.update { it.copy(error = AudioOperationError.RECORDING_UNAVAILABLE) }
                return
            }

            val record =
                try {
                    AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                    )
                } catch (_: RuntimeException) {
                    _state.update { it.copy(error = AudioOperationError.RECORDING_UNAVAILABLE) }
                    return
                }

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                record.release()
                _state.update { it.copy(error = AudioOperationError.RECORDING_UNAVAILABLE) }
                return
            }

            recordOwner.replace(record)
            val operationToken = recordGate.start()
            _state.update { it.copy(isRecording = true, relativeInputLevel = 0f) }

            val maxRecordSamples =
                (
                    sampleRate.toLong() * maxDurationMs / MILLIS_PER_SECOND
                ).coerceAtLeast(sampleRate.toLong() / 2).toInt()

            recordJob =
                viewModelScope.launch(ioDispatcher) {
                    captureRecording(record, bufferSize, maxRecordSamples, operationToken)
                }
        }

        private fun CoroutineScope.captureRecording(
            record: AudioRecord,
            bufferSize: Int,
            maxRecordSamples: Int,
            operationToken: Long,
        ) {
            val allSamples = ShortArray(maxRecordSamples)
            val buffer = ShortArray(bufferSize / 2)
            var totalSamples = 0
            var failed = false
            try {
                if (!recordGate.isCurrent(operationToken)) return
                record.startRecording()
                while (isActive && totalSamples < maxRecordSamples) {
                    val read = record.read(buffer, 0, buffer.size)
                    if (read <= 0) {
                        failed = isActive
                        break
                    }
                    val copyCount = minOf(read, maxRecordSamples - totalSamples)
                    buffer.copyInto(allSamples, totalSamples, 0, copyCount)
                    totalSamples += copyCount
                    if (recordGate.isCurrent(operationToken)) {
                        _state.update {
                            it.copy(
                                relativeInputLevel = RelativeInputLevel.fromPcm16(buffer, read),
                            )
                        }
                    }
                }
            } catch (_: IllegalStateException) {
                // Recording can become unavailable after the permission and hardware checks.
                failed = isActive
            } catch (_: SecurityException) {
                // Permission can be revoked between the check and AudioRecord.startRecording().
                failed = isActive
            } finally {
                recordOwner.release(record)
                if (recordGate.isCurrent(operationToken)) {
                    recordedData = allSamples.copyOf(totalSamples)
                    _state.update {
                        it.copy(
                            isRecording = false,
                            hasRecordedAudio = totalSamples > 0,
                            error = AudioOperationError.RECORDING_UNAVAILABLE.takeIf { failed },
                        )
                    }
                }
                buffer.fill(0)
                allSamples.fill(0)
            }
        }

        fun stopRecording() {
            stopRecording(AudioRecordingStopMode.KEEP_RESULT)
        }

        fun cancelRecording() {
            stopRecording(AudioRecordingStopMode.DISCARD_RESULT)
            discardRecordedSamples()
        }

        private fun stopRecording(mode: AudioRecordingStopMode) {
            recordGate.stop(mode)
            recordJob?.cancel()
            recordJob = null
            recordOwner.release()
            _state.update { it.copy(isRecording = false) }
        }

        fun discardRecordedSamples() {
            recordedData?.fill(0)
            recordedData = null
        }

        fun playRecording() {
            val data = recordedData ?: return
            stopTone()
            stopPlayback()
            _state.update { it.copy(error = null) }
            val routeSession = openRoute(AudioOutputRoute.MEDIA) ?: return
            val operationToken = playbackGate.start()
            _state.update { it.copy(isPlayingRecording = true) }

            playbackJob =
                viewModelScope.launch(ioDispatcher) {
                    var track: AudioTrack? = null
                    try {
                        val created =
                            createAudioTrack(
                                channelMask = AudioFormat.CHANNEL_OUT_MONO,
                                usage = AudioAttributes.USAGE_MEDIA,
                                contentType = AudioAttributes.CONTENT_TYPE_MUSIC,
                            )?.first
                                ?: run {
                                    if (playbackGate.isCurrent(operationToken)) {
                                        _state.update { it.copy(error = AudioOperationError.OUTPUT_UNAVAILABLE) }
                                    }
                                    return@launch
                                }
                        if (!playbackGate.isCurrent(operationToken)) {
                            stopAndRelease(created)
                            return@launch
                        }
                        track = created
                        playbackOwner.replace(created)
                        created.play()
                        if (created.write(data, 0, data.size) <= 0 && playbackGate.isCurrent(operationToken)) {
                            _state.update { it.copy(error = AudioOperationError.OUTPUT_UNAVAILABLE) }
                        }
                    } catch (_: IllegalStateException) {
                        if (playbackGate.isCurrent(operationToken)) {
                            _state.update { it.copy(error = AudioOperationError.OUTPUT_UNAVAILABLE) }
                        }
                    } finally {
                        track?.let(playbackOwner::release)
                        routeOwner.release(routeSession)
                        if (playbackGate.isCurrent(operationToken)) {
                            _state.update { it.copy(isPlayingRecording = false) }
                        }
                    }
                }
        }

        private fun createAudioTrack(
            channelMask: Int,
            usage: Int,
            contentType: Int,
        ): Pair<AudioTrack, Int>? {
            val bufferSize =
                AudioTrack.getMinBufferSize(
                    sampleRate,
                    channelMask,
                    AudioFormat.ENCODING_PCM_16BIT,
                )
            if (bufferSize <= 0) return null
            val track =
                try {
                    AudioTrack
                        .Builder()
                        .setAudioAttributes(
                            AudioAttributes
                                .Builder()
                                .setUsage(usage)
                                .setContentType(contentType)
                                .build(),
                        ).setAudioFormat(
                            AudioFormat
                                .Builder()
                                .setSampleRate(sampleRate)
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setChannelMask(channelMask)
                                .build(),
                        ).setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } catch (_: RuntimeException) {
                    return null
                }
            if (track.state != AudioTrack.STATE_INITIALIZED) {
                track.release()
                return null
            }
            return track to bufferSize
        }

        fun stopPlayback() {
            playbackGate.cancel()
            playbackJob?.cancel()
            playbackJob = null
            playbackOwner.release()
            routeOwner.release()
            _state.update { it.copy(isPlayingRecording = false) }
        }

        fun recordManualResult(
            check: AudioManualCheck,
            passed: Boolean,
        ) {
            _state.update { it.copy(manualResults = it.manualResults + (check to passed)) }
        }

        fun updateVolumeState() {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            _state.update { it.copy(volumeLevel = current, maxVolume = max) }
        }

        private fun onVolumeButton(direction: VolumeButtonDirection) {
            _state.update { it.recordVolumeButton(direction) }
            updateVolumeState()
            viewModelScope.launch {
                delay(300)
                _state.update {
                    when (direction) {
                        VolumeButtonDirection.UP -> it.copy(volumeUpPressed = false)
                        VolumeButtonDirection.DOWN -> it.copy(volumeDownPressed = false)
                    }
                }
            }
        }

        fun resetVolumeTest() {
            _state.update {
                it.copy(
                    volumeUpPressed = false,
                    volumeDownPressed = false,
                    volumeUpCount = 0,
                    volumeDownCount = 0,
                )
            }
            updateVolumeState()
        }

        override fun onCleared() {
            stopTone()
            cancelRecording()
            stopPlayback()
        }

        private fun stopAndRelease(track: AudioTrack) {
            runCatching { track.stop() }
            runCatching { track.release() }
        }

        private fun stopAndRelease(record: AudioRecord) {
            runCatching { record.stop() }
            runCatching { record.release() }
        }

        private fun openRoute(route: AudioOutputRoute): AudioRouteSession? {
            routeOwner.release()
            val session = AudioRouteSession(routeController)
            if (!session.open(route)) {
                _state.update { it.copy(error = AudioOperationError.OUTPUT_UNAVAILABLE) }
                return null
            }
            routeOwner.replace(session)
            return session
        }

        companion object {
            const val DEFAULT_RECORDING_DURATION_MS = 10_000L
            private const val MILLIS_PER_SECOND = 1_000L
        }
    }

internal fun headphoneTypeCode(type: Int): HeadphoneTypeCode =
    when (type) {
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> HeadphoneTypeCode.WIRED_HEADSET
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> HeadphoneTypeCode.WIRED_HEADPHONES
        AudioDeviceInfo.TYPE_USB_HEADSET -> HeadphoneTypeCode.USB_HEADSET
        else -> HeadphoneTypeCode.UNKNOWN
    }
