package com.insaner.fonecheck.ui.screens.thermal

import androidx.lifecycle.ViewModel
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.runtime.EpochMillisClock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import javax.inject.Inject

enum class ThermalErrorCode {
    STATUS_UNAVAILABLE,
    LISTENER_REGISTRATION_FAILED,
}

data class ThermalTestState(
    val statusApiSupported: Boolean = false,
    val headroomApiSupported: Boolean = false,
    val status: ThermalStatusCode = ThermalStatusCode.UNAVAILABLE,
    val severity: ThermalSeverityCode = ThermalSeverityCode.UNAVAILABLE,
    val statusConfidence: Confidence = Confidence.UNAVAILABLE,
    val headroom: Float? = null,
    val headroomConfidence: Confidence = Confidence.UNAVAILABLE,
    val batteryTemperatureCelsius: Float? = null,
    val batteryTemperatureConfidence: Confidence = Confidence.UNAVAILABLE,
    val isMonitoring: Boolean = false,
    val capturedAt: Instant? = null,
    val error: ThermalErrorCode? = null,
)

@HiltViewModel
class ThermalTestViewModel
    @Inject
    constructor(
        private val platform: ThermalPlatform,
        private val clock: EpochMillisClock,
    ) : ViewModel() {
        private val _state = MutableStateFlow(ThermalTestState())
        val state: StateFlow<ThermalTestState> = _state.asStateFlow()

        private var registration: ThermalStatusRegistration? = null
        private var lastHeadroomAttemptMillis: Long? = null

        fun startMonitoring() {
            if (registration != null) return
            refresh()
            if (!platform.statusApiSupported) return

            val nextRegistration =
                platform.registerStatusListener { status ->
                    _state.update { current ->
                        current.copy(
                            status = status,
                            severity = ThermalRuntimePolicy.severity(status),
                            statusConfidence =
                                if (status == ThermalStatusCode.UNAVAILABLE) {
                                    Confidence.UNAVAILABLE
                                } else {
                                    Confidence.HIGH
                                },
                            capturedAt = Instant.ofEpochMilli(clock.currentTimeMillis()),
                            error =
                                ThermalErrorCode.STATUS_UNAVAILABLE.takeIf {
                                    status == ThermalStatusCode.UNAVAILABLE
                                },
                        )
                    }
                }
            registration = nextRegistration
            _state.update { current ->
                current.copy(
                    isMonitoring = nextRegistration != null,
                    error =
                        if (nextRegistration == null) {
                            ThermalErrorCode.LISTENER_REGISTRATION_FAILED
                        } else {
                            current.error.takeUnless { it == ThermalErrorCode.LISTENER_REGISTRATION_FAILED }
                        },
                )
            }
        }

        fun refresh() {
            val nowMillis = clock.currentTimeMillis()
            val status =
                if (platform.statusApiSupported) {
                    platform.readStatus() ?: ThermalStatusCode.UNAVAILABLE
                } else {
                    ThermalStatusCode.UNAVAILABLE
                }
            val shouldReadHeadroom =
                platform.headroomApiSupported &&
                    (
                        lastHeadroomAttemptMillis == null ||
                            nowMillis - requireNotNull(lastHeadroomAttemptMillis) >= HEADROOM_MIN_INTERVAL_MILLIS
                    )
            val headroom =
                if (shouldReadHeadroom) {
                    lastHeadroomAttemptMillis = nowMillis
                    platform.readHeadroom()
                } else {
                    _state.value.headroom
                }
            val batteryTemperature = platform.readBatteryTemperatureCelsius()

            _state.update { current ->
                current.copy(
                    statusApiSupported = platform.statusApiSupported,
                    headroomApiSupported = platform.headroomApiSupported,
                    status = status,
                    severity = ThermalRuntimePolicy.severity(status),
                    statusConfidence =
                        if (status == ThermalStatusCode.UNAVAILABLE) {
                            Confidence.UNAVAILABLE
                        } else {
                            Confidence.HIGH
                        },
                    headroom = headroom,
                    headroomConfidence =
                        if (headroom != null) Confidence.LOW else Confidence.UNAVAILABLE,
                    batteryTemperatureCelsius = batteryTemperature,
                    batteryTemperatureConfidence =
                        if (batteryTemperature != null) Confidence.HIGH else Confidence.UNAVAILABLE,
                    capturedAt = Instant.ofEpochMilli(nowMillis),
                    error =
                        when {
                            current.error == ThermalErrorCode.LISTENER_REGISTRATION_FAILED && registration == null ->
                                current.error
                            platform.statusApiSupported && status == ThermalStatusCode.UNAVAILABLE ->
                                ThermalErrorCode.STATUS_UNAVAILABLE
                            else -> null
                        },
                )
            }
        }

        fun stopMonitoring() {
            val currentRegistration = registration ?: return
            registration = null
            currentRegistration.close()
            _state.update { it.copy(isMonitoring = false) }
        }

        override fun onCleared() {
            stopMonitoring()
        }

        private companion object {
            const val HEADROOM_MIN_INTERVAL_MILLIS = 10_000L
        }
    }
