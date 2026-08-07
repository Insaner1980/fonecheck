package com.insaner.fonecheck.ui.screens.audio

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
