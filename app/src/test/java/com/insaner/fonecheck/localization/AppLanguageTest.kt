package com.insaner.fonecheck.localization

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AppLanguageTest {
    @Test
    fun frenchRegionsUseOneGenericLanguageChoice() {
        assertEquals("fr", AppLanguage.FRENCH.languageTag)
        listOf("fr", "fr-FR", "fr-BE", "fr-CH", "fr-CA").forEach { tag ->
            assertEquals(AppLanguage.FRENCH, AppLanguage.fromLocale(Locale.forLanguageTag(tag)))
        }
    }

    @Test
    fun germanRegionsUseOneGenericLanguageChoice() {
        assertEquals("de", AppLanguage.GERMAN.languageTag)
        listOf("de", "de-DE", "de-AT", "de-CH").forEach { tag ->
            assertEquals(AppLanguage.GERMAN, AppLanguage.fromLocale(Locale.forLanguageTag(tag)))
        }
    }

    @Test
    fun brazilianPortugueseKeepsItsRegionWithoutAdvertisingOtherPortugueseLocales() {
        assertEquals("pt-BR", AppLanguage.PORTUGUESE_BRAZIL.languageTag)
        assertEquals(AppLanguage.PORTUGUESE_BRAZIL, AppLanguage.fromLocale(Locale.forLanguageTag("pt-BR")))
        listOf("pt", "pt-PT").forEach { tag ->
            assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLocale(Locale.forLanguageTag(tag)))
        }
    }

    @Test
    fun spanishRegionsUseTheSameLanguageChoice() {
        listOf("es", "es-ES", "es-MX").forEach { tag ->
            assertEquals(AppLanguage.SPANISH, AppLanguage.fromLocale(Locale.forLanguageTag(tag)))
        }
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLocale(Locale.ENGLISH))
        assertEquals(AppLanguage.FINNISH, AppLanguage.fromLocale(Locale.forLanguageTag("fi")))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLocale(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLocale(Locale.ITALIAN))
    }
}
