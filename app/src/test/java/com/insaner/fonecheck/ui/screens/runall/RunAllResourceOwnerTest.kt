package com.insaner.fonecheck.ui.screens.runall

import org.junit.Assert.assertEquals
import org.junit.Test

class RunAllResourceOwnerTest {
    @Test
    fun eachActiveStageStopsOnlyItsOwnedResources() {
        val stopped = mutableListOf<String>()
        val owner = resourceOwner(stopped)

        owner.stopStage(RunAllStage.AUTOMATIC)
        assertEquals(listOf("device", "performance", "sim", "microphone", "gps", "storage"), stopped)

        val expected =
            mapOf(
                RunAllStage.DISPLAY to listOf("display"),
                RunAllStage.AUDIO to listOf("audio"),
                RunAllStage.CAMERA to listOf("camera"),
                RunAllStage.SENSORS to listOf("sensors"),
                RunAllStage.VIBRATION to listOf("vibration"),
                RunAllStage.BUTTONS to listOf("buttons"),
                RunAllStage.BIOMETRICS to listOf("biometrics"),
            )
        expected.forEach { (stage, actions) ->
            stopped.clear()
            owner.stopStage(stage)
            assertEquals(actions, stopped)
        }
    }

    @Test
    fun cancellingTheRunStopsEveryResourceIdempotently() {
        val stopped = mutableListOf<String>()
        val owner = resourceOwner(stopped)

        owner.stopAll()
        owner.stopAll()

        assertEquals(allStopOrder, stopped)
    }

    @Test
    fun cleanupFailureDoesNotPreventRemainingResourcesFromStopping() {
        val stopped = mutableListOf<String>()
        val owner =
            resourceOwner(stopped) {
                stopped += "performance"
                error("cleanup failed")
            }

        owner.stopAll()
        owner.stopAll()

        assertEquals(allStopOrder, stopped)
    }

    private fun resourceOwner(
        stopped: MutableList<String>,
        stopPerformance: () -> Unit = { stopped += "performance" },
    ) = RunAllResourceOwner(
        stopDeviceInfo = { stopped += "device" },
        stopPerformance = stopPerformance,
        stopSimInfo = { stopped += "sim" },
        stopMicrophone = { stopped += "microphone" },
        stopGps = { stopped += "gps" },
        stopStorage = { stopped += "storage" },
        stopDisplay = { stopped += "display" },
        stopAudio = { stopped += "audio" },
        stopCamera = { stopped += "camera" },
        stopSensors = { stopped += "sensors" },
        stopVibration = { stopped += "vibration" },
        stopButtons = { stopped += "buttons" },
        stopBiometrics = { stopped += "biometrics" },
        stopThermal = { stopped += "thermal" },
    )

    private val allStopOrder =
        listOf(
            "device",
            "performance",
            "sim",
            "microphone",
            "gps",
            "storage",
            "display",
            "audio",
            "camera",
            "sensors",
            "vibration",
            "buttons",
            "biometrics",
            "thermal",
        )
}
