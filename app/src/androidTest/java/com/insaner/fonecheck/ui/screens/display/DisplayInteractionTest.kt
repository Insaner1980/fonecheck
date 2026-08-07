package com.insaner.fonecheck.ui.screens.display

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DisplayInteractionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dragAndSimultaneousPointersReachTheTouchCallbacksAndExitIsAccessible() {
        val touchedCells = mutableSetOf<Int>()
        val pointerCounts = mutableListOf<Int>()
        var exited = false

        composeRule.setContent {
            FonecheckTheme {
                TouchTestOverlay(
                    state = TouchTestState(isActive = true),
                    onCellsTouched = touchedCells::addAll,
                    onPointersChanged = { pointerCounts += it.size },
                    onReset = {},
                    onComplete = {},
                    onExit = { exited = true },
                )
            }
        }

        composeRule
            .onNodeWithTag(DISPLAY_TOUCH_GRID_TAG)
            .performTouchInput {
                down(0, Offset(10f, centerY))
                moveTo(0, Offset(width - 10f, centerY))
                up(0)
                down(0, Offset(width * 0.25f, centerY))
                down(1, Offset(width * 0.75f, centerY))
                move()
                up(0)
                up(1)
            }

        composeRule.runOnIdle {
            assertTrue(touchedCells.size > 1)
            assertTrue(pointerCounts.any { it == 2 })
        }

        composeRule.onNodeWithText("Exit test").performClick()
        composeRule.runOnIdle { assertTrue(exited) }
    }
}
