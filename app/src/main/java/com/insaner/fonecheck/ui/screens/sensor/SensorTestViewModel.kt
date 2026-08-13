package com.insaner.fonecheck.ui.screens.sensor

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SensorInfo(
    val type: Int,
    val name: String,
    val vendor: String,
    val version: Int,
    val resolution: Float,
    val maxRange: Float,
    val power: Float,
    val minDelay: Int,
    val isWakeUp: Boolean,
)

data class SensorLiveData(
    val values: FloatArray = floatArrayOf(),
    val accuracy: SensorAccuracyCode = SensorAccuracyCode.UNKNOWN,
    val timestamp: Long = 0L,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SensorLiveData) return false
        return values.contentEquals(other.values) && accuracy == other.accuracy && timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = values.contentHashCode()
        result = 31 * result + accuracy.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

enum class InteractiveChallenge {
    SHAKE,
    TILT_LEFT,
    TILT_RIGHT,
    FACE_DOWN,
    FACE_UP,
    ROTATE,
}

data class ChallengeState(
    val challenge: InteractiveChallenge? = null,
    val sensorCode: GuidedSensorCode? = null,
    val completed: Boolean = false,
    val progress: Float = 0f,
    val sampleCount: Int = 0,
)

data class SensorTestState(
    val sensors: List<SensorInfo> = emptyList(),
    val guidedTests: List<GuidedSensorTestState> = GuidedSensorCatalog.create(emptySet()),
    val activeSensorType: Int? = null,
    val liveData: Map<Int, SensorLiveData> = emptyMap(),
    val expandedSensor: GuidedSensorCode? = null,
    val challenge: ChallengeState = ChallengeState(),
    val completedChallenges: Set<InteractiveChallenge> = emptySet(),
    val sensorCount: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class SensorTestViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val sensorManager = application.getSystemService(SensorManager::class.java)
        private val listenerOwner =
            SensorListenerOwner<Int, SensorEventListener> { listener ->
                sensorManager.unregisterListener(listener)
            }

        private val _state = MutableStateFlow(SensorTestState())
        val state: StateFlow<SensorTestState> = _state.asStateFlow()

        private var sampler: GuidedSensorSampler? = null
        private var challengeRuntime = SensorChallengeRuntime()
        private var challengeClearJob: Job? = null

        init {
            discoverSensors()
        }

        private fun discoverSensors() {
            val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL).toList()
            val infos =
                sensorList
                    .map { sensor ->
                        SensorInfo(
                            type = sensor.type,
                            name = sensor.name,
                            vendor = sensor.vendor,
                            version = sensor.version,
                            resolution = sensor.resolution,
                            maxRange = sensor.maximumRange,
                            power = sensor.power,
                            minDelay = sensor.minDelay,
                            isWakeUp = sensor.isWakeUpSensor,
                        )
                    }.sortedBy { sensorCategoryOrder(it.type) }

            _state.update {
                it.copy(
                    sensors = infos,
                    guidedTests = GuidedSensorCatalog.create(infos.map { info -> info.type }.toSet()),
                    sensorCount = infos.size,
                )
            }
        }

        fun toggleSensorExpanded(code: GuidedSensorCode) {
            if (_state.value.expandedSensor == code) {
                cancelActiveTest()
                _state.update { it.copy(expandedSensor = null) }
            } else {
                startGuidedTest(code)
            }
        }

        fun startGuidedTest(code: GuidedSensorCode) {
            clearActiveOperation()
            val test = _state.value.guidedTests.first { it.code == code }
            val sensorType = test.sensorType
            if (sensorType == null) {
                _state.update { it.copy(expandedSensor = code) }
                return
            }

            sampler = GuidedSensorSampler(code, sensorType)
            updateGuidedTest(code) {
                it.copy(
                    status = GuidedSensorStatus.SAMPLING,
                    sampleCount = 0,
                    accuracy = SensorAccuracyCode.UNKNOWN,
                )
            }
            _state.update { it.copy(expandedSensor = code, error = null) }
            if (!startListening(sensorType)) {
                sampler = null
                updateGuidedTest(code) { it.copy(status = GuidedSensorStatus.NOT_TESTED) }
                _state.update { it.copy(activeSensorType = null, error = "listener_registration_failed") }
            }
        }

        fun skipGuidedTest(code: GuidedSensorCode) {
            if (_state.value.expandedSensor == code) clearActiveOperation()
            updateGuidedTest(code) { test ->
                if (test.status == GuidedSensorStatus.NOT_AVAILABLE) {
                    test
                } else {
                    test.copy(status = GuidedSensorStatus.SKIPPED)
                }
            }
        }

        fun startChallenge(
            challenge: InteractiveChallenge,
            sensorCode: GuidedSensorCode = challenge.defaultSensorCode(),
        ) {
            clearActiveOperation()
            val test = _state.value.guidedTests.first { it.code == sensorCode }
            val sensorType = test.sensorType
            if (sensorType == null) {
                _state.update {
                    it.copy(
                        challenge = ChallengeState(challenge = challenge, sensorCode = sensorCode),
                        expandedSensor = sensorCode,
                    )
                }
                return
            }

            challengeRuntime = SensorChallengeRuntime()
            _state.update {
                it.copy(
                    challenge = ChallengeState(challenge = challenge, sensorCode = sensorCode),
                    expandedSensor = sensorCode,
                    error = null,
                )
            }
            if (!startListening(sensorType)) {
                _state.update { it.copy(error = "listener_registration_failed") }
            }
        }

        fun clearChallenge() {
            challengeClearJob?.cancel()
            challengeClearJob = null
            stopListening()
            challengeRuntime = SensorChallengeRuntime()
            _state.update { it.copy(challenge = ChallengeState()) }
        }

        fun skipChallenge() {
            val sensorCode = _state.value.challenge.sensorCode
            clearChallenge()
            sensorCode?.let(::skipGuidedTest)
        }

        fun stopAllTests() {
            challengeClearJob?.cancel()
            challengeClearJob = null
            sampler = null
            challengeRuntime = SensorChallengeRuntime()
            listenerOwner.clear()
            _state.update {
                it.copy(
                    activeSensorType = null,
                    expandedSensor = null,
                    challenge = ChallengeState(),
                    guidedTests =
                        it.guidedTests.map { test ->
                            if (test.status == GuidedSensorStatus.SAMPLING) {
                                test.copy(status = GuidedSensorStatus.NOT_TESTED)
                            } else {
                                test
                            }
                        },
                )
            }
        }

        fun sensorInfoFor(test: GuidedSensorTestState): SensorInfo? =
            test.sensorType?.let { type -> _state.value.sensors.firstOrNull { it.type == type } }

        fun availableChallenges(code: GuidedSensorCode): List<InteractiveChallenge> =
            when (code) {
                GuidedSensorCode.ACCELEROMETER ->
                    listOf(
                        InteractiveChallenge.SHAKE,
                        InteractiveChallenge.TILT_LEFT,
                        InteractiveChallenge.TILT_RIGHT,
                        InteractiveChallenge.FACE_DOWN,
                        InteractiveChallenge.FACE_UP,
                    )

                GuidedSensorCode.GRAVITY -> listOf(InteractiveChallenge.FACE_DOWN, InteractiveChallenge.FACE_UP)
                GuidedSensorCode.GYROSCOPE -> listOf(InteractiveChallenge.ROTATE)
                else -> emptyList()
            }

        private fun startListening(sensorType: Int): Boolean {
            val sensor = sensorManager.getDefaultSensor(sensorType) ?: return false
            val listener =
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        handleSensorEvent(event)
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor,
                        accuracy: Int,
                    ) = Unit
                }
            val registered = sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            if (registered) {
                listenerOwner.replace(sensorType, listener)
                _state.update { it.copy(activeSensorType = sensorType) }
            }
            return registered
        }

        @Synchronized
        private fun handleSensorEvent(event: SensorEvent) {
            val values = event.values.copyOf()
            val accuracy = SensorAccuracyCode.fromAndroid(event.accuracy)
            _state.update { current ->
                current.copy(
                    liveData =
                        current.liveData +
                            (event.sensor.type to SensorLiveData(values, accuracy, event.timestamp)),
                )
            }

            val activeChallenge = _state.value.challenge
            if (activeChallenge.challenge != null) {
                updateChallenge(activeChallenge, values, accuracy)
                return
            }

            val activeCode = _state.value.expandedSensor ?: return
            val result = sampler?.accept(values) ?: return
            updateGuidedTest(activeCode) {
                it.copy(
                    sampleCount = result.sampleCount,
                    accuracy = accuracy,
                    status = if (result.passed) GuidedSensorStatus.PASSED else GuidedSensorStatus.SAMPLING,
                )
            }
            if (result.passed) stopListening()
        }

        private fun updateChallenge(
            challenge: ChallengeState,
            values: FloatArray,
            accuracy: SensorAccuracyCode,
        ) {
            val activeChallenge = requireNotNull(challenge.challenge)
            val evaluation =
                SensorChallengeEvaluator.evaluate(
                    challenge = activeChallenge,
                    values = values,
                    nowMillis = System.currentTimeMillis(),
                    runtime = challengeRuntime,
                )
            challengeRuntime = evaluation.runtime
            val sampleCount = challenge.sampleCount + 1
            _state.update { current ->
                current.copy(
                    challenge =
                        challenge.copy(
                            progress = evaluation.progress,
                            completed = evaluation.completed,
                            sampleCount = sampleCount,
                        ),
                    completedChallenges =
                        if (evaluation.completed) {
                            current.completedChallenges + activeChallenge
                        } else {
                            current.completedChallenges
                        },
                )
            }

            if (evaluation.completed) {
                challenge.sensorCode?.let { code ->
                    updateGuidedTest(code) { test ->
                        test.copy(
                            status = GuidedSensorStatus.PASSED,
                            sampleCount = sampleCount,
                            accuracy = accuracy,
                        )
                    }
                }
                stopListening()
                challengeClearJob?.cancel()
                challengeClearJob =
                    viewModelScope.launch {
                        delay(CHALLENGE_RESULT_DURATION_MILLIS)
                        if (_state.value.challenge.completed) clearChallenge()
                    }
            }
        }

        private fun cancelActiveTest() {
            val activeCode = _state.value.expandedSensor
            clearActiveOperation()
            activeCode?.let { code ->
                updateGuidedTest(code) { test ->
                    if (test.status == GuidedSensorStatus.SAMPLING) {
                        test.copy(status = GuidedSensorStatus.NOT_TESTED)
                    } else {
                        test
                    }
                }
            }
        }

        private fun clearActiveOperation() {
            val activeCode = _state.value.expandedSensor
            challengeClearJob?.cancel()
            challengeClearJob = null
            sampler = null
            challengeRuntime = SensorChallengeRuntime()
            stopListening()
            _state.update { current ->
                current.copy(
                    challenge = ChallengeState(),
                    guidedTests =
                        current.guidedTests.map { test ->
                            if (test.code == activeCode && test.status == GuidedSensorStatus.SAMPLING) {
                                test.copy(status = GuidedSensorStatus.NOT_TESTED)
                            } else {
                                test
                            }
                        },
                )
            }
        }

        private fun stopListening() {
            listenerOwner.clear()
            _state.update { it.copy(activeSensorType = null) }
        }

        private fun updateGuidedTest(
            code: GuidedSensorCode,
            transform: (GuidedSensorTestState) -> GuidedSensorTestState,
        ) {
            _state.update { current ->
                current.copy(
                    guidedTests = current.guidedTests.map { if (it.code == code) transform(it) else it },
                )
            }
        }

        private fun sensorCategoryOrder(type: Int): Int =
            when (type) {
                Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_GRAVITY -> 0
                Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> 1
                Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> 2
                Sensor.TYPE_LIGHT -> 3
                Sensor.TYPE_PROXIMITY -> 4
                Sensor.TYPE_PRESSURE -> 5
                Sensor.TYPE_STEP_COUNTER, Sensor.TYPE_STEP_DETECTOR -> 6
                else -> 7
            }

        override fun onCleared() {
            stopAllTests()
        }

        companion object {
            private const val CHALLENGE_RESULT_DURATION_MILLIS = 2_000L
        }
    }

private fun InteractiveChallenge.defaultSensorCode(): GuidedSensorCode =
    when (this) {
        InteractiveChallenge.ROTATE -> GuidedSensorCode.GYROSCOPE
        else -> GuidedSensorCode.ACCELEROMETER
    }
