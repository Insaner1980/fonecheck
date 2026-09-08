package com.insaner.fonecheck.ui.components

import com.insaner.fonecheck.ui.format.formatUiNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SegmentGeometryTest {
    @Test
    fun `default geometry uses the visually approved advances`() {
        assertEquals(0.78f, segmentedWidth("1"), 0.00001f)
        assertEquals(0.78f * 3 + 0.16f, segmentedWidth("1.25"), 0.00001f)
        assertEquals(0.4f + 0.78f * 3, segmentedWidth("-100"), 0.00001f)
    }

    @Test
    fun `all digits keep the same cell through live updates`() {
        val geometry = SegmentGeometry(digitAdvance = 0.78f, pointAdvance = 0.16f)
        for (digit in '0'..'9') {
            assertEquals(segmentedWidth("8.88", geometry), segmentedWidth("$digit.$digit$digit", geometry), 0f)
        }
        assertEquals(10, ('0'..'9').map(::segmentMask).toSet().size)
        assertEquals(6, segmentMask('1'))
    }

    @Test
    fun `localized punctuation preserves the numeric value and advance`() {
        val geometry = SegmentGeometry(digitAdvance = 0.78f, pointAdvance = 0.16f)
        val english = formatUiNumber(-1.25, Locale.ENGLISH, 2, 2)
        val finnish = formatUiNumber(-1.25, Locale.forLanguageTag("fi"), 2, 2)
        assertTrue(english.contains('.'))
        assertTrue(finnish.contains(','))
        assertEquals(segmentedWidth(english, geometry), segmentedWidth(finnish, geometry), 0f)
        assertEquals(english, segmentedFigureParts("$english GB")?.number)
        assertEquals(finnish, segmentedFigureParts("$finnish GB")?.number)
    }
}
