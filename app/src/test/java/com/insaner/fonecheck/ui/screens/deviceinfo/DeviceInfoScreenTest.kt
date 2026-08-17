package com.insaner.fonecheck.ui.screens.deviceinfo

import com.insaner.fonecheck.domain.model.DeviceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class DeviceInfoScreenTest {
    @Test
    fun `unavailable sentinel becomes a missing row value`() {
        assertNull(availableDeviceValue(DeviceInfo.UNAVAILABLE))
        assertEquals("Pixel 10", availableDeviceValue("Pixel 10"))
    }

    @Test
    fun `two concatenated identifiers are placed on separate lines`() {
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
    fun `ordinary and multi-part values are left unchanged`() {
        assertEquals("6.1.0-android", splitConcatenatedDeviceIdentifiers("6.1.0-android"))
        assertEquals("one,two,three", splitConcatenatedDeviceIdentifiers("one,two,three"))
        assertNull(splitConcatenatedDeviceIdentifiers(null))
    }

    @Test
    fun `capture timestamp uses fixed ISO date and time separators`() {
        assertEquals(
            "2026-08-17 14:05",
            formatCapturedAt(Instant.parse("2026-08-17T14:05:00Z"), ZoneOffset.UTC),
        )
    }
}
