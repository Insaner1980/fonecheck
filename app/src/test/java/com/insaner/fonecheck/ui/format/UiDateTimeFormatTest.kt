package com.insaner.fonecheck.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.TimeZone

class UiDateTimeFormatTest {
    @Test
    fun frenchDatesUseGenericLanguageAndPreserveTechnicalTimestamps() {
        val instant = Instant.parse("2026-08-11T10:18:00Z")
        val zone = ZoneId.of("Europe/Paris")
        val expected =
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.FRENCH)
                .format(instant.atZone(zone))
        listOf("fr", "fr-FR", "fr-BE", "fr-CH", "fr-CA").forEach { tag ->
            val locale = Locale.forLanguageTag(tag)
            assertEquals(expected, formatUiDateTime(instant, locale, zone))
            assertEquals("$expected UTC+02:00", formatPdfDateTime(instant, locale, zone))
            assertEquals("2026-08-11 12:18", formatTechnicalUiDateTime(instant, locale, zone))
        }
    }

    @Test
    fun germanDatesUseGenericLanguageAndPreserveTechnicalTimestamps() {
        val instant = Instant.parse("2026-08-11T10:18:00Z")
        val zone = ZoneId.of("Europe/Berlin")
        val expected =
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.GERMAN)
                .format(instant.atZone(zone))
        listOf("de", "de-DE", "de-AT", "de-CH").forEach { tag ->
            val locale = Locale.forLanguageTag(tag)
            assertEquals(expected, formatUiDateTime(instant, locale, zone))
            assertEquals("$expected UTC+02:00", formatPdfDateTime(instant, locale, zone))
            assertEquals("2026-08-11 12:18", formatTechnicalUiDateTime(instant, locale, zone))
        }
    }

    @Test
    fun brazilianDatesKeepRegionAndTechnicalTimestampsStable() {
        val instant = Instant.parse("2026-08-11T10:18:00Z")
        val zone = ZoneId.of("America/Sao_Paulo")
        val expected =
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.forLanguageTag("pt-BR"))
                .format(instant.atZone(zone))
        listOf("pt-BR", "pt", "pt-PT").forEach { tag ->
            val locale = Locale.forLanguageTag(tag)
            assertEquals(expected, formatUiDateTime(instant, locale, zone))
            assertEquals("$expected UTC-03:00", formatPdfDateTime(instant, locale, zone))
            assertEquals("2026-08-11 07:18", formatTechnicalUiDateTime(instant, locale, zone))
        }
    }

    @Test
    fun spanishDatesIgnoreRegionAndKeepTechnicalTimestampsStable() {
        val instant = Instant.parse("2026-08-11T10:18:00Z")
        val zone = ZoneId.of("Europe/Helsinki")
        val spanish = Locale.forLanguageTag("es")
        val originalLocale = Locale.getDefault()
        val originalDisplayLocale = Locale.getDefault(Locale.Category.DISPLAY)
        val originalFormatLocale = Locale.getDefault(Locale.Category.FORMAT)
        val originalTimeZone = TimeZone.getDefault()
        try {
            Locale.setDefault(Locale.US)
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            // Localized punctuation belongs to the runtime; language, style and zone are our contract.
            val expected =
                DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .withLocale(spanish)
                    .format(instant.atZone(zone))
            listOf("es", "es-ES", "es-MX", "es-US").forEach { tag ->
                val locale = Locale.forLanguageTag(tag)
                assertEquals(expected, formatUiDateTime(instant, locale, zone))
                assertEquals("$expected UTC+03:00", formatPdfDateTime(instant, locale, zone))
                assertEquals("2026-08-11 13:18", formatTechnicalUiDateTime(instant, locale, zone))
            }
        } finally {
            Locale.setDefault(originalLocale)
            Locale.setDefault(Locale.Category.DISPLAY, originalDisplayLocale)
            Locale.setDefault(Locale.Category.FORMAT, originalFormatLocale)
            TimeZone.setDefault(originalTimeZone)
        }
    }

    @Test
    fun pdfDatesUseEachInstantsOffsetAcrossDaylightSavingTime() {
        val zone = ZoneId.of("Europe/Helsinki")
        val locale = Locale.forLanguageTag("fi")
        assertEquals(
            "25.10.2026 klo 03.30 UTC+03:00",
            formatPdfDateTime(Instant.parse("2026-10-25T00:30:00Z"), locale, zone),
        )
        assertEquals(
            "25.10.2026 klo 03.30 UTC+02:00",
            formatPdfDateTime(Instant.parse("2026-10-25T01:30:00Z"), locale, zone),
        )
    }

    @Test
    fun `English UI uses its language format in the supplied zone`() {
        assertEquals(
            "Aug 11, 2026, 1:18:00 PM",
            formatUiDateTime(
                value = Instant.parse("2026-08-11T10:18:00Z"),
                locale = Locale.forLanguageTag("en-FI"),
                zoneId = ZoneId.of("Europe/Helsinki"),
            ),
        )
    }

    @Test
    fun `Finnish UI uses its language format in the supplied zone`() {
        assertEquals(
            "11.8.2026 klo 13.18",
            formatUiDateTime(
                value = Instant.parse("2026-08-11T10:18:00Z"),
                locale = Locale.forLanguageTag("fi-FI"),
                zoneId = ZoneId.of("Europe/Helsinki"),
            ),
        )
    }

    @Test
    fun `technical English UI keeps its ISO style format`() {
        assertEquals(
            "2026-08-11 13:18",
            formatTechnicalUiDateTime(
                value = Instant.parse("2026-08-11T10:18:00Z"),
                locale = Locale.forLanguageTag("en-FI"),
                zoneId = ZoneId.of("Europe/Helsinki"),
            ),
        )
    }

    @Test
    fun `technical Finnish UI uses the Finnish date and time format`() {
        assertEquals(
            "11.8.2026 klo 13.18",
            formatTechnicalUiDateTime(
                value = Instant.parse("2026-08-11T10:18:00Z"),
                locale = Locale.forLanguageTag("fi-FI"),
                zoneId = ZoneId.of("Europe/Helsinki"),
            ),
        )
    }
}
