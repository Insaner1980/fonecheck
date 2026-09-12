package com.insaner.fonecheck.ui

import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.screens.display.DISPLAY_EXIT_BUTTON_TAG
import com.insaner.fonecheck.ui.screens.display.DISPLAY_TOUCH_GRID_TAG
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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

    @Test
    fun activeTouchTestKeepsFullscreenAfterRecreationAndRestoresNavigationOnExit() {
        val preferences = composeRule.activity.appPreferencesRepository
        val onboardingComplete = runBlocking { preferences.preferences.first().onboardingComplete }
        val homeSettings = composeRule.activity.getString(R.string.home_settings_content_description)
        try {
            composeRule.waitUntil(10_000L) {
                composeRule.onAllNodesWithTag("onboarding_skip").fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithContentDescription(homeSettings).fetchSemanticsNodes().isNotEmpty()
            }
            if (composeRule.onAllNodesWithTag("onboarding_skip").fetchSemanticsNodes().isNotEmpty()) {
                composeRule.onNodeWithTag("onboarding_skip").performScrollTo().performClick()
            }
            composeRule.waitUntil(10_000L) {
                composeRule.onAllNodesWithContentDescription(homeSettings).fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag("home_category_display"))
            composeRule.onNodeWithTag("home_category_display").performScrollTo().performClick()
            composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(
                hasText(composeRule.activity.getString(R.string.display_touch_title), ignoreCase = true),
            )
            composeRule
                .onAllNodesWithText(composeRule.activity.getString(R.string.display_start_test))
                .onLast()
                .performScrollTo()
                .performClick()
            composeRule.onNodeWithTag(DISPLAY_TOUCH_GRID_TAG).assertIsDisplayed()
            composeRule
                .onNodeWithContentDescription(composeRule.activity.getString(R.string.navigation_back))
                .assertDoesNotExist()
            waitForSystemBarsVisibility(visible = false)

            composeRule.activityRule.scenario.recreate()
            composeRule.waitUntil(10_000L) {
                composeRule.onAllNodesWithTag(DISPLAY_TOUCH_GRID_TAG).fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag(DISPLAY_TOUCH_GRID_TAG).assertIsDisplayed()
            composeRule
                .onNodeWithContentDescription(composeRule.activity.getString(R.string.navigation_back))
                .assertDoesNotExist()
            waitForSystemBarsVisibility(visible = false)

            composeRule.onNodeWithTag(DISPLAY_EXIT_BUTTON_TAG).performClick()
            waitForSystemBarsVisibility(visible = true)
            composeRule
                .onNodeWithContentDescription(composeRule.activity.getString(R.string.navigation_back))
                .assertIsDisplayed()
                .performClick()
            composeRule
                .onNode(hasScrollToNodeAction())
                .performScrollToNode(hasContentDescription(homeSettings))
            composeRule
                .onNodeWithContentDescription(homeSettings)
                .assertIsDisplayed()
        } finally {
            runBlocking { preferences.setOnboardingComplete(onboardingComplete) }
        }
    }

    private fun waitForSystemBarsVisibility(visible: Boolean) {
        composeRule.waitUntil(10_000L) {
            var matches = false
            composeRule.activityRule.scenario.onActivity { activity ->
                val insets = ViewCompat.getRootWindowInsets(activity.window.decorView)
                matches =
                    insets != null &&
                    insets.isVisible(WindowInsetsCompat.Type.statusBars()) == visible &&
                    insets.isVisible(WindowInsetsCompat.Type.navigationBars()) == visible
            }
            matches
        }
    }
}
