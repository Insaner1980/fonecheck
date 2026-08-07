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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

data class AudioTestState(
    val isPlaying: Boolean = false,
    val isRecording: Boolean = false,
    val currentFrequency: Int = 440,
    val stereoChannel: StereoChannel = StereoChannel.BOTH,
    val relativeInputLevel: Float = 0f,
    val hasRecordedAudio: Boolean = false,
    val isPlayingRecording: Boolean = false,
    val headphonePlugged: Boolean = false,
    val headphoneType: String? = null,
    val volumeLevel: Int = 0,
    val maxVolume: Int = 15,
    val volumeStream: Int = AudioManager.STREAM_MUSIC,
    val volumeUpPressed: Boolean = false,
    val volumeDownPressed: Boolean = false,
    val volumeUpCount: Int = 0,
    val volumeDownCount: Int = 0,
    val manualResults: Map<AudioManualCheck, Boolean> = emptyMap(),
)

enum class StereoChannel { LEFT, RIGHT, BOTH }

@HiltViewModel
class AudioTestViewModel
    @Inject
    constructor(
        application: Application,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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

        private val sampleRate = 44100

        fun updateHeadphoneState() {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val headphone =
                devices.firstOrNull { device ->
                    device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                        device.type == AudioDeviceInfo.TYPE_USB_HEADSET
                }
            _state.value =
                _state.value.copy(
                    headphonePlugged = headphone != null,
                    headphoneType = headphone?.let { getHeadphoneTypeName(it.type) },
                )
        }

        private fun getHeadphoneTypeName(type: Int): String =
            when (type) {
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset"
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
                AudioDeviceInfo.TYPE_USB_HEADSET -> "USB Headset"
                else -> "Unknown"
            }

        fun playTone(
            frequencyHz: Int,
            streamType: Int = AudioManager.STREAM_MUSIC,
        ) {
            stopTone()
            stopPlayback()
            val route = if (streamType == AudioManager.STREAM_VOICE_CALL) AudioOutputRoute.EARPIECE else AudioOutputRoute.MEDIA
            val routeSession = openRoute(route) ?: return
            _state.value = _state.value.copy(isPlaying = true, currentFrequency = frequencyHz)

            toneJob =
                viewModelScope.launch(ioDispatcher) {
                    var track: AudioTrack? = null
                    try {
                        val created =
                            createAudioTrack(
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
                            )
                        track = created.first
                        toneOwner.replace(created.first)
                        created.first.play()
                        val bufferSize = created.second
                        val buffer = ShortArray(bufferSize / 2)
                        var phase = 0.0
                        val phaseIncrement = 2.0 * PI * frequencyHz / sampleRate
                        while (isActive) {
                            for (i in buffer.indices) {
                                buffer[i] = (sin(phase) * Short.MAX_VALUE).toInt().toShort()
                                phase += phaseIncrement
                            }
                            created.first.write(buffer, 0, buffer.size)
                        }
                    } finally {
                        track?.let(toneOwner::release)
                        routeOwner.release(routeSession)
                        _state.value = _state.value.copy(isPlaying = false)
                    }
                }
        }

        fun playStereoTone(channel: StereoChannel) {
            stopTone()
            stopPlayback()
            val routeSession = openRoute(AudioOutputRoute.MEDIA) ?: return
            _state.value = _state.value.copy(isPlaying = true, stereoChannel = channel)
            val frequencyHz = 440

            toneJob =
                viewModelScope.launch(ioDispatcher) {
                    var track: AudioTrack? = null
                    try {
                        val created =
                            createAudioTrack(
                                channelMask = AudioFormat.CHANNEL_OUT_STEREO,
                                usage = AudioAttributes.USAGE_MEDIA,
                                contentType = AudioAttributes.CONTENT_TYPE_MUSIC,
                            )
                        track = created.first
                        toneOwner.replace(created.first)
                        created.first.play()
                        val bufferSize = created.second
                        val buffer = ShortArray(bufferSize / 2)
                        var phase = 0.0
                        val phaseIncrement = 2.0 * PI * frequencyHz / sampleRate
                        while (isActive) {
                            var i = 0
                            while (i < buffer.size - 1) {
                                val sample = (sin(phase) * Short.MAX_VALUE).toInt().toShort()
                                when (channel) {
                                    StereoChannel.LEFT -> {
                                        buffer[i] = sample
                                        buffer[i + 1] = 0
                                    }
                                    StereoChannel.RIGHT -> {
                                        buffer[i] = 0
                                        buffer[i + 1] = sample
                                    }
                                    StereoChannel.BOTH -> {
                                        buffer[i] = sample
                                        buffer[i + 1] = sample
                                    }
                                }
                                phase += phaseIncrement
                                i += 2
                            }
                            created.first.write(buffer, 0, buffer.size)
                        }
                    } finally {
                        track?.let(toneOwner::release)
                        routeOwner.release(routeSession)
                        _state.value = _state.value.copy(isPlaying = false)
                    }
                }
        }

        fun playEarpieceTone() {
            playTone(1000, AudioManager.STREAM_VOICE_CALL)
        }

        fun stopTone() {
            toneJob?.cancel()
            toneJob = null
            toneOwner.release()
            routeOwner.release()
            _state.value = _state.value.copy(isPlaying = false)
        }

        @Suppress("MissingPermission")
        fun startRecording(maxDurationMs: Long = DEFAULT_RECORDING_DURATION_MS) {
            val permissionGranted =
                ContextCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.RECORD_AUDIO,
                ) == PackageManager.PERMISSION_GRANTED
            val hasMicrophone = getApplication<Application>().packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
            if (!AudioRecordingPolicy.canStart(hasMicrophone, permissionGranted, _state.value.isRecording)) return
            stopRecording()
            val bufferSize =
                AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                )
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) return

            val record =
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                record.release()
                return
            }

            recordOwner.replace(record)
            _state.value = _state.value.copy(isRecording = true, relativeInputLevel = 0f)

            val maxRecordSamples =
                (
                    sampleRate.toLong() * maxDurationMs / MILLIS_PER_SECOND
                ).coerceAtLeast(sampleRate.toLong() / 2).toInt()
            val allSamples = ShortArray(maxRecordSamples)
            var totalSamples = 0

            recordJob =
                viewModelScope.launch(ioDispatcher) {
                    try {
                        record.startRecording()
                        val buffer = ShortArray(bufferSize / 2)
                        while (isActive && totalSamples < maxRecordSamples) {
                            val read = record.read(buffer, 0, buffer.size)
                            if (read > 0) {
                                val copyCount = minOf(read, maxRecordSamples - totalSamples)
                                buffer.copyInto(allSamples, totalSamples, 0, copyCount)
                                totalSamples += copyCount
                                _state.value =
                                    _state.value.copy(
                                        relativeInputLevel = RelativeInputLevel.fromPcm16(buffer, read),
                                    )
                            }
                        }
                    } finally {
                        recordOwner.release(record)
                        recordedData = allSamples.copyOf(totalSamples)
                        _state.value =
                            _state.value.copy(
                                isRecording = false,
                                hasRecordedAudio = totalSamples > 0,
                            )
                    }
                }
        }

        fun stopRecording() {
            recordJob?.cancel()
            recordJob = null
            recordOwner.release()
            _state.value = _state.value.copy(isRecording = false)
        }

        fun playRecording() {
            val data = recordedData ?: return
            stopTone()
            stopPlayback()
            val routeSession = openRoute(AudioOutputRoute.MEDIA) ?: return
            _state.value = _state.value.copy(isPlayingRecording = true)

            playbackJob =
                viewModelScope.launch(ioDispatcher) {
                    var track: AudioTrack? = null
                    try {
                        val created =
                            createAudioTrack(
                                channelMask = AudioFormat.CHANNEL_OUT_MONO,
                                usage = AudioAttributes.USAGE_MEDIA,
                                contentType = AudioAttributes.CONTENT_TYPE_MUSIC,
                            ).first
                        track = created
                        playbackOwner.replace(created)
                        created.play()
                        created.write(data, 0, data.size)
                    } finally {
                        track?.let(playbackOwner::release)
                        routeOwner.release(routeSession)
                        _state.value = _state.value.copy(isPlayingRecording = false)
                    }
                }
        }

        private fun createAudioTrack(
            channelMask: Int,
            usage: Int,
            contentType: Int,
        ): Pair<AudioTrack, Int> {
            val bufferSize =
                AudioTrack.getMinBufferSize(
                    sampleRate,
                    channelMask,
                    AudioFormat.ENCODING_PCM_16BIT,
                )
            val track =
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
            return track to bufferSize
        }

        fun stopPlayback() {
            playbackJob?.cancel()
            playbackJob = null
            playbackOwner.release()
            routeOwner.release()
            _state.value = _state.value.copy(isPlayingRecording = false)
        }

        fun recordManualResult(
            check: AudioManualCheck,
            passed: Boolean,
        ) {
            _state.value = _state.value.copy(manualResults = _state.value.manualResults + (check to passed))
        }

        fun updateVolumeState() {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            _state.value = _state.value.copy(volumeLevel = current, maxVolume = max)
        }

        fun onVolumeUp() {
            val s = _state.value
            _state.value =
                s.copy(
                    volumeUpPressed = true,
                    volumeUpCount = s.volumeUpCount + 1,
                )
            updateVolumeState()
            viewModelScope.launch {
                delay(300)
                _state.value = _state.value.copy(volumeUpPressed = false)
            }
        }

        fun onVolumeDown() {
            val s = _state.value
            _state.value =
                s.copy(
                    volumeDownPressed = true,
                    volumeDownCount = s.volumeDownCount + 1,
                )
            updateVolumeState()
            viewModelScope.launch {
                delay(300)
                _state.value = _state.value.copy(volumeDownPressed = false)
            }
        }

        fun resetVolumeTest() {
            _state.value =
                _state.value.copy(
                    volumeUpPressed = false,
                    volumeDownPressed = false,
                    volumeUpCount = 0,
                    volumeDownCount = 0,
                )
            updateVolumeState()
        }

        override fun onCleared() {
            super.onCleared()
            stopTone()
            stopRecording()
            stopPlayback()
            recordedData = null
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
            if (!session.open(route)) return null
            routeOwner.replace(session)
            return session
        }

        companion object {
            const val DEFAULT_RECORDING_DURATION_MS = 10_000L
            private const val MILLIS_PER_SECOND = 1_000L
        }
    }
