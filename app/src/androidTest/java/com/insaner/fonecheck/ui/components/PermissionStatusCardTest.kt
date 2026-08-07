package com.insaner.fonecheck.ui.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionStatusCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun revokedPermissionShowsExplanationAndRequestAction() {
        render(PermissionState.NOT_REQUESTED)

        composeRule.onNodeWithText("Permission explanation").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.permission_action_allow)).assertIsDisplayed()
    }

    @Test
    fun deniedPermissionShowsRetryAction() {
        render(PermissionState.DENIED)

        composeRule.onNodeWithText(context.getString(R.string.permission_status_denied)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.permission_action_retry)).assertIsDisplayed()
    }

    @Test
    fun permanentlyDeniedPermissionShowsSettingsRecovery() {
        render(PermissionState.SETTINGS_RECOVERY)

        composeRule.onNodeWithText(context.getString(R.string.permission_status_settings_recovery)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.permission_action_open_settings)).assertIsDisplayed()
    }

    @Test
    fun grantedPermissionHasNoRecoveryAction() {
        render(PermissionState.GRANTED)

        composeRule.onNodeWithText(context.getString(R.string.permission_status_granted)).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.permission_action_retry)).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.permission_action_open_settings)).assertCountEquals(0)
    }

    @Test
    fun partialPermissionShowsPartialStateAndSettingsRecovery() {
        render(PermissionState.PARTIAL)

        composeRule.onNodeWithText(context.getString(R.string.permission_status_partial)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.permission_action_open_settings)).assertIsDisplayed()
    }

    private fun render(state: PermissionState) {
        composeRule.setContent {
            FonecheckTheme {
                PermissionStatusCard(
                    state = state,
                    rationale = "Permission explanation",
                    onRequest = {},
                    onOpenSettings = {},
                )
            }
        }
    }
}
