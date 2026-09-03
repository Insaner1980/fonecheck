package com.insaner.fonecheck.ui.screens.home

import com.insaner.fonecheck.ui.components.LARGE_FONT_SCALE_THRESHOLD

internal const val HOME_STATUS_PANEL_COLUMNS = 2
internal const val HOME_STATUS_PANEL_COMPACT_COLUMNS = 1

/**
 * Home's big passed-category figure stacks at the same point every label-and-value row does. One
 * threshold, so a screen does not change shape in two stages as the font grows.
 */
internal const val HOME_LARGE_FONT_SCALE_THRESHOLD = LARGE_FONT_SCALE_THRESHOLD

internal fun homeStatusGridColumnCount(
    twoColumnNameWidthPx: Int,
    longestNameWidthPx: Int,
): Int =
    if (longestNameWidthPx > twoColumnNameWidthPx) {
        HOME_STATUS_PANEL_COMPACT_COLUMNS
    } else {
        HOME_STATUS_PANEL_COLUMNS
    }

internal fun <T> homeStatusPanelRows(
    items: List<T>,
    columns: Int = HOME_STATUS_PANEL_COLUMNS,
): List<List<T>> = items.chunked(columns)
