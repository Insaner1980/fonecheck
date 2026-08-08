package com.insaner.fonecheck.ui.screens.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lightThemeRendersPrimaryHomeEntries() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            FonecheckTheme(darkTheme = false) {
                HomeContent(
                    summary = DeviceSummary("Test phone", "Android 16", 80),
                    onNavigate = {},
                    onRunAllTests = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.home_run_all)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_history)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_settings)).assertIsDisplayed()
    }

    @Test
    fun darkThemeRemainsScrollableAtTwoHundredPercentFontScale() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = 2f),
            ) {
                FonecheckTheme(darkTheme = true) {
                    HomeContent(
                        summary = DeviceSummary("Test phone", "Android 16", 80),
                        onNavigate = {},
                        onRunAllTests = {},
                    )
                }
            }
        }

        composeRule
            .onNodeWithText(context.getString(R.string.home_cat_biometrics))
            .performScrollTo()
            .assertIsDisplayed()
    }
}
