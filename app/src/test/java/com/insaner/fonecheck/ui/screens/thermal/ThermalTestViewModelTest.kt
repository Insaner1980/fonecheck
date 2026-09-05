package com.insaner.fonecheck.ui.screens.thermal

import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.runtime.EpochMillisClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThermalTestViewModelTest {
    @Test
    fun statusCallbackDoesNotRefreshOtherReadingTimes() {
        var now = 1_000L
        val platform = FakeThermalPlatform()
        val viewModel = ThermalTestViewModel(platform, EpochMillisClock { now })
        viewModel.startMonitoring()
        val initial = viewModel.state.value

        now = 5_000L
        platform.emit(ThermalStatusCode.SEVERE)
        assertEquals(
            now,
            viewModel.state.value.capturedAt
                ?.toEpochMilli(),
        )
        assertEquals(initial.headroomReadAt, viewModel.state.value.headroomReadAt)
        assertEquals(initial.batteryTemperatureReadAt, viewModel.state.value.batteryTemperatureReadAt)
        assertEquals(1_000L, initial.headroomReadAt?.toEpochMilli())
        assertEquals(1_000L, initial.batteryTemperatureReadAt?.toEpochMilli())

        now = 6_000L
        viewModel.refresh()
        assertEquals(initial.headroomReadAt, viewModel.state.value.headroomReadAt)
        assertEquals(
            now,
            viewModel.state.value.batteryTemperatureReadAt
                ?.toEpochMilli(),
        )
        assertEquals(1, platform.headroomReadCount)
        now = 11_000L
        viewModel.refresh()
        assertEquals(
            now,
            viewModel.state.value.headroomReadAt
                ?.toEpochMilli(),
        )
        assertEquals(2, platform.headroomReadCount)
        viewModel.stopMonitoring()
    }

    @Test
    fun unsupportedApiIsDistinctFromANormalThermalState() {
        val platform = FakeThermalPlatform(statusApiSupported = false, headroomApiSupported = false)
        val viewModel = ThermalTestViewModel(platform, EpochMillisClock { 1_000L })

        viewModel.startMonitoring()

        assertFalse(viewModel.state.value.statusApiSupported)
        assertEquals(ThermalStatusCode.UNAVAILABLE, viewModel.state.value.status)
        assertEquals(ThermalSeverityCode.UNAVAILABLE, viewModel.state.value.severity)
        assertFalse(viewModel.state.value.isMonitoring)
        assertEquals(0, platform.registrationCount)
    }

    @Test
    fun listenerUpdatesLiveStatusAndIsClosedExactlyOnce() {
        val platform =
            FakeThermalPlatform(
                currentStatus = ThermalStatusCode.NONE,
                headroom = 0.4f,
                batteryTemperatureCelsius = 31.5f,
            )
        val viewModel = ThermalTestViewModel(platform, EpochMillisClock { 1_000L })

        viewModel.startMonitoring()
        viewModel.startMonitoring()

        assertTrue(viewModel.state.value.isMonitoring)
        assertEquals(ThermalStatusCode.NONE, viewModel.state.value.status)
        assertEquals(0.4f, viewModel.state.value.headroom)
        assertEquals(31.5f, viewModel.state.value.batteryTemperatureCelsius)
        assertEquals(1, platform.registrationCount)

        platform.emit(ThermalStatusCode.SEVERE)

        assertEquals(ThermalStatusCode.SEVERE, viewModel.state.value.status)
        assertEquals(ThermalSeverityCode.SEVERE, viewModel.state.value.severity)

        viewModel.stopMonitoring()
        viewModel.stopMonitoring()

        assertFalse(viewModel.state.value.isMonitoring)
        assertEquals(1, platform.closeCount)
    }

    @Test
    fun callbackFromAClosedListenerCannotOverwriteANewerMonitoringSession() {
        val platform = FakeThermalPlatform(currentStatus = ThermalStatusCode.NONE)
        val viewModel = ThermalTestViewModel(platform, EpochMillisClock { 1_000L })

        viewModel.startMonitoring()
        viewModel.stopMonitoring()
        platform.currentStatus = ThermalStatusCode.LIGHT
        viewModel.startMonitoring()

        platform.emitAfterClose(ThermalStatusCode.SEVERE)

        assertEquals(ThermalStatusCode.LIGHT, viewModel.state.value.status)
        assertEquals(ThermalSeverityCode.LIGHT, viewModel.state.value.severity)
        assertTrue(viewModel.state.value.isMonitoring)
    }

    @Test
    fun unavailableHeadroomRemainsUnavailableAndSamplingIsRateLimited() {
        var now = 1_000L
        val platform = FakeThermalPlatform(headroom = null)
        val viewModel = ThermalTestViewModel(platform, EpochMillisClock { now })

        viewModel.startMonitoring()
        viewModel.stopMonitoring()
        now = 5_000L
        viewModel.startMonitoring()

        assertNull(viewModel.state.value.headroom)
        assertEquals(1, platform.headroomReadCount)

        viewModel.stopMonitoring()
        now = 11_000L
        viewModel.startMonitoring()

        assertEquals(2, platform.headroomReadCount)
    }

    @Test
    fun refreshPreservesListenerRegistrationFailureUntilRetrySucceeds() {
        val platform = FakeThermalPlatform(registrationSucceeds = false)
        val viewModel = ThermalTestViewModel(platform, EpochMillisClock { 1_000L })

        viewModel.startMonitoring()
        assertEquals(ThermalErrorCode.LISTENER_REGISTRATION_FAILED, viewModel.state.value.error)

        viewModel.refresh()
        assertEquals(ThermalErrorCode.LISTENER_REGISTRATION_FAILED, viewModel.state.value.error)

        platform.registrationSucceeds = true
        viewModel.startMonitoring()

        assertTrue(viewModel.state.value.isMonitoring)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun restartMonitoringRetriesListenerRegistrationAfterFailure() {
        val platform = FakeThermalPlatform(registrationSucceeds = false)
        val viewModel = ThermalTestViewModel(platform, EpochMillisClock { 1_000L })

        viewModel.startMonitoring()
        platform.registrationSucceeds = true

        viewModel.restartMonitoring()

        assertTrue(viewModel.state.value.isMonitoring)
        assertNull(viewModel.state.value.error)
        assertEquals(1, platform.registrationCount)
    }

    private class FakeThermalPlatform(
        override val statusApiSupported: Boolean = true,
        override val headroomApiSupported: Boolean = true,
        var currentStatus: ThermalStatusCode? = ThermalStatusCode.NONE,
        var headroom: Float? = 0.3f,
        var batteryTemperatureCelsius: Float? = 30f,
        var registrationSucceeds: Boolean = true,
    ) : ThermalPlatform {
        var registrationCount = 0
        var closeCount = 0
        var headroomReadCount = 0
        private var listener: ((ThermalStatusCode) -> Unit)? = null
        private var closedListener: ((ThermalStatusCode) -> Unit)? = null

        override fun readStatus(): ThermalStatusCode? = currentStatus

        override fun readHeadroom(): Float? {
            headroomReadCount += 1
            return headroom
        }

        override fun readBatteryTemperatureCelsius(): Float? = batteryTemperatureCelsius

        override fun registerStatusListener(listener: (ThermalStatusCode) -> Unit): ThermalStatusRegistration? {
            if (!statusApiSupported || !registrationSucceeds) return null
            registrationCount += 1
            this.listener = listener
            return ThermalStatusRegistration {
                closeCount += 1
                closedListener = listener
                if (this.listener === listener) this.listener = null
            }
        }

        fun emit(status: ThermalStatusCode) {
            listener?.invoke(status)
        }

        fun emitAfterClose(status: ThermalStatusCode) {
            closedListener?.invoke(status)
        }
    }
}
