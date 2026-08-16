package com.insaner.fonecheck.ui.screens.home

import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeResponsiveLayoutTest {
    @Test
    fun `category headlines use only values that stay stable between full checks`() {
        assertEquals("report.device.api_level", stableHomeHeadlineSource(DiagnosticCategoryId.DEVICE))
        assertEquals("performance.cpu", stableHomeHeadlineSource(DiagnosticCategoryId.PERFORMANCE))
        assertEquals("camera.inventory", stableHomeHeadlineSource(DiagnosticCategoryId.CAMERA))
        assertEquals("sensors.inventory", stableHomeHeadlineSource(DiagnosticCategoryId.SENSORS))
        assertEquals("battery.health", stableHomeHeadlineSource(DiagnosticCategoryId.BATTERY))
    }

    @Test
    fun `changing readings and situational state are not category headlines`() {
        assertNull(stableHomeHeadlineSource(DiagnosticCategoryId.SIM))
        assertNull(stableHomeHeadlineSource(DiagnosticCategoryId.CONNECTIVITY))
        assertNull(stableHomeHeadlineSource(DiagnosticCategoryId.THERMAL))
        assertNull(stableHomeHeadlineSource(DiagnosticCategoryId.STORAGE))
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
