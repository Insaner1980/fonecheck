package com.insaner.fonecheck.ui.screens.simtelephony

import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.PhoneTypeCode
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
import com.insaner.fonecheck.domain.model.TelephonyHardwareCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimTelephonyViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun captureIsDeferredAndFailedRefreshKeepsLastSnapshot() =
        runTest(dispatcher.scheduler) {
            var captureCount = 0
            var shouldFail = false
            val viewModel =
                SimTelephonyViewModel(
                    provider =
                        SimTelephonyProvider {
                            captureCount += 1
                            if (shouldFail) error("capture failed")
                            simInfo()
                        },
                    ioDispatcher = dispatcher,
                )

            assertEquals(0, captureCount)
            assertEquals(SimTelephonyState(isLoading = true), viewModel.state.value)

            advanceUntilIdle()
            assertEquals(1, captureCount)
            assertEquals(SimInventoryCode.SINGLE_SIM, viewModel.state.value.info?.inventory)
            assertFalse(viewModel.state.value.isLoading)

            shouldFail = true
            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(SimInventoryCode.SINGLE_SIM, viewModel.state.value.info?.inventory)
            assertFalse(viewModel.state.value.isLoading)
            assertNotNull(viewModel.state.value.error)
        }

    private fun simInfo() =
        SimTelephonyInfo(
            hardware = TelephonyHardwareCode.AVAILABLE,
            inventory = SimInventoryCode.SINGLE_SIM,
            simSlots = emptyList(),
            phoneType = PhoneTypeCode.GSM,
            phoneCount = 1,
            dataNetworkType = NetworkGenerationCode.FOURTH_GENERATION,
            phoneStatePermissionGranted = true,
        )
}
