package com.insaner.fonecheck.ui.screens.camera

import com.insaner.fonecheck.runtime.EpochMillisClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraCaptureSessionTest {
    @Test
    fun successfulRetakeNeedsANewConfirmationAndKeepsItsOwnCompletionTime() =
        runTest {
            var now = 100L
            val state = MutableStateFlow(CameraTestState())
            val session = CameraCaptureSession(state, EpochMillisClock { now }, backgroundScope)
            val first = requireNotNull(session.begin("rear", null))
            session.succeed(first, 640, 480)
            session.confirm(true)
            val second = requireNotNull(session.begin("rear", null))
            now = 200L
            session.succeed(second, 800, 600)
            assertNull(state.value.confirmations["rear"])
            now = 900L
            assertFalse(session.fail(first, "late"))
            assertEquals(200L, state.value.lastCapture?.timestamp)
            session.confirm(false)
            assertEquals(false, state.value.confirmations["rear"])
        }

    @Test
    fun cancelledCallbacksCannotPublishIntoANewAttempt() =
        runTest {
            val state = MutableStateFlow(CameraTestState())
            val session = CameraCaptureSession(state, EpochMillisClock { 123L }, backgroundScope)
            val old = requireNotNull(session.begin("rear", 1L))
            session.cancel()
            val current = requireNotNull(session.begin("front", 2L))

            assertFalse(session.succeed(old, 640, 480))
            assertFalse(session.fail(old, "old error"))
            assertTrue(state.value.isCapturing)
            assertNull(state.value.error)
            assertNull(state.value.lastCapture)
            assertTrue(session.succeed(current, 800, 600))
            assertEquals(current, state.value.lastCapture?.attempt)
            assertEquals(123L, state.value.lastCapture?.timestamp)
            assertFalse(session.succeed(current, 1, 1))
            assertFalse(session.fail(current, "duplicate"))
            assertEquals(800, state.value.lastCapture?.width)
        }

    @Test
    fun aNewAttemptInvalidatesOnlyItsOwnConfirmationEvenWhenItFails() =
        runTest {
            val state = MutableStateFlow(CameraTestState(confirmations = mapOf("front" to true)))
            val session = CameraCaptureSession(state, EpochMillisClock { 10L }, backgroundScope)
            val first = requireNotNull(session.begin("rear", null))
            session.succeed(first, 640, 480)
            session.confirm(true)
            assertEquals(true, state.value.confirmations["rear"])
            val second = requireNotNull(session.begin("rear", null))
            assertNull(state.value.lastCapture)
            assertEquals(mapOf("front" to true), state.value.confirmations)
            session.fail(second, "failed")
            assertFalse(session.succeed(first, 640, 480))
            session.confirm(true)
            assertEquals(mapOf("front" to true), state.value.confirmations)
        }

    @Test
    fun timeoutAndSuccessHaveOneTerminalResult() =
        runTest {
            val state = MutableStateFlow(CameraTestState())
            val session = CameraCaptureSession(state, EpochMillisClock { 10L }, backgroundScope)
            val attempt = requireNotNull(session.begin("rear", 1L))
            testScheduler.advanceTimeBy(CameraCaptureSession.TIMEOUT_MS + 1)
            testScheduler.runCurrent()
            assertEquals("camera_capture_timeout", state.value.error)
            assertFalse(session.succeed(attempt, 640, 480))
            assertFalse(state.value.isCapturing)
        }
}
