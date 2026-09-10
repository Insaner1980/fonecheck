package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
class StageCompletionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun failedResultRemainsVisibleUntilContinueIsPressed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var continueCount = 0
        composeRule.setContent {
            FonecheckTheme {
                StageCompletionScreen(
                    title = "Display",
                    outcome = RunAllStageOutcome.FAILED,
                    permissionLimited = false,
                    onContinue = { continueCount++ },
                    onCancel = {},
                )
            }
        }
        composeRule.onNodeWithText("Display").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.run_all_status_fail)).assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, continueCount) }
        composeRule
            .onNodeWithText(context.getString(R.string.run_all_buttons_continue))
            .performScrollTo()
            .performClick()
        composeRule.runOnIdle { assertEquals(1, continueCount) }
    }
}
