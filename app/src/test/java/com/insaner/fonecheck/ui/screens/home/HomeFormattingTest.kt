package com.insaner.fonecheck.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class HomeFormattingTest {
    @Test
    fun `English UI ignores the Finnish regional percent convention`() {
        val locale = homeUiLanguageLocale(Locale.forLanguageTag("en-FI"))

        assertEquals("en", locale.toLanguageTag())
        assertEquals("84%", NumberFormat.getPercentInstance(locale).format(0.84))
    }

    @Test
    fun `completed time keeps the technical English format`() {
        val completedAt = Instant.parse("2026-08-11T10:18:00Z")

        assertEquals(
            "2026-08-11 13:18",
            formatHomeCompletedAt(
                value = completedAt,
                locale = Locale.forLanguageTag("en-FI"),
                zoneId = ZoneId.of("Europe/Helsinki"),
            ),
        )
    }
}
