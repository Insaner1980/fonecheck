package com.insaner.fonecheck.ui.screens.camera

import com.insaner.fonecheck.runtime.EpochMillisClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CameraCaptureAttempt(
    val id: Long,
    val cameraId: String,
    val stageToken: Long?,
)

/** Owns the same capture transitions used by CameraX callbacks, timeout and teardown. */
internal class CameraCaptureSession(
    private val state: MutableStateFlow<CameraTestState>,
    private val clock: EpochMillisClock,
    private val scope: CoroutineScope,
) {
    private val gate = CameraOperationGate()
    private var timeout: Job? = null

    fun begin(
        cameraId: String,
        stageToken: Long?,
    ): CameraCaptureAttempt? =
        synchronized(gate) {
            if (state.value.isCapturing) return@synchronized null
            val attempt = CameraCaptureAttempt(gate.begin(), cameraId, stageToken)
            timeout?.cancel()
            state.update {
                it.copy(
                    isCapturing = true,
                    lastCapture = null,
                    error = null,
                    captureCompletedAt = null,
                    confirmations =
                        it.confirmations - cameraId,
                )
            }
            timeout =
                scope.launch {
                    delay(TIMEOUT_MS)
                    fail(attempt, "camera_capture_timeout")
                }
            attempt
        }

    fun succeed(
        attempt: CameraCaptureAttempt,
        width: Int,
        height: Int,
    ): Boolean =
        gate.complete(attempt.id) {
            timeout?.cancel()
            val result = CaptureResult(width, height, clock.currentTimeMillis(), attempt)
            state.update {
                it.copy(
                    isCapturing = false,
                    lastCapture = result,
                    error = null,
                    captureCompletedAt = result.timestamp,
                )
            }
        }

    fun fail(
        attempt: CameraCaptureAttempt,
        message: String,
    ): Boolean =
        gate.complete(attempt.id) {
            timeout?.cancel()
            val completedAt = clock.currentTimeMillis()
            state.update {
                it.copy(
                    isCapturing = false,
                    lastCapture = null,
                    error = message,
                    captureCompletedAt = completedAt,
                )
            }
        }

    fun cancel() =
        synchronized(gate) {
            gate.cancelAll()
            timeout?.cancel()
            timeout = null
            state.update { it.copy(isCapturing = false) }
        }

    fun confirm(passed: Boolean) =
        synchronized(gate) {
            val capture = state.value.lastCapture ?: return@synchronized
            val cameraId = capture.attempt?.cameraId ?: return@synchronized
            if (state.value.isCapturing) return@synchronized
            state.update { it.copy(confirmations = it.confirmations + (cameraId to passed)) }
        }

    companion object {
        const val TIMEOUT_MS = 8_000L
    }
}
