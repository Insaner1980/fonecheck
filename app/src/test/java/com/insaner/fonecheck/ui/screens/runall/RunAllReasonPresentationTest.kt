package com.insaner.fonecheck.ui.screens.runall

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunAllReasonPresentationTest {
    @Test
    fun `consecutive repeated reasons move to section level`() {
        val reasons = listOf("shared", "shared", "single", null, "later", "later")

        assertEquals(setOf("shared", "later"), repeatedConsecutiveReasons(reasons))
    }

    @Test
    fun `separated matching reasons stay with their rows`() {
        val reasons = listOf("same", "middle", "same")

        assertTrue(repeatedConsecutiveReasons(reasons).isEmpty())
    }
}
