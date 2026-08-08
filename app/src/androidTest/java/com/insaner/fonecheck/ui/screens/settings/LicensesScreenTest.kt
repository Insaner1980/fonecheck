package com.insaner.fonecheck.ui.screens.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LicensesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bundledNoticesAreVisible() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            FonecheckTheme { LicensesScreen() }
        }

        composeRule.onNodeWithText(context.getString(R.string.licenses_notices_heading)).assertIsDisplayed()
        composeRule.onNodeWithText("Apache License", substring = true).assertExists()
    }
}
