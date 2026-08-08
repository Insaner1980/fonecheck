package com.insaner.fonecheck.ui.screens.audio

import android.media.AudioDeviceInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class HeadphoneTypeCodeTest {
    @Test
    fun `platform device types map to locale-neutral codes`() {
        assertEquals(
            HeadphoneTypeCode.WIRED_HEADSET,
            headphoneTypeCode(AudioDeviceInfo.TYPE_WIRED_HEADSET),
        )
        assertEquals(
            HeadphoneTypeCode.WIRED_HEADPHONES,
            headphoneTypeCode(AudioDeviceInfo.TYPE_WIRED_HEADPHONES),
        )
        assertEquals(
            HeadphoneTypeCode.USB_HEADSET,
            headphoneTypeCode(AudioDeviceInfo.TYPE_USB_HEADSET),
        )
        assertEquals(HeadphoneTypeCode.UNKNOWN, headphoneTypeCode(Int.MIN_VALUE))
    }
}
