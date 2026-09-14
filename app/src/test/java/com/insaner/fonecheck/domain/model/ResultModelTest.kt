package com.insaner.fonecheck.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResultModelTest {
    @Test
    fun `result model retains diagnostic statuses and presentation fields`() {
        val statuses = DiagnosticStatus.entries
        val results =
            statuses.mapIndexed { index, status ->
                TestResult(
                    id = "result-$index",
                    name = "Result $index",
                    status = status,
                    detail = null,
                    confidence = Confidence.HIGH,
                    timestamp = index.toLong(),
                )
            }
        val category =
            CategoryTestResult(
                category = DiagnosticCategoryId.BATTERY,
                status = DiagnosticStatus.WARNING,
                summary = "Battery summary",
                results = results,
            )

        assertEquals(
            statuses,
            category.results.map(TestResult::status),
        )
        assertEquals(DiagnosticCategoryId.BATTERY, category.category)
        assertEquals(DiagnosticStatus.WARNING, category.status)
        assertEquals("Battery summary", category.summary)
        results.forEachIndexed { index, result ->
            assertEquals("result-$index", result.id)
            assertEquals("Result $index", result.name)
            assertEquals(Confidence.HIGH, result.confidence)
            assertEquals(index.toLong(), result.timestamp)
            assertNull(result.detail)
        }
        val localized =
            results.first().copy(
                detail = "42",
                source = EvidenceSource.ANDROID_API,
                reason = "Localized reason",
            )
        assertEquals("42", localized.detail)
        assertEquals(EvidenceSource.ANDROID_API, localized.source)
        assertEquals("Localized reason", localized.reason)
        assertNull(results.first().source)
        assertNull(results.first().reason)
    }
}
