package com.insaner.fonecheck.ui

import android.app.UiModeManager
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.core.os.LocaleListCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.data.preferences.AppThemeMode
import com.insaner.fonecheck.localization.AppLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/** The real MainActivity/NavHost. Only the three approved reachable Settings routes are opened. */
@RunWith(AndroidJUnit4::class)
class A06aUiShellTest {
    private val selection = A06aUiSelection()
    private val composeRule = createAndroidComposeRule<MainActivity>()
    private var originalLocales: LocaleListCompat? = null
    private var originalPreferences: AppPreferences? = null
    private var originalNightMode: Int? = null

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(selection).around(composeRule)

    @Before
    fun saveOwnedState() {
        originalLocales = composeRule.runOnIdle { AppCompatDelegate.getApplicationLocales() }
        originalPreferences = runBlocking { withTimeout(10_000L) { repository().preferences.first() } }
    }

    @After
    fun restoreOwnedState() {
        val failures = mutableListOf<Throwable>()

        fun restore(action: () -> Unit) {
            try {
                action()
            } catch (failure: Throwable) {
                failures.add(failure)
            }
        }
        restore {
            originalNightMode?.let { mode ->
                setSystemNightMode(mode)
                assertEquals(mode, nightModeManager().nightMode)
            }
        }
        restore {
            originalPreferences?.let { original ->
                val owner = repository()
                runBlocking {
                    withTimeout(10_000L) {
                        val current = owner.preferences.first()
                        if (current.themeMode != original.themeMode) owner.setThemeMode(original.themeMode)
                        if (current.testWarningsEnabled != original.testWarningsEnabled) {
                            owner.setTestWarningsEnabled(original.testWarningsEnabled)
                        }
                        if (current.homeIntroductionDismissed != original.homeIntroductionDismissed) {
                            owner.setHomeIntroductionDismissed(original.homeIntroductionDismissed)
                        }
                        assertEquals(original, owner.preferences.first { it == original })
                    }
                }
            }
        }
        restore {
            originalLocales?.let { locales ->
                composeRule.activityRule.scenario.onActivity { AppCompatDelegate.setApplicationLocales(locales) }
                composeRule.waitUntil(10_000L) {
                    composeRule.runOnIdle { AppCompatDelegate.getApplicationLocales().toLanguageTags() } ==
                        locales.toLanguageTags()
                }
                composeRule.waitForIdle()
            }
        }
        failures.firstOrNull()?.let { first ->
            failures.drop(1).forEach(first::addSuppressed)
            throw first
        }
    }

    @Test
    fun reachableScreen() {
        val cell = selection.cell
        prepareLocale()
        setAppTheme(if (cell.dark) AppThemeMode.DARK else AppThemeMode.LIGHT)
        openSettings()
        val inputs =
            JSONObject()
                .put("source", "actual MainActivity route; observed production count/permissions/version")
                .put("historyMutation", "none: no seeding, deletion or diagnostic journey")
                .put("appLanguageSelection", cell.locale)
                .put("renderedScreen", "Settings")
        val capture = A06aUiCapture(composeRule, cell, "actual-shell", inputs)
        capture.guard {
            when (cell.state) {
                "A1" -> {
                    capture.admit(hasTestTag("settings_language"))
                    capture.assertTheme(cell.dark)
                    capture.shellChrome(cell.localizedContext().getString(R.string.settings_title))
                    a06aInspectSettings(capture)
                    capture.sweep()
                    back()
                    assertHome()
                }
                "A2" -> {
                    capture.bring(hasTestTag("settings_language")).performClick()
                    waitForTitle(R.string.settings_language)
                    inputs.put("renderedScreen", "LanguageSelection")
                    capture.admit(hasTestTag("settings_language_system"))
                    capture.shellChrome(cell.localizedContext().getString(R.string.settings_language))
                    a06aInspectLanguages(capture, if (cell.locale == "fi") AppLanguage.FINNISH else AppLanguage.ENGLISH)
                    capture.sweep()
                    back()
                    waitForTitle(R.string.settings_title)
                    inputs
                        .put("renderedScreen", "Settings")
                        .put("actualTitle", cell.localizedContext().getString(R.string.settings_title))
                    capture.bring(hasTestTag("settings_language")).assertIsDisplayed()
                    capture.checkpoint("back_to_settings")
                }
                "A3" -> {
                    capture.bring(hasTestTag("settings_licenses")).performClick()
                    waitForTitle(R.string.licenses_title)
                    inputs.put("renderedScreen", "Licenses")
                    a06aInspectLicenses(capture)
                    capture.shellChrome(cell.localizedContext().getString(R.string.licenses_title))
                    back()
                    waitForTitle(R.string.settings_title)
                    inputs
                        .put("renderedScreen", "Settings")
                        .put("actualTitle", cell.localizedContext().getString(R.string.settings_title))
                    capture.bring(hasTestTag("settings_licenses")).assertIsDisplayed()
                    capture.checkpoint("back_to_settings")
                }
            }
        }
    }

