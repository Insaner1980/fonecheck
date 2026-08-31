package com.insaner.fonecheck.ui.screens.home

import com.insaner.fonecheck.navigation.diagnosticDestinations
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class HomeResponsiveLayoutTest {
    @Test
    fun `status panel keeps the fourteen canonical categories in a two column instrument grid`() {
        val columns = homeStatusGridColumnCount(100, 100)
        val rows = homeStatusPanelRows(diagnosticDestinations, columns)

        assertEquals(HOME_STATUS_PANEL_COLUMNS, columns)
        assertEquals(7, rows.size)
        rows.forEach { row -> assertEquals(2, row.size) }
        assertEquals(diagnosticDestinations, rows.flatten())
    }

    @Test
    fun `grid uses two columns exactly when the longest category name fits`() {
        assertEquals(1, homeStatusGridColumnCount(99, 100))
        assertEquals(2, homeStatusGridColumnCount(100, 100))
        assertEquals(2, homeStatusGridColumnCount(101, 100))
        assertEquals(2, homeStatusGridColumnCount(600, 550))
        assertEquals(14, homeStatusPanelRows(diagnosticDestinations, HOME_STATUS_PANEL_COMPACT_COLUMNS).size)
    }

    @Test
    fun `instrument readout uses two digits and a fixed timestamp pattern`() {
        assertEquals("09", homePaddedCount(9))
        assertEquals("14", homePaddedCount(14))
        assertEquals(
            "2026-08-11 13:18",
            formatHomeCompletedAt(
                Instant.parse("2026-08-11T10:18:00Z"),
                Locale.forLanguageTag("en-FI"),
                ZoneId.of("Europe/Helsinki"),
            ),
        )
    }

    @Test
    fun `report becomes stale at the single twenty four hour threshold`() {
        val completedAt = Instant.parse("2026-08-19T12:00:00Z")

        assertEquals(
            HomeReportRecency(isStale = false, elapsedDays = 0),
            homeReportRecency(completedAt, completedAt.plus(HOME_REPORT_STALE_AFTER).minusSeconds(1)),
        )
        assertEquals(
            HomeReportRecency(isStale = true, elapsedDays = 1),
            homeReportRecency(completedAt, completedAt.plus(HOME_REPORT_STALE_AFTER)),
        )
    }

    @Test
    fun `stale report age is expressed as elapsed whole days`() {
        val completedAt = Instant.parse("2026-08-11T12:00:00Z")

        assertEquals(
            HomeReportRecency(isStale = true, elapsedDays = 9),
            homeReportRecency(completedAt, Instant.parse("2026-08-20T13:00:00Z")),
        )
    }

    @Test
    fun `latest report keeps reference composition on a normal phone`() {
        assertEquals(false, latestReportUsesStackedLayout(328f, 1f))
        assertEquals(false, latestReportUsesStackedLayout(480f, HOME_LARGE_FONT_SCALE_THRESHOLD))
    }

    @Test
    fun `latest report stacks only when width or font scale requires it`() {
        assertEquals(true, latestReportUsesStackedLayout(311f, 1f))
        assertEquals(
            true,
            latestReportUsesStackedLayout(480f, Math.nextUp(HOME_LARGE_FONT_SCALE_THRESHOLD)),
        )
    }
}
