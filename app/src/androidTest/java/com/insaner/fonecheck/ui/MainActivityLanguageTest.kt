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

        selectLanguage("fi")
        assertLanguage("fi", "Kieli")
        assertSame(initialActivity, composeRule.activity)

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
