package com.insaner.fonecheck.ui.components

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
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
        val environment = RenderEnvironment()

        composeRule.setContent {
            ThemedRenderFixture(language, fontScale, DpSize(contentWidthDp.dp, 200.dp), dark, environment) {
                // No housing inset or side padding: this width is the effective content width.
                SectionHeader(
                    label = label,
                    trailing = trailing,
                    modifier = Modifier.background(FonecheckTheme.colors.panel).testTag("section-header-fixture"),
                )
            }
        }

        val density = environment.density
        val root = composeRule.onNodeWithTag("section-header-fixture").fetchSemanticsNode().boundsInRoot
        Log.i(
            "A06aSectionHeader",
            "contentWidthDp=${root.width / density} requestedDp=$contentWidthDp fontScale=${environment.fontScale} " +
                "density=$density resourceLanguage=${environment.resourceLanguage} " +
                "textLanguage=${environment.textLanguage} dark=$dark withTrailing=$withTrailing bounds=$root",
        )
        assertEquals(contentWidthDp.toFloat(), root.width / density, 0.5f)
        assertEquals(fontScale, environment.fontScale, 0.001f)
        assertEquals(language, environment.resourceLanguage)
        assertEquals(language, environment.textLanguage)
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
        assertTextFitsLayout(layout, text)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}dp scale={1} dark={2} locale={3} trailing={4}")
        fun configurations(): List<Array<Any>> =
            renderConfigurations(276 to 1f, 320 to 1f, 276 to 2f, 320 to 2f).flatMap { base ->
                listOf(false, true).map { trailing -> arrayOf<Any>(*base, trailing) }
            }
    }
}
