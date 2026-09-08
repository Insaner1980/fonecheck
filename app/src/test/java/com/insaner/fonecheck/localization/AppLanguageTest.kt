package com.insaner.fonecheck.localization

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AppLanguageTest {
    @Test
    fun spanishRegionsUseTheSameLanguageChoice() {
        listOf("es", "es-ES", "es-MX").forEach { tag ->
            assertEquals(AppLanguage.SPANISH, AppLanguage.fromLocale(Locale.forLanguageTag(tag)))
        }
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLocale(Locale.ENGLISH))
        assertEquals(AppLanguage.FINNISH, AppLanguage.fromLocale(Locale.forLanguageTag("fi")))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLocale(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLocale(Locale.FRENCH))
    }
}
