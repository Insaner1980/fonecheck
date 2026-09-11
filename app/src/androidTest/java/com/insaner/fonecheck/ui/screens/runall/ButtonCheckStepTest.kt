package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestPhase
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestState
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ButtonCheckStepTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun completingBothButtonsWaitsForContinueClick() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val state = mutableStateOf(ButtonTestState(phase = ButtonTestPhase.RUNNING))
        var continueCount = 0
        composeRule.setContent {
            FonecheckTheme {
                ButtonCheckStep(
                    state = state.value,
                    progress = RunAllProgress(position = 6, total = 7),
                    onContinue = { continueCount++ },
                    onRetry = {},
                    onSkip = {},
                    onCancel = {},
                )
            }
        }

        val continueLabel = context.getString(R.string.run_all_buttons_continue)
        composeRule.onNodeWithText(continueLabel).assertDoesNotExist()
        composeRule.runOnIdle {
            state.value = state.value.copy(volumeUpDetected = true)
        }
        composeRule.onNodeWithText(continueLabel).assertDoesNotExist()
        composeRule.runOnIdle {
            state.value = state.value.copy(volumeDownDetected = true, phase = ButtonTestPhase.COMPLETED)
        }
        composeRule.onNodeWithText(context.getString(R.string.button_volume_up)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.button_volume_down)).assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, continueCount) }
        composeRule.onNodeWithText(continueLabel).performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals(1, continueCount) }
    }
}
