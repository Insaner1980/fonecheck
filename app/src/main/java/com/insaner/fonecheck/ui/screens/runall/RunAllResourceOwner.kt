package com.insaner.fonecheck.ui.screens.runall

class RunAllResourceOwner(
    private val stopPerformance: () -> Unit,
    private val stopMicrophone: () -> Unit,
    private val stopGps: () -> Unit,
    private val stopStorage: () -> Unit,
    private val stopDisplay: () -> Unit,
    private val stopAudio: () -> Unit,
    private val stopCamera: () -> Unit,
    private val stopSensors: () -> Unit,
    private val stopVibration: () -> Unit,
    private val stopButtons: () -> Unit,
    private val stopBiometrics: () -> Unit,
    private val stopThermal: () -> Unit,
) {
    private var allStopped = false

    fun markRunStarted() {
        allStopped = false
    }

    fun stopStage(stage: RunAllStage) {
        when (stage) {
            RunAllStage.AUTOMATIC -> {
                stopPerformance()
                stopMicrophone()
                stopGps()
                stopStorage()
            }

            RunAllStage.DISPLAY -> stopDisplay()
            RunAllStage.AUDIO -> stopAudio()
            RunAllStage.CAMERA -> stopCamera()
            RunAllStage.SENSORS -> stopSensors()
            RunAllStage.VIBRATION -> stopVibration()
            RunAllStage.BUTTONS -> stopButtons()
            RunAllStage.BIOMETRICS -> stopBiometrics()
            RunAllStage.PREFLIGHT,
            RunAllStage.PERMISSIONS,
            RunAllStage.RESULTS,
            -> Unit
        }
    }

    fun stopAll() {
        if (allStopped) return
        allStopped = true
        stopPerformance()
        stopMicrophone()
        stopGps()
        stopStorage()
        stopDisplay()
        stopAudio()
        stopCamera()
        stopSensors()
        stopVibration()
        stopButtons()
        stopBiometrics()
        stopThermal()
    }
}
