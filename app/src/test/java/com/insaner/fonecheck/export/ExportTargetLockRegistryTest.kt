package com.insaner.fonecheck.export

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ExportTargetLockRegistryTest {
    @Test
    fun theSameFinalPathCannotEnterFinalizationTwiceAtOnce() =
        runTest {
            val target = File("same-${UUID.randomUUID()}.json")
            val firstEntered = CompletableDeferred<Unit>()
            val releaseFirst = CompletableDeferred<Unit>()
            val secondEntered = CompletableDeferred<Unit>()

            val first =
                launch {
                    ExportTargetLockRegistry.withLock(target) {
                        firstEntered.complete(Unit)
                        releaseFirst.await()
                    }
                }
            firstEntered.await()
            val second =
                launch {
                    ExportTargetLockRegistry.withLock(target) {
                        secondEntered.complete(Unit)
                    }
                }
            runCurrent()

            assertFalse(secondEntered.isCompleted)
            releaseFirst.complete(Unit)
            first.join()
            second.join()
            assertTrue(secondEntered.isCompleted)
        }

    @Test
    fun differentFinalPathsRemainIndependent() =
        runTest {
            val firstTarget = File("first-${UUID.randomUUID()}.json")
            val secondTarget = File("second-${UUID.randomUUID()}.json")
            val firstEntered = CompletableDeferred<Unit>()
            val releaseFirst = CompletableDeferred<Unit>()
            val secondEntered = CompletableDeferred<Unit>()

            val first =
                launch {
                    ExportTargetLockRegistry.withLock(firstTarget) {
                        firstEntered.complete(Unit)
                        releaseFirst.await()
                    }
                }
            firstEntered.await()
            val second =
                launch {
                    ExportTargetLockRegistry.withLock(secondTarget) {
                        secondEntered.complete(Unit)
                    }
                }

            secondEntered.await()
            assertTrue(secondEntered.isCompleted)
            releaseFirst.complete(Unit)
            first.join()
            second.join()
        }

    @Test
    fun completedFinalPathIsEvictedAfterItsLastUserExits() =
        runTest {
            val target = File("completed-${UUID.randomUUID()}.json")

            ExportTargetLockRegistry.withLock(target) {}

            assertFalse(registeredPaths().contains(target.absolutePath))
        }

    @Suppress("UNCHECKED_CAST")
    private fun registeredPaths(): Set<String> {
        val locksField = ExportTargetLockRegistry::class.java.getDeclaredField("locks")
        locksField.isAccessible = true
        val locks = locksField.get(ExportTargetLockRegistry) as ConcurrentHashMap<String, *>
        return locks.keys.toSet()
    }
}
