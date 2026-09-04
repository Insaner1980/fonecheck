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

        val mixedPressure = GuidedSensorSampler(GuidedSensorCode.BAROMETER)
        repeat(GuidedSensorSampler.REQUIRED_SAMPLE_COUNT - 1) {
            assertEquals(0, mixedPressure.accept(floatArrayOf(299f)).sampleCount)
        }
        assertFalse(mixedPressure.accept(floatArrayOf(1_013f)).passed)
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
        assertEquals(SensorAccuracyCode.MEDIUM, SensorAccuracyCode.fromAndroid(2))
        assertEquals(SensorAccuracyCode.LOW, SensorAccuracyCode.fromAndroid(1))
        assertEquals(SensorAccuracyCode.UNRELIABLE, SensorAccuracyCode.fromAndroid(0))
        assertEquals(SensorAccuracyCode.UNKNOWN, SensorAccuracyCode.fromAndroid(99))
    }

    @Test
    fun allAvailableSensorTypesResolveToTestableCatalogEntries() {
        val availableTypes =
            setOf(
                SensorType.ACCELEROMETER,
                SensorType.MAGNETIC_FIELD,
                SensorType.GYROSCOPE,
                SensorType.LIGHT,
                SensorType.PRESSURE,
                SensorType.PROXIMITY,
                SensorType.GRAVITY,
                SensorType.STEP_COUNTER,
            )

        val tests = GuidedSensorCatalog.create(availableTypes)

        assertTrue(tests.all { it.status == GuidedSensorStatus.NOT_TESTED })
        assertTrue(tests.all { it.sensorType != null })
    }

    @Test
    fun samplerCoversDetectorAndVectorSensorThresholds() {
        val detector = GuidedSensorSampler(GuidedSensorCode.STEP, SensorType.STEP_DETECTOR)
        assertFalse(detector.accept(floatArrayOf(0f)).passed)
        assertTrue(detector.accept(floatArrayOf(1f)).passed)

        listOf(
            GuidedSensorCode.GYROSCOPE to floatArrayOf(1f, 0f, 0f),
            GuidedSensorCode.GRAVITY to floatArrayOf(2f, 0f, 0f),
            GuidedSensorCode.MAGNETOMETER to floatArrayOf(4f, 0f, 0f),
        ).forEach { (code, changed) ->
            val sampler = GuidedSensorSampler(code)
            assertFalse(sampler.accept(floatArrayOf(0f, 0f, 0f)).passed)
            assertTrue(sampler.accept(changed).passed)
        }

        val shortVector = GuidedSensorSampler(GuidedSensorCode.ACCELEROMETER)
        assertFalse(shortVector.accept(floatArrayOf(0f)).passed)
        assertFalse(shortVector.accept(floatArrayOf(2f)).passed)
    }

    @Test
    fun invalidAndOutOfRangeSamplesNeverPass() {
        val invalid = GuidedSensorSampler(GuidedSensorCode.LIGHT)
        assertEquals(0, invalid.accept(floatArrayOf()).sampleCount)
        assertEquals(0, invalid.accept(floatArrayOf(Float.POSITIVE_INFINITY)).sampleCount)

        val lowPressure = GuidedSensorSampler(GuidedSensorCode.BAROMETER)
        repeat(GuidedSensorSampler.REQUIRED_SAMPLE_COUNT) { lowPressure.accept(floatArrayOf(299f)) }
        assertFalse(lowPressure.accept(floatArrayOf(299f)).passed)

        val highPressure = GuidedSensorSampler(GuidedSensorCode.BAROMETER)
        repeat(GuidedSensorSampler.REQUIRED_SAMPLE_COUNT) { highPressure.accept(floatArrayOf(1_101f)) }
        assertFalse(highPressure.accept(floatArrayOf(1_101f)).passed)
    }

    @Test
    fun `every interactive challenge uses its directional threshold`() {
        val runtime = SensorChallengeRuntime()
        val passingValues =
            mapOf(
                InteractiveChallenge.TILT_LEFT to floatArrayOf(7f),
                InteractiveChallenge.TILT_RIGHT to floatArrayOf(-7f),
                InteractiveChallenge.FACE_DOWN to floatArrayOf(0f, 0f, -9f),
                InteractiveChallenge.FACE_UP to floatArrayOf(0f, 0f, 9f),
                InteractiveChallenge.ROTATE to floatArrayOf(0f, 0f, 4f),
            )

        passingValues.forEach { (challenge, values) ->
            val evaluation = SensorChallengeEvaluator.evaluate(challenge, values, 0L, runtime)
            assertTrue("$challenge should complete", evaluation.completed)
            assertEquals(1f, evaluation.progress)
        }

        assertFalse(
            SensorChallengeEvaluator
                .evaluate(InteractiveChallenge.ROTATE, floatArrayOf(4f), 0L, runtime)
                .completed,
        )
        assertFalse(
            SensorChallengeEvaluator
                .evaluate(InteractiveChallenge.TILT_RIGHT, floatArrayOf(1f), 0L, runtime)
                .completed,
        )
    }

    @Test
    fun invalidWeakAndDebouncedChallengeSamplesKeepRuntimeUnchanged() {
        val runtime = SensorChallengeRuntime(lastShakeTimeMillis = 1_000L, shakeCount = 2)
        listOf(floatArrayOf(), floatArrayOf(Float.NaN)).forEach { values ->
            val result = SensorChallengeEvaluator.evaluate(InteractiveChallenge.SHAKE, values, 1_500L, runtime)
            assertEquals(0f, result.progress)
            assertEquals(runtime, result.runtime)
        }

        val weak =
            SensorChallengeEvaluator.evaluate(
                InteractiveChallenge.SHAKE,
                floatArrayOf(1f, 1f, 1f),
                1_500L,
                runtime,
            )
        assertEquals(runtime, weak.runtime)

        val debounced =
            SensorChallengeEvaluator.evaluate(
                InteractiveChallenge.SHAKE,
                floatArrayOf(21f, 0f, 0f),
                1_100L,
                runtime,
            )
        assertEquals(runtime, debounced.runtime)
    }

    @Test
    fun replacingAListenerWithTheSameInstanceDoesNotUnregisterIt() {
        val stopped = mutableListOf<Any>()
        val owner = SensorListenerOwner<String, Any> { stopped += it }
        val listener = Any()

        owner.replace("sensor", listener)
        owner.replace("sensor", listener)

        assertTrue(stopped.isEmpty())
        owner.clear()
        assertEquals(listOf(listener), stopped)
    }

    @Test
    fun stepPermissionAndCallbackGenerationAreNarrowlyScoped() {
        assertFalse(SensorPermissionPolicy.requiresActivityRecognition(SensorType.STEP_COUNTER, 28))
        assertTrue(SensorPermissionPolicy.requiresActivityRecognition(SensorType.STEP_COUNTER, 29))
        assertTrue(SensorPermissionPolicy.requiresActivityRecognition(SensorType.STEP_DETECTOR, 36))
        assertFalse(SensorPermissionPolicy.requiresActivityRecognition(SensorType.ACCELEROMETER, 36))

        val gate = SensorCallbackGate()
        val old = gate.begin()
        val current = gate.begin()
        assertFalse(gate.isCurrent(old))
        assertTrue(gate.isCurrent(current))
        gate.cancel()
        assertFalse(gate.isCurrent(current))
    }
}
