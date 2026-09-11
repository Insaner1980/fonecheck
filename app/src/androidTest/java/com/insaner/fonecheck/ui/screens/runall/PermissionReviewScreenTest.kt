package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionReviewScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun grantedPermissionsKeepTheirNamesAndDeniedPermissionKeepsItsRequestAction() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val labels =
            listOf(
                R.string.settings_permission_microphone,
                R.string.settings_permission_camera,
                R.string.settings_permission_location,
                R.string.settings_permission_phone,
                R.string.settings_permission_bluetooth,
            ).map(context::getString)
        var requestedPermission: String? = null
        val prompts =
            mutableStateOf(
                labels.map { label ->
                    PermissionPrompt(
                        title = label,
                        state = PermissionState.GRANTED,
                        rationale = "Permission explanation",
                        onRequest = { requestedPermission = label },
                        onOpenSettings = {},
                    )
                },
            )
        composeRule.setContent {
            FonecheckTheme {
                PermissionReviewScreen(prompts = prompts.value, onContinue = {}, onCancel = {})
            }
        }

        labels.forEach { label ->
            composeRule.onNodeWithContentDescription(label).performScrollTo().assertIsDisplayed()
        }
        composeRule.onNodeWithText("Permission explanation").assertDoesNotExist()
        composeRule.runOnIdle {
            prompts.value =
                prompts.value.mapIndexed { index, prompt ->
                    if (index == 0) prompt.copy(state = PermissionState.DENIED) else prompt
                }
        }
        composeRule.onNodeWithContentDescription(labels.first()).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.permission_action_retry)).performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals(labels.first(), requestedPermission) }
    }
}
