package com.insaner.fonecheck.localization

import android.icu.text.PluralRules
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.observation.ObservationReason
import com.insaner.fonecheck.export.ReportPdfRenderer
import com.insaner.fonecheck.ui.format.formatUiNumber
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.text.DecimalFormatSymbols
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class SwedishResourcesTest {
    @Test
    fun bothRegionsResolveGenericSwedishUiAccessibilityAndPdf() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val english = localizedContext(base, Locale.ENGLISH)
        listOf("sv", "sv-SE", "sv-FI").forEach { tag ->
            val context = localizedContext(base, Locale.forLanguageTag(tag))
            assertEquals(AppLanguage.SWEDISH, AppLanguage.fromLocale(context.resources.configuration.locales[0]))
            mapOf(
                R.string.full_check_title to "Fullständig kontroll",
                R.string.onboarding_title to "Kom igång",
                R.string.settings_language to "Språk",
                R.string.settings_language_swedish to "Svenska",
                R.string.home_cat_battery to "Batteri",
                R.string.perf_ram_title to "RAM",
                R.string.home_cat_storage to "Lagring",
                R.string.history_title to "Rapporthistorik",
                R.string.report_saved_title to "Sparad rapport",
                R.string.comparison_title to "Rapportjämförelse",
                R.string.export_title to "Exportera rapport",
                R.string.report_retest to "Testa igen och spara",
                R.string.accessibility_expanded to "Utökad",
                R.string.accessibility_collapsed to "Hopfälld",
                R.string.permission_status_denied to "Behörighet nekad",
                R.string.value_unavailable_short to "n/a",
                R.string.home_settings_content_description to "Öppna inställningar",
                R.string.readout_scroll_hint to "Rulla horisontellt för att läsa hela värdet.",
                R.string.thermal_headroom_title to "Termisk marginal",
            ).forEach { (id, expected) -> assertEquals(tag, expected, context.getString(id)) }
            val labels = ReportPdfRenderer(context).labels(context)
            assertEquals("fonecheck diagnostikrapport", labels.title)
            assertEquals("Godkänt", labels.statusName(DiagnosticStatus.PASS))
            assertEquals("Inte mätt", labels.statusName(DiagnosticStatus.NOT_TESTED))
            assertEquals("Inte tillgängligt", labels.statusName(DiagnosticStatus.NOT_AVAILABLE))
            assertEquals("Underkänt", labels.statusName(DiagnosticStatus.FAIL))
            assertEquals("Varning", labels.statusName(DiagnosticStatus.WARNING))
            assertEquals("Information", labels.statusName(DiagnosticStatus.INFO))
            assertEquals("Kontrollen kunde inte slutföras", labels.reasonName(EvidenceReasonCode.ERROR))
            assertEquals("Tidsgränsen nådd", labels.reasonName(EvidenceReasonCode.TIMEOUT))
            assertEquals("Kontrollen slutfördes inte", labels.reasonName(EvidenceReasonCode.NOT_RUN))
            assertEquals("Andel genomförda kontroller", labels.coverage)
            assertEquals("Tillförlitlighet", labels.confidence)
            assertEquals("Hög tillförlitlighet", labels.confidenceName(Confidence.HIGH))
            assertEquals("Låg tillförlitlighet", labels.confidenceName(Confidence.LOW))
            assertEquals("Inte tillgängligt", labels.confidenceName(Confidence.UNAVAILABLE))
            assertEquals(
                EvidenceSource.entries.size,
                EvidenceSource.entries
                    .map(labels.sourceName)
                    .toSet()
                    .size,
            )
            mapOf(
                "battery.health" to "Batteristatus i Android",
                "battery.level" to "Laddningsnivå",
                "performance.ram" to "RAM",
                "camera.capture_dimensions" to "Mått på senaste testbilden",
            ).forEach { (checkId, expected) ->
                val resource = requireNotNull(evidenceLabelResource(checkId))
                assertEquals(expected, context.getString(resource.stringResId))
            }
            assertEquals("Laddar", labels.stableTextName("charging"))
            assertEquals("Laddar ur", labels.stableTextName("discharging"))
            assertEquals("Sida 1 / 2", context.getString(R.string.pdf_page, 1, 2))
            assertEquals("12,5%", context.getString(R.string.report_coverage_value, labels.numberValue(12.5)))
            val symbols = DecimalFormatSymbols(uiLanguageLocale(Locale.forLanguageTag(tag)))
            assertEquals("${symbols.minusSign}12,5", labels.numberValue(-12.5))
            ObservationReason.entries.forEach { reason ->
                val resource = observationReasonStringRes(reason)
                assertNotEquals(reason.name, english.getString(resource), context.getString(resource))
            }
            assertEquals(
                "Det här testet avbröts innan ett resultat erhölls.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_CANCELLED)),
            )
            assertEquals(
                "Det här testet hoppades över. Kör det för att få ett resultat.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_SKIPPED)),
            )
        }
    }

    @Test
    fun integerPluralsAndFormattingFollowGenericSwedish() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val locale = uiLanguageLocale(Locale.forLanguageTag("sv-FI"))
        assertEquals("sv", locale.toLanguageTag())
        val context = localizedContext(base, locale)
        val rules = PluralRules.forLocale(locale)
        assertEquals(setOf("one", "other"), rules.keywords)
        listOf(0, 1, 2, 1234, 1_000_000).forEach { count ->
            assertEquals(if (count == 1) "one" else "other", rules.select(count.toDouble()))
            val number = formatUiNumber(count, locale)
            val expected =
                if (count == 1) {
                    listOf(
                        "$number mätvärde",
                        "För $count dag sedan",
                        "$number kategori",
                        "$count observation behöver ses över",
                    )
                } else {
                    listOf(
                        "$number mätvärden",
                        "För $count dagar sedan",
                        "$number kategorier",
                        "$count observationer behöver ses över",
                    )
                }
            assertEquals(expected[0], context.resources.getQuantityString(R.plurals.sensor_samples, count, number))
            assertEquals(expected[1], context.resources.getQuantityString(R.plurals.home_latest_days_ago, count, count))
            assertEquals(
                expected[2],
                context.resources.getQuantityString(R.plurals.home_status_channel_count, count, number),
            )
            assertEquals(
                expected[3],
                context.resources.getQuantityString(R.plurals.home_latest_evidence_attention_summary, count, count),
            )
            assertEquals(
                "+ $number till",
                context.resources.getQuantityString(R.plurals.conn_gps_more_sats, count, number),
            )
        }
        assertFileSizeAndPercentFormatting(context, locale)
        val symbols = DecimalFormatSymbols(locale)
        assertEquals(
            "${symbols.minusSign}1${symbols.groupingSeparator}234,5",
            formatUiNumber(-1234.5, locale, 1, 1, grouping = true),
        )
    }

    @Test
    fun localeListsPreserveSwedishSelectionAndExistingLanguageFallbacks() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        assertLocaleListResolutions(
            base,
            mapOf(
                "sv-SE,en" to "Fullständig kontroll",
                "sv-FI,en" to "Fullständig kontroll",
                "ja-JP,sv,en" to "Fullständig kontroll",
                "en,sv" to "Full Check",
                "fi,sv" to "Full Check",
                "es,sv" to "Comprobación completa",
                "pt-BR,sv" to "Verificação completa",
                "de,sv" to "Gesamtcheck",
                "fr,sv" to "Vérification complète",
                "id,sv" to "Pemeriksaan lengkap",
                "ja-JP,en" to "Full Check",
            ),
        )
    }
}
