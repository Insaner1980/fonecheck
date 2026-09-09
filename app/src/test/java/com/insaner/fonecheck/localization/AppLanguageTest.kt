package com.insaner.fonecheck.localization

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AppLanguageTest {
    @Test
    fun italianRegionsUseOneGenericLanguageChoice() {
        assertEquals("it", AppLanguage.ITALIAN.languageTag)
        listOf("it", "it-IT", "it-CH").forEach { tag ->
            assertEquals(AppLanguage.ITALIAN, AppLanguage.fromLocale(Locale.forLanguageTag(tag)))
        }
    }

    @Test
    fun danishAndDenmarkUseOneGenericLanguageChoice() {
        assertEquals("da", AppLanguage.DANISH.languageTag)
        listOf("da", "da-DK").forEach { tag ->
            assertEquals(AppLanguage.DANISH, AppLanguage.fromLocale(Locale.forLanguageTag(tag)))
        }
    }

    @Test
    fun bokmalUsesItsModernTagWithoutClaimingGenericNorwegianOrNynorsk() {
        assertEquals("nb", AppLanguage.NORWEGIAN_BOKMAL.languageTag)
        listOf("nb", "nb-NO").forEach { tag ->
            assertEquals(AppLanguage.NORWEGIAN_BOKMAL, AppLanguage.fromLocale(Locale.forLanguageTag(tag)))
        }
        listOf("no", "no-NO", "nn", "nn-NO").forEach { tag ->
            assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLocale(Locale.forLanguageTag(tag)))
        }
    }

    @Test
    fun swedishRegionsUseOneGenericLanguageChoice() {
        assertEquals("sv", AppLanguage.SWEDISH.languageTag)
        listOf("sv", "sv-SE", "sv-FI").forEach { tag ->
            assertEquals(AppLanguage.SWEDISH, AppLanguage.fromLocale(Locale.forLanguageTag(tag)))
        }
    }

    @Test
    fun indonesianModernAndLegacyRegionalLocalesUseOneLanguageChoice() {
        assertEquals("id", AppLanguage.INDONESIAN.languageTag)
        listOf("id", "id-ID", "id-SG", "in", "in-ID").forEach { tag ->
            assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromLocale(Locale.forLanguageTag(tag)))
        }
    }

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
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLocale(Locale.JAPANESE))
    }
}
