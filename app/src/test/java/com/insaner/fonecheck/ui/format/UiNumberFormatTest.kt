package com.insaner.fonecheck.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class UiNumberFormatTest {
    @Test
    fun `Italian UI uses generic Italian regardless of region and device formatting default`() {
        assertRegionalCommaDecimalFormatting(
            expectedLanguageTag = "it",
            localeTags = listOf("it", "it-IT", "it-CH"),
        )
    }

    @Test
    fun `Danish UI uses generic Danish regardless of region and device formatting default`() {
        assertRegionalCommaDecimalFormatting(
            expectedLanguageTag = "da",
            localeTags = listOf("da", "da-DK"),
        )
    }

    @Test
    fun `Bokmal UI keeps Norwegian numbers independent of region and device default`() {
        assertRegionalCommaDecimalFormatting(
            expectedLanguageTag = "nb",
            localeTags = listOf("nb", "nb-NO"),
        )
    }

    @Test
    fun `Swedish UI uses generic Swedish regardless of region and device formatting default`() {
        assertRegionalCommaDecimalFormatting(
            expectedLanguageTag = "sv",
            localeTags = listOf("sv", "sv-SE", "sv-FI"),
        )
    }

    private fun assertRegionalCommaDecimalFormatting(
        expectedLanguageTag: String,
        localeTags: List<String>,
    ) {
        val original = Locale.getDefault(Locale.Category.FORMAT)
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.US)
            val symbols = java.text.DecimalFormatSymbols(Locale.forLanguageTag(expectedLanguageTag))
            localeTags.forEach { tag ->
                val locale = Locale.forLanguageTag(tag)
                assertEquals(expectedLanguageTag, uiLanguageLocale(locale).toLanguageTag())
                listOf(0, 1, 2, 1234).forEach { count ->
                    assertEquals(count.toString(), formatUiNumber(count, locale))
                }
                assertEquals("0,59", formatUiNumber(0.59, locale, 2, 2))
                assertEquals("${symbols.minusSign}31,2", formatUiNumber(-31.2, locale, 1, 1))
                assertEquals(
                    "${symbols.minusSign}1${symbols.groupingSeparator}234,5",
                    formatUiNumber(-1234.5, locale, 1, 1, grouping = true),
                )
                assertEquals(
                    "1,25${symbols.exponentSeparator.replace('E', 'e')}${symbols.minusSign}4",
                    formatUiScientificNumber(0.000125, locale, 2),
                )
            }
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, original)
        }
    }

    @Test
    fun `Indonesian UI ignores region and device formatting default`() {
        val original = Locale.getDefault(Locale.Category.FORMAT)
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.US)
            listOf("id", "id-ID", "id-SG", "in-ID").forEach { tag ->
                val locale = Locale.forLanguageTag(tag)
                assertCommaDecimalFormatting(locale, "id")
                assertEquals("-1.234,5", formatUiNumber(-1234.5, locale, 1, 1, grouping = true))
            }
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, original)
        }
    }

    @Test
    fun `French UI ignores region and device formatting default`() {
        val original = Locale.getDefault(Locale.Category.FORMAT)
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.US)
            listOf("fr", "fr-FR", "fr-BE", "fr-CH", "fr-CA").forEach { tag ->
                val locale = Locale.forLanguageTag(tag)
                assertCommaDecimalFormatting(locale, "fr")
                // Grouping glyphs belong to the runtime CLDR version, not to a device region.
                val separator = java.text.DecimalFormatSymbols(Locale.FRENCH).groupingSeparator
                assertEquals("-1${separator}234,5", formatUiNumber(-1234.5, locale, 1, 1, grouping = true))
            }
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, original)
        }
    }

    @Test
    fun `German UI ignores region and device formatting default`() {
        val original = Locale.getDefault(Locale.Category.FORMAT)
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.US)
            listOf("de", "de-DE", "de-AT", "de-CH").forEach { tag ->
                val locale = Locale.forLanguageTag(tag)
                assertCommaDecimalFormatting(locale, "de")
                assertEquals("-1.234,5", formatUiNumber(-1234.5, locale, 1, 1, grouping = true))
            }
            assertEquals("-1,234.5", formatUiNumber(-1234.5, Locale.ENGLISH, 1, 1, grouping = true))
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, original)
        }
    }

    @Test
    fun `Brazilian app language is independent of the device formatting default`() {
        val original = Locale.getDefault(Locale.Category.FORMAT)
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.US)
            assertEquals("-1.234,5", formatUiNumber(-1234.5, Locale.forLanguageTag("pt-BR"), 1, 1, grouping = true))
            assertEquals("-1,234.5", formatUiNumber(-1234.5, Locale.ENGLISH, 1, 1, grouping = true))
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, original)
        }
    }

    @Test
    fun `Portuguese UI uses Brazilian formatting including language fallback`() {
        listOf("pt-BR", "pt", "pt-PT", "pt-US").forEach { tag ->
            val locale = Locale.forLanguageTag(tag)
            assertCommaDecimalFormatting(locale, "pt-BR")
            assertEquals("12,5", formatUiNumber(12.5, locale, 1, 1))
            assertEquals("1.234,5", formatUiNumber(1234.5, locale, 1, 1, grouping = true))
        }
    }

    @Test
    fun `Spanish UI uses language formatting for every region`() {
        listOf("es", "es-ES", "es-MX", "es-US").forEach { tag ->
            val locale = Locale.forLanguageTag(tag)
            assertCommaDecimalFormatting(locale, "es")
        }
    }

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

    private fun assertCommaDecimalFormatting(
        locale: Locale,
        expectedLanguageTag: String,
    ) {
        assertEquals(expectedLanguageTag, uiLanguageLocale(locale).toLanguageTag())
        listOf(0, 1, 2, 1234).forEach { count ->
            assertEquals(count.toString(), formatUiNumber(count, locale))
        }
        assertEquals("-31,2", formatUiNumber(-31.2, locale, 1, 1))
        assertEquals("0,59", formatUiNumber(0.59, locale, 2, 2))
        assertEquals("1,25e-4", formatUiScientificNumber(0.000125, locale, 2))
    }
}
