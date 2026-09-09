package com.insaner.fonecheck.localization

import android.content.res.Configuration
import android.icu.text.PluralRules
import android.text.format.Formatter
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.observation.ObservationReason
import com.insaner.fonecheck.export.ReportPdfRenderer
import com.insaner.fonecheck.ui.format.formatUiNumber
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.text.NumberFormat
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class GermanResourcesTest {
    @Test
    fun genericAndRegionalConfigurationsResolveGermanUiAccessibilityAndPdf() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("de", "de-DE", "de-AT", "de-CH").forEach { tag ->
            val configuration =
                Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) }
            val context = base.createConfigurationContext(configuration)
            mapOf(
                R.string.full_check_title to "Gesamtcheck",
                R.string.onboarding_title to "Erste Schritte",
                R.string.settings_language to "Sprache",
                R.string.settings_language_german to "Deutsch",
                R.string.home_cat_battery to "Akku",
                R.string.perf_ram_title to "Arbeitsspeicher",
                R.string.home_cat_storage to "Speicher",
                R.string.history_title to "Berichtsverlauf",
                R.string.report_saved_title to "Gespeicherter Bericht",
                R.string.comparison_title to "Berichtsvergleich",
                R.string.export_title to "Bericht exportieren",
                R.string.report_retest to "Erneut prüfen und speichern",
                R.string.accessibility_expanded to "Ausgeklappt",
                R.string.accessibility_collapsed to "Eingeklappt",
                R.string.permission_status_denied to "Berechtigung verweigert",
                R.string.value_unavailable_short to "n/a",
                R.string.readout_scroll_hint to "Scrolle horizontal, um den vollständigen Wert zu lesen.",
            ).forEach { (id, expected) -> assertEquals(tag, expected, context.getString(id)) }
            val labels = ReportPdfRenderer(context).labels(context)
            assertEquals("fonecheck-Diagnosebericht", labels.title)
            assertEquals("Nicht gemessen", labels.statusName(DiagnosticStatus.NOT_TESTED))
            assertEquals("Nicht bestanden", labels.statusName(DiagnosticStatus.FAIL))
            assertEquals("Warnung", labels.statusName(DiagnosticStatus.WARNING))
            assertEquals("Information", labels.statusName(DiagnosticStatus.INFO))
            assertEquals("Prüfung konnte nicht abgeschlossen werden", labels.reasonName(EvidenceReasonCode.ERROR))
            assertEquals("Zeitlimit erreicht", labels.reasonName(EvidenceReasonCode.TIMEOUT))
            assertEquals("Prüfung wurde nicht abgeschlossen", labels.reasonName(EvidenceReasonCode.NOT_RUN))
            assertEquals("Prüfumfang", labels.coverage)
            assertEquals("Vertrauensgrad", labels.confidence)
            assertPdfDecimalFormatting(context, labels, "Seite 1 / 2")
            assertEquals(
                "Android meldet eine Überspannung des Akkus. Beende das Laden und lass Akku und Ladesystem überprüfen.",
                context.getString(observationReasonStringRes(ObservationReason.BATTERY_OVER_VOLTAGE)),
            )
            assertEquals(
                "Dieser Test wurde abgebrochen, bevor ein Ergebnis vorlag.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_CANCELLED)),
            )
            assertEquals(
                "Dieser Test wurde übersprungen. Führe ihn aus, um ein Ergebnis zu erhalten.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_SKIPPED)),
            )
            // Category labels stay in the nominative; these templates must not require an inflected article.
            listOf(R.string.home_cat_battery, R.string.home_cat_camera, R.string.home_cat_sensors).forEach { id ->
                val category = context.getString(id)
                assertEquals("Erneut prüfen: $category", context.getString(R.string.report_retest_title, category))
                assertEquals("Umfang: nur $category.", context.getString(R.string.report_scope_category, category))
            }
            assertTrue(context.getString(R.string.licenses_component_inventory).contains("Hinweise zu Drittanbietern"))
        }
    }

    @Test
    fun integerQuantitiesAndSharedFormattingUseGenericGerman() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val locale = uiLanguageLocale(Locale.forLanguageTag("de-CH"))
        assertEquals("de", locale.toLanguageTag())
        val context = localizedContext(base, locale)
        val rules = PluralRules.forLocale(locale)
        // Every current plural call site uses integer quantities, with a separate Int or formatted String argument.
        listOf(0, 1, 2, 1234, 1_000_000).forEach { count ->
            val singular = count == 1
            assertEquals(if (singular) "one" else "other", rules.select(count.toDouble()))
            val number = formatUiNumber(count, locale)
            assertEquals(
                "$number ${if (singular) "Messwert" else "Messwerte"}",
                context.resources.getQuantityString(R.plurals.sensor_samples, count, number),
            )
            assertEquals(
                "vor $count ${if (singular) "Tag" else "Tagen"}",
                context.resources.getQuantityString(R.plurals.home_latest_days_ago, count, count),
            )
            assertEquals(
                "$number ${if (singular) "Kategorie" else "Kategorien"}",
                context.resources.getQuantityString(R.plurals.home_status_channel_count, count, number),
            )
            assertEquals(
                "+ $number ${if (singular) "weiterer" else "weitere"}",
                context.resources.getQuantityString(R.plurals.conn_gps_more_sats, count, number),
            )
            assertEquals(
                "$count ${if (singular) "Beobachtung benötigt" else "Beobachtungen benötigen"} Aufmerksamkeit",
                context.resources.getQuantityString(R.plurals.home_latest_evidence_attention_summary, count, count),
            )
        }
        listOf(1_500_000L to "1,5", 0L to "0").forEach { (bytes, expected) ->
            assertTrue(Formatter.formatFileSize(context, bytes).contains(expected))
        }
        val percent = NumberFormat.getPercentInstance(locale).apply { maximumFractionDigits = 1 }
        assertEquals("12,5", percent.format(0.125).removeSuffix("%").trim())
    }

    @Test
    fun localeListsPreferGermanOrTheNextSupportedLanguage() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        assertLocaleListResolutions(
            base,
            mapOf(
                "de-DE,pt-BR,en" to "Gesamtcheck",
                "de-AT,en" to "Gesamtcheck",
                "de-CH,fi" to "Gesamtcheck",
                "it-IT,de,en" to "Gesamtcheck",
                "es,de" to "Comprobación completa",
                "pt-BR,de" to "Verificação completa",
                "fi,de" to "Full Check",
                "en,de" to "Full Check",
                "it-IT,en" to "Full Check",
            ),
        )
    }
}
