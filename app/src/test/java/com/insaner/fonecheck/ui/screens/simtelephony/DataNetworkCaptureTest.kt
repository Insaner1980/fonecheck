package com.insaner.fonecheck.ui.screens.simtelephony

import com.insaner.fonecheck.domain.model.NetworkReadState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DataNetworkCaptureTest {
    private val epoch = Instant.parse("2026-09-23T18:00:00Z")

    @Test
    fun receivedValuesUseOneSubscriptionAndRetainReadAndReceiptTimes() =
        runTest {
            val platform = FakePlatform()
            val capture =
                DataNetworkCapture(
                    platform,
                    { epoch.plusMillis(testScheduler.currentTime) },
                    { testScheduler.currentTime },
                )
            val job = async { capture.capture() }
            runCurrent()
            advanceTimeBy(100)
            platform.display(13, 3)
            runCurrent()
            val result = job.await()
            assertEquals(13, result.baseType)
            assertEquals(epoch.plusMillis(100), result.baseReadAt)
            assertEquals(epoch.plusMillis(100), result.display.receivedAt)
            assertEquals(3, result.display.overrideType)
            assertEquals(listOf(7, 7), platform.readSubscriptions)
            assertEquals(7, platform.listenSubscription)
            assertEquals(1, platform.stops)
        }

    @Test
    fun timeoutCleansUpAndLateCallbackCannotChangeCompletedSnapshot() =
        runTest {
            val platform = FakePlatform()
            val job = async { DataNetworkCapture(platform, { epoch }, { testScheduler.currentTime }).capture() }
            runCurrent()
            advanceTimeBy(1_200)
            runCurrent()
            val result = job.await()
            assertEquals(NetworkReadState.TIMED_OUT, result.display.state)
            assertNull(result.display.overrideType)
            assertNull(result.display.receivedAt)
            platform.display(13, 3)
            assertEquals(result, job.await())
            assertEquals(1, platform.stops)
        }

    @Test
    fun cancellationAndReplacementDoNotAcceptOldCallbacks() =
        runTest {
            val platform = FakePlatform()
            val capture = DataNetworkCapture(platform, { epoch }, { testScheduler.currentTime })
            val first = async { capture.capture() }
            runCurrent()
            val oldCallback = platform.display
            first.cancel()
            first.join()
            assertEquals(1, platform.stops)
            val second = async { capture.capture() }
            runCurrent()
            oldCallback(13, 3)
            runCurrent()
            assertTrue(second.isActive)
            platform.display(13, 0)
            runCurrent()
            assertEquals(0, second.await().display.overrideType)
            assertEquals(2, platform.stops)
        }

    @Test
    fun duplicateCallbackDoesNotReplaceFirstReceivedObservation() =
        runTest {
            val platform = FakePlatform()
            val job = async { DataNetworkCapture(platform, { epoch }, { testScheduler.currentTime }).capture() }
            runCurrent()
            platform.display(13, 3)
            platform.display(13, 0)
            runCurrent()
            assertEquals(3, job.await().display.overrideType)
            assertEquals(1, platform.stops)
        }

    @Test
    fun subscriptionChangeIncludingChangeBackInvalidatesCapture() =
        runTest {
            for (changeBack in listOf(false, true)) {
                val platform = FakePlatform()
                val job = async { DataNetworkCapture(platform, { epoch }, { testScheduler.currentTime }).capture() }
                runCurrent()
                platform.subscription = 8
                platform.changed()
                if (changeBack) platform.subscription = 7
                platform.display(13, 3)
                runCurrent()
                assertEquals(NetworkReadState.STALE, job.await().display.state)
                assertEquals(NetworkReadState.STALE, job.await().baseState)
                assertEquals(1, platform.stops)
            }
        }

    @Test
    fun mismatchedBaseAndUnknownOverrideRemainExplicit() =
        runTest {
            val cases =
                listOf(
                    Triple(20, 0, NetworkReadState.STALE),
                    Triple(13, 999, NetworkReadState.UNRECOGNIZED),
                )
            for ((displayBase, overrideType, expected) in cases) {
                val platform = FakePlatform()
                val job = async { DataNetworkCapture(platform, { epoch }, { testScheduler.currentTime }).capture() }
                runCurrent()
                platform.display(displayBase, overrideType)
                runCurrent()
                val result = job.await()
                assertEquals(expected, result.display.state)
                assertEquals(displayBase, result.display.baseType)
                assertEquals(overrideType, result.display.overrideType)
                assertEquals(1, platform.stops)
            }
        }

    @Test
    fun delayedDispatchBeyondMonotonicDeadlineIsStaleEvenIfWallClockMovesBackwards() =
        runTest {
            val platform = FakePlatform()
            var elapsed = 0L
            var wall = epoch
            val job = async { DataNetworkCapture(platform, { wall }, { elapsed }).capture() }
            runCurrent()
            wall = epoch.minusSeconds(30)
            elapsed = 1_201
            platform.display(13, 3)
            runCurrent()
            assertEquals(NetworkReadState.STALE, job.await().display.state)
            assertEquals(wall, job.await().display.receivedAt)
        }

    @Test
    fun unavailablePrerequisitesDoNotRegisterListenersOrInventNone() =
        runTest {
            val cases =
                listOf(
                    FakePlatform().apply { hardware = false } to NetworkReadState.NO_HARDWARE,
                    FakePlatform().apply { permission = false } to NetworkReadState.PERMISSION_DENIED,
                    FakePlatform().apply { subscription = null } to NetworkReadState.NO_SUBSCRIPTION,
                    FakePlatform().apply { displaySupported = false } to NetworkReadState.UNSUPPORTED,
                    FakePlatform().apply { readFailure = SecurityException() } to NetworkReadState.PERMISSION_DENIED,
                    FakePlatform().apply { readFailure = UnsupportedOperationException() } to NetworkReadState.FAILED,
                )
            for ((platform, expected) in cases) {
                val result = DataNetworkCapture(platform, { epoch }, { 0L }).capture()
                assertEquals(expected, result.display.state)
                assertNull(result.display.overrideType)
                assertNull(platform.listenSubscription)
                if (expected == NetworkReadState.UNSUPPORTED) assertEquals(13, result.baseType)
            }
        }

    @Test
    fun failedRereadRetainsRawCallbackButDoesNotPresentAReceivedIndication() =
        runTest {
            val platform = FakePlatform()
            val job = async { DataNetworkCapture(platform, { epoch }, { testScheduler.currentTime }).capture() }
            runCurrent()
            platform.readFailure = IllegalStateException()
            platform.display(13, 3)
            runCurrent()
            val result = job.await()
            assertEquals(NetworkReadState.FAILED, result.baseState)
            assertEquals(NetworkReadState.FAILED, result.display.state)
            assertEquals(3, result.display.overrideType)
            assertEquals(epoch, result.display.receivedAt)
            assertEquals(1, platform.stops)
        }

    @Test
    fun permissionRevocationAndRegistrationFailureAreExplicit() =
        runTest {
            val failed = FakePlatform().apply { registrationFailure = SecurityException() }
            val failure = DataNetworkCapture(failed, { epoch }, { 0L }).capture()
            assertEquals(NetworkReadState.PERMISSION_DENIED, failure.display.state)
            val unavailableListener = FakePlatform().apply { registrationFailure = IllegalStateException() }
            val registration = DataNetworkCapture(unavailableListener, { epoch }, { 0L }).capture()
            assertEquals(NetworkReadState.FAILED, registration.display.state)
            assertEquals(NetworkReadState.RECEIVED, registration.baseState)
            assertEquals(13, registration.baseType)
            val platform = FakePlatform()
            val job = async { DataNetworkCapture(platform, { epoch }, { testScheduler.currentTime }).capture() }
            runCurrent()
            platform.permission = false
            platform.display(13, 3)
            runCurrent()
            assertEquals(NetworkReadState.PERMISSION_DENIED, job.await().display.state)
            assertEquals(1, platform.stops)
        }

    private class FakePlatform : DataNetworkPlatform {
        override var displaySupported = true
        var hardware = true
        var permission = true
        var subscription: Int? = 7
        var readFailure: RuntimeException? = null
        var registrationFailure: RuntimeException? = null
        var display: (Int, Int) -> Unit = { _, _ -> }
        var changed: () -> Unit = {}
        var listenSubscription: Int? = null
        var stops = 0
        val readSubscriptions = mutableListOf<Int>()

        override fun hasHardware() = hardware

        override fun hasPermission() = permission

        override fun dataSubscription() = subscription

        override fun readBase(subscription: Int): Int {
            readFailure?.let { throw it }
            readSubscriptions += subscription
            return 13
        }

        override fun listen(
            subscription: Int,
            onDisplay: (Int, Int) -> Unit,
            onSubscriptionChanged: () -> Unit,
        ): () -> Unit {
            registrationFailure?.let { throw it }
            listenSubscription = subscription
            display = onDisplay
            changed = onSubscriptionChanged
            return { stops += 1 }
        }
    }
}
