package com.insaner.fonecheck.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticColorTest {
    @Test
    fun `every text role meets normal text contrast against its own background`() {
        bothThemes().forEach { colors ->
            textRolesOf(colors).forEach { (name, color) ->
                val ratio = contrast(color, colors.background)
                assertTrue(
                    "$name is $ratio:1 against its background, below the 4.5:1 minimum",
                    ratio >= MINIMUM_TEXT_CONTRAST,
                )
            }
        }
    }

    @Test
    fun `primary button content meets normal text contrast against the button`() {
        bothThemes().forEach { colors ->
            val ratio = contrast(colors.primaryButtonContent, colors.primaryButtonBackground)
            assertTrue("Primary button is $ratio:1", ratio >= MINIMUM_TEXT_CONTRAST)
        }
    }

    @Test
    fun `secondary button outline stays visible as a control boundary`() {
        bothThemes().forEach { colors ->
            val ratio = contrast(colors.textMuted, colors.background)
            assertTrue("Outline is $ratio:1", ratio >= MINIMUM_BOUNDARY_CONTRAST)
        }
    }

    @Test
    fun `the strong rule is the primary ink of its own ramp`() {
        bothThemes().forEach { colors ->
            assertEquals(colors.textPrimary, colors.ruleStrong)
        }
    }

    private fun bothThemes() = listOf(LightFonecheckColors, DarkFonecheckColors)

    private fun textRolesOf(colors: FonecheckColors) =
        listOf(
            "textPrimary" to colors.textPrimary,
            "textSecondary" to colors.textSecondary,
            "textMuted" to colors.textMuted,
            "pass" to colors.pass,
            "attention" to colors.attention,
            "fail" to colors.fail,
        )

    private fun contrast(
        foreground: Color,
        background: Color,
    ): Float {
        val lighter = maxOf(foreground.luminance(), background.luminance())
        val darker = minOf(foreground.luminance(), background.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private companion object {
        const val MINIMUM_TEXT_CONTRAST = 4.5f
        const val MINIMUM_BOUNDARY_CONTRAST = 3f
    }
}