    @Test
    fun systemThemeSmoke() {
        prepareLocale()
        val initialNightMode = nightModeManager().nightMode
        originalNightMode = initialNightMode
        openSettings()
        val capture =
            A06aUiCapture(
                composeRule,
                selection.cell,
                "actual-shell",
                JSONObject()
                    .put("source", "single Settings System-theme persistence smoke")
                    .put("originalSystemNightMode", initialNightMode),
            )
        capture.guard {
            capture.bring(hasTestTag("settings_theme_system")).performClick()
            awaitAppTheme(AppThemeMode.SYSTEM)
            val first = if (selection.cell.dark) UiModeManager.MODE_NIGHT_YES else UiModeManager.MODE_NIGHT_NO
            val second = if (selection.cell.dark) UiModeManager.MODE_NIGHT_NO else UiModeManager.MODE_NIGHT_YES
            for (mode in listOf(first, second)) {
                setSystemNightMode(mode)
                val dark = mode == UiModeManager.MODE_NIGHT_YES
                composeRule.waitUntil(10_000L) {
                    (composeRule.activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                        if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
                }
                waitForTitle(R.string.settings_title)
                capture.assertTheme(dark)
                capture.admit(hasTestTag("settings_language"))
                capture.bring(hasTestTag("settings_theme_system")).assertIsSelected()
                capture.shellChrome(selection.cell.localizedContext().getString(R.string.settings_title), dark = dark)
                capture.checkpoint("system_${if (dark) "dark" else "light"}_before_back")
                back()
                assertHome()
                openSettings()
                capture.bring(hasTestTag("settings_theme_system")).assertIsSelected()
                awaitAppTheme(AppThemeMode.SYSTEM)
                capture.assertTheme(dark)
                capture.checkpoint("system_${if (dark) "dark" else "light"}_after_reopen")
            }
        }
    }

    private fun prepareLocale() {
        composeRule.activityRule.scenario.onActivity {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selection.cell.locale))
        }
        composeRule.waitUntil(10_000L) {
            composeRule.activity.resources.configuration.locales[0]
                .language == selection.cell.locale
        }
        assertEquals(selection.cell.fontScale, composeRule.activity.resources.configuration.fontScale, 0.001f)
    }

    private fun setAppTheme(mode: AppThemeMode) {
        runBlocking { withTimeout(10_000L) { repository().setThemeMode(mode) } }
        awaitAppTheme(mode)
    }

    private fun awaitAppTheme(mode: AppThemeMode) {
        runBlocking { withTimeout(10_000L) { repository().preferences.first { it.themeMode == mode } } }
        composeRule.waitForIdle()
    }

    private fun openSettings() {
        assertHome()
        composeRule
            .onNode(
                hasContentDescription(
                    selection.cell.localizedContext().getString(R.string.home_settings_content_description),
                ),
            ).performClick()
        waitForTitle(R.string.settings_title)
    }

    private fun assertHome() {
        val settings = selection.cell.localizedContext().getString(R.string.home_settings_content_description)
        composeRule.waitUntil(10_000L) {
            composeRule.onAllNodes(hasContentDescription(settings)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(hasContentDescription(settings)).assertIsDisplayed()
    }

    private fun waitForTitle(id: Int) {
        val title = selection.cell.localizedContext().getString(id)
        composeRule.waitUntil(10_000L) { composeRule.onAllNodes(hasText(title)).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNode(hasText(title)).assertIsDisplayed()
    }

    private fun back() {
        composeRule
            .onNode(hasContentDescription(selection.cell.localizedContext().getString(R.string.navigation_back)))
            .assertIsDisplayed()
            .performClick()
    }

    private fun repository() = composeRule.activity.appPreferencesRepository

    private fun nightModeManager(): UiModeManager =
        checkNotNull(
            InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(UiModeManager::class.java),
        )

    private fun setSystemNightMode(mode: Int) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.adoptShellPermissionIdentity("android.permission.MODIFY_DAY_NIGHT_MODE")
        try {
            nightModeManager().setNightMode(mode)
        } finally {
            automation.dropShellPermissionIdentity()
        }
    }
}
