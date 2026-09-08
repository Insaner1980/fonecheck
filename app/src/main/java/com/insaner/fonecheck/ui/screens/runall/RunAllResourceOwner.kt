package com.insaner.fonecheck.ui.screens.runall

import kotlinx.coroutines.Job

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
    private var automaticExecution: Job? = null

    internal fun beginAutomatic(execution: Job) {
        stopAutomatic()
        allStopped = false
        automaticExecution = execution
    }

    internal fun endAutomatic(execution: Job) {
        // An obsolete coroutine's finally must never release a newer execution's resources.
        if (automaticExecution !== execution) return
        automaticExecution = null
        stopAutomaticResources()
    }

    private fun stopAutomatic() {
        val execution = automaticExecution ?: return
        automaticExecution = null
        execution.cancel()
        stopAutomaticResources()
    }

    private fun stopAutomaticResources() =
        stopEach(stopDeviceInfo, stopPerformance, stopSimInfo, stopMicrophone, stopGps, stopStorage)

    fun markRunStarted() {
        allStopped = false
    }

    fun stopStage(stage: RunAllStage) {
        when (stage) {
            RunAllStage.AUTOMATIC -> {
                if (automaticExecution != null) stopAutomatic() else stopAutomaticResources()
            }

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
        automaticExecution?.cancel()
        automaticExecution = null
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
