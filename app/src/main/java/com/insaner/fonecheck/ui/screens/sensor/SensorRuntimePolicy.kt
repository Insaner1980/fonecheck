package com.insaner.fonecheck.ui.screens.sensor

import kotlin.math.sqrt

object SensorType {
    const val ACCELEROMETER = 1
    const val MAGNETIC_FIELD = 2
    const val GYROSCOPE = 4
    const val LIGHT = 5
    const val PRESSURE = 6
    const val PROXIMITY = 8
    const val GRAVITY = 9
    const val STEP_DETECTOR = 18
    const val STEP_COUNTER = 19
}

enum class GuidedSensorCode(
    val stableCode: String,
) {
    ACCELEROMETER("accelerometer"),
    GYROSCOPE("gyroscope"),
    GRAVITY("gravity"),
    PROXIMITY("proximity"),
    LIGHT("light"),
    MAGNETOMETER("magnetometer"),
    BAROMETER("barometer"),
    STEP("step"),
}

enum class GuidedSensorStatus {
    NOT_AVAILABLE,
    NOT_TESTED,
    SAMPLING,
    PASSED,
    SKIPPED,
}

enum class SensorAccuracyCode {
    UNRELIABLE,
    LOW,
    MEDIUM,
    HIGH,
    UNKNOWN,
    ;

    companion object {
        fun fromAndroid(accuracy: Int): SensorAccuracyCode =
            when (accuracy) {
                0 -> UNRELIABLE
                1 -> LOW
                2 -> MEDIUM
                3 -> HIGH
                else -> UNKNOWN
            }
    }
}

data class GuidedSensorTestState(
    val code: GuidedSensorCode,
    val sensorType: Int?,
    val status: GuidedSensorStatus,
    val sampleCount: Int = 0,
    val accuracy: SensorAccuracyCode = SensorAccuracyCode.UNKNOWN,
)

object GuidedSensorCatalog {
    fun create(availableTypes: Set<Int>): List<GuidedSensorTestState> =
        GuidedSensorCode.entries.map { code ->
            val sensorType = code.selectType(availableTypes)
            GuidedSensorTestState(
                code = code,
                sensorType = sensorType,
                status =
                    if (sensorType == null) {
                        GuidedSensorStatus.NOT_AVAILABLE
                    } else {
                        GuidedSensorStatus.NOT_TESTED
                    },
            )
        }

    private fun GuidedSensorCode.selectType(availableTypes: Set<Int>): Int? =
        when (this) {
            GuidedSensorCode.ACCELEROMETER -> SensorType.ACCELEROMETER.takeIf(availableTypes::contains)
            GuidedSensorCode.GYROSCOPE -> SensorType.GYROSCOPE.takeIf(availableTypes::contains)
            GuidedSensorCode.GRAVITY -> SensorType.GRAVITY.takeIf(availableTypes::contains)
            GuidedSensorCode.PROXIMITY -> SensorType.PROXIMITY.takeIf(availableTypes::contains)
            GuidedSensorCode.LIGHT -> SensorType.LIGHT.takeIf(availableTypes::contains)
            GuidedSensorCode.MAGNETOMETER -> SensorType.MAGNETIC_FIELD.takeIf(availableTypes::contains)
            GuidedSensorCode.BAROMETER -> SensorType.PRESSURE.takeIf(availableTypes::contains)
            GuidedSensorCode.STEP ->
                when {
                    SensorType.STEP_DETECTOR in availableTypes -> SensorType.STEP_DETECTOR
                    SensorType.STEP_COUNTER in availableTypes -> SensorType.STEP_COUNTER
                    else -> null
                }
        }
}

data class SensorSamplingResult(
    val sampleCount: Int,
    val passed: Boolean,
)

class GuidedSensorSampler(
    private val code: GuidedSensorCode,
    private val sensorType: Int? = null,
) {
    private var firstValues: FloatArray? = null
    private var sampleCount = 0

    @Synchronized
    fun accept(values: FloatArray): SensorSamplingResult {
        if (values.isEmpty() || values.any { !it.isFinite() }) {
            return SensorSamplingResult(sampleCount, passed = false)
        }

        sampleCount += 1
        val baseline = firstValues
        if (baseline == null) firstValues = values.copyOf()

        val passed =
            when (code) {
                GuidedSensorCode.BAROMETER ->
                    sampleCount >= REQUIRED_SAMPLE_COUNT && values[0] in MIN_PRESSURE_HPA..MAX_PRESSURE_HPA

                GuidedSensorCode.STEP ->
                    if (sensorType == SensorType.STEP_DETECTOR) {
                        values[0] > 0f
                    } else {
                        baseline != null && values[0] - baseline[0] >= STEP_DELTA
                    }

                GuidedSensorCode.LIGHT -> baseline.changedBy(values, LIGHT_DELTA)
                GuidedSensorCode.PROXIMITY -> baseline.changedBy(values, PROXIMITY_DELTA)
                GuidedSensorCode.ACCELEROMETER -> baseline.vectorDelta(values) >= ACCELERATION_DELTA
                GuidedSensorCode.GYROSCOPE -> baseline.vectorDelta(values) >= GYROSCOPE_DELTA
                GuidedSensorCode.GRAVITY -> baseline.vectorDelta(values) >= GRAVITY_DELTA
                GuidedSensorCode.MAGNETOMETER -> baseline.vectorDelta(values) >= MAGNETIC_FIELD_DELTA
            }

        return SensorSamplingResult(sampleCount, passed)
    }

    private fun FloatArray?.changedBy(
        current: FloatArray,
        threshold: Float,
    ): Boolean =
        this != null &&
            current.isNotEmpty() &&
            isNotEmpty() &&
            kotlin.math.abs(current[0] - this[0]) >= threshold

    private fun FloatArray?.vectorDelta(current: FloatArray): Float {
        if (this == null || size < 3 || current.size < 3) return 0f
        val x = current[0] - this[0]
        val y = current[1] - this[1]
        val z = current[2] - this[2]
        return sqrt(x * x + y * y + z * z)
    }

    companion object {
        const val REQUIRED_SAMPLE_COUNT = 5
        private const val MIN_PRESSURE_HPA = 300f
        private const val MAX_PRESSURE_HPA = 1_100f
        private const val STEP_DELTA = 1f
        private const val LIGHT_DELTA = 1f
        private const val PROXIMITY_DELTA = 0.1f
        private const val ACCELERATION_DELTA = 1.5f
        private const val GYROSCOPE_DELTA = 0.5f
        private const val GRAVITY_DELTA = 1f
        private const val MAGNETIC_FIELD_DELTA = 3f
    }
}

