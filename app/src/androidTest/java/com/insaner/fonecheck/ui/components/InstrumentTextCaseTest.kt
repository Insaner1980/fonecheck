package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstrumentTextCaseTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun constrainedInstrumentLabelsPreserveCallerProvidedCase() {
        composeRule.setContent {
            FonecheckTheme {
                Column {
                    WindowLabel("Current reading")
                    WindowUnit("mAh")
                    WindowRow(label = "Sensor axis", value = "1")
                    InstrumentActionButton(label = "Start full check", onClick = {})
                    DisclosureHeader(
                        label = "Battery details",
                        summary = "Ready",
                        expanded = false,
                        onClick = {},
                    )
                }
            }
        }

        listOf(
            "Current reading",
            "mAh",
            "Sensor axis",
            "Start full check",
            "Battery details",
        ).forEach { label ->
            composeRule.onNodeWithText(label, useUnmergedTree = true).assertIsDisplayed()
        }
    }
}
