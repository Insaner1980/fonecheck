package com.insaner.fonecheck.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.preferences.AppPreferences
import com.insaner.fonecheck.data.preferences.AppThemeMode
import com.insaner.fonecheck.localization.AppLanguage
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsExposeThemeLanguageWarningsPermissionsLinksAndOnboarding() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var theme: AppThemeMode? = null
        var language by mutableStateOf(AppLanguage.SYSTEM)
        var warnings: Boolean? = null
        var openedSettings = false
        var openedPrivacy = false
        var openedSupport = false
        var openedLicenses = false
        var openedOnboarding = false
        composeRule.setContent {
            FonecheckTheme {
                SettingsScreen(
                    state =
                        SettingsState(
                            preferences = AppPreferences(),
                            reportCount = 2,
                            permissions = SettingsPermissionSnapshot(camera = true),
                            isLoading = false,
                        ),
                    appVersion = "1.0.0 (1)",
                    onThemeMode = { theme = it },
                    selectedLanguage = language,
                    onLanguage = { language = it },
                    onTestWarnings = { warnings = it },
                    onOpenAppSettings = { openedSettings = true },
                    onDeleteAll = {},
                    onOpenPrivacy = { openedPrivacy = true },
                    onOpenSupport = { openedSupport = true },
                    onOpenLicenses = { openedLicenses = true },
                    onReopenOnboarding = { openedOnboarding = true },
                )
            }
        }

        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup))
            .assertCountEquals(2)
        composeRule
            .onNodeWithTag("settings_theme_system")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        composeRule
            .onNodeWithTag("settings_theme_light")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false))
        composeRule
            .onNodeWithTag("settings_theme_dark")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false))
        composeRule.onNodeWithTag("settings_theme_dark").performClick()
        composeRule
            .onNodeWithTag("settings_language_system")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        listOf(AppLanguage.FINNISH, AppLanguage.ENGLISH, AppLanguage.SYSTEM).forEach { choice ->
            composeRule
                .onNodeWithTag("settings_language_${choice.name.lowercase()}")
                .performScrollTo()
                .performClick()
            composeRule.runOnIdle { assertEquals(choice, language) }
            AppLanguage.entries.forEach { option ->
                composeRule
                    .onNodeWithTag("settings_language_${option.name.lowercase()}")
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, option == choice))
            }
        }
        composeRule.onNodeWithTag("settings_test_warnings").performScrollTo().performClick()
        assertEquals(AppThemeMode.DARK, theme)
        assertEquals(false, warnings)
        composeRule
            .onNodeWithText(context.getString(R.string.settings_permission_granted))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("settings_open_app_settings").performScrollTo().performClick()
        composeRule.onNodeWithTag("settings_privacy").performScrollTo().performClick()
        composeRule.onNodeWithTag("settings_support").performScrollTo().performClick()
        composeRule.onNodeWithTag("settings_licenses").performScrollTo().performClick()
        composeRule.onNodeWithTag("settings_onboarding").performScrollTo().performClick()
        assertTrue(openedSettings && openedPrivacy && openedSupport && openedLicenses && openedOnboarding)
    }

    @Test
    fun deleteAllRequiresConfirmation() {
        var deleted = false
        composeRule.setContent {
            FonecheckTheme {
                SettingsScreen(
                    state = SettingsState(reportCount = 3, isLoading = false),
                    appVersion = "1.0.0 (1)",
                    onThemeMode = {},
                    selectedLanguage = AppLanguage.SYSTEM,
                    onLanguage = {},
                    onTestWarnings = {},
                    onOpenAppSettings = {},
                    onDeleteAll = { deleted = true },
                    onOpenPrivacy = {},
                    onOpenSupport = {},
                    onOpenLicenses = {},
                    onReopenOnboarding = {},
                )
            }
        }

        composeRule.onNodeWithTag("settings_delete_all").performScrollTo().performClick()
        assertEquals(false, deleted)
        composeRule.onNodeWithTag("settings_confirm_delete_all").performClick()
        assertTrue(deleted)
    }
}
