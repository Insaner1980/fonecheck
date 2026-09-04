package com.insaner.fonecheck.ui.screens.audio

import kotlin.math.sqrt

class AudioResourceOwner<T : Any>(
    private val stopAndRelease: (T) -> Unit,
) {
    private var current: T? = null

    @Synchronized
    fun replace(resource: T) {
        if (current === resource) return
        val previous = current
        current = resource
        previous?.let(stopAndRelease)
    }

    @Synchronized
    fun release() {
        val resource = current ?: return
        current = null
        stopAndRelease(resource)
    }

    @Synchronized
    fun release(resource: T) {
        if (current !== resource) return
        current = null
        stopAndRelease(resource)
    }
}

class AudioOperationGate {
    private var generation = 0L

    @Synchronized
    fun start(): Long = ++generation

    @Synchronized
    fun cancel() {
        generation += 1
    }

    @Synchronized
    fun isCurrent(token: Long): Boolean = token == generation
}

enum class AudioRecordingStopMode {
    KEEP_RESULT,
    DISCARD_RESULT,
}

fun AudioOperationGate.stop(mode: AudioRecordingStopMode) {
    if (mode == AudioRecordingStopMode.DISCARD_RESULT) cancel()
}

enum class AudioOutputRoute {
    MEDIA,
    EARPIECE,
}

interface AudioRouteController {
    fun request(route: AudioOutputRoute): Boolean

    fun clear()
}

class AudioRouteSession(
    private val controller: AudioRouteController,
) {
    private var isOpen = false

    @Synchronized
    fun open(route: AudioOutputRoute): Boolean {
        close()
        isOpen = controller.request(route)
        return isOpen
    }

    @Synchronized
    fun close() {
        if (!isOpen) return
        isOpen = false
        controller.clear()
    }
}

object AudioRecordingPolicy {
    fun canStart(
        hasMicrophone: Boolean,
        permissionGranted: Boolean,
        isRecording: Boolean,
    ): Boolean = hasMicrophone && permissionGranted && !isRecording
}

object RelativeInputLevel {
    fun fromPcm16(
        samples: ShortArray,
        count: Int,
    ): Float {
        if (count <= 0) return 0f
        var sum = 0.0
        for (index in 0 until count.coerceAtMost(samples.size)) {
            val sample = samples[index].toDouble()
            sum += sample * sample
        }
        val rms = sqrt(sum / count.coerceAtMost(samples.size).coerceAtLeast(1))
        return (rms / 32768.0).toFloat().coerceIn(0f, 1f)
    }
}
