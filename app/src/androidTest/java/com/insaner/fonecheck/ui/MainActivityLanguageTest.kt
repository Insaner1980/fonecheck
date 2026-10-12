package com.insaner.fonecheck.ui

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.LocaleList
import android.os.Parcel
import android.os.SystemClock
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.app.LocaleManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.insaner.fonecheck.R
import com.insaner.fonecheck.export.ReportPdfRenderer
import org.json.JSONObject
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
    private var languageTrace: LanguageTrace? = null

    @Before
    fun rememberLanguage() {
        composeRule.activityRule.scenario.onActivity {
            originalLocales = AppCompatDelegate.getApplicationLocales()
        }
    }

    @After
    fun restoreLanguage() {
        try {
            traceStage("restoreLanguage") {
                composeRule.activityRule.scenario.onActivity { activity ->
                    languageTrace?.scenarioActivity(activity)
                    traceStage("restore:setter:${originalLocales.toLanguageTags()}") {
                        AppCompatDelegate.setApplicationLocales(originalLocales)
                    }
                }
                traceStage("restore:waitForIdle") { composeRule.waitForIdle() }
            }
        } finally {
            languageTrace?.close()
            languageTrace = null
        }
    }

    @Test
    fun languageUpdatesInPlaceSurvivesRecreationAndResetsToSystem() {
        val initialActivity = composeRule.activity
        languageTrace = LanguageTrace(initialActivity.application)
        languageTrace?.start(initialActivity, originalLocales.toLanguageTags())
        selectLanguage("en")
        assertLanguage("en", "Language")
        traceStage("assertSame:en") { assertSame(initialActivity, tracedComposeActivity()) }

        selectLanguage("es")
        assertLanguage("es", "Idioma")
        traceStage("assertSame:es") { assertSame(initialActivity, tracedComposeActivity()) }

        selectLanguage("pt-BR")
        assertLanguage("pt-BR", "Idioma")
        traceStage("assertTitle:pt-BR") {
            assertEquals("Verificação completa", tracedComposeActivity().getString(R.string.full_check_title))
        }
        traceStage("assertSame:pt-BR") { assertSame(initialActivity, tracedComposeActivity()) }

        selectLanguage("de")
        assertLanguage("de", "Sprache")
        traceStage("assertTitle:de") {
            assertEquals("Gesamtcheck", tracedComposeActivity().getString(R.string.full_check_title))
        }
        traceStage("assertSame:de") { assertSame(initialActivity, tracedComposeActivity()) }

        selectLanguage("fi")
        assertLanguage("fi", "Kieli")
        traceStage("assertSame:fi") { assertSame(initialActivity, tracedComposeActivity()) }

        selectLanguage("fr")
        assertLanguage("fr", "Langue")
        traceStage("assertTitle:fr") {
            assertEquals("Vérification complète", tracedComposeActivity().getString(R.string.full_check_title))
        }
        traceStage("assertSame:fr") { assertSame(initialActivity, tracedComposeActivity()) }

        selectLanguage("fi")

        traceStage("explicit:recreate") { composeRule.activityRule.scenario.recreate() }
        traceStage("explicit:waitForIdle") { composeRule.waitForIdle() }
        assertLanguage("fi", "Kieli")

        val recreatedActivity = tracedComposeActivity()
        selectLanguage("")
        composeRule.activityRule.scenario.onActivity { activity ->
            languageTrace?.scenarioActivity(activity)
            traceStage("system:assertSame") { assertSame(recreatedActivity, activity) }
            traceStage("system:assertLocalesEmpty") { assertTrue(AppCompatDelegate.getApplicationLocales().isEmpty) }
            val systemConfiguration =
                Configuration(activity.resources.configuration).apply {
                    setLocales(
                        LocaleList.forLanguageTags(LocaleManagerCompat.getSystemLocales(activity).toLanguageTags()),
                    )
                }
            val expected =
                activity.createConfigurationContext(systemConfiguration).getString(R.string.settings_language)
            traceStage("system:assertLabel") { assertEquals(expected, activity.getString(R.string.settings_language)) }
        }
        languageTrace?.record("method:complete")
    }

    private fun selectLanguage(languageTag: String) {
        traceStage("selectLanguage:$languageTag") {
            composeRule.activityRule.scenario.onActivity { activity ->
                languageTrace?.scenarioActivity(activity)
                traceStage("setter:$languageTag") {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
                }
            }
            traceStage("waitForIdle:$languageTag") { composeRule.waitForIdle() }
        }
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

    @Test
    fun polishSwitchingRefreshesAnExistingPdfRendererAndSurvivesRecreation() {
        selectLanguage("it")
        val renderer = ReportPdfRenderer(composeRule.activity.applicationContext)
        assertEquals("Rapporto diagnostico fonecheck", renderer.labels().title)
        listOf("pl", "pl-PL").forEach { tag ->
            selectLanguage(tag)
            assertLanguage(tag, "Język")
            assertEquals("Pełna diagnostyka", composeRule.activity.getString(R.string.full_check_title))
            assertEquals("Raport diagnostyczny fonecheck", renderer.labels().title)
            assertEquals("-12,5", renderer.labels().numberValue(-12.5))
            composeRule.activityRule.scenario.recreate()
            composeRule.waitForIdle()
            assertLanguage(tag, "Język")
            assertEquals("Raport diagnostyczny fonecheck", renderer.labels().title)
        }
        assertFinnishAndEnglishPdfLabels(renderer)
        assertSystemLanguageRestored()
    }

    @Test
    fun turkishSwitchingRefreshesAnExistingPdfRendererAndSurvivesRecreation() {
        selectLanguage("pl")
        val renderer = ReportPdfRenderer(composeRule.activity.applicationContext)
        assertEquals("Raport diagnostyczny fonecheck", renderer.labels().title)
        listOf("tr", "tr-TR").forEach { tag ->
            selectLanguage(tag)
            assertLanguage(tag, "Dil")
            assertEquals("Tam kontrol", composeRule.activity.getString(R.string.full_check_title))
            assertEquals("fonecheck tanılama raporu", renderer.labels().title)
            assertEquals("-12,5", renderer.labels().numberValue(-12.5))
            composeRule.activityRule.scenario.recreate()
            composeRule.waitForIdle()
            assertLanguage(tag, "Dil")
            assertEquals("fonecheck tanılama raporu", renderer.labels().title)
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
        traceStage("assertLanguage:waitUntil:$languageTag") {
            composeRule.waitUntil(timeoutMillis = 5_000) {
                val activity = composeRule.activity
                activity.getString(R.string.settings_language) == expectedLabel &&
                    activity.resources.configuration.locales[0]
                        .language == Locale.forLanguageTag(languageTag).language &&
                    ContextCompat.getString(activity.applicationContext, R.string.settings_language) == expectedLabel
            }
        }
        composeRule.activityRule.scenario.onActivity { activity ->
            languageTrace?.scenarioActivity(activity)
            traceStage("assertLanguage:appLocales:$languageTag") {
                assertEquals(languageTag, AppCompatDelegate.getApplicationLocales().toLanguageTags())
            }
            traceStage("assertLanguage:activityLabel:$languageTag") {
                assertEquals(expectedLabel, activity.getString(R.string.settings_language))
            }
            traceStage("assertLanguage:applicationLabel:$languageTag") {
                assertEquals(
                    expectedLabel,
                    ContextCompat.getString(activity.applicationContext, R.string.settings_language),
                )
            }
        }
    }

    private fun tracedComposeActivity(): MainActivity = composeRule.activity.also { languageTrace?.composeActivity(it) }

    private inline fun traceStage(
        stage: String,
        action: () -> Unit,
    ) {
        languageTrace?.record("$stage:start")
        try {
            action()
            languageTrace?.record("$stage:complete")
        } finally {
            languageTrace?.record("$stage:exit")
        }
    }

    private class LanguageTrace(
        private val application: Application,
    ) : Application.ActivityLifecycleCallbacks,
        ComponentCallbacks {
        private val configurations = mutableMapOf<String, Configuration>()
        private var latestActivity: Activity? = null
        private var scenarioReference = "not observed"
        private var composeReference = "not observed"
        private var sequence = 0
        private val traceId = SystemClock.elapsedRealtimeNanos()

        fun start(
            activity: Activity,
            originalLocales: String,
        ) {
            application.registerActivityLifecycleCallbacks(this)
            application.registerComponentCallbacks(this)
            latestActivity = activity
            composeReference = identity(activity)
            record("method:start;originalLocales=$originalLocales", activity)
        }

        fun close() {
            try {
                record("observer:cleanup")
            } finally {
                application.unregisterActivityLifecycleCallbacks(this)
                application.unregisterComponentCallbacks(this)
                latestActivity = null
                configurations.clear()
            }
        }

        fun scenarioActivity(activity: Activity) {
            scenarioReference = identity(activity)
            record("reference:scenario", activity)
        }

        fun composeActivity(activity: Activity) {
            composeReference = identity(activity)
            record("reference:compose", activity)
        }

        @Synchronized
        fun record(
            stage: String,
            activity: Activity? = latestActivity,
            incomingConfiguration: Configuration? = null,
        ) {
            val event =
                JSONObject()
                    .put("traceId", traceId)
                    .put("sequence", ++sequence)
                    .put("wallMillis", System.currentTimeMillis())
                    .put("elapsedRealtimeNanos", SystemClock.elapsedRealtimeNanos())
                    .put("thread", Thread.currentThread().name)
                    .put("stage", stage)
                    .put("diagnosticMethod", "languageUpdatesInPlaceSurvivesRecreationAndResetsToSystem")
                    .put("application", identity(application))
                    .put("activity", activity?.let(::identity))
                    .put("isChangingConfigurations", activity?.isChangingConfigurations)
                    .put("scenarioReference", scenarioReference)
                    .put("composeReference", composeReference)
                    .put("appLocales", AppCompatDelegate.getApplicationLocales().toLanguageTags())
                    .put("systemLocales", LocaleManagerCompat.getSystemLocales(application).toLanguageTags())
                    .put("applicationConfiguration", snapshot("application", application.resources.configuration))
                    .put("systemConfiguration", snapshot("system", Resources.getSystem().configuration))
                    .put("frameworkRelaunchDecisionMask", "NOT CAPTURED")
            activity?.let { event.put("activityConfiguration", snapshot("activity", it.resources.configuration)) }
            incomingConfiguration?.let { event.put("incomingConfiguration", snapshot("incoming", it)) }
            // Chunk complete copied Configuration parcels to avoid logcat's per-entry truncation.
            val chunks = event.toString().chunked(2_400)
            chunks.forEachIndexed { index, chunk ->
                Log.i("A06aLocaleTrace", "$traceId/$sequence/${index + 1}/${chunks.size} $chunk")
            }
        }

        private fun snapshot(
            owner: String,
            configuration: Configuration,
        ): JSONObject {
            val copy = Configuration(configuration)
            val previous = configurations.put(owner, Configuration(copy))
            val parcel = Parcel.obtain()
            val encoded =
                try {
                    copy.writeToParcel(parcel, 0)
                    Base64.encodeToString(parcel.marshall(), Base64.NO_WRAP)
                } finally {
                    parcel.recycle()
                }
            return JSONObject()
                .put("text", copy.toString())
                .put("locales", copy.locales.toLanguageTags())
                .put("parcelBase64", encoded)
                .put("observedDiffFromPreviousCopy", previous?.diff(Configuration(copy)))
        }

        private fun identity(value: Any): String =
            "${value.javaClass.name}@${Integer.toHexString(System.identityHashCode(value))}"

        private fun lifecycle(
            stage: String,
            activity: Activity,
        ) {
            if (activity !is MainActivity) return
            latestActivity = activity
            record("lifecycle:$stage", activity)
        }

        override fun onActivityCreated(
            activity: Activity,
            savedInstanceState: Bundle?,
        ) = lifecycle("created;hasSavedState=${savedInstanceState != null}", activity)

        override fun onActivityStarted(activity: Activity) = lifecycle("started", activity)

        override fun onActivityResumed(activity: Activity) = lifecycle("resumed", activity)

        override fun onActivityPaused(activity: Activity) = lifecycle("paused", activity)

        override fun onActivityStopped(activity: Activity) = lifecycle("stopped", activity)

        override fun onActivitySaveInstanceState(
            activity: Activity,
            outState: Bundle,
        ) = lifecycle("saveInstanceState", activity)

        override fun onActivityDestroyed(activity: Activity) {
            lifecycle("destroyed", activity)
            if (latestActivity === activity) latestActivity = null
        }

        override fun onConfigurationChanged(newConfig: Configuration) {
            record("application:onConfigurationChanged", incomingConfiguration = newConfig)
        }

        override fun onLowMemory() {
            record("application:onLowMemory")
        }
    }
}
