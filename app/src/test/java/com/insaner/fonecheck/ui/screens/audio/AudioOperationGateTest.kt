package com.insaner.fonecheck.ui.screens.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AudioOperationGateTest {
    @Test
    fun obsoleteOperationCannotInstallAResourceOrPublishAResult() {
        val gate = AudioOperationGate()
        val obsolete = gate.start()
        gate.cancel()
        val current = gate.start()
        var published = false

        assertFalse(gate.runIfCurrent(obsolete) { published = true })
        assertFalse(published)
        assertTrue(gate.runIfCurrent(current) { published = true })
        assertTrue(published)
    }

    @Test
    fun cancellationCannotReturnBetweenAdmissionAndPublication() {
        val gate = AudioOperationGate()
        val token = gate.start()
        val admitted = CountDownLatch(1)
        val finishPublication = CountDownLatch(1)
        val cancelling = CountDownLatch(1)
        val cancelled = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val publication =
                executor.submit<Boolean> {
                    gate.runIfCurrent(token) {
                        admitted.countDown()
                        check(finishPublication.await(5, TimeUnit.SECONDS))
                    }
                }
            assertTrue(admitted.await(5, TimeUnit.SECONDS))
            val cancellation =
                executor.submit {
                    cancelling.countDown()
                    gate.cancel()
                    cancelled.countDown()
                }
            assertTrue(cancelling.await(5, TimeUnit.SECONDS))
            assertFalse(cancelled.await(100, TimeUnit.MILLISECONDS))
            finishPublication.countDown()

            assertTrue(publication.get(5, TimeUnit.SECONDS))
            cancellation.get(5, TimeUnit.SECONDS)
            assertFalse(gate.runIfCurrent(token) { error("Cancelled result was published") })
        } finally {
            finishPublication.countDown()
            executor.shutdownNow()
            check(executor.awaitTermination(5, TimeUnit.SECONDS))
        }
    }
}
