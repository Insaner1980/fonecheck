package com.insaner.fonecheck.ui

import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySystemBarsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigationBarContrastIsDisabledOnQAndAbove() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)

        assertFalse(composeRule.activity.window.isNavigationBarContrastEnforced)
    }
}
