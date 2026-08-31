package com.insaner.fonecheck.ui.screens.home

internal const val HOME_STATUS_PANEL_COLUMNS = 2
internal const val HOME_STATUS_PANEL_COMPACT_COLUMNS = 1
internal const val HOME_LARGE_FONT_SCALE_THRESHOLD = 1.3f

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
