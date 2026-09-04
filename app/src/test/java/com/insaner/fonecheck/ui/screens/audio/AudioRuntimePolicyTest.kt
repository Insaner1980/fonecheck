package com.insaner.fonecheck.ui.screens.audio

import com.insaner.fonecheck.ui.screens.buttons.VolumeButtonDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioRuntimePolicyTest {
    @Test
    fun ownerStopsAndReleasesEachResourceExactlyOnceAcrossRepeatedStopAndReplacement() {
        val first = FakeResource()
        val second = FakeResource()
        val owner = AudioResourceOwner<FakeResource> { it.stopAndRelease() }

        owner.replace(first)
        owner.replace(second)
        owner.release(first)
        owner.release()
        owner.release(second)

        assertEquals(1, first.releaseCount)
        assertEquals(1, second.releaseCount)
    }

    @Test
    fun routeSessionRestoresRouteExactlyOnceOnCancellationAndRepeatedClose() {
        val route = FakeRouteController()
        val session = AudioRouteSession(route)

        assertTrue(session.open(AudioOutputRoute.EARPIECE))
        session.close()
        session.close()

        assertEquals(listOf(AudioOutputRoute.EARPIECE), route.requests)
        assertEquals(1, route.clearCount)
    }

    @Test
    fun recordingPolicyRejectsDeniedMissingHardwareAndDuplicateStart() {
        assertFalse(AudioRecordingPolicy.canStart(hasMicrophone = false, permissionGranted = true, isRecording = false))
        assertFalse(AudioRecordingPolicy.canStart(hasMicrophone = true, permissionGranted = false, isRecording = false))
        assertFalse(AudioRecordingPolicy.canStart(hasMicrophone = true, permissionGranted = true, isRecording = true))
        assertTrue(AudioRecordingPolicy.canStart(hasMicrophone = true, permissionGranted = true, isRecording = false))
    }

    @Test
    fun relativeInputLevelHandlesMuteAndMaximumAmplitudeWithoutDbClaim() {
        assertEquals(0f, RelativeInputLevel.fromPcm16(shortArrayOf(0, 0, 0), 3), 0.0001f)
        assertEquals(
            1f,
            RelativeInputLevel.fromPcm16(shortArrayOf(Short.MAX_VALUE, Short.MIN_VALUE), 2),
            0.001f,
        )
    }

    @Test
    fun operationGateRejectsReplacedAndCancelledCompletions() {
        val gate = AudioOperationGate()
        val first = gate.start()
        val second = gate.start()

        assertFalse(gate.isCurrent(first))
        assertTrue(gate.isCurrent(second))

        gate.cancel()

        assertFalse(gate.isCurrent(second))
    }

    @Test
    fun userStopKeepsRecordingCompletionCurrentButCancellationRejectsIt() {
        val gate = AudioOperationGate()
        val userStoppedRecording = gate.start()

        gate.stop(AudioRecordingStopMode.KEEP_RESULT)

        assertTrue(gate.isCurrent(userStoppedRecording))

        gate.stop(AudioRecordingStopMode.DISCARD_RESULT)

        assertFalse(gate.isCurrent(userStoppedRecording))
    }

    @Test
    fun volumeButtonDirectionUpdatesOnlyItsOwnCounterAndPressedState() {
        val initial = AudioTestState(volumeDownCount = 2)

        val updated = initial.recordVolumeButton(VolumeButtonDirection.UP)

        assertTrue(updated.volumeUpPressed)
        assertEquals(1, updated.volumeUpCount)
        assertFalse(updated.volumeDownPressed)
        assertEquals(2, updated.volumeDownCount)
    }

    @Test
    fun lifecycleCleanupCancelsOnlyAnActiveRecording() {
        var cancellationCount = 0

        cancelActiveRecording(isRecording = false) { cancellationCount += 1 }
        assertEquals(0, cancellationCount)

        cancelActiveRecording(isRecording = true) { cancellationCount += 1 }
        assertEquals(1, cancellationCount)
    }

    private class FakeResource {
        var releaseCount = 0

        fun stopAndRelease() {
            releaseCount += 1
        }
    }

    private class FakeRouteController : AudioRouteController {
        val requests = mutableListOf<AudioOutputRoute>()
        var clearCount = 0

        override fun request(route: AudioOutputRoute): Boolean {
            requests += route
            return true
        }

        override fun clear() {
            clearCount += 1
        }
    }
}
