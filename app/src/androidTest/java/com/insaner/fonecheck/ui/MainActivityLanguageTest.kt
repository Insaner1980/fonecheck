package com.insaner.fonecheck.ui

import android.content.res.Configuration
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.app.LocaleManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.R
import com.insaner.fonecheck.export.ReportPdfRenderer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class MainActivityLanguageTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private var originalLocales = LocaleListCompat.getEmptyLocaleList()

    @Before
    fun rememberLanguage() {
        composeRule.activityRule.scenario.onActivity {
            originalLocales = AppCompatDelegate.getApplicationLocales()
        }
    }

    @After
    fun restoreLanguage() {
        composeRule.activityRule.scenario.onActivity {
            AppCompatDelegate.setApplicationLocales(originalLocales)
        }
        composeRule.waitForIdle()
    }

    @Test
    fun languageUpdatesInPlaceSurvivesRecreationAndResetsToSystem() {
        val initialActivity = composeRule.activity
        selectLanguage("en")
        assertLanguage("en", "Language")
        assertSame(initialActivity, composeRule.activity)

        selectLanguage("es")
        assertLanguage("es", "Idioma")
        assertSame(initialActivity, composeRule.activity)

        selectLanguage("pt-BR")
        assertLanguage("pt-BR", "Idioma")
        assertEquals("Verificação completa", composeRule.activity.getString(R.string.full_check_title))
        assertSame(initialActivity, composeRule.activity)

        selectLanguage("de")
        assertLanguage("de", "Sprache")
        assertEquals("Gesamtcheck", composeRule.activity.getString(R.string.full_check_title))
        assertSame(initialActivity, composeRule.activity)

        selectLanguage("fi")
        assertLanguage("fi", "Kieli")
        assertSame(initialActivity, composeRule.activity)

        selectLanguage("fr")
        assertLanguage("fr", "Langue")
        assertEquals("Vérification complète", composeRule.activity.getString(R.string.full_check_title))
        assertSame(initialActivity, composeRule.activity)

        selectLanguage("fi")

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        assertLanguage("fi", "Kieli")

        val recreatedActivity = composeRule.activity
        selectLanguage("")
        composeRule.activityRule.scenario.onActivity { activity ->
            assertSame(recreatedActivity, activity)
            assertTrue(AppCompatDelegate.getApplicationLocales().isEmpty)
            val systemConfiguration =
                Configuration(activity.resources.configuration).apply {
                    setLocales(
                        LocaleList.forLanguageTags(LocaleManagerCompat.getSystemLocales(activity).toLanguageTags()),
                    )
                }
            val expected =
                activity.createConfigurationContext(systemConfiguration).getString(R.string.settings_language)
            assertEquals(expected, activity.getString(R.string.settings_language))
        }
    }

    private fun selectLanguage(languageTag: String) {
        composeRule.activityRule.scenario.onActivity {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
        }
        composeRule.waitForIdle()
    }

    @Test
    fun spanishRegionsUpdateExistingPdfRendererAndSurviveRecreation() {
        selectLanguage("en")
        assertLanguage("en", "Language")
        val renderer = ReportPdfRenderer(composeRule.activity.applicationContext)
        assertEquals("fonecheck diagnostic report", renderer.labels().title)
        listOf("es", "es-ES", "es-MX").forEach { tag ->
            selectLanguage(tag)
            assertLanguage(tag, "Idioma")
            assertEquals("Informe de diagnóstico de fonecheck", renderer.labels().title)
            assertEquals("-12,5", renderer.labels().numberValue(-12.5))
        }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        assertLanguage("es-MX", "Idioma")
        assertFinnishAndEnglishPdfLabels(renderer)
    }

    @Test
    fun brazilianSelectionRefreshesExistingPdfRendererAndSurvivesRecreation() {
        selectLanguage("en")
        val renderer = ReportPdfRenderer(composeRule.activity.applicationContext)
        assertEquals("fonecheck diagnostic report", renderer.labels().title)
        selectLanguage("pt-BR")
        assertLanguage("pt-BR", "Idioma")
        assertEquals("Relatório de diagnóstico do fonecheck", renderer.labels().title)
        assertEquals("-1.234,5", renderer.labels().numberValue(-1234.5))
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        assertLanguage("pt-BR", "Idioma")
        assertEquals("Relatório de diagnóstico do fonecheck", renderer.labels().title)
        selectLanguage("es")
        assertLanguage("es", "Idioma")
        assertEquals("Informe de diagnóstico de fonecheck", renderer.labels().title)
        assertFinnishAndEnglishPdfLabels(renderer)
        assertEquals("12.5", renderer.labels().numberValue(12.5))
    }

    @Test
    fun germanSelectionRefreshesExistingPdfRendererAndSurvivesRecreation() {
        selectLanguage("en")
        val renderer = ReportPdfRenderer(composeRule.activity.applicationContext)
        assertEquals("fonecheck diagnostic report", renderer.labels().title)
        selectLanguage("de")
        assertLanguage("de", "Sprache")
        assertEquals("fonecheck-Diagnosebericht", renderer.labels().title)
        assertEquals("-1.234,5", renderer.labels().numberValue(-1234.5))
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        assertLanguage("de", "Sprache")
        assertEquals("fonecheck-Diagnosebericht", renderer.labels().title)
        assertFinnishAndEnglishPdfLabels(renderer)
    }

    @Test
    fun frenchRegionsRefreshExistingPdfRendererAndSurviveRecreation() {
        selectLanguage("en")
        val renderer = ReportPdfRenderer(composeRule.activity.applicationContext)
        assertEquals("fonecheck diagnostic report", renderer.labels().title)
        listOf("fr", "fr-FR", "fr-CA").forEach { tag ->
            selectLanguage(tag)
            assertLanguage(tag, "Langue")
            assertEquals("Rapport de diagnostic fonecheck", renderer.labels().title)
            assertEquals("-12,5", renderer.labels().numberValue(-12.5))
        }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        assertLanguage("fr-CA", "Langue")
        assertEquals("Rapport de diagnostic fonecheck", renderer.labels().title)
        assertFinnishAndEnglishPdfLabels(renderer)
    }

    @Test
    fun indonesianSelectionRefreshesExistingPdfRendererAndSurvivesRecreation() {
        selectLanguage("en")
        val renderer = ReportPdfRenderer(composeRule.activity.applicationContext)
        assertEquals("fonecheck diagnostic report", renderer.labels().title)
        listOf("id", "id-ID").forEach { tag ->
            selectLanguage(tag)
            assertLanguage(tag, "Bahasa")
            assertEquals("Pemeriksaan lengkap", composeRule.activity.getString(R.string.full_check_title))
            assertEquals("Laporan diagnostik fonecheck", renderer.labels().title)
            assertEquals("-1.234,5", renderer.labels().numberValue(-1234.5))
        }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        assertLanguage("id-ID", "Bahasa")
        assertEquals("Laporan diagnostik fonecheck", renderer.labels().title)
        assertFinnishAndEnglishPdfLabels(renderer)
        assertSystemLanguageRestored()
    }

    @Test
    fun swedishRegionsRefreshExistingPdfRendererAndSurviveRecreation() {
        selectLanguage("en")
        val renderer = ReportPdfRenderer(composeRule.activity.applicationContext)
        assertEquals("fonecheck diagnostic report", renderer.labels().title)
        listOf("sv", "sv-SE", "sv-FI").forEach { tag ->
            selectLanguage(tag)
            assertLanguage(tag, "Språk")
            assertEquals("Fullständig kontroll", composeRule.activity.getString(R.string.full_check_title))
            assertEquals("fonecheck diagnostikrapport", renderer.labels().title)
            assertEquals("12,5", renderer.labels().numberValue(12.5))
        }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        assertLanguage("sv-FI", "Språk")
        assertEquals("fonecheck diagnostikrapport", renderer.labels().title)
        assertFinnishAndEnglishPdfLabels(renderer)
        assertSystemLanguageRestored()
    }

    @Test
    fun bokmalSelectionRefreshesExistingPdfRendererAndSurvivesRecreation() {
        selectLanguage("en")
        val renderer = ReportPdfRenderer(composeRule.activity.applicationContext)
        assertEquals("fonecheck diagnostic report", renderer.labels().title)
        listOf("nb", "nb-NO").forEach { tag ->
            selectLanguage(tag)
            assertLanguage(tag, "Språk")
            assertEquals("Fullstendig sjekk", composeRule.activity.getString(R.string.full_check_title))
            assertEquals("Diagnostikkrapport fra fonecheck", renderer.labels().title)
            assertEquals("12,5", renderer.labels().numberValue(12.5))
        }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        assertLanguage("nb-NO", "Språk")
        assertEquals("Diagnostikkrapport fra fonecheck", renderer.labels().title)
        selectLanguage("sv")
        assertLanguage("sv", "Språk")
        assertEquals("fonecheck diagnostikrapport", renderer.labels().title)
        assertFinnishAndEnglishPdfLabels(renderer)
        assertSystemLanguageRestored()
    }

    @Test
    fun danishSwitchingRefreshesAnExistingPdfRendererAndSurvivesRecreation() {
        selectLanguage("nb")
        val renderer = ReportPdfRenderer(composeRule.activity.applicationContext)
        assertEquals("Diagnostikkrapport fra fonecheck", renderer.labels().title)
        listOf("da", "da-DK").forEach { tag ->
            selectLanguage(tag)
            assertLanguage(tag, "Sprog")
            assertEquals("Fuld kontrol", composeRule.activity.getString(R.string.full_check_title))
            assertEquals("Testrapport fra fonecheck", renderer.labels().title)
            assertEquals("-1.234,5", renderer.labels().numberValue(-1234.5))
            composeRule.activityRule.scenario.recreate()
            composeRule.waitForIdle()
            assertLanguage(tag, "Sprog")
            assertEquals("Testrapport fra fonecheck", renderer.labels().title)
        }
        assertFinnishAndEnglishPdfLabels(renderer)
        assertSystemLanguageRestored()
    }

    @Test
    fun italianSwitchingRefreshesAnExistingPdfRendererAndSurvivesRecreation() {
        selectLanguage("nb")
        val renderer = ReportPdfRenderer(composeRule.activity.applicationContext)
        assertEquals("Diagnostikkrapport fra fonecheck", renderer.labels().title)
        listOf("it", "it-IT", "it-CH").forEach { tag ->
            selectLanguage(tag)
            assertLanguage(tag, "Lingua")
            assertEquals("Controllo completo", composeRule.activity.getString(R.string.full_check_title))
            assertEquals("Rapporto diagnostico fonecheck", renderer.labels().title)
            assertEquals("-1.234,5", renderer.labels().numberValue(-1234.5))
            composeRule.activityRule.scenario.recreate()
            composeRule.waitForIdle()
            assertLanguage(tag, "Lingua")
            assertEquals("Rapporto diagnostico fonecheck", renderer.labels().title)
        }
        assertFinnishAndEnglishPdfLabels(renderer)
        assertSystemLanguageRestored()
    }

    private fun assertSystemLanguageRestored() {
        selectLanguage("")
        composeRule.activityRule.scenario.onActivity { activity ->
            assertTrue(AppCompatDelegate.getApplicationLocales().isEmpty)
            val configuration =
                Configuration(activity.resources.configuration).apply {
                    setLocales(
                        LocaleList.forLanguageTags(LocaleManagerCompat.getSystemLocales(activity).toLanguageTags()),
                    )
                }
            assertEquals(
                activity.createConfigurationContext(configuration).getString(R.string.settings_language),
                activity.getString(R.string.settings_language),
            )
        }
    }

    private fun assertFinnishAndEnglishPdfLabels(renderer: ReportPdfRenderer) {
        selectLanguage("fi")
        assertLanguage("fi", "Kieli")
        assertEquals("12,5", renderer.labels().numberValue(12.5))
        selectLanguage("en")
        assertLanguage("en", "Language")
        assertEquals("fonecheck diagnostic report", renderer.labels().title)
    }

    private fun assertLanguage(
        languageTag: String,
        expectedLabel: String,
    ) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            val activity = composeRule.activity
            activity.getString(R.string.settings_language) == expectedLabel &&
                activity.resources.configuration.locales[0]
                    .language == Locale.forLanguageTag(languageTag).language &&
                ContextCompat.getString(activity.applicationContext, R.string.settings_language) == expectedLabel
        }
        composeRule.activityRule.scenario.onActivity { activity ->
            assertEquals(languageTag, AppCompatDelegate.getApplicationLocales().toLanguageTags())
            assertEquals(expectedLabel, activity.getString(R.string.settings_language))
            assertEquals(
                expectedLabel,
                ContextCompat.getString(activity.applicationContext, R.string.settings_language),
            )
        }
    }
}
