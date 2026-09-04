package com.insaner.fonecheck.ui.screens.deviceinfo

import com.insaner.fonecheck.testing.testDeviceInfo
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceInfoViewModelTest {
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
    fun captureIsDeferredAndRefreshReplacesSnapshot() =
        runTest(dispatcher.scheduler) {
            val snapshots = ArrayDeque(listOf(device("first"), device("second")))
            var captureCount = 0
            val viewModel =
                DeviceInfoViewModel(
                    deviceInfoProvider =
                        DeviceInfoProvider {
                            captureCount += 1
                            snapshots.removeFirst()
                        },
                    ioDispatcher = dispatcher,
                )

            assertEquals(0, captureCount)
            assertEquals(DeviceInfoState(isLoading = true), viewModel.state.value)

            advanceUntilIdle()
            assertEquals(1, captureCount)
            assertEquals(
                "first",
                viewModel.state.value.info
                    ?.model,
            )
            assertFalse(viewModel.state.value.isLoading)

            viewModel.refresh()
            advanceUntilIdle()
            assertEquals(2, captureCount)
            assertEquals(
                "second",
                viewModel.state.value.info
                    ?.model,
            )
            assertNull(viewModel.state.value.error)
        }

    @Test
    fun failedRefreshKeepsLastSuccessfulSnapshot() =
        runTest(dispatcher.scheduler) {
            var shouldFail = false
            val viewModel =
                DeviceInfoViewModel(
                    deviceInfoProvider =
                        DeviceInfoProvider {
                            if (shouldFail) error("capture failed")
                            device("available")
                        },
                    ioDispatcher = dispatcher,
                )
            advanceUntilIdle()

            shouldFail = true
            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(
                "available",
                viewModel.state.value.info
                    ?.model,
            )
            assertFalse(viewModel.state.value.isLoading)
            assertNotNull(viewModel.state.value.error)
        }

    @Test
    fun queuedCaptureCanBeCancelledBeforeItStarts() =
        runTest(dispatcher.scheduler) {
            var captureCount = 0
            val viewModel =
                DeviceInfoViewModel(
                    deviceInfoProvider =
                        DeviceInfoProvider {
                            captureCount += 1
                            device("late")
                        },
                    ioDispatcher = dispatcher,
                )

            viewModel.cancelCapture()
            advanceUntilIdle()

            assertEquals(0, captureCount)
            assertNull(viewModel.state.value.info)
        }

    private fun device(model: String) = testDeviceInfo(model = model)
}
