package com.insaner.fonecheck.data.repository

import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.testing.batteryReport
import com.insaner.fonecheck.testing.testReport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ReportRepositoryTest {
    @Test
    fun `duplicate report with different evidence is not confirmed and preserves stored content`() =
        runTest {
            val repository = FakeReportRepository()
            val original = batteryReport(id = "saved-report", deviceModel = "Test")
            val category = original.categories.single()
            val conflicting =
                original.copy(
                    categories =
                        listOf(
                            category.copy(
                                evidence = listOf(category.evidence.single().copy(value = EvidenceValue.IntValue(81))),
                            ),
                        ),
                )
            assertNotEquals(original, conflicting)
            repository.insert(original)

            val confirmed = repository.insertOrConfirm(conflicting)

            val stored = (repository.getById(original.stableId) as ReportLoadResult.Available).report
            assertEquals(original, stored)
            assertEquals(
                EvidenceValue.IntValue(80),
                stored.categories
                    .single()
                    .evidence
                    .single()
                    .value,
            )
            assertEquals("Different report content must not be confirmed", false, confirmed)
        }

    @Test
    fun `duplicate report with equal content is confirmed after insert fails`() =
        runTest {
            val repository = FakeReportRepository()
            val original = batteryReport(id = "saved-report", deviceModel = "Test")
            val sameContent = original.copy()
            assertNotSame(original, sameContent)
            assertEquals(original, sameContent)
            repository.insert(original)

            val confirmed = repository.insertOrConfirm(sameContent)

            assertTrue(confirmed)
            assertEquals(ReportLoadResult.Available(original), repository.getById(original.stableId))
        }

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
