package com.insaner.fonecheck.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ReportEntityTest {
    @Test
    fun `valid report can be constructed`() {
        val report = validReport()

        assertEquals("report-1", report.id)
        assertEquals("{\"schemaVersion\":1}", report.payloadJson)
    }

    @Test
    fun `id and payload must be nonblank`() {
        assertInvalid { validReport().copy(id = " ") }
        assertInvalid { validReport().copy(payloadJson = "\n") }
    }

    @Test
    fun `kind and category must form a valid stable pair`() {
        assertInvalid { validReport().copy(reportKindCode = "unknown") }
        assertInvalid { validReport().copy(reportKindCode = "category_only", categoryId = null) }
        assertInvalid { validReport().copy(reportKindCode = "category_only", categoryId = " ") }
        assertInvalid { validReport().copy(categoryId = "battery") }
    }

    @Test
    fun `timestamps and versions must be valid`() {
        assertInvalid { validReport().copy(startedAtEpochMillis = 2_001L) }
        assertInvalid { validReport().copy(reportSchemaVersion = 0) }
        assertInvalid { validReport().copy(scoreVersion = -1) }
    }

    @Test
    fun `score value must match its stable state`() {
        assertInvalid { validReport().copy(scoreStateCode = "unknown") }
        assertInvalid { validReport().copy(scoreStateCode = "incomplete", scoreValue = 80) }
        assertInvalid { validReport().copy(scoreStateCode = "partial", scoreValue = null) }
        assertInvalid { validReport().copy(scoreValue = 101) }
    }

    @Test
    fun `coverage and counts must stay within their contract`() {
        assertInvalid { validReport().copy(coveragePercentage = -1) }
        assertInvalid { validReport().copy(coveragePercentage = 101) }
        assertInvalid { validReport().copy(unavailableCount = -1) }
        assertInvalid { validReport().copy(completedCount = 2) }
    }

    private fun validReport() =
        ReportEntity(
            id = "report-1",
            reportKindCode = "full_check",
            categoryId = null,
            startedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 2_000L,
            reportSchemaVersion = 1,
            scoreVersion = 1,
            scoreValue = 80,
            scoreStateCode = "complete",
            coveragePercentage = 100,
            applicableCount = 4,
            completedCount = 3,
            notTestedCount = 1,
            unavailableCount = 1,
            warningCount = 1,
            failureCount = 0,
            payloadJson = "{\"schemaVersion\":1}",
        )

    private fun assertInvalid(block: () -> Unit) {
        assertThrows(IllegalArgumentException::class.java, block)
    }
}
