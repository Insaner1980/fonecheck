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
class NorwegianBokmalResourcesTest {
    @Test
    fun bokmalAndNorwayRegionResolveUiAccessibilityAndPdfResources() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val english = localizedContext(base, Locale.ENGLISH)
        listOf("nb", "nb-NO").forEach { tag ->
            val context = localizedContext(base, Locale.forLanguageTag(tag))
            assertEquals(
                AppLanguage.NORWEGIAN_BOKMAL,
                AppLanguage.fromLocale(context.resources.configuration.locales[0]),
            )
            mapOf(
                R.string.full_check_title to "Fullstendig sjekk",
                R.string.onboarding_title to "Kom i gang",
                R.string.settings_language to "Språk",
                R.string.settings_language_norwegian_bokmal to "Norsk bokmål",
                R.string.home_cat_battery to "Batteri",
                R.string.perf_ram_title to "RAM",
                R.string.home_cat_storage to "Lagring",
                R.string.history_title to "Rapporthistorikk",
                R.string.report_saved_title to "Lagret rapport",
                R.string.comparison_title to "Rapportsammenligning",
                R.string.export_title to "Eksporter rapport",
                R.string.report_retest to "Test på nytt og lagre",
                R.string.accessibility_expanded to "Utvidet",
                R.string.accessibility_collapsed to "Sammenfoldet",
                R.string.permission_status_denied to "Tillatelse avslått",
                R.string.value_unavailable_short to "n/a",
                R.string.home_settings_content_description to "Åpne innstillinger",
                R.string.readout_scroll_hint to "Rull vannrett for å lese hele verdien.",
                R.string.thermal_headroom_title to "Termisk margin",
            ).forEach { (id, expected) -> assertEquals(tag, expected, context.getString(id)) }
            val labels = ReportPdfRenderer(context).labels(context)
            assertEquals("Diagnostikkrapport fra fonecheck", labels.title)
            assertEquals("Bestått", labels.statusName(DiagnosticStatus.PASS))
            assertEquals("Ikke målt", labels.statusName(DiagnosticStatus.NOT_TESTED))
            assertEquals("Ikke tilgjengelig", labels.statusName(DiagnosticStatus.NOT_AVAILABLE))
            assertEquals("Feil", labels.statusName(DiagnosticStatus.FAIL))
            assertEquals("Advarsel", labels.statusName(DiagnosticStatus.WARNING))
            assertEquals("Informasjon", labels.statusName(DiagnosticStatus.INFO))
            assertEquals("Sjekken kunne ikke fullføres", labels.reasonName(EvidenceReasonCode.ERROR))
            assertEquals("Tidsavbrudd", labels.reasonName(EvidenceReasonCode.TIMEOUT))
            assertEquals("Sjekken ble ikke fullført", labels.reasonName(EvidenceReasonCode.NOT_RUN))
            assertEquals("Fullføringsgrad", labels.coverage)
            assertEquals("Pålitelighet", labels.confidence)
            assertEquals("Høy pålitelighet", labels.confidenceName(Confidence.HIGH))
            assertEquals("Lav pålitelighet", labels.confidenceName(Confidence.LOW))
            assertEquals("Utilgjengelig", labels.confidenceName(Confidence.UNAVAILABLE))
            assertEquals(
                setOf("Automatisk måling", "Android API", "Bekreftelse fra bruker", "Beregnet", "Estimat"),
                EvidenceSource.entries.map(labels.sourceName).toSet(),
            )
            mapOf(
                "battery.health" to "Batteristatus fra Android",
                "battery.level" to "Ladenivå",
                "performance.ram" to "RAM",
                "camera.capture_dimensions" to "Mål på siste testbilde",
            ).forEach { (checkId, expected) ->
                val resource = requireNotNull(evidenceLabelResource(checkId))
                assertEquals(expected, context.getString(resource.stringResId))
            }
            assertEquals("Lader", labels.stableTextName("charging"))
            assertEquals("Lades ut", labels.stableTextName("discharging"))
            // CPD-OFF
            // Norwegian and Swedish tests intentionally mirror these language-specific expectations.
            assertEquals("Side 1 / 2", context.getString(R.string.pdf_page, 1, 2))
            assertEquals("12,5%", context.getString(R.string.report_coverage_value, labels.numberValue(12.5)))
            val symbols = DecimalFormatSymbols(uiLanguageLocale(Locale.forLanguageTag(tag)))
            assertEquals("${symbols.minusSign}12,5", labels.numberValue(-12.5))
            ObservationReason.entries.forEach { reason ->
                val resource = observationReasonStringRes(reason)
                assertNotEquals(reason.name, english.getString(resource), context.getString(resource))
            }
            assertEquals(
                "Denne testen ble avbrutt før et resultat ble oppnådd.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_CANCELLED)),
            )
            assertEquals(
                "Denne testen ble hoppet over. Kjør den for å få et resultat.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_SKIPPED)),
            )
            // CPD-ON
        }
    }

    @Test
    fun integerPluralsNumbersFileSizesAndPercentagesUseBokmal() {
        // CPD-OFF
        // Norwegian and Swedish tests intentionally mirror these language-specific expectations.
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val locale = uiLanguageLocale(Locale.forLanguageTag("nb-NO"))
        assertEquals("nb", locale.toLanguageTag())
        val context = localizedContext(base, locale)
        val rules = PluralRules.forLocale(locale)
        assertEquals(setOf("one", "other"), rules.keywords)
        listOf(0, 1, 2, 1234, 1_000_000).forEach { count ->
            assertEquals(if (count == 1) "one" else "other", rules.select(count.toDouble()))
            val number = formatUiNumber(count, locale)
            val expected =
                if (count == 1) {
                    listOf(
                        "$number måling",
                        "For $count dag siden",
                        "$number kategori",
                        "$count observasjon trenger oppfølging",
                    )
                } else {
                    listOf(
                        "$number målinger",
                        "For $count dager siden",
                        "$number kategorier",
                        "$count observasjoner trenger oppfølging",
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
                "+ $number til",
                context.resources.getQuantityString(R.plurals.conn_gps_more_sats, count, number),
            )
        }
        assertFileSizeAndPercentFormatting(context, locale)
        val symbols = DecimalFormatSymbols(locale)
        assertEquals(
            "${symbols.minusSign}1${symbols.groupingSeparator}234,5",
            formatUiNumber(-1234.5, locale, 1, 1, grouping = true),
        )
        // CPD-ON
    }

    @Test
    fun genericNorwegianAndNynorskKeepPlatformFallbackWithoutAnAppAlias() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        assertLocaleListResolutions(
            base,
            mapOf(
                "nb" to "Fullstendig sjekk",
                "nb-NO,en" to "Fullstendig sjekk",
                "no" to "Full Check",
                "no-NO" to "Full Check",
                "nn" to "Full Check",
                "nn-NO" to "Full Check",
                "no-NO,nb,en" to "Fullstendig sjekk",
                "nn-NO,fi,en" to "Full Check",
                "ja-JP,nb,en" to "Fullstendig sjekk",
                "en,nb" to "Full Check",
                "fi,nb" to "Full Check",
                "es,nb" to "Comprobación completa",
                "pt-BR,nb" to "Verificação completa",
                "de,nb" to "Gesamtcheck",
                "fr,nb" to "Vérification complète",
                "id,nb" to "Pemeriksaan lengkap",
                "sv,nb" to "Fullständig kontroll",
                "ja-JP,en" to "Full Check",
            ),
        )
    }
}
