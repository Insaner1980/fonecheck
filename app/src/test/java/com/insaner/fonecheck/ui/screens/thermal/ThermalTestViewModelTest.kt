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

    private class FakeThermalPlatform(
        override val statusApiSupported: Boolean = true,
        override val headroomApiSupported: Boolean = true,
        var currentStatus: ThermalStatusCode? = ThermalStatusCode.NONE,
        var headroom: Float? = 0.3f,
        var batteryTemperatureCelsius: Float? = 30f,
    ) : ThermalPlatform {
        var registrationCount = 0
        var closeCount = 0
        var headroomReadCount = 0
        private var listener: ((ThermalStatusCode) -> Unit)? = null

        override fun readStatus(): ThermalStatusCode? = currentStatus

        override fun readHeadroom(): Float? {
            headroomReadCount += 1
            return headroom
        }

        override fun readBatteryTemperatureCelsius(): Float? = batteryTemperatureCelsius

        override fun registerStatusListener(listener: (ThermalStatusCode) -> Unit): ThermalStatusRegistration? {
            if (!statusApiSupported) return null
            registrationCount += 1
            this.listener = listener
            return ThermalStatusRegistration {
                closeCount += 1
                this.listener = null
            }
        }

        fun emit(status: ThermalStatusCode) {
            listener?.invoke(status)
        }
    }
}
