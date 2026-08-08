package com.insaner.fonecheck.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticColorTest {
    @Test
    fun `semantic text colors meet normal text contrast in both themes`() {
        listOf(Green400, Yellow400, Red400, Aqua80).forEach { color ->
            assertTrue(contrast(statusColorForTheme(color, lightTheme = true), Color.White) >= 4.5f)
            assertTrue(contrast(statusColorForTheme(color, lightTheme = false), Neutral850) >= 4.5f)
        }
    }

    private fun contrast(
        foreground: Color,
        background: Color,
    ): Float {
        val lighter = maxOf(foreground.luminance(), background.luminance())
        val darker = minOf(foreground.luminance(), background.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}
