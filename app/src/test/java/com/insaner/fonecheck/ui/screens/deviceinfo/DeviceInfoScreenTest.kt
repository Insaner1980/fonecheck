package com.insaner.fonecheck.ui.screens.deviceinfo

import com.insaner.fonecheck.domain.model.DeviceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

class DeviceInfoScreenTest {
    @Test
    fun `unavailable sentinel becomes a missing row value`() {
        assertNull(availableDeviceValue(DeviceInfo.UNAVAILABLE))
        assertEquals("Pixel 10", availableDeviceValue("Pixel 10"))
    }

    @Test
    fun `two distinct concatenated identifiers are placed on separate lines`() {
        assertEquals(
            "radio-one\nradio-two",
            splitConcatenatedDeviceIdentifiers("radio-one, radio-two"),
        )
        assertEquals(
            "boot-one\nboot-two",
            splitConcatenatedDeviceIdentifiers("boot-one;boot-two"),
        )
    }

    @Test
    fun `identical comma and semicolon identifier pairs are displayed once`() {
        assertEquals("radio-one", splitConcatenatedDeviceIdentifiers("radio-one, radio-one"))
        assertEquals("radio-one", splitConcatenatedDeviceIdentifiers("radio-one; radio-one"))
    }

    @Test
    fun `ordinary multi-part and unavailable values are left unchanged`() {
        assertEquals("6.1.0-android", splitConcatenatedDeviceIdentifiers("6.1.0-android"))
        assertEquals("one,two,three", splitConcatenatedDeviceIdentifiers("one,two,three"))
        assertNull(splitConcatenatedDeviceIdentifiers(null))
    }

    @Test
    fun `capture timestamp keeps fixed ISO separators in English`() {
        assertEquals(
            "2026-08-17 14:05",
            formatCapturedAt(
                Instant.parse("2026-08-17T14:05:00Z"),
                Locale.ENGLISH,
                ZoneOffset.UTC,
            ),
        )
    }
}
