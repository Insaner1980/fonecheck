package com.insaner.fonecheck.ui.screens.deviceinfo

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.then
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.ui.TopBarAction
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class DeviceInfoContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun initialLoadingKeepsLocalizedTextAndPoliteAnnouncement() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var language by mutableStateOf("en")
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.Locales(LocaleList(language))) {
                FonecheckTheme {
                    DeviceInfoContent(state = DeviceInfoState(isLoading = true))
                }
            }
        }

        listOf("en", "fi").forEach { tag ->
            composeRule.runOnIdle { language = tag }
            val configuration =
                Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) }
            val localizedContext = context.createConfigurationContext(configuration)
            val loadingText = localizedContext.getString(R.string.device_loading)
            composeRule.onAllNodesWithText(loadingText).assertCountEquals(1)
            composeRule
                .onNodeWithText(loadingText)
                .assertIsDisplayed()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
            composeRule.onNodeWithText(localizedContext.getString(R.string.live_state_label)).assertDoesNotExist()
        }
    }

    @Test
    fun sectionsKeepTheirOrderAndGrouping() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(DpSize(320.dp, 240.dp))) {
                FonecheckTheme {
                    DeviceInfoContent(state = DeviceInfoState(info = deviceInfo(rootArtifactDetected = false)))
                }
            }
        }

        val sectionTitles =
            listOf(
                R.string.device_identity_title,
                R.string.os_info_title,
                R.string.drm_info_title,
                R.string.security_info_title,
            )
        val firstHeadingTop =
            composeRule
                .onNodeWithContentDescription(context.getString(sectionTitles.first()))
                .fetchSemanticsNode()
                .boundsInRoot.top
        sectionTitles.forEachIndexed { index, title ->
            composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(index)
            val heading = composeRule.onNodeWithContentDescription(context.getString(title))
            heading.assertIsDisplayed()
            assertEquals(firstHeadingTop, heading.fetchSemanticsNode().boundsInRoot.top, 1f)
        }
    }

    @Test
    fun snapshotHasExactlyOneBottomTimestampWithTheCapturedTime() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val info = deviceInfo(rootArtifactDetected = false)
        var refreshing by mutableStateOf(false)
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.Locales(LocaleList("en"))) {
                FonecheckTheme {
                    DeviceInfoContent(state = DeviceInfoState(info = info, isLoading = refreshing))
                }
            }
        }

        val englishContext =
            context.createConfigurationContext(
                Configuration(context.resources.configuration).apply { setLocale(Locale.ENGLISH) },
            )
        val label = englishContext.getString(R.string.live_state_label)
        val timestamp =
            englishContext.getString(
                R.string.live_state_updated_at,
                formatCapturedAt(info.capturedAt, Locale.ENGLISH),
            )
        val timestampMatcher = hasText(timestamp) or hasContentDescription(timestamp)
        listOf(false, true).forEach { isRefreshing ->
            composeRule.runOnIdle { refreshing = isRefreshing }
            composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(timestampMatcher)
            composeRule.onNode(timestampMatcher).performScrollTo().assertIsDisplayed()
            composeRule.onAllNodesWithText(label).assertCountEquals(1)
            composeRule.onAllNodes(timestampMatcher).assertCountEquals(1)
            composeRule.onNodeWithText(englishContext.getString(R.string.device_loading)).assertDoesNotExist()
        }
    }

    @Test
    fun danishDeveloperOptionsLabelWrapsOnlyBetweenWordsAtLargeFontScale() {
        var enabled by mutableStateOf(true)
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride
                    .Locales(LocaleList("da"))
                    .then(DeviceConfigurationOverride.FontScale(2f))
                    .then(DeviceConfigurationOverride.ForcedSize(DpSize(320.dp, 800.dp))),
            ) {
                FonecheckTheme {
                    DeviceInfoContent(
                        state =
                            DeviceInfoState(
                                info = deviceInfo(rootArtifactDetected = false).copy(developerOptionsEnabled = enabled),
                            ),
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
            }
        }

        listOf(true, false).forEach { isEnabled ->
            composeRule.runOnIdle { enabled = isEnabled }
            composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Indstillinger for udviklere"))
            val label = composeRule.onNodeWithText("Indstillinger for udviklere", useUnmergedTree = true)
            label.performScrollTo().assertIsDisplayed()
            val layouts = mutableListOf<TextLayoutResult>()
            label.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            val layout = layouts.single()
            assertFalse(layout.hasVisualOverflow)
            val text = layout.layoutInput.text.text
            for (line in 1 until layout.lineCount) {
                val start = layout.getLineStart(line)
                assertTrue(
                    "Label wraps inside a word: $text",
                    text[start - 1].isWhitespace() || text[start].isWhitespace(),
                )
            }
        }
    }

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

        listOf(
            R.string.device_identity_title,
            R.string.os_info_title,
            R.string.drm_info_title,
            R.string.security_info_title,
        ).forEach { title ->
            val text = context.getString(title)
            composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription(text))
            composeRule.onNodeWithContentDescription(text).assertIsDisplayed()
        }
        listOf(
            R.string.device_value_restricted,
            R.string.device_serial_restricted_note,
            R.string.run_all_status_warning,
            R.string.device_root_finding_note,
            R.string.device_root_heuristic_disclaimer,
            R.string.status_enabled,
            R.string.device_developer_options_note,
        ).forEach { text -> scrollToText(context.getString(text)).assertIsDisplayed() }
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription("radio-one\nradio-two"))
        composeRule
            .onNodeWithContentDescription("radio-one\nradio-two", useUnmergedTree = true)
            .assertIsDisplayed()
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

        scrollToText(context.getString(R.string.device_value_unavailable)).assertIsDisplayed()
        scrollToText(context.getString(R.string.run_all_status_pass)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.device_root_finding_note)).assertDoesNotExist()
        scrollToText(context.getString(R.string.device_root_heuristic_disclaimer)).assertIsDisplayed()
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

        scrollToText(context.getString(R.string.device_copy_all))
            .performClick()
        scrollToText(context.getString(R.string.device_export))
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

        scrollToText("radio-one").assertIsDisplayed()
        composeRule.onAllNodesWithText("radio-one", useUnmergedTree = true).assertCountEquals(1)
        composeRule
            .onNodeWithText("radio-one")
            .performSemanticsAction(SemanticsActions.OnLongClick)
        assertEquals("radio-one", copiedValue)
    }

    @Test
    fun deviceRegistersAndClearsOneTopBarAction() {
        var showAction by mutableStateOf(true)
        var registeredAction: TopBarAction? = null
        var refreshCount = 0

        composeRule.setContent {
            if (showAction) {
                RegisterDeviceTopBarAction(
                    enabled = false,
                    onRefresh = { refreshCount += 1 },
                    onTopBarActionChange = { registeredAction = it },
                )
            }
        }

        composeRule.runOnIdle {
            assertNotNull(registeredAction)
            assertEquals(false, registeredAction?.enabled)
            registeredAction?.onClick?.invoke()
            assertEquals(1, refreshCount)
            showAction = false
        }
        composeRule.runOnIdle { assertNull(registeredAction) }
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

    private fun scrollToText(text: String): SemanticsNodeInteraction {
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(text))
        return composeRule.onNodeWithText(text).performScrollTo()
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
