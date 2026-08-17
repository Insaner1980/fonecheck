package com.insaner.fonecheck.ui.screens.deviceinfo

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class DeviceInfoContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun deviceSnapshotUsesSectionsRestrictedSerialAndBoundedRootWarning() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            FonecheckTheme {
                DeviceInfoContent(
                    state = DeviceInfoState(info = deviceInfo(rootArtifactDetected = true)),
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.device_identity_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.os_info_title)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.drm_info_title)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.security_info_title)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.device_value_restricted)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.device_serial_restricted_note)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.run_all_status_warning)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.device_root_finding_note)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.device_root_heuristic_disclaimer)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.status_enabled)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.device_developer_options_note)).assertExists()
        composeRule.onNodeWithContentDescription("radio-one,\nradio-two").assertExists()
    }

    @Test
    fun missingMeasuredValueHasNoInventedReasonAndCleanRootUsesPass() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            FonecheckTheme {
                DeviceInfoContent(
                    state =
                        DeviceInfoState(
                            info =
                                deviceInfo(rootArtifactDetected = false).copy(
                                    widevineLevel = DeviceInfo.UNAVAILABLE,
                                ),
                        ),
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.device_value_unavailable)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.run_all_status_pass)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.device_root_finding_note)).assertDoesNotExist()
        composeRule.onNodeWithText(context.getString(R.string.device_root_heuristic_disclaimer)).assertExists()
    }

    private fun deviceInfo(rootArtifactDetected: Boolean) =
        DeviceInfo(
            model = "Pixel 10",
            manufacturer = "Google",
            brand = "google",
            product = "mustang",
            androidVersion = "17",
            apiLevel = 37,
            securityPatch = "2026-08-01",
            buildNumber = "BP3A.260805.001",
            kernelVersion = "6.1.0-android",
            basebandVersion = "radio-one, radio-two",
            bootloaderVersion = "mustang-1.2",
            widevineLevel = "L1",
            rootArtifactDetected = rootArtifactDetected,
            developerOptionsEnabled = true,
            usbDebuggingEnabled = false,
            capturedAt = Instant.parse("2026-08-17T14:05:00Z"),
        )
}
