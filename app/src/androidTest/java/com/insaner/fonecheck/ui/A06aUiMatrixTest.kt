package com.insaner.fonecheck.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.then
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.data.preferences.AppThemeMode
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.localization.AppLanguage
import com.insaner.fonecheck.ui.screens.access.FullAccessScreen
import com.insaner.fonecheck.ui.screens.settings.LanguageSelectionScreen
import com.insaner.fonecheck.ui.screens.settings.LicensesScreen
import com.insaner.fonecheck.ui.screens.settings.SettingsPermissionSnapshot
import com.insaner.fonecheck.ui.screens.settings.SettingsScreen
import com.insaner.fonecheck.ui.screens.settings.SettingsState
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.text.Collator

/** Synthetic screen inputs only. No route, Room history, preference writes or external handoffs. */
@RunWith(AndroidJUnit4::class)
class A06aUiMatrixTest {
    private val selection = A06aUiSelection()
    private val composeRule = createComposeRule()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(selection).around(composeRule)

    @Test
    fun settingsState() {
        val cell = selection.cell
        val context = cell.localizedContext()
        val counts = mutableMapOf<String, Int>()

        fun called(name: String) {
            counts[name] = counts.getOrDefault(name, 0) + 1
        }
        val initial =
            SettingsState(
                preferences = AppPreferences(),
                reportCount =
                    when (cell.state) {
                        "S2", "S6" -> 0
                        "S1", "S8" -> 2
                        else -> 3
                    },
                permissions = SettingsPermissionSnapshot(camera = true),
                isLoading = false,
                isDeletingReports = cell.state == "S5",
                error =
                    when (cell.state) {
                        "S6" -> "settings_load_failed"
                        "S7" -> "delete_reports_failed"
                        else -> null
                    },
            )
        var state by mutableStateOf(initial)
        val version =
            if (cell.state == "S8") {
                "1.0.0-debug+a06a-layout-fixture.with-a-long-version-name (123456789)"
            } else {
                "1.0.0 (1)"
            }
        val inputs =
            JSONObject()
                .put("source", "synthetic SettingsScreen arguments; no Room deletion")
                .put("reportCount", initial.reportCount)
                .put("permissions", "camera=true; microphone/phone/location/bluetooth/physicalActivity=false")
                .put("isDeletingReports", initial.isDeletingReports)
                .put("error", initial.error ?: JSONObject.NULL)
                .put("appVersionFixture", version)
                .put("languageSelection", AppLanguage.SYSTEM.name)
        val capture = A06aUiCapture(composeRule, cell, "component", inputs)
        fixture(capture) {
            SettingsScreen(
                state = state,
                appVersion = version,
                onThemeMode = { mode ->
                    called("theme_${mode.name}")
                    // The opposite theme is a callback check; never capture it as this cell's theme.
                    if (mode == AppThemeMode.SYSTEM || (mode == AppThemeMode.DARK) == cell.dark) {
                        state = state.copy(preferences = state.preferences.copy(themeMode = mode))
                    }
                },
                selectedLanguage = AppLanguage.SYSTEM,
                onOpenLanguage = { called("language") },
                onTestWarnings = {
                    called("warnings")
                    state = state.copy(preferences = state.preferences.copy(testWarningsEnabled = it))
                },
                onOpenAppSettings = { called("app_settings") },
                onDeleteAll = { called("delete") },
                onOpenPrivacy = { called("privacy") },
                onOpenSupport = { called("support") },
                onOpenLicenses = { called("licenses") },
            )
        }
        capture.guard {
            capture.admit(hasTestTag("settings_language"))
            AppThemeMode.entries.forEach { mode ->
                composeRule
                    .onNodeWithTag("settings_theme_${mode.name.lowercase()}")
                    .assert(selected(mode == AppThemeMode.SYSTEM))
            }
            capture.assertTheme(cell.dark)
            a06aInspectSettings(capture, initial.reportCount, version)
            if (initial.error != null) {
                capture.visit(hasText(context.getString(R.string.state_error_title), ignoreCase = true), "error_title")
                val message = if (cell.state == "S7") R.string.settings_delete_error else R.string.settings_error
                capture.visit(hasText(context.getString(message)), "error_message")
            }
            val delete = capture.bring(hasTestTag("settings_delete_all"))
            if (cell.state == "S2" || cell.state == "S5" || cell.state == "S6") {
                delete.assertIsNotEnabled().performTouchInput { click() }
                composeRule.onNodeWithTag("settings_confirm_delete_all").assertDoesNotExist()
                assertEquals(0, counts.getOrDefault("delete", 0))
                capture.checkpoint("disabled_delete_no_callback")
            } else {
                delete.assertIsEnabled()
                assertEquals(0, counts.getOrDefault("delete", 0))
            }
            if (cell.state == "S1") {
                capture.bring(hasTestTag("settings_theme_system"))
                composeRule
                    .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup))
                    .assertCountEquals(1)
                for (mode in AppThemeMode.entries) {
                    val tag = "settings_theme_${mode.name.lowercase()}"
                    capture.bring(hasTestTag(tag)).performClick()
                    assertEquals(1, counts["theme_${mode.name}"])
                    if (mode == AppThemeMode.SYSTEM || (mode == AppThemeMode.DARK) == cell.dark) {
                        composeRule.onNodeWithTag(tag).assert(selected(true))
                        capture.checkpoint("theme_${mode.name.lowercase()}")
                    }
                }
                val matchingMode = if (cell.dark) AppThemeMode.DARK else AppThemeMode.LIGHT
                capture.bring(hasTestTag("settings_theme_${matchingMode.name.lowercase()}")).performClick()
                capture.checkpoint("matching_theme_selected")
                val warnings = capture.bring(hasTestTag("settings_test_warnings"))
                warnings
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.On))
                    .performClick()
                warnings.assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.Off))
                capture.checkpoint("warnings_off")
                warnings.performClick()
                warnings.assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.On))
                assertEquals(2, counts["warnings"])
                capture.checkpoint("warnings_on")
                listOf(
                    "settings_language" to "language",
                    "settings_open_app_settings" to "app_settings",
                    "settings_privacy" to "privacy",
                    "settings_support" to "support",
                    "settings_licenses" to "licenses",
                ).forEach { (tag, callback) ->
                    capture.bring(hasTestTag(tag)).performClick()
                    assertEquals(1, counts[callback])
                }
                inputs.put("fixtureCallbackCounts", JSONObject(counts.toMap()))
            }
            capture.sweep()
            if (cell.state == "S4") confirmation(capture, counts)
            assertEquals(if (cell.state == "S4") 1 else 0, counts.getOrDefault("delete", 0))
            assertEquals(initial.reportCount, state.reportCount)
            inputs.put("fixtureCallbackCounts", JSONObject(counts.toMap()))
            capture.checkpoint("fixture_complete")
        }
    }

    @Test
    fun languageState() {
        val cell = selection.cell
        var language by mutableStateOf(
            when (cell.state) {
                "L1" -> AppLanguage.SYSTEM
                "L2" -> AppLanguage.ENGLISH
                else -> AppLanguage.FINNISH
            },
        )
        val inputs = JSONObject().put("source", "local selection independent of resource locale")
        val capture = A06aUiCapture(composeRule, cell, "component", inputs)
        fixture(capture) {
            LanguageSelectionScreen(selectedLanguage = language, onLanguage = { language = it })
        }
        capture.guard {
            capture.admit(hasTestTag("settings_language_system"))
            a06aInspectLanguages(capture, language)
            capture.sweep()
            for (choice in listOf(AppLanguage.SYSTEM, AppLanguage.ENGLISH, AppLanguage.FINNISH)) {
                capture.bring(hasTestTag(languageTag(choice))).performClick()
                composeRule.runOnIdle { assertEquals(choice, language) }
                a06aAssertOneSelection(composeRule, choice)
                inputs.put("selectedLanguage", choice.name)
                capture.checkpoint("selected_${choice.name.lowercase()}")
            }
        }
    }

    @Test
    fun licensesContent() {
        val capture = A06aUiCapture(composeRule, selection.cell, "component")
        fixture(capture) { LicensesScreen() }
        capture.guard { a06aInspectLicenses(capture) }
    }

    @Test
    fun cameraFullAccessContent() {
        var returned = 0
        val capture =
            A06aUiCapture(
                composeRule,
                selection.cell,
                "component",
                JSONObject().put("source", "fixture-only CAMERA; inactive paywall route never forced"),
            )
        fixture(capture) {
            FullAccessScreen(featureId = DiagnosticCategoryId.CAMERA.stableId, onBack = { returned++ })
        }
        capture.guard {
            val context = selection.cell.localizedContext()
            capture.admit(hasText(context.getString(R.string.full_access_return)))
            capture.visit(hasContentDescription(context.getString(R.string.home_cat_camera)), "camera_heading")
            capture.visit(hasText(context.getString(R.string.full_access_camera_description)), "camera_explanation")
            capture.visit(hasContentDescription(context.getString(R.string.full_access_title)), "full_heading")
            capture.visit(hasText(context.getString(R.string.full_access_offer)), "full_offer")
            capture.visit(hasText(context.getString(R.string.home_intro_free)), "free_note")
            capture.visit(hasText(context.getString(R.string.full_access_return)), "return_target", action = true)
            capture.sweep()
            capture.bring(hasText(context.getString(R.string.full_access_return))).performClick()
            assertEquals(1, returned)
            capture.inputs.put("returnCallbacks", returned)
            capture.checkpoint("return_callback_only")
        }
    }

    private fun fixture(
        capture: A06aUiCapture,
        content: @Composable () -> Unit,
    ) {
        val metrics =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext.resources.displayMetrics
        val heightDp = metrics.heightPixels / metrics.density
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride
                    .Locales(LocaleList(capture.cell.locale))
                    .then(DeviceConfigurationOverride.FontScale(capture.cell.fontScale))
                    .then(
                        DeviceConfigurationOverride.ForcedSize(
                            DpSize((capture.cell.contentDp + 32f).dp, heightDp.dp),
                        ),
                    ),
            ) {
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current
                SideEffect {
                    capture.inputs
                        .put("composeResourceLocales", configuration.locales.toLanguageTags())
                        .put("composeResourceUiMode", configuration.uiMode)
                        .put("fixtureDensity", density.density)
                        .put("fixtureFontScale", density.fontScale)
                }
                FonecheckTheme(darkTheme = capture.cell.dark) {
                    // ForcedSize rounds the total width; production padding rounds each side separately.
                    val fixtureWidth =
                        with(density) {
                            val contentWidth = capture.cell.contentDp.dp
                            val paddingPx = FonecheckTheme.spacing.md.roundToPx()
                            (contentWidth.roundToPx() + 2 * paddingPx).toDp()
                        }
                    Box(
                        Modifier
                            .requiredWidth(fixtureWidth)
                            .fillMaxSize()
                            .background(FonecheckTheme.colors.panel)
                            .testTag(A06A_FIXTURE_ROOT),
                    ) {
                        content()
                    }
                }
            }
        }
    }

    private fun confirmation(
        capture: A06aUiCapture,
        counts: Map<String, Int>,
    ) {
        val context = capture.cell.localizedContext()

        fun open(name: String) {
            capture.bring(hasTestTag("settings_delete_all")).performClick()
            assertEquals(0, counts.getOrDefault("delete", 0))
            capture.dialogCheckpoint(name)
        }
        open("dialog_cancel_before")
        capture.dialogAction(hasText(context.getString(R.string.settings_delete_all_cancel))).performClick()
        composeRule.onNodeWithTag("settings_confirm_delete_all").assertDoesNotExist()
        assertEquals(0, counts.getOrDefault("delete", 0))
        capture.checkpoint("dialog_cancel_after")
        open("dialog_back_before")
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_confirm_delete_all").assertDoesNotExist()
        assertEquals(0, counts.getOrDefault("delete", 0))
        capture.checkpoint("dialog_back_after")
        open("dialog_confirm_before")
        capture.dialogAction(hasTestTag("settings_confirm_delete_all")).performClick()
        composeRule.onNodeWithTag("settings_confirm_delete_all").assertDoesNotExist()
        assertEquals(1, counts["delete"])
        capture.checkpoint("dialog_confirm_after")
    }
}

