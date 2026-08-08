package com.insaner.fonecheck.ui.screens.onboarding

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun welcomeSupportsNextAndSkipAtLargeFontScale() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var next = false
        var skipped = false
        composeRule.setContent {
            val density = LocalDensity.current
            FonecheckTheme {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalDensity provides Density(density.density, fontScale = 2f),
                ) {
                    OnboardingScreen(
                        state = OnboardingState(pageIndex = 0),
                        onPrevious = {},
                        onNext = { next = true },
                        onSkip = { skipped = true },
                        onComplete = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.onboarding_welcome_title)).assertIsDisplayed()
        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.onNodeWithTag("onboarding_skip").performClick()
        assertTrue(next && skipped)
    }

    @Test
    fun finalPageStartsTheApp() {
        var completed = false
        composeRule.setContent {
            FonecheckTheme {
                OnboardingScreen(
                    state = OnboardingState(pageIndex = OnboardingPage.entries.lastIndex),
                    onPrevious = {},
                    onNext = {},
                    onSkip = {},
                    onComplete = { completed = true },
                )
            }
        }

        composeRule.onNodeWithTag("onboarding_complete").performClick()
        assertTrue(completed)
    }
}
