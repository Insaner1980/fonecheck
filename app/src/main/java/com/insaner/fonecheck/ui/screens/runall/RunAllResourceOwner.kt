package com.insaner.fonecheck.ui.screens.runall

class RunAllResourceOwner(
    private val stopDeviceInfo: () -> Unit,
    private val stopPerformance: () -> Unit,
    private val stopSimInfo: () -> Unit,
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
            RunAllStage.AUTOMATIC ->
                stopEach(
                    stopDeviceInfo,
                    stopPerformance,
                    stopSimInfo,
                    stopMicrophone,
                    stopGps,
                    stopStorage,
                )

            RunAllStage.DISPLAY -> stopEach(stopDisplay)
            RunAllStage.AUDIO -> stopEach(stopAudio)
            RunAllStage.CAMERA -> stopEach(stopCamera)
            RunAllStage.SENSORS -> stopEach(stopSensors)
            RunAllStage.VIBRATION -> stopEach(stopVibration)
            RunAllStage.BUTTONS -> stopEach(stopButtons)
            RunAllStage.BIOMETRICS -> stopEach(stopBiometrics)
            RunAllStage.PREFLIGHT,
            RunAllStage.PERMISSIONS,
            RunAllStage.RESULTS,
            -> Unit
        }
    }

    fun stopAll() {
        if (allStopped) return
        allStopped = true
        stopEach(
            stopDeviceInfo,
            stopPerformance,
            stopSimInfo,
            stopMicrophone,
            stopGps,
            stopStorage,
            stopDisplay,
            stopAudio,
            stopCamera,
            stopSensors,
            stopVibration,
            stopButtons,
            stopBiometrics,
            stopThermal,
        )
    }

    private fun stopEach(vararg stops: () -> Unit) {
        stops.forEach { stop ->
            try {
                stop()
            } catch (_: Exception) {
                // Cleanup is best-effort; one resource must not prevent the remaining releases.
            }
        }
    }
}
