package com.insaner.fonecheck.ui.screens.runall

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisplayCheckStepTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun hiddenControlsLeaveTheWholeTestAreaUncoveredAndRestoreWithoutRecordingAnAnswer() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val colorIndex = mutableIntStateOf(0)
        var nextCount = 0
        var resultCount = 0
        var skipCount = 0
        var cancelCount = 0
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(2f)) {
                FonecheckTheme {
                    DisplayCheckStep(
                        colorIndex = colorIndex.intValue,
                        progress = RunAllProgress(position = 1, total = 7),
                        onNextColor = { nextCount++ },
                        onResult = { resultCount++ },
                        onSkip = { skipCount++ },
                        onCancel = { cancelCount++ },
                        modifier = Modifier.testTag("displayTest"),
                    )
                }
            }
        }

        val hideLabel = context.getString(R.string.run_all_display_hide_controls)
        val showLabel = context.getString(R.string.run_all_display_show_controls)
        composeRule.onNodeWithText(hideLabel).performScrollTo().performClick()
        composeRule.onNodeWithText(context.getString(R.string.run_all_display_title)).assertDoesNotExist()
        val pixels = composeRule.onNodeWithTag("displayTest").captureToImage().toPixelMap()
        for (y in 0 until pixels.height) {
            for (x in 0 until pixels.width) {
                assertEquals("Display pixel ($x, $y)", Color.Red.toArgb(), pixels[x, y].toArgb())
            }
        }
        composeRule.onNodeWithContentDescription(showLabel).performClick()
        composeRule.onNodeWithText(hideLabel).assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(0, nextCount)
            assertEquals(0, resultCount)
            assertEquals(0, skipCount)
            assertEquals(0, cancelCount)
        }
        composeRule.onNodeWithText(context.getString(R.string.run_all_next_color)).performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(1, nextCount)
            colorIndex.intValue = displayTestPatterns.lastIndex
        }
        composeRule.onNodeWithText(hideLabel).performScrollTo().performClick()
        composeRule.onNodeWithContentDescription(showLabel).performClick()
        composeRule.onNodeWithText(context.getString(R.string.run_all_looks_good)).performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals(1, resultCount) }
    }

    @Test
    fun backRestoresHiddenControls() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        lateinit var backDispatcher: OnBackPressedDispatcher
        composeRule.setContent {
            backDispatcher = requireNotNull(LocalOnBackPressedDispatcherOwner.current).onBackPressedDispatcher
            FonecheckTheme {
                DisplayCheckStep(
                    colorIndex = 0,
                    progress = RunAllProgress(position = 1, total = 7),
                    onNextColor = {},
                    onResult = {},
                    onSkip = {},
                    onCancel = {},
                )
            }
        }
        val hideLabel = context.getString(R.string.run_all_display_hide_controls)
        composeRule.onNodeWithText(hideLabel).performScrollTo().performClick()
        composeRule.runOnIdle { backDispatcher.onBackPressed() }
        composeRule.onNodeWithText(hideLabel).assertIsDisplayed()
    }
}
