package com.insaner.fonecheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FonecheckThemeSmokeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun themeContentIsDisplayed() {
        composeRule.setContent {
            FonecheckTheme {
                Text("Theme smoke test")
            }
        }

        composeRule.onNodeWithText("Theme smoke test").assertIsDisplayed()
    }

    @Test
    fun darkThemeProjectsMonochromeInteractionRolesOntoMaterialScheme() {
        var materialPrimary = Color.Unspecified
        var materialOnPrimary = Color.Unspecified
        var materialSecondary = Color.Unspecified
        var materialTertiary = Color.Unspecified

        composeRule.setContent {
            FonecheckTheme(darkTheme = true) {
                materialPrimary = MaterialTheme.colorScheme.primary
                materialOnPrimary = MaterialTheme.colorScheme.onPrimary
                materialSecondary = MaterialTheme.colorScheme.secondary
                materialTertiary = MaterialTheme.colorScheme.tertiary
            }
        }

        assertEquals(InkDark, materialPrimary)
        assertEquals(PaperDark, materialOnPrimary)
        assertEquals(InkDark2, materialSecondary)
        assertEquals(InkDark3, materialTertiary)
    }
}
