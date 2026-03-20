package com.insaner.phonecheck.ui.screens.sensor

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

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
    val accuracy: Int = SensorManager.SENSOR_STATUS_NO_CONTACT,
    val timestamp: Long = 0L,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SensorLiveData) return false
        return values.contentEquals(other.values) && accuracy == other.accuracy && timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = values.contentHashCode()
        result = 31 * result + accuracy
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
    val completed: Boolean = false,
    val progress: Float = 0f,
)

data class SensorTestState(
    val sensors: List<SensorInfo> = emptyList(),
    val activeSensorType: Int? = null,
    val liveData: Map<Int, SensorLiveData> = emptyMap(),
    val expandedSensor: Int? = null,
    val challenge: ChallengeState = ChallengeState(),
    val sensorCount: Int = 0,
)

@HiltViewModel
class SensorTestViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    private val sensorManager = application.getSystemService(SensorManager::class.java)

    private val _state = MutableStateFlow(SensorTestState())
    val state: StateFlow<SensorTestState> = _state

    private val activeListeners = mutableMapOf<Int, SensorEventListener>()

    // Shake detection state
    private var lastShakeTime = 0L
    private var shakeCount = 0

    init {
        discoverSensors()
    }

    private fun discoverSensors() {
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val infos = sensorList.map { sensor ->
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
        }.sortedBy { getSensorCategoryOrder(it.type) }

        _state.value = _state.value.copy(
            sensors = infos,
            sensorCount = infos.size,
        )
    }

    private fun getSensorCategoryOrder(type: Int): Int = when (type) {
        Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_LINEAR_ACCELERATION,
        Sensor.TYPE_GRAVITY -> 0
        Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> 1
        Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> 2
        Sensor.TYPE_LIGHT -> 3
        Sensor.TYPE_PROXIMITY -> 4
        Sensor.TYPE_PRESSURE -> 5
        Sensor.TYPE_AMBIENT_TEMPERATURE, Sensor.TYPE_RELATIVE_HUMIDITY -> 6
        Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GAME_ROTATION_VECTOR,
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> 7
        Sensor.TYPE_STEP_COUNTER, Sensor.TYPE_STEP_DETECTOR -> 8
        Sensor.TYPE_SIGNIFICANT_MOTION -> 9
        else -> 10
    }

    fun toggleSensorExpanded(sensorType: Int) {
        val current = _state.value.expandedSensor
        if (current == sensorType) {
            stopListening(sensorType)
            _state.value = _state.value.copy(expandedSensor = null)
        } else {
            if (current != null) stopListening(current)
            startListening(sensorType)
            _state.value = _state.value.copy(expandedSensor = sensorType)
        }
    }

    private fun startListening(sensorType: Int) {
        val sensor = sensorManager.getDefaultSensor(sensorType) ?: return

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val data = SensorLiveData(
                    values = event.values.copyOf(),
                    accuracy = event.accuracy,
                    timestamp = event.timestamp,
                )
                val newMap = _state.value.liveData.toMutableMap()
                newMap[sensorType] = data
                _state.value = _state.value.copy(liveData = newMap)

                // Check challenge progress
                checkChallengeProgress(sensorType, event.values)
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        activeListeners[sensorType] = listener
    }

    private fun stopListening(sensorType: Int) {
        activeListeners.remove(sensorType)?.let {
            sensorManager.unregisterListener(it)
        }
    }

    fun startChallenge(challenge: InteractiveChallenge) {
        _state.value = _state.value.copy(
            challenge = ChallengeState(challenge = challenge, completed = false, progress = 0f),
        )
        shakeCount = 0

        // Ensure accelerometer is listening for challenges that need it
        val sensorType = when (challenge) {
            InteractiveChallenge.SHAKE, InteractiveChallenge.FACE_DOWN,
            InteractiveChallenge.FACE_UP -> Sensor.TYPE_ACCELEROMETER
            InteractiveChallenge.TILT_LEFT, InteractiveChallenge.TILT_RIGHT -> Sensor.TYPE_ACCELEROMETER
            InteractiveChallenge.ROTATE -> Sensor.TYPE_GYROSCOPE
        }

        if (!activeListeners.containsKey(sensorType)) {
            startListening(sensorType)
        }
    }

    fun clearChallenge() {
        _state.value = _state.value.copy(challenge = ChallengeState())
    }

    private fun checkChallengeProgress(sensorType: Int, values: FloatArray) {
        val challenge = _state.value.challenge
        if (challenge.challenge == null || challenge.completed) return

        when (challenge.challenge) {
            InteractiveChallenge.SHAKE -> {
                if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                    val magnitude = sqrt(
                        values[0] * values[0] + values[1] * values[1] + values[2] * values[2],
                    )
                    if (magnitude > 20f) {
                        val now = System.currentTimeMillis()
                        if (now - lastShakeTime > 200) {
                            shakeCount++
                            lastShakeTime = now
                            val progress = (shakeCount / 5f).coerceAtMost(1f)
                            _state.value = _state.value.copy(
                                challenge = challenge.copy(
                                    progress = progress,
                                    completed = progress >= 1f,
                                ),
                            )
                        }
                    }
                }
            }
            InteractiveChallenge.TILT_LEFT -> {
                if (sensorType == Sensor.TYPE_ACCELEROMETER && values.size >= 2) {
                    val roll = values[0] // positive = tilt left
                    val progress = (roll / 7f).coerceIn(0f, 1f)
                    _state.value = _state.value.copy(
                        challenge = challenge.copy(
                            progress = progress,
                            completed = progress >= 1f,
                        ),
                    )
                }
            }
            InteractiveChallenge.TILT_RIGHT -> {
                if (sensorType == Sensor.TYPE_ACCELEROMETER && values.size >= 2) {
                    val roll = -values[0] // negative = tilt right
                    val progress = (roll / 7f).coerceIn(0f, 1f)
                    _state.value = _state.value.copy(
                        challenge = challenge.copy(
                            progress = progress,
                            completed = progress >= 1f,
                        ),
                    )
                }
            }
            InteractiveChallenge.FACE_DOWN -> {
                if (sensorType == Sensor.TYPE_ACCELEROMETER && values.size >= 3) {
                    val z = values[2]
                    val progress = ((-z) / 9f).coerceIn(0f, 1f) // z < 0 => face down
                    _state.value = _state.value.copy(
                        challenge = challenge.copy(
                            progress = progress,
                            completed = progress >= 0.95f,
                        ),
                    )
                }
            }
            InteractiveChallenge.FACE_UP -> {
                if (sensorType == Sensor.TYPE_ACCELEROMETER && values.size >= 3) {
                    val z = values[2]
                    val progress = (z / 9f).coerceIn(0f, 1f) // z > 0 => face up
                    _state.value = _state.value.copy(
                        challenge = challenge.copy(
                            progress = progress,
                            completed = progress >= 0.95f,
                        ),
                    )
                }
            }
            InteractiveChallenge.ROTATE -> {
                if (sensorType == Sensor.TYPE_GYROSCOPE && values.size >= 3) {
                    val angularSpeed = sqrt(
                        values[0] * values[0] + values[1] * values[1] + values[2] * values[2],
                    )
                    val progress = (angularSpeed / 5f).coerceIn(0f, 1f)
                    if (progress >= 0.8f) {
                        _state.value = _state.value.copy(
                            challenge = challenge.copy(progress = 1f, completed = true),
                        )
                    } else {
                        _state.value = _state.value.copy(
                            challenge = challenge.copy(progress = progress),
                        )
                    }
                }
            }
        }

        // Auto-clear completed challenge after a delay
        if (_state.value.challenge.completed) {
            viewModelScope.launch {
                delay(2000)
                if (_state.value.challenge.completed) {
                    _state.value = _state.value.copy(challenge = ChallengeState())
                }
            }
        }
    }

    fun getSensorValueLabels(sensorType: Int): List<String> = when (sensorType) {
        Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_LINEAR_ACCELERATION,
        Sensor.TYPE_GRAVITY -> listOf("X (m/s²)", "Y (m/s²)", "Z (m/s²)")
        Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> listOf("X (rad/s)", "Y (rad/s)", "Z (rad/s)")
        Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> listOf("X (μT)", "Y (μT)", "Z (μT)")
        Sensor.TYPE_LIGHT -> listOf("Illuminance (lx)")
        Sensor.TYPE_PROXIMITY -> listOf("Distance (cm)")
        Sensor.TYPE_PRESSURE -> listOf("Pressure (hPa)")
        Sensor.TYPE_AMBIENT_TEMPERATURE -> listOf("Temperature (°C)")
        Sensor.TYPE_RELATIVE_HUMIDITY -> listOf("Humidity (%)")
        Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GAME_ROTATION_VECTOR,
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> listOf("X", "Y", "Z", "cos(θ/2)")
        Sensor.TYPE_STEP_COUNTER -> listOf("Steps")
        Sensor.TYPE_STEP_DETECTOR -> listOf("Step")
        Sensor.TYPE_SIGNIFICANT_MOTION -> listOf("Motion")
        Sensor.TYPE_HEART_RATE -> listOf("BPM")
        else -> List(6) { "Value ${it + 1}" }
    }

    fun getSensorTypeName(type: Int): String = when (type) {
        Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
        Sensor.TYPE_MAGNETIC_FIELD -> "Magnetometer"
        Sensor.TYPE_GYROSCOPE -> "Gyroscope"
        Sensor.TYPE_LIGHT -> "Light"
        Sensor.TYPE_PROXIMITY -> "Proximity"
        Sensor.TYPE_PRESSURE -> "Barometer"
        Sensor.TYPE_AMBIENT_TEMPERATURE -> "Temperature"
        Sensor.TYPE_GRAVITY -> "Gravity"
        Sensor.TYPE_LINEAR_ACCELERATION -> "Linear Acceleration"
        Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
        Sensor.TYPE_RELATIVE_HUMIDITY -> "Humidity"
        Sensor.TYPE_GAME_ROTATION_VECTOR -> "Game Rotation"
        Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> "Gyroscope (Uncalib.)"
        Sensor.TYPE_SIGNIFICANT_MOTION -> "Significant Motion"
        Sensor.TYPE_STEP_DETECTOR -> "Step Detector"
        Sensor.TYPE_STEP_COUNTER -> "Step Counter"
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> "Geomagnetic Rotation"
        Sensor.TYPE_HEART_RATE -> "Heart Rate"
        Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> "Magnetometer (Uncalib.)"
        Sensor.TYPE_STATIONARY_DETECT -> "Stationary Detect"
        Sensor.TYPE_MOTION_DETECT -> "Motion Detect"
        Sensor.TYPE_HEART_BEAT -> "Heart Beat"
        Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT -> "Off-Body Detect"
        Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> "Accelerometer (Uncalib.)"
        Sensor.TYPE_HINGE_ANGLE -> "Hinge Angle"
        else -> "Sensor (type $type)"
    }

    fun getSensorIcon(type: Int): String = when (type) {
        Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_LINEAR_ACCELERATION,
        Sensor.TYPE_GRAVITY, Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> "ACC"
        Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> "GYR"
        Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> "MAG"
        Sensor.TYPE_LIGHT -> "LUX"
        Sensor.TYPE_PROXIMITY -> "PRX"
        Sensor.TYPE_PRESSURE -> "BAR"
        Sensor.TYPE_AMBIENT_TEMPERATURE -> "TMP"
        Sensor.TYPE_RELATIVE_HUMIDITY -> "HUM"
        Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GAME_ROTATION_VECTOR,
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> "ROT"
        Sensor.TYPE_STEP_COUNTER, Sensor.TYPE_STEP_DETECTOR -> "STP"
        Sensor.TYPE_SIGNIFICANT_MOTION, Sensor.TYPE_MOTION_DETECT -> "MOT"
        Sensor.TYPE_HEART_RATE, Sensor.TYPE_HEART_BEAT -> "HRT"
        Sensor.TYPE_HINGE_ANGLE -> "HNG"
        else -> "SNS"
    }

    fun getAvailableChallenges(sensorType: Int): List<InteractiveChallenge> = when (sensorType) {
        Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_LINEAR_ACCELERATION -> listOf(
            InteractiveChallenge.SHAKE,
            InteractiveChallenge.TILT_LEFT,
            InteractiveChallenge.TILT_RIGHT,
            InteractiveChallenge.FACE_DOWN,
            InteractiveChallenge.FACE_UP,
        )
        Sensor.TYPE_GRAVITY -> listOf(
            InteractiveChallenge.FACE_DOWN,
            InteractiveChallenge.FACE_UP,
        )
        Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> listOf(
            InteractiveChallenge.ROTATE,
        )
        else -> emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        activeListeners.forEach { (_, listener) ->
            sensorManager.unregisterListener(listener)
        }
        activeListeners.clear()
    }
}
