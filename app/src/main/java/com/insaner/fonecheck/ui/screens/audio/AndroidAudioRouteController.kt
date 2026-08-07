package com.insaner.fonecheck.ui.screens.audio

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

class AndroidAudioRouteController(
    private val audioManager: AudioManager,
) : AudioRouteController {
    private val focusRequest =
        AudioFocusRequest
            .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            ).setOnAudioFocusChangeListener {}
            .build()
    private var previousMode = AudioManager.MODE_NORMAL
    private var earpieceRouteActive = false

    override fun request(route: AudioOutputRoute): Boolean {
        if (audioManager.requestAudioFocus(focusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return false
        }
        if (route == AudioOutputRoute.EARPIECE) {
            previousMode = audioManager.mode
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            earpieceRouteActive = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val earpiece =
                    audioManager.availableCommunicationDevices.firstOrNull {
                        it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                    }
                if (earpiece != null) audioManager.setCommunicationDevice(earpiece)
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = false
            }
        }
        return true
    }

    override fun clear() {
        if (earpieceRouteActive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.clearCommunicationDevice()
            }
            audioManager.mode = previousMode
            earpieceRouteActive = false
        }
        audioManager.abandonAudioFocusRequest(focusRequest)
    }
}
