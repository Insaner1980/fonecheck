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
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ItalianResourcesTest {
    @Test
    fun italianRegionsResolveDiagnosticAccessibilityAndPdfText() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val english = localizedContext(base, Locale.ENGLISH)
        listOf("it", "it-IT", "it-CH").forEach { tag ->
            val context = localizedContext(base, Locale.forLanguageTag(tag))
            assertEquals(AppLanguage.ITALIAN, AppLanguage.fromLocale(context.resources.configuration.locales[0]))
            mapOf(
                R.string.full_check_title to "Controllo completo",
                R.string.onboarding_title to "Primi passi",
                R.string.settings_language to "Lingua",
                R.string.settings_language_italian to "Italiano",
                R.string.home_cat_storage to "Archiviazione",
                R.string.perf_ram_title to "RAM",
                R.string.history_title to "Cronologia rapporti",
                R.string.report_saved_title to "Rapporto salvato",
                R.string.comparison_title to "Confronto rapporti",
                R.string.export_title to "Esporta rapporto",
                R.string.report_retest to "Ripeti test e salva",
                R.string.accessibility_expanded to "Espanso",
                R.string.accessibility_collapsed to "Compresso",
                R.string.permission_status_denied to "Autorizzazione negata",
                R.string.value_unavailable_short to "n/a",
                R.string.home_settings_content_description to "Apri impostazioni",
                R.string.readout_scroll_hint to "Scorri in orizzontale per leggere il valore completo.",
                R.string.thermal_headroom_title to "Margine termico",
            ).forEach { (id, expected) -> assertEquals(tag, expected, context.getString(id)) }
            assertRetestCategoryTitles(context, "Ripeti test: ")
            val labels = ReportPdfRenderer(context).labels(context)
            mapOf(
                DiagnosticStatus.PASS to "Superato",
                DiagnosticStatus.FAIL to "Non superato",
                DiagnosticStatus.WARNING to "Avviso",
                DiagnosticStatus.INFO to "Informativo",
                DiagnosticStatus.NOT_AVAILABLE to "Non disponibile",
                DiagnosticStatus.NOT_TESTED to "Non misurato",
            ).forEach { (status, expected) -> assertEquals(expected, labels.statusName(status)) }
            assertEquals("Rapporto diagnostico fonecheck", labels.title)
            assertEquals("Completezza dei controlli", labels.coverage)
            assertEquals("Affidabilità", labels.confidence)
            mapOf(
                Confidence.HIGH to "Affidabilità alta",
                Confidence.LOW to "Affidabilità bassa",
                Confidence.UNAVAILABLE to "Non disponibile",
            ).forEach { (confidence, expected) -> assertEquals(expected, labels.confidenceName(confidence)) }
            assertEquals(
                setOf("Misurazione automatica", "API Android", "Conferma dell’utente", "Valore derivato", "Stima"),
                EvidenceSource.entries.map(labels.sourceName).toSet(),
            )
            mapOf(
                EvidenceReasonCode.ERROR to "Impossibile completare il controllo",
                EvidenceReasonCode.TIMEOUT to "Tempo scaduto",
                EvidenceReasonCode.NOT_RUN to "Controllo non completato",
            ).forEach { (reason, expected) -> assertEquals(expected, labels.reasonName(reason)) }
            mapOf(
                "battery.health" to "Stato batteria da Android",
                "battery.level" to "Livello di carica",
                "performance.ram" to "RAM",
                "camera.capture_dimensions" to "Dimensioni dell’ultima foto di prova",
            ).forEach { (id, expected) ->
                assertEquals(expected, context.getString(requireNotNull(evidenceLabelResource(id)).stringResId))
            }
            assertEquals("In carica", labels.stableTextName("charging"))
            assertEquals("In scarica", labels.stableTextName("discharging"))
            assertObservationReasonsTranslated(english, context)
            assertEquals(
                "Questo test è stato annullato prima di ottenere un risultato.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_CANCELLED)),
            )
            assertEquals(
                "Questo test è stato saltato. Eseguilo per ottenere un risultato.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_SKIPPED)),
            )
            assertPdfDecimalFormatting(context, labels, "Pagina 1 / 2")
        }
    }

    @Test
    fun italianIntegerPluralsRespectTheDeviceIcuVersion() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("it", "it-IT", "it-CH").forEach { tag ->
            val locale = uiLanguageLocale(Locale.forLanguageTag(tag))
            assertEquals("it", locale.toLanguageTag())
            val context = localizedContext(base, Locale.forLanguageTag(tag))
            val rules = PluralRules.forLocale(locale)
            // Older Android ICU uses other for exact millions; current CLDR uses many.
            assertTrue(rules.keywords in listOf(setOf("one", "other"), setOf("one", "many", "other")))
            listOf(0, 1, 2, 1234, 1_000_000).forEach { count ->
                val quantity = rules.select(count.toDouble())
                when (count) {
                    1 -> assertEquals("one", quantity)
                    1_000_000 -> assertTrue(quantity in setOf("many", "other"))
                    else -> assertEquals("other", quantity)
                }
                val singular = quantity == "one"
                val number = formatUiNumber(count, locale)
                val nounPrefix = if (quantity == "many") "di " else ""
                val expectations =
                    mapOf(
                        R.plurals.sensor_samples to "$number $nounPrefix${if (singular) "campione" else "campioni"}",
                        R.plurals.home_latest_days_ago to
                            "$count $nounPrefix${if (singular) "giorno" else "giorni"} fa",
                        R.plurals.home_status_channel_count to
                            "$number $nounPrefix${if (singular) "categoria" else "categorie"}",
                        R.plurals.home_latest_evidence_attention_summary to
                            "$count $nounPrefix${if (singular) "osservazione" else "osservazioni"} da verificare",
                        R.plurals.conn_gps_more_sats to "+ $number ${if (singular) "altro" else "altri"}",
                    )
                assertPluralResourceValues(context, expectations, count, number)
            }
            // uiFileSize uses the normalized UI language rather than the requested region.
            assertFileSizeAndPercentFormatting(localizedContext(base, locale), locale)
        }
    }

    @Test
    fun localeListsPreserveItalianResolutionAndExistingLanguagePriorities() {
        assertLocaleListResolutions(
            InstrumentationRegistry.getInstrumentation().targetContext,
            mapOf(
                "it" to "Controllo completo",
                "it-IT,en" to "Controllo completo",
                "it-CH,en" to "Controllo completo",
                "ja-JP,it,en" to "Controllo completo",
                "en,it" to "Full Check",
                "fi,it" to "Full Check",
                "es,it" to "Comprobación completa",
                "pt-BR,it" to "Verificação completa",
                "de,it" to "Gesamtcheck",
                "fr,it" to "Vérification complète",
                "id,it" to "Pemeriksaan lengkap",
                "sv,it" to "Fullständig kontroll",
                "nb,it" to "Fullstendig sjekk",
                "da,it" to "Fuld kontrol",
                "ja-JP,en" to "Full Check",
            ),
        )
    }
}
