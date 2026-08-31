package com.insaner.fonecheck.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class UiNumberFormatTest {
    @Test
    fun `English UI ignores Finnish regional decimal separator`() {
        val locale = Locale.forLanguageTag("en-FI")

        assertEquals("31.2", formatUiNumber(31.2, locale, 1, 1))
        assertEquals("0.59", formatUiNumber(0.59, locale, 2, 2))
    }

    @Test
    fun `Finnish UI keeps Finnish decimal separator`() {
        val locale = Locale.forLanguageTag("fi-US")

        assertEquals("31,2", formatUiNumber(31.2, locale, 1, 1))
    }

    @Test
    fun `scientific values use the UI language decimal separator`() {
        assertEquals(
            "1.25e-4",
            formatUiScientificNumber(0.000125, Locale.forLanguageTag("en-FI"), 2),
        )
    }
}