data class SensorChallengeRuntime(
    val lastShakeTimeMillis: Long = Long.MIN_VALUE,
    val shakeCount: Int = 0,
)

data class SensorChallengeEvaluation(
    val progress: Float,
    val completed: Boolean,
    val runtime: SensorChallengeRuntime,
)

object SensorChallengeEvaluator {
    fun evaluate(
        challenge: InteractiveChallenge,
        values: FloatArray,
        nowMillis: Long,
        runtime: SensorChallengeRuntime,
    ): SensorChallengeEvaluation {
        if (values.isEmpty() || values.any { !it.isFinite() }) {
            return SensorChallengeEvaluation(0f, completed = false, runtime)
        }

        return when (challenge) {
            InteractiveChallenge.SHAKE -> evaluateShake(values, nowMillis, runtime)
            InteractiveChallenge.TILT_LEFT -> progress(values.firstOrNull() ?: 0f, TILT_THRESHOLD, runtime)
            InteractiveChallenge.TILT_RIGHT -> progress(-(values.firstOrNull() ?: 0f), TILT_THRESHOLD, runtime)
            InteractiveChallenge.FACE_DOWN -> progress(-(values.getOrNull(2) ?: 0f), FACE_THRESHOLD, runtime)
            InteractiveChallenge.FACE_UP -> progress(values.getOrNull(2) ?: 0f, FACE_THRESHOLD, runtime)
            InteractiveChallenge.ROTATE -> {
                val magnitude = vectorMagnitude(values)
                progress(magnitude, ROTATE_THRESHOLD, runtime)
            }
        }
    }

    private fun evaluateShake(
        values: FloatArray,
        nowMillis: Long,
        runtime: SensorChallengeRuntime,
    ): SensorChallengeEvaluation {
        val isStrongEnough = vectorMagnitude(values) >= SHAKE_MAGNITUDE_THRESHOLD
        val isOutsideDebounce =
            runtime.lastShakeTimeMillis == Long.MIN_VALUE ||
                nowMillis - runtime.lastShakeTimeMillis >= SHAKE_DEBOUNCE_MILLIS
        val nextRuntime =
            if (isStrongEnough && isOutsideDebounce) {
                runtime.copy(lastShakeTimeMillis = nowMillis, shakeCount = runtime.shakeCount + 1)
            } else {
                runtime
            }
        val progress = (nextRuntime.shakeCount.toFloat() / REQUIRED_SHAKES).coerceIn(0f, 1f)
        return SensorChallengeEvaluation(progress, progress >= 1f, nextRuntime)
    }

    private fun progress(
        value: Float,
        threshold: Float,
        runtime: SensorChallengeRuntime,
    ): SensorChallengeEvaluation {
        val progress = (value / threshold).coerceIn(0f, 1f)
        return SensorChallengeEvaluation(progress, progress >= 1f, runtime)
    }

    private fun vectorMagnitude(values: FloatArray): Float {
        if (values.size < 3) return 0f
        return sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
    }

    const val REQUIRED_SHAKES = 5
    const val SHAKE_DEBOUNCE_MILLIS = 200L
    private const val SHAKE_MAGNITUDE_THRESHOLD = 20f
    private const val TILT_THRESHOLD = 7f
    private const val FACE_THRESHOLD = 8.55f
    private const val ROTATE_THRESHOLD = 4f
}

class SensorListenerOwner<K : Any, L : Any>(
    private val unregister: (L) -> Unit,
) {
    private val listeners = mutableMapOf<K, L>()

    @Synchronized
    fun replace(
        key: K,
        listener: L,
    ) {
        val previous = listeners.put(key, listener)
        if (previous !== listener) previous?.let(unregister)
    }

    @Synchronized
    fun remove(key: K) {
        listeners.remove(key)?.let(unregister)
    }

    fun clear() {
        val snapshot =
            synchronized(this) {
                val current = listeners.values.toList()
                listeners.clear()
                current
            }
        snapshot.forEach(unregister)
    }

    @Synchronized
    fun isEmpty(): Boolean = listeners.isEmpty()
}
