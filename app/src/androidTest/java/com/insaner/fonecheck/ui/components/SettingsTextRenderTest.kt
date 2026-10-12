package com.insaner.fonecheck.ui.components

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.then
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.data.preferences.AppThemeMode
import com.insaner.fonecheck.localization.AppLanguage
import com.insaner.fonecheck.ui.screens.settings.SettingsScreen
import com.insaner.fonecheck.ui.screens.settings.SettingsState
import com.insaner.fonecheck.ui.theme.DarkFonecheckColors
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.FonecheckType
import com.insaner.fonecheck.ui.theme.LightFonecheckColors
import com.insaner.fonecheck.ui.theme.SemanticTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale
import kotlin.math.ceil

/** Exercises the production Text calls without adding onTextLayout or changing their constraints. */
@RunWith(Parameterized::class)
class SettingsTextRenderTest(
    private val contentWidthDp: Int,
    private val fontScale: Float,
    private val dark: Boolean,
    private val language: String,
) {
    @get:Rule
    val composeRule = createComposeRule()

    private val context =
        InstrumentationRegistry.getInstrumentation().targetContext.let { base ->
            base.createConfigurationContext(
                Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag(language)) },
            )
        }
    private val colors get() = if (dark) DarkFonecheckColors else LightFonecheckColors

    @Test
    fun settingsLabelsKeepFullLayoutsSelectionNavigationAndToggleContracts() {
        var preferences by mutableStateOf(AppPreferences())
        val themes = mutableListOf<AppThemeMode>()
        val warnings = mutableListOf<Boolean>()
        var languageClicks = 0
        var deleteClicks = 0
        fixture(screenPadding = true) {
            SettingsScreen(
                state = SettingsState(preferences = preferences, reportCount = 0, isLoading = false),
                appVersion = "1.0.0 (1)",
                onThemeMode = {
                    themes += it
                    preferences = preferences.copy(themeMode = it)
                },
                selectedLanguage = AppLanguage.SYSTEM,
                onOpenLanguage = { languageClicks++ },
                onTestWarnings = {
                    warnings += it
                    preferences = preferences.copy(testWarningsEnabled = it)
                },
                onOpenAppSettings = {},
                onDeleteAll = { deleteClicks++ },
                onOpenPrivacy = {},
                onOpenSupport = {},
                onOpenLicenses = {},
            )
        }
        composeRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup)).assertCountEquals(1)
        assertThemeButtons(AppThemeMode.SYSTEM)
        AppThemeMode.entries.forEach { mode ->
            settingsNode("settings_theme_${mode.name.lowercase()}").performClick()
            assertThemeButtons(mode)
        }
        assertEquals(AppThemeMode.entries.toList(), themes)

        val navigation = settingsNode("settings_language")
        assertControl(navigation, Role.Button)
        val label = context.getString(R.string.settings_language)
        val value = context.getString(AppLanguage.SYSTEM.labelResId)
        assertEquals(
            listOf(label, value),
            navigation.fetchSemanticsNode().config[SemanticsProperties.Text].map { it.text },
        )
        val labelNode = textNode("settings_language", label)
        val valueNode = textNode("settings_language", value)
        assertLayout(labelNode, label, FonecheckType.rowLabel, colors.textSecondary)
        assertLayout(valueNode, value, FonecheckType.rowValue, colors.textPrimary)
        assertRowPlacement(labelNode, valueNode)
        navigation.performClick()
        assertEquals(1, languageClicks)

        val toggle = settingsNode("settings_test_warnings")
        assertControl(toggle, Role.Switch)
        val warningLabel = context.getString(R.string.settings_test_warnings)
        assertEquals(
            listOf(warningLabel, context.getString(R.string.settings_test_warnings_description)),
            toggle.fetchSemanticsNode().config[SemanticsProperties.Text].map { it.text },
        )
        assertLayout(
            textNode("settings_test_warnings", warningLabel),
            warningLabel,
            FonecheckType.rowLabel,
            colors.textSecondary,
        )
        toggle
            .assert(
                SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.On),
            ).performClick()
        toggle
            .assert(
                SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.Off),
            ).performClick()
        toggle.assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.On))
        assertEquals(listOf(false, true), warnings)
        composeRule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)).assertCountEquals(1)

        val delete = settingsNode("settings_delete_all").assertIsNotEnabled()
        assertEquals(Role.Button, delete.fetchSemanticsNode().config[SemanticsProperties.Role])
        val deleteLabel = context.getString(R.string.settings_delete_all)
        assertLayout(
            textNode("settings_delete_all", deleteLabel),
            deleteLabel,
            FonecheckType.buttonLabel,
            colors.textDisabled,
        )
        delete.performTouchInput { click() }
        composeRule.onNodeWithTag("settings_confirm_delete_all").assertDoesNotExist()
        assertEquals(0, deleteClicks)
    }

    @Test
    fun dataRowValuesAndUnavailableLabelsKeepFullLayoutsAndLongPressContracts() {
        val granted = context.getString(R.string.settings_permission_granted)
        val denied = context.getString(R.string.settings_permission_not_granted)
        val unavailable = context.getString(R.string.value_unavailable_short)
        val labels =
            listOf(
                R.string.settings_permission_camera,
                R.string.settings_permission_microphone,
                R.string.settings_permission_phone,
                R.string.settings_permission_location,
            ).map { context.getString(it) }
        val copies = mutableListOf<String>()
        fixture(screenPadding = false) {
            Column(Modifier.fillMaxWidth()) {
                DataRow(
                    label = labels[0],
                    value = granted,
                    modifier = Modifier.testTag("granted"),
                    tone = SemanticTone.PASS,
                    onValueLongClick = { copies += "granted" },
                )
                DataRow(
                    label = labels[1],
                    value = denied,
                    modifier = Modifier.testTag("denied"),
                    onValueLongClick = { copies += "denied" },
                )
                DataRow(
                    label = labels[2],
                    value = null,
                    modifier = Modifier.testTag("unavailable"),
                    tone = SemanticTone.FAIL,
                    onValueLongClick = { copies += "unexpected" },
                )
                DataRow(
                    label = labels[3],
                    value = null,
                    modifier = Modifier.testTag("custom-unavailable"),
                    tone = SemanticTone.FAIL,
                    unavailableLabel = denied,
                    onValueLongClick = { copies += "unexpected" },
                )
            }
        }
        listOf("granted" to granted, "denied" to denied, "unavailable" to unavailable, "custom-unavailable" to denied)
            .forEachIndexed { index, (tag, value) ->
                val row = composeRule.onNodeWithTag(tag).assertIsDisplayed()
                val label = textNode(tag, labels[index]).assertIsDisplayed()
                assertLayout(label, labels[index], FonecheckType.rowLabel, colors.textSecondary)
                val text = textNode(tag, value)
                assertLayout(
                    text,
                    value,
                    FonecheckType.rowValue,
                    when (index) {
                        0 -> colors.pass
                        1 -> colors.textPrimary
                        else -> colors.textMuted
                    },
                    singleLine = fontScale <= LARGE_FONT_SCALE_THRESHOLD,
                )
                assertRowPlacement(label, text)
                val config = row.fetchSemanticsNode().config
                assertEquals(null, config.getOrNull(SemanticsActions.OnClick))
                assertEquals(null, config.getOrNull(SemanticsProperties.Role))
                if (index < 2) {
                    assertEquals(listOf(labels[index], value), config[SemanticsProperties.Text].map { it.text })
                    assertEquals(
                        context.getString(R.string.copy_value_action),
                        config[SemanticsActions.OnLongClick].label,
                    )
                    row.performSemanticsAction(SemanticsActions.OnLongClick) { assertTrue(it()) }
                } else {
                    assertEquals(null, config.getOrNull(SemanticsActions.OnLongClick))
                }
            }
        assertEquals(listOf("granted", "denied"), copies)
    }

    private fun fixture(
        screenPadding: Boolean,
        content: @Composable () -> Unit,
    ) {
        // Settings owns 16dp per side; the direct DataRow fixture owns no horizontal padding.
        val forcedWidth = contentWidthDp + if (screenPadding) 32 else 0
        var fixtureWidth = forcedWidth.dp
        var density = 0f
        var actualFontScale = 0f
        var resourceLanguage = ""
        var textLanguage = ""
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride
                    .Locales(LocaleList(language))
                    .then(DeviceConfigurationOverride.FontScale(fontScale))
                    .then(DeviceConfigurationOverride.ForcedSize(DpSize(forcedWidth.dp, 640.dp))),
            ) {
                density = LocalDensity.current.density
                actualFontScale = LocalDensity.current.fontScale
                resourceLanguage = LocalConfiguration.current.locales[0].language
                textLanguage = LocalLocale.current.platformLocale.language
                FonecheckTheme(darkTheme = dark) {
                    // ForcedSize rounds the total width; production padding rounds each side separately.
                    fixtureWidth =
                        with(LocalDensity.current) {
                            val paddingPx = if (screenPadding) FonecheckTheme.spacing.md.roundToPx() else 0
                            (contentWidthDp.dp.roundToPx() + 2 * paddingPx).toDp()
                        }
                    Box(
                        Modifier
                            .requiredWidth(fixtureWidth)
                            .fillMaxSize()
                            .background(FonecheckTheme.colors.panel)
                            .testTag("settings-text-fixture"),
                    ) {
                        content()
                    }
                }
            }
        }
        val root = composeRule.onNodeWithTag("settings-text-fixture").fetchSemanticsNode().boundsInRoot
        val contentBounds =
            if (screenPadding) {
                composeRule.onNodeWithTag("settings_language").fetchSemanticsNode().boundsInRoot
            } else {
                root
            }
        Log.i(
            "A06aSettingsText",
            "contentDp=${contentBounds.width / density} requestedDp=$contentWidthDp scale=$actualFontScale " +
                "density=$density resourceLanguage=$resourceLanguage textLanguage=$textLanguage dark=$dark bounds=$contentBounds",
        )
        assertEquals(fixtureWidth.value, root.width / density, 0.5f)
        assertEquals(contentWidthDp.toFloat(), contentBounds.width / density, 0.5f)
        assertEquals(fontScale, actualFontScale, 0.001f)
        assertEquals(language, resourceLanguage)
        assertEquals(language, textLanguage)
    }

    private fun settingsNode(tag: String): SemanticsNodeInteraction {
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasTestTag(tag))
        return composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
    }

    private fun textNode(
        tag: String,
        text: String,
    ): SemanticsNodeInteraction =
        composeRule.onNode(hasText(text) and hasAnyAncestor(hasTestTag(tag)), useUnmergedTree = true)

    private fun assertThemeButtons(selected: AppThemeMode) {
        AppThemeMode.entries.forEach { mode ->
            val tag = "settings_theme_${mode.name.lowercase()}"
            val label =
                context.getString(
                    when (mode) {
                        AppThemeMode.SYSTEM -> R.string.settings_theme_system
                        AppThemeMode.LIGHT -> R.string.settings_theme_light
                        AppThemeMode.DARK -> R.string.settings_theme_dark
                    },
                )
            val button = settingsNode(tag)
            assertControl(button, Role.Button)
            button.assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, mode == selected))
            assertEquals(listOf(label), button.fetchSemanticsNode().config[SemanticsProperties.Text].map { it.text })
            assertLayout(
                textNode(tag, label),
                label,
                FonecheckType.buttonLabel,
                if (mode == selected) colors.primaryButtonContent else colors.textPrimary,
            )
        }
    }

    private fun assertControl(
        node: SemanticsNodeInteraction,
        role: Role,
    ) {
        node.assertIsEnabled()
        val config = node.fetchSemanticsNode().config
        assertEquals(role, config[SemanticsProperties.Role])
        assertTrue(config.contains(SemanticsActions.OnClick))
        assertEquals(null, config.getOrNull(SemanticsProperties.ContentDescription))
    }

    private fun assertRowPlacement(
        label: SemanticsNodeInteraction,
        value: SemanticsNodeInteraction,
    ) {
        val labelBounds = label.fetchSemanticsNode().boundsInRoot
        val valueBounds = value.fetchSemanticsNode().boundsInRoot
        if (fontScale > LARGE_FONT_SCALE_THRESHOLD) {
            assertTrue(valueBounds.top >= labelBounds.bottom)
        } else {
            assertTrue(valueBounds.left >= labelBounds.right)
        }
    }

    private fun assertLayout(
        node: SemanticsNodeInteraction,
        text: String,
        style: TextStyle,
        color: Color,
        singleLine: Boolean = false,
    ) {
        node.assertIsDisplayed()
        val layouts = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val layout = layouts.single()
        val paragraph = layout.multiParagraph
        val nodeBounds = node.fetchSemanticsNode().boundsInRoot
        Log.i(
            "A06aSettingsText",
            "text=$text layout=${layout.size} paragraph=${paragraph.width}x${paragraph.height} " +
                "constraints=${layout.layoutInput.constraints} lines=${layout.lineCount} " +
                "widthOverflow=${layout.didOverflowWidth} heightOverflow=${layout.didOverflowHeight} visualOverflow=${layout.hasVisualOverflow}",
        )
        val config = node.fetchSemanticsNode().config
        assertEquals(text, config[SemanticsProperties.Text].single().text)
        assertEquals(null, config.getOrNull(SemanticsProperties.ContentDescription))
        assertFalse(config.contains(SemanticsProperties.Heading))
        assertEquals(text, layout.layoutInput.text.text)
        assertTrue(layout.layoutInput.softWrap)
        assertEquals(if (singleLine) 1 else Int.MAX_VALUE, layout.layoutInput.maxLines)
        assertEquals(if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip, layout.layoutInput.overflow)
        assertEquals(style.fontFamily, layout.layoutInput.style.fontFamily)
        assertEquals(style.fontSize, layout.layoutInput.style.fontSize)
        assertEquals(style.fontWeight, layout.layoutInput.style.fontWeight)
        assertEquals(style.lineHeight, layout.layoutInput.style.lineHeight)
        assertEquals(style.fontFeatureSettings, layout.layoutInput.style.fontFeatureSettings)
        assertEquals(color, layout.layoutInput.style.color)
        assertEquals(ceil(paragraph.width).toInt(), layout.size.width)
        assertEquals(ceil(paragraph.height).toInt(), layout.size.height)
        assertEquals(layout.size.width.toFloat(), nodeBounds.width, 1f)
        assertEquals(layout.size.height.toFloat(), nodeBounds.height, 1f)
        assertFalse(layout.didOverflowWidth)
        assertFalse(layout.didOverflowHeight)
        assertFalse(layout.hasVisualOverflow)
        assertFalse(paragraph.didExceedMaxLines)
        assertTrue(layout.lineCount > 0)
        assertEquals(0, layout.getLineStart(0))
        for (line in 0 until layout.lineCount) {
            assertFalse(layout.isLineEllipsized(line))
            assertTrue(layout.getLineLeft(line) >= -1f)
            assertTrue(layout.getLineRight(line) <= layout.size.width + 1f)
            if (line > 0) assertEquals(layout.getLineEnd(line - 1), layout.getLineStart(line))
        }
        assertEquals(text.length, layout.getLineEnd(layout.lineCount - 1, visibleEnd = true))
        for (offset in text.indices) {
            val bounds = layout.getBoundingBox(offset)
            assertTrue("Character $offset outside layout: $bounds", bounds.left >= -1f)
            assertTrue("Character $offset outside layout: $bounds", bounds.right <= layout.size.width + 1f)
            assertTrue("Character $offset outside layout: $bounds", bounds.top >= -1f)
            assertTrue("Character $offset outside layout: $bounds", bounds.bottom <= layout.size.height + 1f)
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}dp scale={1} dark={2} locale={3}")
        fun configurations(): List<Array<Any>> =
            listOf(276 to 1f, 320 to 1f, 276 to 2f, 320 to 2f).flatMap { (width, scale) ->
                listOf(false, true).flatMap { dark ->
                    listOf("en", "fi").map { language -> arrayOf<Any>(width, scale, dark, language) }
                }
            }
    }
}
