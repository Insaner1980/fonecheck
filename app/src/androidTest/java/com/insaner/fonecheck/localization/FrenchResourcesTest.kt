package com.insaner.fonecheck.localization

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
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class FrenchResourcesTest {
    @Test
    fun genericAndRegionalConfigurationsResolveFrenchUiAccessibilityAndPdf() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("fr", "fr-FR", "fr-BE", "fr-CH", "fr-CA").forEach { tag ->
            val context = localizedContext(base, Locale.forLanguageTag(tag))
            mapOf(
                R.string.full_check_title to "Vérification complète",
                R.string.onboarding_title to "Premiers pas",
                R.string.settings_language to "Langue",
                R.string.settings_language_french to "Français",
                R.string.home_cat_battery to "Batterie",
                R.string.perf_ram_title to "RAM",
                R.string.home_cat_storage to "Stockage",
                R.string.history_title to "Historique des rapports",
                R.string.report_saved_title to "Rapport enregistré",
                R.string.comparison_title to "Comparaison de rapports",
                R.string.export_title to "Exporter le rapport",
                R.string.report_retest to "Retester et enregistrer",
                R.string.accessibility_expanded to "Développé",
                R.string.accessibility_collapsed to "Réduit",
                R.string.permission_status_denied to "Autorisation refusée",
                R.string.value_unavailable_short to "n/a",
                R.string.home_settings_content_description to "Ouvrir les paramètres",
                R.string.readout_scroll_hint to "Faites défiler horizontalement pour lire la valeur entière.",
            ).forEach { (id, expected) -> assertEquals(tag, expected, context.getString(id)) }
            val labels = ReportPdfRenderer(context).labels(context)
            assertEquals("Rapport de diagnostic fonecheck", labels.title)
            assertEquals("Non mesuré", labels.statusName(DiagnosticStatus.NOT_TESTED))
            assertEquals("Indisponible", labels.statusName(DiagnosticStatus.NOT_AVAILABLE))
            assertEquals("Échec", labels.statusName(DiagnosticStatus.FAIL))
            assertEquals("Avertissement", labels.statusName(DiagnosticStatus.WARNING))
            assertEquals("Information", labels.statusName(DiagnosticStatus.INFO))
            assertEquals("La vérification n’a pas pu être menée à terme", labels.reasonName(EvidenceReasonCode.ERROR))
            assertEquals("Délai dépassé", labels.reasonName(EvidenceReasonCode.TIMEOUT))
            assertEquals("La vérification n’a pas été terminée", labels.reasonName(EvidenceReasonCode.NOT_RUN))
            assertEquals("Étendue des vérifications", labels.coverage)
            assertEquals("Niveau de confiance", labels.confidence)
            mapOf(
                "battery.health" to "État de la batterie selon Android",
                "battery.level" to "Niveau de charge",
                "performance.ram" to "RAM",
                "camera.capture_dimensions" to "Dimensions de la dernière image de test",
            ).forEach { (checkId, expected) ->
                val resource = requireNotNull(evidenceLabelResource(checkId))
                assertEquals(expected, context.getString(resource.stringResId))
            }
            assertEquals("Charge", labels.stableTextName("charging"))
            assertEquals("Décharge", labels.stableTextName("discharging"))
            assertPdfDecimalFormatting(context, labels, "Page 1 / 2")
            assertEquals(
                "Android signale une surtension de la batterie. Arrêtez la recharge et faites examiner la batterie et le système de charge.",
                context.getString(observationReasonStringRes(ObservationReason.BATTERY_OVER_VOLTAGE)),
            )
            assertEquals(
                "Ce test a été annulé avant l’obtention d’un résultat.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_CANCELLED)),
            )
            assertEquals(
                "Ce test a été ignoré. Exécutez-le pour obtenir un résultat.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_SKIPPED)),
            )
            // Neutral templates accept feminine, masculine, vowel-initial and plural category names.
            listOf(R.string.home_cat_battery, R.string.home_cat_display, R.string.home_cat_sensors).forEach { id ->
                val category = context.getString(id)
                assertEquals("Retester\u00a0: $category", context.getString(R.string.report_retest_title, category))
                assertEquals(
                    "Périmètre\u00a0: $category uniquement.",
                    context.getString(R.string.report_scope_category, category),
                )
            }
            assertTrue(
                context
                    .getString(R.string.licenses_component_inventory)
                    .contains("Mentions relatives aux composants tiers"),
            )
        }
    }

    @Test
    fun integerQuantitiesAndSharedFormattingUseGenericFrench() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val locale = uiLanguageLocale(Locale.forLanguageTag("fr-CA"))
        assertEquals("fr", locale.toLanguageTag())
        val context = localizedContext(base, locale)
        assertZeroOneAndMillionQuantities(locale) { count, quantity ->
            val singular = count <= 1
            val number = formatUiNumber(count, locale)
            val elidedConnector = if (quantity == "many") "d’" else ""
            val connector = if (quantity == "many") "de " else ""
            assertEquals(
                "$number $elidedConnector${if (singular) "échantillon" else "échantillons"}",
                context.resources.getQuantityString(R.plurals.sensor_samples, count, number),
            )
            assertEquals(
                "Il y a $count $connector${if (singular) "jour" else "jours"}",
                context.resources.getQuantityString(R.plurals.home_latest_days_ago, count, count),
            )
            assertEquals(
                "$number $connector${if (singular) "catégorie" else "catégories"}",
                context.resources.getQuantityString(R.plurals.home_status_channel_count, count, number),
            )
            assertEquals(
                "+ $number ${if (singular) "autre" else "autres"}",
                context.resources.getQuantityString(R.plurals.conn_gps_more_sats, count, number),
            )
            assertEquals(
                "$count $elidedConnector${if (singular) "observation" else "observations"} à examiner",
                context.resources.getQuantityString(R.plurals.home_latest_evidence_attention_summary, count, count),
            )
        }
        assertFileSizeAndPercentFormatting(context, locale)
    }

    @Test
    fun localeListsPreferFrenchOrTheNextSupportedLanguage() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        assertLocaleListResolutions(
            base,
            mapOf(
                "fr-FR,en" to "Vérification complète",
                "fr-BE,de" to "Vérification complète",
                "fr-CH,fi" to "Vérification complète",
                "fr-CA,pt-BR" to "Vérification complète",
                "ja-JP,fr,en" to "Vérification complète",
                "es,fr" to "Comprobación completa",
                "pt-BR,fr" to "Verificação completa",
                "de,fr" to "Gesamtcheck",
                "fi,fr" to "Full Check",
                "en,fr" to "Full Check",
                "ja-JP,en" to "Full Check",
            ),
        )
    }
}
