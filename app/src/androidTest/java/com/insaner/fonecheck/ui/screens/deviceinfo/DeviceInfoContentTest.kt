package com.insaner.fonecheck.ui.screens.deviceinfo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.ui.TopBarActionHostState
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneOffset

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
        composeRule
            .onNodeWithContentDescription("radio-one\nradio-two", useUnmergedTree = true)
            .assertExists()
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
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.device_value_unavailable)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.run_all_status_pass)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.device_root_finding_note)).assertDoesNotExist()
        composeRule.onNodeWithText(context.getString(R.string.device_root_heuristic_disclaimer)).assertExists()
    }

    @Test
    fun valueLongPressAndBottomActionsUseTheProvidedCallbacks() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var copiedValue: String? = null
        var copiedAll = false
        var exported = false

        composeRule.setContent {
            FonecheckTheme {
                DeviceInfoContent(
                    state = DeviceInfoState(info = deviceInfo(rootArtifactDetected = false)),
                    onCopyValue = { copiedValue = it },
                    onCopyAll = { copiedAll = true },
                    onExport = { exported = true },
                )
            }
        }

        composeRule
            .onNodeWithText("Pixel 10")
            .performSemanticsAction(SemanticsActions.OnLongClick)
        assertEquals("Pixel 10", copiedValue)

        composeRule
            .onNodeWithText(context.getString(R.string.device_copy_all))
            .performScrollTo()
            .performClick()
        composeRule
            .onNodeWithText(context.getString(R.string.device_export))
            .performScrollTo()
            .performClick()
        assertTrue(copiedAll)
        assertTrue(exported)
    }

    @Test
    fun duplicateBasebandIsDisplayedAndCopiedOnce() {
        var copiedValue: String? = null

        composeRule.setContent {
            FonecheckTheme {
                DeviceInfoContent(
                    state =
                        DeviceInfoState(
                            info =
                                deviceInfo(rootArtifactDetected = false).copy(
                                    basebandVersion = "radio-one, radio-one",
                                ),
                        ),
                    onCopyValue = { copiedValue = it },
                )
            }
        }

        composeRule.onAllNodesWithText("radio-one", useUnmergedTree = true).assertCountEquals(1)
        composeRule
            .onNodeWithText("radio-one")
            .performSemanticsAction(SemanticsActions.OnLongClick)
        assertEquals("radio-one", copiedValue)
    }

    @Test
    fun routeScopedTopBarActionsIgnoreStaleOwners() {
        var showFirstAction by mutableStateOf(true)
        var showSecondAction by mutableStateOf(false)
        var showSecondRouteAction by mutableStateOf(false)
        var secondActionEnabled by mutableStateOf(true)
        val topBarActionHostState = TopBarActionHostState()
        val firstRouteRegistry = topBarActionHostState.registryFor("first-route")
        val secondRouteRegistry = topBarActionHostState.registryFor("second-route")
        var firstRefreshCount = 0
        var secondRefreshCount = 0
        var secondRouteRefreshCount = 0

        composeRule.setContent {
            if (showFirstAction) {
                RegisterDeviceTopBarAction(
                    enabled = false,
                    onRefresh = { firstRefreshCount += 1 },
                    topBarActionRegistry = firstRouteRegistry,
                )
            }
            if (showSecondAction) {
                RegisterDeviceTopBarAction(
                    enabled = secondActionEnabled,
                    onRefresh = { secondRefreshCount += 1 },
                    topBarActionRegistry = firstRouteRegistry,
                )
            }
            if (showSecondRouteAction) {
                RegisterDeviceTopBarAction(
                    enabled = true,
                    onRefresh = { secondRouteRefreshCount += 1 },
                    topBarActionRegistry = secondRouteRegistry,
                )
            }
        }

        composeRule.runOnIdle {
            assertNotNull(topBarActionHostState.actionFor("first-route"))
            assertEquals(false, topBarActionHostState.actionFor("first-route")?.enabled)
            topBarActionHostState.actionFor("first-route")?.onClick?.invoke()
            assertEquals(1, firstRefreshCount)
            showSecondAction = true
        }
        composeRule.runOnIdle {
            assertNotNull(topBarActionHostState.actionFor("first-route"))
            assertTrue(topBarActionHostState.actionFor("first-route")?.enabled == true)
            topBarActionHostState.actionFor("first-route")?.onClick?.invoke()
            assertEquals(1, secondRefreshCount)
            showFirstAction = false
        }
        composeRule.runOnIdle {
            assertNotNull(topBarActionHostState.actionFor("first-route"))
            topBarActionHostState.actionFor("first-route")?.onClick?.invoke()
            assertEquals(2, secondRefreshCount)
            showSecondRouteAction = true
        }
        composeRule.runOnIdle {
            assertNotNull(topBarActionHostState.actionFor("second-route"))
            topBarActionHostState.actionFor("second-route")?.onClick?.invoke()
            assertEquals(1, secondRouteRefreshCount)
            secondActionEnabled = false
        }
        composeRule.runOnIdle {
            assertTrue(topBarActionHostState.actionFor("first-route")?.enabled == false)
            assertTrue(topBarActionHostState.actionFor("second-route")?.enabled == true)
            topBarActionHostState.actionFor("second-route")?.onClick?.invoke()
            assertEquals(2, secondRouteRefreshCount)
            showSecondAction = false
        }
        composeRule.runOnIdle {
            assertNull(topBarActionHostState.actionFor("first-route"))
            assertNotNull(topBarActionHostState.actionFor("second-route"))
            showSecondRouteAction = false
        }
        composeRule.runOnIdle { assertNull(topBarActionHostState.actionFor("second-route")) }
    }

    @Test
    fun plainTextExportContainsOnlyTheVisibleDeviceSnapshot() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val snapshot =
            buildDeviceSnapshotText(
                context = context,
                info =
                    deviceInfo(rootArtifactDetected = true).copy(
                        basebandVersion = "radio-one, radio-one",
                        widevineLevel = DeviceInfo.UNAVAILABLE,
                    ),
                zoneId = ZoneOffset.UTC,
            )

        assertTrue(snapshot.contains("${context.getString(R.string.label_model)}: Pixel 10"))
        assertTrue(snapshot.contains("${context.getString(R.string.label_baseband)}: radio-one"))
        assertEquals(1, snapshot.split("radio-one").size - 1)
        assertTrue(snapshot.contains(context.getString(R.string.device_value_restricted)))
        assertTrue(snapshot.contains(context.getString(R.string.device_value_unavailable)))
        assertTrue(snapshot.contains(context.getString(R.string.device_root_finding_note)))
        assertTrue(snapshot.contains(context.getString(R.string.device_root_heuristic_disclaimer)))
        assertTrue(
            snapshot.endsWith(
                context.getString(R.string.device_captured_at, "2026-08-17 14:05"),
            ),
        )
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
