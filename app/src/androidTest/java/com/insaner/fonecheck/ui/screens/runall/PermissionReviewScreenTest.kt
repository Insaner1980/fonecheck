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
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.permissions.PermissionController
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
    fun fullCheckWithNoActionablePermissionsStillWaitsForContinue() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var continued = 0
        val kinds = relevantRunAllPermissionKinds(null, RunAllSelections())
        val states = listOf(PermissionState.GRANTED, PermissionState.NOT_REQUIRED, PermissionState.HARDWARE_ABSENT)
        composeRule.setContent {
            FonecheckTheme {
                PermissionReviewScreen(
                    prompts =
                        kinds.mapIndexed { index, kind ->
                            PermissionPrompt(
                                title = kind.name,
                                state = states[index % states.size],
                                rationale = "Permission explanation",
                                onRequest = { error("No request expected") },
                                onOpenSettings = { error("No Settings recovery expected") },
                            )
                        },
                    onContinue = { continued++ },
                    onCancel = {},
                )
            }
        }
        composeRule.runOnIdle { assertEquals(0, continued) }
        kinds.forEach { kind ->
            composeRule.onNodeWithContentDescription(kind.name).performScrollTo().assertIsDisplayed()
        }
        composeRule.runOnIdle { assertEquals(0, continued) }
        composeRule
            .onNodeWithText(context.getString(R.string.run_all_permissions_continue))
            .performScrollTo()
            .performClick()
        composeRule.runOnIdle { assertEquals(1, continued) }
    }

    @Test
    fun canonicalKindsMapToOrderedPromptsAndTheirOwnControllersEvenWithoutHardware() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val labels =
            mapOf(
                PermissionKind.MICROPHONE to R.string.settings_permission_microphone,
                PermissionKind.CAMERA to R.string.settings_permission_camera,
                PermissionKind.LOCATION to R.string.settings_permission_location,
                PermissionKind.PHONE to R.string.settings_permission_phone,
                PermissionKind.BLUETOOTH to R.string.settings_permission_bluetooth,
            )
        val controllers =
            labels.keys.reversed().map { kind ->
                PermissionController(context, null, kind, hardwareAvailable = false)
            }
        val scopes = listOf(null) + DiagnosticCategoryId.entries
        val selections =
            listOf(
                RunAllSelections(),
                RunAllSelections(includeMicrophone = false),
                RunAllSelections(includeCamera = false),
                RunAllSelections(includeMicrophone = false, includeCamera = false),
            )
        val expectedKinds = scopes.flatMap { scope -> selections.map { relevantRunAllPermissionKinds(scope, it) } }
        var actualPrompts = emptyList<List<PermissionPrompt>>()
        var requested: PermissionController? = null
        composeRule.setContent {
            actualPrompts =
                expectedKinds.map { kinds ->
                    runAllPermissionPrompts(kinds, controllers, onRequest = { requested = it })
                }
        }
        composeRule.runOnIdle {
            assertEquals(expectedKinds.size, actualPrompts.size)
            expectedKinds.zip(actualPrompts).forEach { (kinds, prompts) ->
                assertEquals(kinds.map { context.getString(labels.getValue(it)) }, prompts.map { it.title })
                assertEquals(kinds.map { PermissionState.HARDWARE_ABSENT }, prompts.map { it.state })
                kinds.zip(prompts).forEach { (kind, prompt) ->
                    prompt.onRequest()
                    assertEquals(controllers.single { it.kind == kind }, requested)
                }
            }
        }
    }

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
