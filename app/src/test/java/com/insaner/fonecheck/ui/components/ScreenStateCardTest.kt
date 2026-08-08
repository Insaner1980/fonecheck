package com.insaner.fonecheck.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenStateCardTest {
    @Test
    fun `every semantic state has a distinct title`() {
        val titleIds = ScreenStateType.entries.map(::screenStateTitleResId)

        assertEquals(ScreenStateType.entries.size, titleIds.toSet().size)
    }
}
