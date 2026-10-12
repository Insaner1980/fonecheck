package com.insaner.fonecheck.ui.components

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class PermissionStatusCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var locale: Locale

    @Test
    fun revokedPermissionShowsExplanationAndRequestAction() {
        render(PermissionState.NOT_REQUESTED)

        composeRule.onNodeWithText("Permission explanation").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.permission_action_allow)).assertIsDisplayed()
    }

    @Test
    fun deniedPermissionShowsRetryAction() {
        render(PermissionState.DENIED)

        composeRule
            .onNodeWithText(context.getString(R.string.permission_status_denied).uppercase(locale))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.permission_action_retry)).assertIsDisplayed()
    }

    @Test
    fun permanentlyDeniedPermissionShowsSettingsRecovery() {
        render(PermissionState.SETTINGS_RECOVERY)

        composeRule
            .onNodeWithText(context.getString(R.string.permission_status_settings_recovery).uppercase(locale))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.permission_action_open_settings)).assertIsDisplayed()
    }

    @Test
    fun grantedPermissionHasNoRecoveryAction() {
        render(PermissionState.GRANTED)

        composeRule
            .onNodeWithText(context.getString(R.string.permission_status_granted).uppercase(locale))
            .assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.permission_action_retry)).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.permission_action_open_settings)).assertCountEquals(0)
    }

    @Test
    fun partialPermissionShowsPartialStateAndSettingsRecovery() {
        render(PermissionState.PARTIAL)

        composeRule
            .onNodeWithText(context.getString(R.string.permission_status_partial).uppercase(locale))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.permission_action_open_settings)).assertIsDisplayed()
    }

    private fun render(state: PermissionState) {
        composeRule.setContent {
            FonecheckTheme {
                context = LocalContext.current
                locale = LocalLocale.current.platformLocale
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
