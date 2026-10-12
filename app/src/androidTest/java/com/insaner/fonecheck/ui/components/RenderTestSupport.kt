package com.insaner.fonecheck.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.Locales
import androidx.compose.ui.test.then
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.DpSize
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.math.ceil

/** Every width and font scale pair, in light and dark, in English and Finnish. */
internal fun renderConfigurations(vararg sizes: Pair<Int, Float>): List<Array<Any>> =
    sizes.toList().flatMap { (width, scale) ->
        listOf(false, true).flatMap { dark ->
            listOf("en", "fi").map { language -> arrayOf<Any>(width, scale, dark, language) }
        }
    }

/** The environment a [ThemedRenderFixture] actually composed in, read back for assertions. */
internal class RenderEnvironment {
    var density = 0f
    var fontScale = 0f
    var resourceLanguage = ""
    var textLanguage = ""
}

/** Composes [content] in the app theme under a forced locale, font scale and size. */
@Composable
internal fun ThemedRenderFixture(
    language: String,
    fontScale: Float,
    size: DpSize,
    dark: Boolean,
    environment: RenderEnvironment,
    content: @Composable () -> Unit,
) {
    DeviceConfigurationOverride(
        DeviceConfigurationOverride
            .Locales(LocaleList(language))
            .then(DeviceConfigurationOverride.FontScale(fontScale))
            .then(DeviceConfigurationOverride.ForcedSize(size)),
    ) {
        environment.density = LocalDensity.current.density
        environment.fontScale = LocalDensity.current.fontScale
        environment.resourceLanguage = LocalConfiguration.current.locales[0].language
        environment.textLanguage = LocalLocale.current.platformLocale.language
        FonecheckTheme(darkTheme = dark) {
            content()
        }
    }
}

/**
 * Asserts that [layout] holds all of [text] at its measured size: nothing overflows or is
 * ellipsized, the lines cover the text without gaps, and every character lies inside the layout.
 */
internal fun assertTextFitsLayout(
    layout: TextLayoutResult,
    text: String,
) {
    val paragraph = layout.multiParagraph
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
