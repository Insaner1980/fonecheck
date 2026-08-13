package com.insaner.fonecheck.domain.model

import com.insaner.fonecheck.testing.testDeviceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResultModelTest {
    @Test
    fun `result model retains every status payload and session relationship`() {
        val statuses =
            listOf(
                TestStatus.Pass,
                TestStatus.Fail("failed"),
                TestStatus.Warning("warning"),
                TestStatus.Info("measured"),
                TestStatus.NotAvailable,
                TestStatus.NotTested,
            )
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
                status = TestStatus.Warning("warning"),
                summary = "Battery summary",
                results = results,
            )
        val session =
            TestSession(
                id = "session-1",
                timestamp = 1_000L,
                deviceInfo = testDeviceInfo(),
                categories = listOf(category),
                overallScore = 83,
            )

        assertEquals(
            statuses,
            session.categories
                .single()
                .results
                .map(TestResult::status),
        )
        assertEquals("failed", (statuses[1] as TestStatus.Fail).reason)
        assertEquals("warning", (statuses[2] as TestStatus.Warning).reason)
        assertEquals("measured", (statuses[3] as TestStatus.Info).message)
        assertEquals(83, session.overallScore)
        assertNull(results.first().source)
        assertNull(results.first().reason)
    }
}
