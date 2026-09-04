package com.insaner.fonecheck.ui.screens.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisplayResolutionPolicyTest {
    @Test
    fun positiveWindowBoundsProduceAnAppWindowResolution() {
        val resolution = appWindowResolution(width = 800, height = 600)

        requireNotNull(resolution)
        assertEquals(800, resolution.width)
        assertEquals(600, resolution.height)
        assertEquals(DisplayResolutionSource.APP_WINDOW, resolution.source)
    }

    @Test
    fun emptyWindowBoundsDoNotReplaceTheDisplayFallback() {
        assertNull(appWindowResolution(width = 0, height = 600))
        assertNull(appWindowResolution(width = 800, height = 0))
    }
}
