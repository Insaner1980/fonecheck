package com.insaner.fonecheck.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SegmentedNumberTest {
    @Test
    fun `plain and compound numbers stay in the segmented figure`() {
        assertEquals(SegmentedFigureParts("09", null), segmentedFigureParts("09"))
        assertEquals(SegmentedFigureParts("2.07", null), segmentedFigureParts("2.07"))
        assertEquals(SegmentedFigureParts("77 / 255", null), segmentedFigureParts("77 / 255"))
        assertEquals(SegmentedFigureParts("1,25e-4", null), segmentedFigureParts("1,25e-4"))
    }

    @Test
    fun `units are kept outside the segmented figure`() {
        assertEquals(SegmentedFigureParts("2.07", "GB"), segmentedFigureParts("2.07 GB"))
        assertEquals(SegmentedFigureParts("2.07", "GB"), segmentedFigureParts("2.07\u00A0GB"))
        assertEquals(SegmentedFigureParts("77", "%"), segmentedFigureParts("77%"))
        assertEquals(SegmentedFigureParts("31,2", "°C"), segmentedFigureParts("31,2 °C"))
    }

    @Test
    fun `non numeric states retain normal text rendering`() {
        assertNull(segmentedFigureParts("n/a"))
        assertNull(segmentedFigureParts("Unavailable"))
    }

    @Test
    fun `digit masks distinguish active segments`() {
        assertEquals(127, segmentMask('8'))
        assertEquals(6, segmentMask('1'))
        assertEquals(0, segmentMask('/'))
    }
}