internal fun a06aInspectSettings(
    capture: A06aUiCapture,
    count: Int? = null,
    version: String? = null,
) {
    val context = capture.cell.localizedContext()
    listOf(
        R.string.settings_appearance,
        R.string.full_access_title,
        R.string.settings_permissions,
        R.string.settings_reports,
        R.string.settings_privacy_section,
        R.string.settings_about,
    ).forEach { capture.visit(hasContentDescription(context.getString(it)), "heading_$it") }
    listOf(
        R.string.settings_theme,
        R.string.settings_test_warnings,
        R.string.settings_test_warnings_description,
        R.string.home_intro_body,
        R.string.home_intro_free,
        R.string.full_access_offer,
        R.string.settings_permissions_description,
        R.string.settings_local_only,
        R.string.settings_disclaimer,
    ).forEach { capture.visit(hasText(context.getString(it)), "text_$it") }
    listOf("system", "light", "dark").forEach {
        capture.visit(hasTestTag("settings_theme_$it"), "theme_target_$it", action = true)
    }
    listOf(
        "settings_language",
        "settings_test_warnings",
        "settings_open_app_settings",
        "settings_delete_all",
        "settings_privacy",
        "settings_support",
        "settings_licenses",
    ).forEach { capture.visit(hasTestTag(it), "target_$it", action = true) }
    listOf(
        R.string.settings_permission_camera,
        R.string.settings_permission_microphone,
        R.string.settings_permission_phone,
        R.string.settings_permission_location,
        R.string.settings_permission_bluetooth,
        R.string.settings_permission_physical_activity,
    ).forEach { label ->
        capture.visit(hasText(context.getString(label)), checkpoint = null)
        val value =
            if (count == null) {
                null
            } else {
                context.getString(
                    if (label == R.string.settings_permission_camera) {
                        R.string.settings_permission_granted
                    } else {
                        R.string.settings_permission_not_granted
                    },
                )
            }
        capture.observeRow(context.getString(label), value)
        capture.checkpoint("permission_$label")
    }
    capture.visit(hasText(context.getString(R.string.settings_report_count)), "report_count")
    capture.observeRow(context.getString(R.string.settings_report_count), count?.toString())
    capture.visit(hasText(context.getString(R.string.settings_version)), "version")
    capture.observeRow(context.getString(R.string.settings_version), version)
}

