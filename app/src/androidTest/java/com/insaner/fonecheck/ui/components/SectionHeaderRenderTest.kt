package com.insaner.fonecheck.ui.components

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.then
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.DarkFonecheckColors
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.LightFonecheckColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale
import kotlin.math.ceil

@RunWith(Parameterized::class)
class SectionHeaderRenderTest(
    private val contentWidthDp: Int,
    private val fontScale: Float,
    private val dark: Boolean,
    private val language: String,
    private val withTrailing: Boolean,
) {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun appearanceKeepsConsistentLayoutFullTextAndOriginalSemantics() {
        val locale = Locale.forLanguageTag(language)
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val context =
            base.createConfigurationContext(
                Configuration(base.resources.configuration).apply { setLocale(locale) },
            )
        val label = context.getString(R.string.settings_appearance)
        // A mixed-case date catches accidental uppercasing of the trailing value.
        val trailing =
            if (withTrailing) {
                if (language == "fi") "11. elok. 2026" else "11 Aug 2026"
            } else {
                null
            }
        var density = 0f
        var actualFontScale = 0f
        var resourceLanguage = ""
        var textLanguage = ""

        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride
                    .Locales(LocaleList(language))
                    .then(DeviceConfigurationOverride.FontScale(fontScale))
                    .then(DeviceConfigurationOverride.ForcedSize(DpSize(contentWidthDp.dp, 200.dp))),
            ) {
                density = LocalDensity.current.density
                actualFontScale = LocalDensity.current.fontScale
                resourceLanguage = LocalConfiguration.current.locales[0].language
                textLanguage = LocalLocale.current.platformLocale.language
                FonecheckTheme(darkTheme = dark) {
                    // No housing inset or side padding: this width is the effective content width.
                    SectionHeader(
                        label = label,
                        trailing = trailing,
                        modifier = Modifier.background(FonecheckTheme.colors.panel).testTag("section-header-fixture"),
                    )
                }
            }
        }

        val root = composeRule.onNodeWithTag("section-header-fixture").fetchSemanticsNode().boundsInRoot
        Log.i(
            "A06aSectionHeader",
            "contentWidthDp=${root.width / density} requestedDp=$contentWidthDp fontScale=$actualFontScale " +
                "density=$density resourceLanguage=$resourceLanguage textLanguage=$textLanguage " +
                "dark=$dark withTrailing=$withTrailing bounds=$root",
        )
        assertEquals(contentWidthDp.toFloat(), root.width / density, 0.5f)
        assertEquals(fontScale, actualFontScale, 0.001f)
        assertEquals(language, resourceLanguage)
        assertEquals(language, textLanguage)
        assertEquals(
            if (withTrailing) 2 else 1,
            composeRule
                .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .size,
        )
        val heading = composeRule.onNodeWithContentDescription(label, useUnmergedTree = true).assertIsDisplayed()
        assertTrue(heading.fetchSemanticsNode().config.contains(SemanticsProperties.Heading))
        assertEquals(listOf(label), heading.fetchSemanticsNode().config[SemanticsProperties.ContentDescription])
        assertLayout(heading, label.uppercase(locale))
        if (trailing != null) {
            val value = composeRule.onNodeWithText(trailing, useUnmergedTree = true).assertIsDisplayed()
            val config = value.fetchSemanticsNode().config
            assertFalse(config.contains(SemanticsProperties.Heading))
            assertEquals(null, config.getOrNull(SemanticsProperties.ContentDescription))
            assertLayout(value, trailing)
            val headingBounds = heading.fetchSemanticsNode().boundsInRoot
            val valueBounds = value.fetchSemanticsNode().boundsInRoot
            if (fontScale > LARGE_FONT_SCALE_THRESHOLD) {
                assertTrue(valueBounds.top >= headingBounds.bottom)
            } else {
                assertTrue(valueBounds.left >= headingBounds.right)
            }
        }
    }

    private fun assertLayout(
        node: SemanticsNodeInteraction,
        text: String,
    ) {
        val layouts = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val layout = layouts.single()
        val paragraph = layout.multiParagraph
        Log.i(
            "A06aSectionHeader",
            "text=$text layoutSize=${layout.size} paragraphSize=${paragraph.width}x${paragraph.height} " +
                "constraints=${layout.layoutInput.constraints} lines=${layout.lineCount} " +
                "overflowWidth=${layout.didOverflowWidth} overflowHeight=${layout.didOverflowHeight} " +
                "visualOverflow=${layout.hasVisualOverflow}",
        )
        val config = node.fetchSemanticsNode().config
        assertEquals(text, config[SemanticsProperties.Text].single().text)
        assertEquals(text, layout.layoutInput.text.text)
        assertTrue(layout.layoutInput.softWrap)
        assertEquals(TextOverflow.Clip, layout.layoutInput.overflow)
        assertEquals(Int.MAX_VALUE, layout.layoutInput.maxLines)
        assertEquals(
            if (dark) DarkFonecheckColors.textMuted else LightFonecheckColors.textMuted,
            layout.layoutInput.style.color,
        )
        assertEquals(ceil(paragraph.width).toInt(), layout.size.width)
        assertEquals(ceil(paragraph.height).toInt(), layout.size.height)
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
        @Parameterized.Parameters(name = "{0}dp scale={1} dark={2} locale={3} trailing={4}")
        fun configurations(): List<Array<Any>> =
            listOf(276 to 1f, 320 to 1f, 276 to 2f, 320 to 2f).flatMap { (width, scale) ->
                listOf(false, true).flatMap { dark ->
                    listOf("en", "fi").flatMap { language ->
                        listOf(false, true).map { trailing -> arrayOf<Any>(width, scale, dark, language, trailing) }
                    }
                }
            }
    }
}
