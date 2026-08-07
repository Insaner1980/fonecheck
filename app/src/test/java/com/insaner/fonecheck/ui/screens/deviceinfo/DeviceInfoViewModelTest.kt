package com.insaner.fonecheck.ui.screens.deviceinfo

import com.insaner.fonecheck.domain.model.DeviceInfo
import java.time.Instant
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
            assertEquals("first", viewModel.state.value.info?.model)
            assertFalse(viewModel.state.value.isLoading)

            viewModel.refresh()
            advanceUntilIdle()
            assertEquals(2, captureCount)
            assertEquals("second", viewModel.state.value.info?.model)
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

            assertEquals("available", viewModel.state.value.info?.model)
            assertFalse(viewModel.state.value.isLoading)
            assertNotNull(viewModel.state.value.error)
        }

    private fun device(model: String) =
        DeviceInfo(
            model = model,
            manufacturer = "manufacturer",
            brand = "brand",
            product = "product",
            androidVersion = "16",
            apiLevel = 36,
            securityPatch = "2026-08-01",
            buildNumber = "build",
            kernelVersion = "kernel",
            basebandVersion = "baseband",
            bootloaderVersion = "bootloader",
            widevineLevel = "L1",
            rootArtifactDetected = false,
            developerOptionsEnabled = false,
            usbDebuggingEnabled = false,
            capturedAt = Instant.parse("2026-08-07T12:00:00Z"),
        )
}
