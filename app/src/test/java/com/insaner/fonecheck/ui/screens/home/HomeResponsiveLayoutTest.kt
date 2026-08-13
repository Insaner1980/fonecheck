package com.insaner.fonecheck.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeResponsiveLayoutTest {
    @Test
    fun `compact width uses two columns`() {
        assertEquals(2, homeGridColumnCount(360f))
        assertEquals(2, homeGridColumnCount(599f))
    }

    @Test
    fun `medium width uses three columns`() {
        assertEquals(3, homeGridColumnCount(600f))
        assertEquals(3, homeGridColumnCount(839f))
    }

    @Test
    fun `expanded width uses four columns`() {
        assertEquals(4, homeGridColumnCount(840f))
        assertEquals(4, homeGridColumnCount(1_280f))
    }

    @Test
    fun `latest report keeps reference composition on a normal phone`() {
        assertEquals(false, latestReportUsesStackedLayout(328f, 1f))
        assertEquals(false, latestReportUsesStackedLayout(480f, 1f))
    }

    @Test
    fun `latest report stacks only when width or font scale requires it`() {
        assertEquals(true, latestReportUsesStackedLayout(311f, 1f))
        assertEquals(true, latestReportUsesStackedLayout(480f, 1.5f))
    }
}
