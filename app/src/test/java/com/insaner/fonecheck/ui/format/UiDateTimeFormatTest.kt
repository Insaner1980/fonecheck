package com.insaner.fonecheck.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class UiDateTimeFormatTest {
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
