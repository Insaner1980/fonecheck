package com.insaner.fonecheck.ui.screens.camera

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CameraLocaleFormattingTest {
    @Test
    fun `megapixels use the requested locale decimal separator`() {
        assertEquals("12.0 MP", formatCameraMegapixels(12_000_000, Locale.US))
        assertEquals("12,0 MP", formatCameraMegapixels(12_000_000, Locale.forLanguageTag("fi-FI")))
    }
}
