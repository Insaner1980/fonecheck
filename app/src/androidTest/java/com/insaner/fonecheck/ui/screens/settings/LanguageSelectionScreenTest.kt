package com.insaner.fonecheck.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.localization.AppLanguage
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageSelectionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun languageRowsExposeOneSelectionAndChangeIt() {
        var selectedLanguage by mutableStateOf(AppLanguage.SYSTEM)
        composeRule.setContent {
            FonecheckTheme {
                LanguageSelectionScreen(
                    selectedLanguage = selectedLanguage,
                    onLanguage = { selectedLanguage = it },
                )
            }
        }

        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup))
            .assertCountEquals(1)
        composeRule
            .onNodeWithTag("settings_language_system")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))

        composeRule.onNodeWithTag("settings_language_german").performClick()

        composeRule.runOnIdle { assertEquals(AppLanguage.GERMAN, selectedLanguage) }
        AppLanguage.entries.forEach { language ->
            composeRule
                .onNodeWithTag("settings_language_${language.name.lowercase()}")
                .assert(
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.Selected,
                        language == AppLanguage.GERMAN,
                    ),
                )
        }
        composeRule.onNodeWithTag("settings_language_french").performClick()
        composeRule.runOnIdle { assertEquals(AppLanguage.FRENCH, selectedLanguage) }
        composeRule
            .onNodeWithTag("settings_language_french")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        composeRule
            .onNodeWithTag("settings_language_german")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false))
    }
}
