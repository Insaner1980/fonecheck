package com.insaner.fonecheck.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfLayoutEngineTest {
    @Test fun exactFitDoesNotCreateTrailingPage() {
        val pages = PdfLayoutEngine.paginate(listOf(group(1, 40f), group(2, 60f)), 100f)
        assertEquals(1, pages.size)
        assertEquals(listOf(0f, 40f), pages.single().map { it.top })
    }

    @Test fun headingMovesWithWholeObservation() {
        val pages = PdfLayoutEngine.paginate(listOf(group(1, 70f), group(2, 15f, true), group(3, 30f)), 100f)
        assertEquals(listOf(listOf(1), listOf(2, 3)), pages.map { page -> page.map { it.row.token } })
    }

    @Test fun continuingCategoryReservesHeadingBeforeMovingWholeObservation() {
        val observation =
            PdfMeasuredGroup(
                listOf(PdfMeasuredRow(2, 25f), PdfMeasuredRow(3, 25f)),
                repeatedHeading = listOf(PdfMeasuredRow(-1, 20f)),
            )
        val pages = PdfLayoutEngine.paginate(listOf(group(1, 60f), observation), 100f)
        assertEquals(listOf(listOf(1), listOf(-1, 2, 3)), pages.map { it.map { row -> row.row.token } })
        assertEquals(listOf(0f, 20f, 45f), pages.last().map { it.top })
    }

    @Test fun categoryAndColumnHeadingChainCannotBeOrphanedBeforeOversizedObservation() {
        val groups =
            listOf(
                group(1, 75f),
                group(2, 15f, true),
                group(3, 15f, true),
                PdfMeasuredGroup((4..10).map { PdfMeasuredRow(it, 20f) }),
            )
        val pages = PdfLayoutEngine.paginate(groups, 100f)
        assertEquals(listOf(1), pages.first().map { it.row.token })
        assertEquals(listOf(2, 3, 4, 5, 6), pages[1].map { it.row.token })
        assertEquals((1..10).toList(), pages.flatten().map { it.row.token })
    }

    @Test fun oversizedObservationRepeatsHeadingAndPreservesEachDataRowOnce() {
        val data = (1..15).map { PdfMeasuredRow(it, 20f) }
        val group =
            PdfMeasuredGroup(
                data,
                repeatedHeading = listOf(PdfMeasuredRow(-1, 10f)),
                continuation = listOf(PdfMeasuredRow(-2, 10f)),
            )
        val pages = PdfLayoutEngine.paginate(listOf(group), 100f)
        assertEquals((1..15).toList(), pages.flatten().filter { it.row.token > 0 }.map { it.row.token })
        assertEquals(4, pages.size)
        pages.drop(1).forEach { assertEquals(listOf(-1, -2), it.take(2).map { row -> row.row.token }) }
        assertTrue(pages.all { page -> page.all { it.top + it.row.height <= 100f } })
    }

    private fun group(
        id: Int,
        height: Float,
        keep: Boolean = false,
    ) = PdfMeasuredGroup(listOf(PdfMeasuredRow(id, height)), keepWithNext = keep)
}
