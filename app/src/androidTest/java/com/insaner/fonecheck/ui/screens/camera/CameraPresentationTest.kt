package com.insaner.fonecheck.ui.screens.camera

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class CameraPresentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun uniqueFacingSelectionsUseActionsWithoutIdsOrTechnicalClassification() {
        val context = localizedContext(Locale.ENGLISH)
        var selectedCameraId: String? = null

        render(context) {
            CameraSelectionList(
                cameras = listOf(rearCamera(), frontCamera()),
                activeCameraId = null,
                enabled = true,
                onSelect = { selectedCameraId = it },
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.camera_open_rear)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.camera_open_front)).assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Camera 0", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("Camera 1", substring = true).assertDoesNotExist()
        composeRule
            .onNodeWithText(context.getString(R.string.camera_class_logical), substring = true)
            .assertDoesNotExist()
        assertEquals("1", selectedCameraId)
    }

    @Test
    fun duplicateFacingSelectionsAreDistinguishableAndKeepSelectedSemantics() {
        val context = localizedContext(Locale.ENGLISH)
        var selectedCameraId: String? = null

        render(context) {
            CameraSelectionList(
                cameras = listOf(rearCamera(cameraId = "0"), rearCamera(cameraId = "2")),
                activeCameraId = "0",
                enabled = true,
                onSelect = { selectedCameraId = it },
            )
        }

        val selectedLabel =
            context.getString(
                R.string.camera_duplicate_selection,
                context.getString(R.string.camera_rear),
                "0",
            )
        val inactiveLabel =
            context.getString(
                R.string.camera_duplicate_selection,
                context.getString(R.string.camera_open_rear),
                "2",
            )
        composeRule
            .onNodeWithText(selectedLabel)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        composeRule.onNodeWithText(inactiveLabel).performClick()
        assertEquals("2", selectedCameraId)
    }

    @Test
    fun capabilityHeadingSeparatesFacingAndIdAndTechnicalDetailsStayCollapsed() {
        val context = localizedContext(Locale.ENGLISH)

        render(context) {
            CapabilitiesSection(rearCamera(physicalCameraIds = setOf("3", "2")))
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.camera_rear))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.camera_identifier, "0")).assertIsDisplayed()
        composeRule.onNodeWithText("4080 × 3072").assertIsDisplayed()
        composeRule.onNodeWithText("4080 × 3072 (12.5 MP)").assertDoesNotExist()
        composeRule.onNodeWithText(context.getString(R.string.camera_type)).assertDoesNotExist()
        composeRule.onNodeWithText(context.getString(R.string.camera_show_technical_details)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.camera_type)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.camera_class_logical)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.camera_physical_ids)).assertIsDisplayed()
        composeRule.onNodeWithText("2, 3").assertIsDisplayed()
    }

    @Test
    fun captureResultUsesDimensionsFollowedByMegapixelsInParentheses() {
        val context = localizedContext(Locale.ENGLISH)

        render(context) {
            CameraCaptureResult(CaptureResult(width = 4080, height = 3072, timestamp = 0L))
        }

        composeRule.onNodeWithText("4080 × 3072 (12.5 MP)").assertIsDisplayed()
    }

    @Test
    fun torchWorkflowAndFlashCapabilityUseTheirOwnTerms() {
        val context = localizedContext(Locale.ENGLISH)

        render(context) {
            Column {
                TorchTestSection(
                    flashTestResult = FlashTestResult.NOT_TESTED,
                    flashOn = false,
                    hasFlash = true,
                    hasPermission = true,
                    onToggleFlash = {},
                )
                CapabilitiesSection(rearCamera())
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.camera_torch_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.camera_torch)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.camera_torch_turn_on)).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.camera_flash)).assertCountEquals(1)
        composeRule.onNodeWithText("Flash / Torch").assertDoesNotExist()
    }

    @Test
    fun loadingStateKeepsLoadingMessage() {
        val context = localizedContext(Locale.ENGLISH)

        render(context) {
            CameraLoadingState()
        }
        composeRule.onNodeWithText(context.getString(R.string.camera_loading)).assertIsDisplayed()
    }

    @Test
    fun noCameraStateKeepsRetryAction() {
        val context = localizedContext(Locale.ENGLISH)
        var retryCount = 0

        render(context) {
            CameraErrorState(
                error = "camera_no_public_cameras",
                onRetry = { retryCount += 1 },
            )
        }

        composeRule
            .onNodeWithText(context.getString(R.string.observation_reason_hardware_unavailable))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.camera_retry)).performClick()
        assertEquals(1, retryCount)
    }

    @Test
    fun deniedCameraPermissionKeepsDeniedStatusAndRetryAction() {
        val context = localizedContext(Locale.ENGLISH)
        var retryCount = 0

        render(context) {
            PermissionStatusCard(
                state = PermissionState.DENIED,
                rationale = context.getString(R.string.permission_rationale_camera),
                onRequest = { retryCount += 1 },
                onOpenSettings = {},
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.permission_status_denied)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.permission_action_retry)).performClick()
        assertEquals(1, retryCount)
    }

    private fun render(
        context: Context,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides context,
                LocalConfiguration provides context.resources.configuration,
            ) {
                FonecheckTheme(content = content)
            }
        }
    }

    private fun localizedContext(locale: Locale): Context {
        val configuration =
            Configuration(
                InstrumentationRegistry
                    .getInstrumentation()
                    .targetContext
                    .resources
                    .configuration,
            )
        configuration.setLocale(locale)
        return InstrumentationRegistry.getInstrumentation().targetContext.createConfigurationContext(configuration)
    }

    private fun rearCamera(
        cameraId: String = "0",
        physicalCameraIds: Set<String> = emptySet(),
    ) = camera(
        cameraId = cameraId,
        facing = CameraFacingCode.REAR,
        cameraClass = CameraClassCode.LOGICAL,
        physicalCameraIds = physicalCameraIds,
    )

    private fun frontCamera() =
        camera(
            cameraId = "1",
            facing = CameraFacingCode.FRONT,
            cameraClass = CameraClassCode.STANDARD,
        )

    private fun camera(
        cameraId: String,
        facing: CameraFacingCode,
        cameraClass: CameraClassCode,
        physicalCameraIds: Set<String> = emptySet(),
    ) = CameraCapabilities(
        cameraId = cameraId,
        resolutions = listOf("4080 × 3072", "1920 × 1080"),
        maxResolution = "4080 × 3072 (12.5 MP)",
        fpsRanges = listOf("15–30 fps", "30–30 fps"),
        hasOis = true,
        hasFlash = true,
        focalLengths = listOf("6.90 mm"),
        zoomRange = "0.5× – 8.0×",
        sensorSize = "4080 × 3072",
        autoFocusModes = listOf("auto"),
        facingCode = facing,
        cameraClass = cameraClass,
        physicalCameraIds = physicalCameraIds,
    )
}
