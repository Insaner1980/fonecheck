package com.insaner.fonecheck.ui.screens.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorRuntimePolicyTest {
    @Test
    fun catalogKeepsEveryGuidedSensorAndMarksMissingHardwareUnavailable() {
        val tests =
            GuidedSensorCatalog
                .create(
                    availableTypes = setOf(SensorType.ACCELEROMETER, SensorType.LIGHT, SensorType.STEP_COUNTER),
                ).associateBy { it.code }

        assertEquals(GuidedSensorStatus.NOT_TESTED, tests.getValue(GuidedSensorCode.ACCELEROMETER).status)
        assertEquals(GuidedSensorStatus.NOT_TESTED, tests.getValue(GuidedSensorCode.LIGHT).status)
        assertEquals(SensorType.STEP_COUNTER, tests.getValue(GuidedSensorCode.STEP).sensorType)
        assertEquals(GuidedSensorStatus.NOT_AVAILABLE, tests.getValue(GuidedSensorCode.GYROSCOPE).status)
        assertEquals(GuidedSensorCode.entries.toSet(), tests.keys)
    }

    @Test
    fun stepDetectorIsPreferredOverCumulativeCounter() {
        val step =
            GuidedSensorCatalog
                .create(setOf(SensorType.STEP_COUNTER, SensorType.STEP_DETECTOR))
                .single { it.code == GuidedSensorCode.STEP }

        assertEquals(SensorType.STEP_DETECTOR, step.sensorType)
    }

    @Test
    fun boundedSamplingRequiresFiniteSamplesAndSensorSpecificEvidence() {
        val barometer = GuidedSensorSampler(GuidedSensorCode.BAROMETER)
        repeat(GuidedSensorSampler.REQUIRED_SAMPLE_COUNT - 1) {
            assertFalse(barometer.accept(floatArrayOf(1_013.2f)).passed)
        }
        assertTrue(barometer.accept(floatArrayOf(1_013.1f)).passed)

        val light = GuidedSensorSampler(GuidedSensorCode.LIGHT)
        repeat(GuidedSensorSampler.REQUIRED_SAMPLE_COUNT) {
            assertFalse(light.accept(floatArrayOf(100f)).passed)
        }
        assertTrue(light.accept(floatArrayOf(120f)).passed)

        val invalid = GuidedSensorSampler(GuidedSensorCode.MAGNETOMETER)
        assertEquals(0, invalid.accept(floatArrayOf(Float.NaN, 1f, 2f)).sampleCount)
    }

    @Test
    fun motionStepAndProximityThresholdsDoNotPassFromIdleSamples() {
        val accelerometer = GuidedSensorSampler(GuidedSensorCode.ACCELEROMETER)
        repeat(GuidedSensorSampler.REQUIRED_SAMPLE_COUNT) {
            assertFalse(accelerometer.accept(floatArrayOf(0f, 0f, 9.81f)).passed)
        }
        assertTrue(accelerometer.accept(floatArrayOf(4f, 0f, 8f)).passed)

        val step = GuidedSensorSampler(GuidedSensorCode.STEP)
        assertFalse(step.accept(floatArrayOf(12f)).passed)
        assertTrue(step.accept(floatArrayOf(13f)).passed)

        val proximity = GuidedSensorSampler(GuidedSensorCode.PROXIMITY)
        assertFalse(proximity.accept(floatArrayOf(5f)).passed)
        assertTrue(proximity.accept(floatArrayOf(0f)).passed)
    }

    @Test
    fun challengeThresholdsAndShakeDebounceAreDeterministic() {
        var runtime = SensorChallengeRuntime()
        repeat(SensorChallengeEvaluator.REQUIRED_SHAKES - 1) { index ->
            val evaluation =
                SensorChallengeEvaluator.evaluate(
                    challenge = InteractiveChallenge.SHAKE,
                    values = floatArrayOf(21f, 0f, 0f),
                    nowMillis = (index + 1L) * SensorChallengeEvaluator.SHAKE_DEBOUNCE_MILLIS,
                    runtime = runtime,
                )
            runtime = evaluation.runtime
            assertFalse(evaluation.completed)
        }
        val completed =
            SensorChallengeEvaluator.evaluate(
                challenge = InteractiveChallenge.SHAKE,
                values = floatArrayOf(21f, 0f, 0f),
                nowMillis = 2_000L,
                runtime = runtime,
            )
        assertTrue(completed.completed)

        val ignored =
            SensorChallengeEvaluator.evaluate(
                challenge = InteractiveChallenge.TILT_LEFT,
                values = floatArrayOf(2f, 0f, 9f),
                nowMillis = 0L,
                runtime = SensorChallengeRuntime(),
            )
        assertFalse(ignored.completed)
    }

    @Test
    fun listenerOwnerReplacesAndCancelsEachRegistrationExactlyOnce() {
        val stopped = mutableListOf<String>()
        val owner = SensorListenerOwner<String, String> { stopped += it }

        owner.replace("accelerometer", "first")
        owner.replace("accelerometer", "second")
        owner.remove("accelerometer")
        owner.remove("accelerometer")
        owner.replace("light", "third")
        owner.clear()
        owner.clear()

        assertEquals(listOf("first", "second", "third"), stopped)
        assertTrue(owner.isEmpty())
    }

    @Test
    fun accuracyMappingUsesStableCodesIncludingUnknownValues() {
        assertEquals(SensorAccuracyCode.HIGH, SensorAccuracyCode.fromAndroid(3))
        assertEquals(SensorAccuracyCode.UNRELIABLE, SensorAccuracyCode.fromAndroid(0))
        assertEquals(SensorAccuracyCode.UNKNOWN, SensorAccuracyCode.fromAndroid(99))
    }
}
