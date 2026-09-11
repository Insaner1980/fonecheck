package com.insaner.fonecheck.localization

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.intl.LocaleList
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TurkishCasingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displayCasingFollowsUiLocaleAndSpokenLabelsKeepNaturalCase() {
        var tag by mutableStateOf("en")
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.Locales(LocaleList(tag))) {
                FonecheckTheme {
                    Column {
                        SectionHeader(label = "İzin ve ışık", trailing = "i I ı İ")
                        StatusText(text = "Bilgi", tone = SemanticTone.NEUTRAL)
                    }
                }
            }
        }
        (AppLanguage.entries.filter { it != AppLanguage.SYSTEM }.map { it.languageTag } + "tr-TR").forEach { locale ->
            composeRule.runOnIdle { tag = locale }
            val turkish = locale.startsWith("tr")
            composeRule.onNodeWithText(if (turkish) "İZİN VE IŞIK" else "İZIN VE IŞIK").assertExists()
            composeRule.onNodeWithText(if (turkish) "BİLGİ" else "BILGI").assertExists()
            composeRule.onNodeWithContentDescription("İzin ve ışık").assertExists()
            composeRule.onNodeWithContentDescription("Bilgi").assertExists()
            composeRule.onNodeWithText("i I ı İ").assertExists()
        }
    }
}
