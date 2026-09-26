package com.insaner.fonecheck.ui.screens.simtelephony

import com.insaner.fonecheck.domain.model.NetworkGenerationCode
import com.insaner.fonecheck.domain.model.PhoneTypeCode
import com.insaner.fonecheck.domain.model.SimInventoryCode
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
import com.insaner.fonecheck.domain.model.TelephonyHardwareCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun obsoleteCaptureFailureCannotFinishReplacementCapture() {
        val ioScheduler = TestCoroutineScheduler()
        val ioDispatcher = StandardTestDispatcher(ioScheduler)
        val viewModel =
            SimTelephonyViewModel(
                provider = SimTelephonyProvider { withContext(ioDispatcher) { error("capture failed") } },
            )
        dispatcher.scheduler.runCurrent()
        ioScheduler.runCurrent()

        // Replace the read before its failure is delivered back to Main.
        viewModel.refresh()
        dispatcher.scheduler.runCurrent()

        assertEquals(SimTelephonyState(isLoading = true), viewModel.state.value)
        viewModel.cancelCapture()
        ioScheduler.runCurrent()
        dispatcher.scheduler.runCurrent()
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
                )

            assertEquals(0, captureCount)
            assertEquals(SimTelephonyState(isLoading = true), viewModel.state.value)

            advanceUntilIdle()
            assertEquals(1, captureCount)
            assertEquals(
                SimInventoryCode.SINGLE_SIM,
                viewModel.state.value.info
                    ?.inventory,
            )
            assertFalse(viewModel.state.value.isLoading)

            shouldFail = true
            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(
                SimInventoryCode.SINGLE_SIM,
                viewModel.state.value.info
                    ?.inventory,
            )
            assertFalse(viewModel.state.value.isLoading)
            assertNotNull(viewModel.state.value.error)
        }

    @Test
    fun cancelledSuccessfulCaptureCannotOverwriteReplacementSnapshot() =
        runTest(dispatcher.scheduler) {
            val releaseOld = CompletableDeferred<Unit>()
            var captures = 0
            val replacement = simInfo().copy(phoneCount = 2)
            val viewModel =
                SimTelephonyViewModel(
                    provider =
                        SimTelephonyProvider {
                            captures += 1
                            if (captures == 1) {
                                withContext(NonCancellable) { releaseOld.await() }
                                simInfo()
                            } else {
                                replacement
                            }
                        },
                )
            runCurrent()
            viewModel.refresh()
            runCurrent()
            assertEquals(replacement, viewModel.state.value.info)
            releaseOld.complete(Unit)
            advanceUntilIdle()
            assertEquals(replacement, viewModel.state.value.info)
            assertFalse(viewModel.state.value.isLoading)
            viewModel.cancelCapture()
        }

    @Test
    fun queuedCaptureCanBeCancelledBeforeItStarts() =
        runTest(dispatcher.scheduler) {
            var captureCount = 0
            val viewModel =
                SimTelephonyViewModel(
                    provider =
                        SimTelephonyProvider {
                            captureCount += 1
                            simInfo()
                        },
                )

            viewModel.cancelCapture()
            advanceUntilIdle()

            assertEquals(0, captureCount)
            assertNull(viewModel.state.value.info)
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
