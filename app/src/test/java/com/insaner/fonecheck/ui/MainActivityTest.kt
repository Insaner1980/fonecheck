package com.insaner.fonecheck.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityTest {
    @Test
    fun `splash animates only when platform and user setting allow it`() {
        assertTrue(shouldAnimateSplash(systemSupportsAnimatedSplash = true, animatorsEnabled = true))
        assertFalse(shouldAnimateSplash(systemSupportsAnimatedSplash = false, animatorsEnabled = true))
        assertFalse(shouldAnimateSplash(systemSupportsAnimatedSplash = true, animatorsEnabled = false))
    }
}
