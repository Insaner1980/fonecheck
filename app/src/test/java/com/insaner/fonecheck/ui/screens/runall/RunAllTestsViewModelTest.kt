package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class RunAllTestsViewModelTest {
    @Test
    fun completeSessionUsesInjectedIdAndTimestamp() =
        runTest {
            val viewModel =
                RunAllTestsViewModel(
                    clock = EpochMillisClock { 1_754_563_200_000L },
                    idProvider = IdProvider { "session-123" },
                )

            viewModel.completeSession(deviceInfo(), emptyList())

            val session = requireNotNull(viewModel.state.value.session)
            assertEquals("session-123", session.id)
            assertEquals(1_754_563_200_000L, session.timestamp)
        }

    @Test
    fun completeSessionKeepsTheFirstCompletedSession() =
        runTest {
            var nextId = 1
            var nextTimestamp = 100L
            val viewModel =
                RunAllTestsViewModel(
                    clock = EpochMillisClock { nextTimestamp++ },
                    idProvider = IdProvider { "session-${nextId++}" },
                )

            viewModel.completeSession(deviceInfo(model = "first"), emptyList())
            val firstSession = requireNotNull(viewModel.state.value.session)

            viewModel.completeSession(deviceInfo(model = "second"), emptyList())
            val secondSession = requireNotNull(viewModel.state.value.session)

            assertSame(firstSession, secondSession)
            assertEquals("session-1", secondSession.id)
            assertEquals(100L, secondSession.timestamp)
        }

    private fun deviceInfo(model: String = "model") =
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
            isRooted = false,
            developerOptionsEnabled = false,
            usbDebuggingEnabled = false,
        )
}
