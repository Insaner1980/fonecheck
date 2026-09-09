package com.insaner.fonecheck.localization

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.observation.ObservationReason
import com.insaner.fonecheck.export.ReportPdfRenderer
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class DanishResourcesTest {
    @Test
    fun genericAndRegionalDanishResolveDiagnosticAccessibilityAndPdfText() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val english = localizedContext(base, Locale.ENGLISH)
        listOf("da", "da-DK").forEach { tag ->
            val context = localizedContext(base, Locale.forLanguageTag(tag))
            assertEquals(AppLanguage.DANISH, AppLanguage.fromLocale(context.resources.configuration.locales[0]))
            mapOf(
                R.string.full_check_title to "Fuld kontrol",
                R.string.onboarding_title to "Kom godt i gang",
                R.string.settings_language to "Sprog",
                R.string.settings_language_danish to "Dansk",
                R.string.home_cat_storage to "Lager",
                R.string.perf_ram_title to "RAM",
                R.string.history_title to "Rapporthistorik",
                R.string.report_saved_title to "Gemt rapport",
                R.string.comparison_title to "Rapportsammenligning",
                R.string.export_title to "Eksportér rapport",
                R.string.report_retest to "Test igen og gem",
                R.string.accessibility_expanded to "Udvidet",
                R.string.accessibility_collapsed to "Sammenfoldet",
                R.string.permission_status_denied to "Tilladelse afvist",
                R.string.value_unavailable_short to "n/a",
                R.string.home_settings_content_description to "Åbn indstillinger",
                R.string.readout_scroll_hint to "Rul vandret for at læse hele værdien.",
                R.string.thermal_headroom_title to "Termisk råderum",
            ).forEach { (id, expected) -> assertEquals(tag, expected, context.getString(id)) }
            assertRetestCategoryTitles(context, "Test igen: ")
            val labels = ReportPdfRenderer(context).labels(context)
            val statuses =
                mapOf(
                    DiagnosticStatus.PASS to "Bestået",
                    DiagnosticStatus.FAIL to "Fejl",
                    DiagnosticStatus.WARNING to "Advarsel",
                    DiagnosticStatus.INFO to "Oplysning",
                    DiagnosticStatus.NOT_AVAILABLE to "Ikke tilgængelig",
                    DiagnosticStatus.NOT_TESTED to "Ikke målt",
                )
            statuses.forEach { (status, expected) -> assertEquals(expected, labels.statusName(status)) }
            assertEquals("Testrapport fra fonecheck", labels.title)
            assertEquals("Gennemførelsesgrad", labels.coverage)
            assertEquals("Pålidelighed", labels.confidence)
            mapOf(
                Confidence.HIGH to "Høj pålidelighed",
                Confidence.LOW to "Lav pålidelighed",
                Confidence.UNAVAILABLE to "Utilgængelig",
            ).forEach { (confidence, expected) -> assertEquals(expected, labels.confidenceName(confidence)) }
            assertEquals(
                setOf("Automatisk måling", "Android API", "Brugerbekræftelse", "Beregnet", "Skøn"),
                EvidenceSource.entries.map(labels.sourceName).toSet(),
            )
            mapOf(
                EvidenceReasonCode.ERROR to "Kontrollen kunne ikke gennemføres",
                EvidenceReasonCode.TIMEOUT to "Tidsgrænsen er nået",
                EvidenceReasonCode.NOT_RUN to "Kontrollen blev ikke gennemført",
            ).forEach { (reason, expected) -> assertEquals(expected, labels.reasonName(reason)) }
            mapOf(
                "battery.health" to "Batteristatus fra Android",
                "battery.level" to "Ladeniveau",
                "performance.ram" to "RAM",
                "camera.capture_dimensions" to "Mål på seneste testbillede",
            ).forEach { (id, expected) ->
                assertEquals(expected, context.getString(requireNotNull(evidenceLabelResource(id)).stringResId))
            }
            assertEquals("Oplader", labels.stableTextName("charging"))
            assertEquals("Aflader", labels.stableTextName("discharging"))
            assertObservationReasonsTranslated(english, context)
            assertEquals(
                "Denne test blev annulleret, før der forelå et resultat.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_CANCELLED)),
            )
            assertEquals(
                "Denne test blev sprunget over. Kør den for at få et resultat.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_SKIPPED)),
            )
            assertPdfDecimalFormatting(context, labels, "Side 1 / 2")
        }
    }

    @Test
    fun allFiveIntegerPluralsAndAndroidFileSizesUseDanish() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("da", "da-DK").forEach { tag ->
            val locale = uiLanguageLocale(Locale.forLanguageTag(tag))
            assertEquals("da", locale.toLanguageTag())
            val context = localizedContext(base, Locale.forLanguageTag(tag))
            assertIntegerOneOtherQuantities(locale) { count, singular, number ->
                val expectations =
                    mapOf(
                        R.plurals.sensor_samples to "$number ${if (singular) "måling" else "målinger"}",
                        R.plurals.home_latest_days_ago to "For $count ${if (singular) "dag" else "dage"} siden",
                        R.plurals.home_status_channel_count to "$number ${if (singular) "kategori" else "kategorier"}",
                        R.plurals.home_latest_evidence_attention_summary to
                            "$count ${if (singular) "observation" else "observationer"} kræver opmærksomhed",
                        R.plurals.conn_gps_more_sats to "+ $number mere",
                    )
                assertPluralResourceValues(context, expectations, count, number)
            }
            assertFileSizeAndPercentFormatting(context, locale)
        }
    }

    @Test
    fun localeListsPreserveDanishResolutionAndExistingLanguagePriorities() {
        assertLocaleListResolutions(
            InstrumentationRegistry.getInstrumentation().targetContext,
            mapOf(
                "da" to "Fuld kontrol",
                "da-DK,en" to "Fuld kontrol",
                "ja-JP,da,en" to "Fuld kontrol",
                "en,da" to "Full Check",
                "fi,da" to "Full Check",
                "es,da" to "Comprobación completa",
                "pt-BR,da" to "Verificação completa",
                "de,da" to "Gesamtcheck",
                "fr,da" to "Vérification complète",
                "id,da" to "Pemeriksaan lengkap",
                "sv,da" to "Fullständig kontroll",
                "nb,da" to "Fullstendig sjekk",
                "ja-JP,en" to "Full Check",
            ),
        )
    }
}
