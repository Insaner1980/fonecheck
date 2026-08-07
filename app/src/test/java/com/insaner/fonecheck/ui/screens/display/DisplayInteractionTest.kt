package com.insaner.fonecheck.ui.screens.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayInteractionTest {
    @Test
    fun visualPatternsIncludeRgbWhiteBlackAndGrayGradientInOrder() {
        assertEquals(
            listOf(
                DisplayPattern.RED,
                DisplayPattern.GREEN,
                DisplayPattern.BLUE,
                DisplayPattern.WHITE,
                DisplayPattern.BLACK,
                DisplayPattern.GRAY_GRADIENT,
            ),
            DisplayPattern.entries,
        )
    }

    @Test
    fun horizontalAndVerticalDragsCoverEveryCrossedGridCell() {
        assertEquals(
            (12..17).toSet(),
            TouchGridGeometry.cellsAlongSegment(
                start = TouchPoint(0f, 0.25f),
                end = TouchPoint(1f, 0.25f),
                columns = 6,
                rows = 10,
            ),
        )
        assertEquals(
            (0 until 10).map { row -> row * 6 + 3 }.toSet(),
            TouchGridGeometry.cellsAlongSegment(
                start = TouchPoint(0.5f, 0f),
                end = TouchPoint(0.5f, 1f),
                columns = 6,
                rows = 10,
            ),
        )
    }

    @Test
    fun touchReducerKeepsActualPointersPeakCountResetAndCompletion() {
        val twoPointers =
            mapOf(
                7L to TouchPoint(0.2f, 0.3f),
                9L to TouchPoint(0.8f, 0.7f),
            )
        val recorded =
            TouchTestReducer.record(
                state = TouchTestState(isActive = true),
                cells = setOf(1, 2),
                activePointers = twoPointers,
            )
        assertEquals(setOf(1, 2), recorded.touchedCells)
        assertEquals(twoPointers, recorded.activePointers)
        assertEquals(2, recorded.maxPointerCount)

        val onePointer =
            TouchTestReducer.record(
                state = recorded,
                cells = setOf(3),
                activePointers = mapOf(9L to TouchPoint(0.6f, 0.6f)),
            )
        assertEquals(setOf(1, 2, 3), onePointer.touchedCells)
        assertEquals(1, onePointer.activePointers.size)
        assertEquals(2, onePointer.maxPointerCount)

        val reset = TouchTestReducer.reset(onePointer)
        assertTrue(reset.isActive)
        assertTrue(reset.touchedCells.isEmpty())
        assertTrue(reset.activePointers.isEmpty())
        assertEquals(0, reset.maxPointerCount)
        assertFalse(reset.isComplete)

        val completed = TouchTestReducer.complete(recorded)
        assertFalse(completed.isActive)
        assertTrue(completed.isComplete)
        assertTrue(completed.activePointers.isEmpty())
    }
}
