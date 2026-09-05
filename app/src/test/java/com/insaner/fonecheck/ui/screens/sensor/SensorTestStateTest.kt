package com.insaner.fonecheck.ui.screens.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SensorTestStateTest {
    @Test
    fun listenerRegistrationFailureClearsChallengeAndSamplingState() {
        val state =
            SensorTestState(
                guidedTests =
                    listOf(
                        GuidedSensorTestState(
                            code = GuidedSensorCode.ACCELEROMETER,
                            sensorType = SensorType.ACCELEROMETER,
                            status = GuidedSensorStatus.SAMPLING,
                        ),
                    ),
                activeSensorType = 1,
                challenge =
                    ChallengeState(
                        challenge = InteractiveChallenge.SHAKE,
                        sensorCode = GuidedSensorCode.ACCELEROMETER,
                    ),
            )

        val failed = state.withChallengeListenerFailure()

        assertEquals(ChallengeState(), failed.challenge)
        assertNull(failed.activeSensorType)
        assertEquals(GuidedSensorStatus.NOT_TESTED, failed.guidedTests.single().status)
        assertEquals("listener_registration_failed", failed.error)
    }
}
