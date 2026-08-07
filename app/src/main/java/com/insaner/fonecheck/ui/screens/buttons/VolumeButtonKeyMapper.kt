package com.insaner.fonecheck.ui.screens.buttons

import android.view.KeyEvent

object VolumeButtonKeyMapper {
    fun directionFor(
        keyCode: Int,
        action: Int,
        repeatCount: Int,
    ): VolumeButtonDirection? {
        if (action != KeyEvent.ACTION_DOWN || repeatCount != 0) return null
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> VolumeButtonDirection.UP
            KeyEvent.KEYCODE_VOLUME_DOWN -> VolumeButtonDirection.DOWN
            else -> null
        }
    }
}
