package com.insaner.fonecheck.data.repository

import com.insaner.fonecheck.testing.testReport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class ReportRepositoryTest {
    @Test
    fun `insert confirmation read rethrows cancellation`() =
        runTest {
            val repository =
                object : ReportRepository by FakeReportRepository(insertFailuresRemaining = 1) {
                    override suspend fun getById(id: String): ReportLoadResult =
                        throw CancellationException("confirmation_cancelled")
                }

            try {
                repository.insertOrConfirm(testReport())
                fail("Expected cancellation")
            } catch (error: CancellationException) {
                assertEquals("confirmation_cancelled", error.message)
            }
        }
}
