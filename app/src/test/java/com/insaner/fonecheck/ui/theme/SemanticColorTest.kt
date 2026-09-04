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
    fun `every text role meets normal text contrast against its own panel`() {
        bothThemes().forEach { colors ->
            textRolesOf(colors).forEach { (name, color) ->
                val ratio = contrast(color, colors.panel)
                assertTrue(
                    "$name is $ratio:1 against its panel, below the 4.5:1 minimum",
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
    fun `primary action accent preserves its colours and text contrast`() {
        bothThemes().forEach { colors ->
            assertEquals(Color(0xFFCF4F24), colors.primaryActionBackground)
            assertEquals(Color(0xFF0F0400), colors.primaryActionContent)
            val ratio = contrast(colors.primaryActionContent, colors.primaryActionBackground)
            assertTrue("Primary action is $ratio:1", ratio >= MINIMUM_TEXT_CONTRAST)
        }
    }

    @Test
    fun `outlined control boundaries stay visible on every app surface`() {
        bothThemes().forEach { colors ->
            listOf(
                "background" to colors.background,
                "panel" to colors.panel,
            ).forEach { (surfaceName, surface) ->
                val ratio = contrast(colors.textMuted, surface)
                assertTrue("Outline on $surfaceName is $ratio:1", ratio >= MINIMUM_BOUNDARY_CONTRAST)
            }
        }
    }

    @Test
    fun `instrument text stays readable on its own surface`() {
        bothThemes().forEach { colors ->
            listOf(
                "window" to contrast(colors.windowText, colors.windowBg),
                "window dim" to contrast(colors.windowDim, colors.windowBg),
                "window alert" to contrast(colors.windowAlert, colors.windowBg),
                "state key" to contrast(colors.textPrimary, colors.panel),
                "pass lamp" to contrast(colors.lampPassInk, colors.lampPass),
                "noted lamp" to contrast(colors.lampNotedInk, colors.lampNoted),
                "info lamp" to contrast(colors.lampInfoInk, colors.lampInfo),
                "unlit lamp" to contrast(colors.lampUnlitInk, colors.lampUnlit),
            ).forEach { (name, ratio) ->
                assertTrue("Instrument $name is $ratio:1", ratio >= MINIMUM_TEXT_CONTRAST)
            }
        }
    }

    @Test
    fun `fault lamp glyph stays visible against its red fill`() {
        bothThemes().forEach { colors ->
            assertEquals(Color(0xFFD32F2F), colors.lampFault)
            assertEquals(Color(0xFF0F0400), colors.lampFaultInk)
            val ratio = contrast(colors.lampFaultInk, colors.lampFault)
            assertTrue("Fault lamp glyph is $ratio:1", ratio >= MINIMUM_BOUNDARY_CONTRAST)
        }
    }

    @Test
    fun `row state glyphs remain visible against their panel`() {
        bothThemes().forEach { colors ->
            listOf(
                "pass" to colors.rowPass,
                "fault" to colors.rowFault,
                "noted" to colors.rowNoted,
                "unlit" to colors.rowUnlit,
            ).forEach { (name, glyph) ->
                val ratio = contrast(glyph, colors.panel)
                assertTrue("Instrument $name glyph is $ratio:1", ratio >= MINIMUM_BOUNDARY_CONTRAST)
            }
        }
    }

    @Test
    fun `panel stays lighter than its readout window`() {
        bothThemes().forEach { colors ->
            assertTrue(colors.panel.luminance() > colors.windowBg.luminance())
        }
    }

    @Test
    fun `chromatic lamps stay fixed while neutral lamp material follows the palette`() {
        assertEquals(LightFonecheckColors.lampPass, DarkFonecheckColors.lampPass)
        assertEquals(LightFonecheckColors.lampPassInk, DarkFonecheckColors.lampPassInk)
        assertEquals(LightFonecheckColors.lampFault, DarkFonecheckColors.lampFault)
        assertEquals(LightFonecheckColors.lampFaultInk, DarkFonecheckColors.lampFaultInk)
        assertEquals(LightFonecheckColors.lampNoted, DarkFonecheckColors.lampNoted)
        assertEquals(LightFonecheckColors.lampNotedInk, DarkFonecheckColors.lampNotedInk)
        assertTrue(LightFonecheckColors.lampInfo != DarkFonecheckColors.lampInfo)
        assertEquals(LightFonecheckColors.lampInfoInk, DarkFonecheckColors.lampInfoInk)
        assertTrue(LightFonecheckColors.lampUnlit != DarkFonecheckColors.lampUnlit)
        assertTrue(LightFonecheckColors.lampUnlitInk != DarkFonecheckColors.lampUnlitInk)
    }

    @Test
    fun `the strong rule is the primary ink of its own ramp`() {
        bothThemes().forEach { colors ->
            assertEquals(colors.textPrimary, colors.ruleStrong)
        }
    }

    @Test
    fun `dark interaction chrome is monochrome while status colours remain semantic`() {
        assertEquals(InkDark, DarkFonecheckColors.primaryButtonBackground)
        assertEquals(PaperDark, DarkFonecheckColors.primaryButtonContent)
        assertEquals(PassDark, DarkFonecheckColors.pass)
        assertEquals(AttentionDark, DarkFonecheckColors.attention)
        assertEquals(AttentionFillDark, DarkFonecheckColors.attentionFill)
        assertEquals(FailDark, DarkFonecheckColors.fail)
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
