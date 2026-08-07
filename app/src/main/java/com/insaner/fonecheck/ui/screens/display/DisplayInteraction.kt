package com.insaner.fonecheck.ui.screens.display

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

enum class DisplayPattern {
    RED,
    GREEN,
    BLUE,
    WHITE,
    BLACK,
    GRAY_GRADIENT,
}

data class TouchPoint(
    val xFraction: Float,
    val yFraction: Float,
)

object TouchGridGeometry {
    fun cellsAlongSegment(
        start: TouchPoint,
        end: TouchPoint,
        columns: Int,
        rows: Int,
    ): Set<Int> {
        require(columns > 0 && rows > 0)
        val horizontalCells = abs(end.xFraction - start.xFraction) * columns
        val verticalCells = abs(end.yFraction - start.yFraction) * rows
        val steps = ceil(max(horizontalCells, verticalCells) * 2f).toInt().coerceAtLeast(1)
        return buildSet {
            for (step in 0..steps) {
                val fraction = step.toFloat() / steps
                val point =
                    TouchPoint(
                        xFraction = start.xFraction + (end.xFraction - start.xFraction) * fraction,
                        yFraction = start.yFraction + (end.yFraction - start.yFraction) * fraction,
                    )
                add(cellAt(point, columns, rows))
            }
        }
    }

    private fun cellAt(
        point: TouchPoint,
        columns: Int,
        rows: Int,
    ): Int {
        val column = (point.xFraction.coerceIn(0f, 1f) * columns).toInt().coerceAtMost(columns - 1)
        val row = (point.yFraction.coerceIn(0f, 1f) * rows).toInt().coerceAtMost(rows - 1)
        return row * columns + column
    }
}

object TouchTestReducer {
    fun record(
        state: TouchTestState,
        cells: Set<Int>,
        activePointers: Map<Long, TouchPoint>,
    ): TouchTestState =
        state.copy(
            touchedCells = state.touchedCells + cells,
            activePointers = activePointers,
            maxPointerCount = max(state.maxPointerCount, activePointers.size),
        )

    fun reset(state: TouchTestState): TouchTestState =
        state.copy(
            touchedCells = emptySet(),
            activePointers = emptyMap(),
            maxPointerCount = 0,
            isComplete = false,
        )

    fun complete(state: TouchTestState): TouchTestState =
        state.copy(
            isActive = false,
            activePointers = emptyMap(),
            isComplete = true,
        )
}
