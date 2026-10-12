package com.insaner.fonecheck.ui.components

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.then
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

@RunWith(Parameterized::class)
class LongValueRowRenderTest(
    private val widthDp: Int,
    private val fontScale: Float,
    private val dark: Boolean,
    private val language: String,
) {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun multilineValueKeepsBothLinesAndShortAndNullValuesKeepDataRowSemantics() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val context =
            base.createConfigurationContext(
                Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag(language)) },
            )
        val label = context.getString(R.string.label_baseband)
        val value = "radio-one\nradio-two"
        val shortValue = "Pixel"
        val unavailable = context.getString(R.string.value_unavailable_short)
        var density = 0f
        var actualFontScale = 0f
        var actualLanguage = ""

        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride
                    .Locales(LocaleList(language))
                    .then(DeviceConfigurationOverride.FontScale(fontScale))
                    .then(DeviceConfigurationOverride.ForcedSize(DpSize(widthDp.dp, 400.dp))),
            ) {
                density = LocalDensity.current.density
                actualFontScale = LocalDensity.current.fontScale
                actualLanguage = LocalConfiguration.current.locales[0].language
                FonecheckTheme(darkTheme = dark) {
                    Column(Modifier.fillMaxWidth().testTag("long-value-row-fixture")) {
                        LongValueRow(label = label, value = value)
                        LongValueRow(label = context.getString(R.string.label_model), value = shortValue)
                        LongValueRow(label = context.getString(R.string.label_serial_number), value = null)
                    }
                }
            }
        }

        val root = composeRule.onNodeWithTag("long-value-row-fixture").fetchSemanticsNode().boundsInRoot
        assertEquals(widthDp.toFloat(), root.width / density, 0.5f)
        assertEquals(fontScale, actualFontScale, 0.001f)
        assertEquals(language, actualLanguage)
        Log.i(
            "A05LongValueRow",
            "widthDp=${root.width / density} fontScale=$actualFontScale density=$density " +
                "language=$actualLanguage dark=$dark bounds=$root",
        )

        val labelNode = composeRule.onNodeWithContentDescription(label, useUnmergedTree = true).assertIsDisplayed()
        val valueNode = composeRule.onNodeWithContentDescription(value, useUnmergedTree = true).assertIsDisplayed()
        assertTrue(
            valueNode.fetchSemanticsNode().boundsInRoot.top >= labelNode.fetchSemanticsNode().boundsInRoot.bottom,
        )
        val layouts = mutableListOf<TextLayoutResult>()
        valueNode.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val layout = layouts.single()
        assertEquals(
            value,
            layout.layoutInput.text.text
                .replace("\u200B", ""),
        )
        assertEquals(2, layout.lineCount)
        assertFalse(layout.hasVisualOverflow)
        for (line in 0 until layout.lineCount) {
            assertFalse(layout.isLineEllipsized(line))
        }
        assertEquals(layout.layoutInput.text.length, layout.getLineEnd(layout.lineCount - 1, visibleEnd = true))

        composeRule.onNodeWithText(shortValue, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(shortValue, useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(unavailable, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(unavailable, useUnmergedTree = true).assertDoesNotExist()
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}dp scale={1} dark={2} locale={3}")
        fun configurations(): List<Array<Any>> =
            listOf(380 to 1f, 320 to 1f, 380 to 2f, 320 to 2f).flatMap { (width, scale) ->
                listOf(false, true).flatMap { dark ->
                    listOf("en", "fi").map { language -> arrayOf<Any>(width, scale, dark, language) }
                }
            }
    }
}
