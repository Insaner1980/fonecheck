package com.insaner.fonecheck.ui.screens.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageRuntimePolicyTest {
    @Test
    fun usagePercentUsesAvailableBytesAndStaysBounded() {
        assertEquals(75.0, StorageRuntimePolicy.usagePercent(totalBytes = 400, availableBytes = 100) ?: 0.0, 0.001)
        assertEquals(0.0, StorageRuntimePolicy.usagePercent(totalBytes = 400, availableBytes = 500) ?: -1.0, 0.001)
        assertEquals(100.0, StorageRuntimePolicy.usagePercent(totalBytes = 400, availableBytes = -1) ?: 0.0, 0.001)
        assertNull(StorageRuntimePolicy.usagePercent(totalBytes = 0, availableBytes = 0))
    }

    @Test
    fun freeSpacePrecheckReservesSpaceAfterTemporaryFile() {
        assertTrue(StorageRuntimePolicy.hasBenchmarkSpace(availableBytes = 12, dataSizeBytes = 8, reserveBytes = 4))
        assertTrue(!StorageRuntimePolicy.hasBenchmarkSpace(availableBytes = 11, dataSizeBytes = 8, reserveBytes = 4))
    }
}
