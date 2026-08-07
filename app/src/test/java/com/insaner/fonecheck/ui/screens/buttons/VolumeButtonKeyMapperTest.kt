package com.insaner.fonecheck.ui.screens.buttons

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VolumeButtonKeyMapperTest {
    @Test
    fun mapsOnlyInitialVolumeKeyDownEvents() {
        assertEquals(
            VolumeButtonDirection.UP,
            VolumeButtonKeyMapper.directionFor(
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.ACTION_DOWN,
                repeatCount = 0,
            ),
        )
        assertEquals(
            VolumeButtonDirection.DOWN,
            VolumeButtonKeyMapper.directionFor(
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.ACTION_DOWN,
                repeatCount = 0,
            ),
        )
        assertNull(
            VolumeButtonKeyMapper.directionFor(
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.ACTION_DOWN,
                repeatCount = 1,
            ),
        )
        assertNull(
            VolumeButtonKeyMapper.directionFor(
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.ACTION_UP,
                repeatCount = 0,
            ),
        )
        assertNull(
            VolumeButtonKeyMapper.directionFor(
                KeyEvent.KEYCODE_POWER,
                KeyEvent.ACTION_DOWN,
                repeatCount = 0,
            ),
        )
    }
}