internal fun languageTag(language: AppLanguage): String = "settings_language_${language.name.lowercase()}"

internal fun selected(value: Boolean): SemanticsMatcher =
    SemanticsMatcher.expectValue(SemanticsProperties.Selected, value)

internal fun a06aAssertOneSelection(
    rule: androidx.compose.ui.test.junit4.ComposeTestRule,
    language: AppLanguage,
) {
    rule.onAllNodes(selected(true)).assertCountEquals(1)
    AppLanguage.entries.forEach { rule.onNode(hasTestTag(languageTag(it))).assert(selected(it == language)) }
}

internal fun a06aInspectLanguages(
    capture: A06aUiCapture,
    selectedLanguage: AppLanguage,
) {
    val context = capture.cell.localizedContext()
    assertEquals(14, AppLanguage.entries.size)
    val collator = Collator.getInstance(java.util.Locale.forLanguageTag(capture.cell.locale))
    val order =
        listOf(AppLanguage.SYSTEM) +
            AppLanguage.entries
                .filterNot { it == AppLanguage.SYSTEM }
                .sortedWith { a, b ->
                    collator.compare(context.getString(a.labelResId), context.getString(b.labelResId))
                }
    capture.inputs
        .put("selectedLanguage", selectedLanguage.name)
        .put("uiCollatorOrder", org.json.JSONArray(order.map { it.name }))
    capture.languageRows(order, selectedLanguage)
}

internal fun a06aInspectLicenses(capture: A06aUiCapture) {
    val context = capture.cell.localizedContext()
    val raw =
        context.resources
            .openRawResource(R.raw.third_party_notices)
            .bufferedReader()
            .use { it.readText() }
    val fullText = "${context.getString(R.string.licenses_component_inventory)}\n\n$raw"
    capture.inputs.put("noticeSource", "bundled raw/third_party_notices; raw language unchanged")
    capture.admit(hasText(fullText), textWidth = true)
    capture.visit(hasText(context.getString(R.string.licenses_description)), "licenses_description")
    capture.visit(hasContentDescription(context.getString(R.string.licenses_notices_heading)), "notices_heading")
    capture.visit(hasText(fullText), "inventory_and_raw_notices")
    capture.sweep()
    capture.licensesEnd(fullText)
}
