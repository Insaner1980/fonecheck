package com.insaner.fonecheck.ui.screens.home

import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.testing.testReport
import com.insaner.fonecheck.ui.theme.SemanticTone
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeReadoutTest {
    @Test
    fun `segment count follows the report rather than the category catalogue`() {
        val threeCategories = report(List(3) { DiagnosticStatus.PASS })

        assertEquals(3, latestCheckSegments(threeCategories).size)
        // The catalogue has more categories than this report carries; the bar must not pad up to it.
        assertEquals(14, DiagnosticCategoryId.entries.size)
    }

    @Test
    fun `a full run draws one segment per category`() {
        val everyCategory = report(DiagnosticCategoryId.entries.map { DiagnosticStatus.PASS })

        assertEquals(DiagnosticCategoryId.entries.size, latestCheckSegments(everyCategory).size)
    }

    @Test
    fun `each segment takes the tone of its own category`() {
        val mixed =
            report(
                listOf(
                    DiagnosticStatus.PASS,
                    DiagnosticStatus.WARNING,
                    DiagnosticStatus.FAIL,
                    DiagnosticStatus.NOT_TESTED,
                ),
            )

        assertEquals(
            listOf(
                SemanticTone.PASS,
                SemanticTone.ATTENTION,
                SemanticTone.FAIL,
                SemanticTone.NEUTRAL,
            ),
            latestCheckSegments(mixed),
        )
    }

    @Test
    fun `a report with no categories draws no segments`() {
        assertEquals(emptyList<SemanticTone>(), latestCheckSegments(report(emptyList())))
    }

    private fun report(statuses: List<DiagnosticStatus>) =
        testReport(
            categories =
                statuses.mapIndexed { index, status ->
                    DiagnosticCategoryResult(
                        categoryId = DiagnosticCategoryId.entries[index],
                        aggregateStatus = status,
                        evidence = emptyList(),
                    )
                },
        )
}
