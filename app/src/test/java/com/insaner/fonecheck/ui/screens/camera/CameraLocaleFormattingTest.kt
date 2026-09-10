package com.insaner.fonecheck.ui.screens.camera

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CameraLocaleFormattingTest {
    @Test
    fun `megapixels use the requested locale decimal separator`() {
        assertEquals("12.0 MP", formatCameraMegapixels(12_000_000, Locale.US))
        assertEquals("12,0 MP", formatCameraMegapixels(12_000_000, Locale.forLanguageTag("fi-FI")))
        assertEquals("12,0 MP", formatCameraMegapixels(12_000_000, Locale.forLanguageTag("pt-BR")))
        listOf("de", "de-DE", "de-AT", "de-CH", "fr", "fr-FR", "fr-BE", "fr-CH", "fr-CA").forEach { tag ->
            assertEquals("12,0 MP", formatCameraMegapixels(12_000_000, Locale.forLanguageTag(tag)))
        }
        listOf(
            "id",
            "id-ID",
            "in-ID",
            "sv",
            "sv-SE",
            "sv-FI",
            "nb",
            "nb-NO",
            "da",
            "da-DK",
            "it",
            "it-IT",
            "it-CH",
            "pl",
            "pl-PL",
        ).forEach { tag ->
            assertEquals("12,0 MP", formatCameraMegapixels(12_000_000, Locale.forLanguageTag(tag)))
        }
    }
}
