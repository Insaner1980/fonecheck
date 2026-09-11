package com.insaner.fonecheck.localization

import android.graphics.Paint
import android.graphics.Typeface
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
class PolishResourcesTest {
    @Test
    fun polishRegionsResolveDiagnosticAccessibilityAndPdfText() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val english = localizedContext(base, Locale.ENGLISH)
        listOf("pl", "pl-PL").forEach { tag ->
            val context = localizedContext(base, Locale.forLanguageTag(tag))
            assertEquals(AppLanguage.POLISH, AppLanguage.fromLocale(context.resources.configuration.locales[0]))
            mapOf(
                R.string.full_check_title to "Pełna diagnostyka",
                R.string.onboarding_title to "Pierwsze kroki",
                R.string.settings_language to "Język",
                R.string.settings_language_polish to "Polski",
                R.string.home_cat_storage to "Pamięć",
                R.string.perf_ram_title to "RAM",
                R.string.history_title to "Historia raportów",
                R.string.report_saved_title to "Zapisany raport",
                R.string.comparison_title to "Porównanie raportów",
                R.string.export_title to "Eksport raportu",
                R.string.report_retest to "Powtórz test i zapisz",
                R.string.accessibility_expanded to "Rozwinięte",
                R.string.accessibility_collapsed to "Zwinięte",
                R.string.permission_status_denied to "Odmówiono uprawnienia",
                R.string.value_unavailable_short to "n/a",
                R.string.home_settings_content_description to "Otwórz ustawienia",
                R.string.readout_scroll_hint to "Przewiń w poziomie, aby odczytać całą wartość.",
                R.string.thermal_headroom_title to "Margines termiczny",
            ).forEach { (id, expected) -> assertEquals(tag, expected, context.getString(id)) }
            assertRetestCategoryTitles(context, "Ponowny test: ")
            val labels = ReportPdfRenderer(context).labels(context)
            mapOf(
                DiagnosticStatus.PASS to "Wynik pozytywny",
                DiagnosticStatus.FAIL to "Wynik negatywny",
                DiagnosticStatus.WARNING to "Ostrzeżenie",
                DiagnosticStatus.INFO to "Informacja",
                DiagnosticStatus.NOT_AVAILABLE to "Niedostępne",
                DiagnosticStatus.NOT_TESTED to "Nie zmierzono",
            ).forEach { (status, expected) -> assertEquals(expected, labels.statusName(status)) }
            assertEquals("Raport diagnostyczny fonecheck", labels.title)
            assertEquals("Stopień ukończenia", labels.coverage)
            assertEquals("Wiarygodność", labels.confidence)
            mapOf(
                Confidence.HIGH to "Wysoka wiarygodność",
                Confidence.LOW to "Niska wiarygodność",
                Confidence.UNAVAILABLE to "Niedostępne",
            ).forEach { (confidence, expected) -> assertEquals(expected, labels.confidenceName(confidence)) }
            assertEquals(
                setOf(
                    "Pomiar automatyczny",
                    "API Androida",
                    "Potwierdzenie użytkownika",
                    "Wartość wyliczona",
                    "Oszacowanie",
                ),
                EvidenceSource.entries.map(labels.sourceName).toSet(),
            )
            mapOf(
                EvidenceReasonCode.ERROR to "Nie udało się ukończyć testu",
                EvidenceReasonCode.TIMEOUT to "Przekroczono limit czasu",
                EvidenceReasonCode.NOT_RUN to "Test nie został ukończony",
            ).forEach { (reason, expected) -> assertEquals(expected, labels.reasonName(reason)) }
            mapOf(
                "battery.health" to "Stan baterii według Androida",
                "battery.level" to "Poziom naładowania",
                "performance.ram" to "RAM",
                "camera.capture_dimensions" to "Wymiary ostatniego zdjęcia testowego",
            ).forEach { (id, expected) ->
                assertEquals(expected, context.getString(requireNotNull(evidenceLabelResource(id)).stringResId))
            }
            assertEquals("Ładowanie", labels.stableTextName("charging"))
            assertEquals("Rozładowywanie", labels.stableTextName("discharging"))
            assertObservationReasonsTranslated(english, context)
            assertEquals(
                "Ten test anulowano przed uzyskaniem wyniku.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_CANCELLED)),
            )
            assertEquals(
                "Ten test został pominięty. Uruchom go, aby uzyskać wynik.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_SKIPPED)),
            )
            assertPdfDecimalFormatting(context, labels, "Strona 1 / 2")
        }
    }

    @Test
    fun polishPluralResourcesUseTheActualCountForAllIntegerCallers() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val countsByQuantity =
            mapOf(
                "one" to listOf(1),
                "few" to listOf(2, 3, 4, 22, 24, 102),
                "many" to listOf(0, 5, 10, 12, 14, 21, 25, 100, 101, 105),
            )
        listOf("pl", "pl-PL").forEach { tag ->
            val locale = uiLanguageLocale(Locale.forLanguageTag(tag))
            assertEquals("pl", locale.toLanguageTag())
            val context = localizedContext(base, Locale.forLanguageTag(tag))
            val rules = PluralRules.forLocale(locale)
            assertEquals(setOf("one", "few", "many", "other"), rules.keywords)
            countsByQuantity.forEach { (quantity, counts) ->
                val sampleNoun =
                    when (quantity) {
                        "one" -> "próbka"
                        "few" -> "próbki"
                        else -> "próbek"
                    }
                val categoryNoun =
                    when (quantity) {
                        "one" -> "kategoria"
                        "few" -> "kategorie"
                        else -> "kategorii"
                    }
                val attention =
                    when (quantity) {
                        "one" -> "obserwacja wymaga uwagi"
                        "few" -> "obserwacje wymagają uwagi"
                        else -> "obserwacji wymaga uwagi"
                    }
                val satellite =
                    when (quantity) {
                        "one" -> "dodatkowy satelita"
                        "few" -> "dodatkowe satelity"
                        else -> "dodatkowych satelitów"
                    }
                val step =
                    when (quantity) {
                        "one" -> "krok"
                        "few" -> "kroki"
                        else -> "kroków"
                    }
                counts.forEach { count ->
                    assertEquals("$tag: $count", quantity, rules.select(count.toDouble()))
                    val number = formatUiNumber(count, locale)
                    assertPluralResourceValues(
                        context,
                        mapOf(
                            R.plurals.sensor_samples to "$number $sampleNoun",
                            R.plurals.home_latest_days_ago to "$count ${if (count == 1) "dzień" else "dni"} temu",
                            R.plurals.home_status_channel_count to "$number $categoryNoun",
                            R.plurals.home_latest_evidence_attention_summary to "$count $attention",
                            R.plurals.conn_gps_more_sats to "+ $number $satellite",
                        ),
                        count,
                        number,
                    )
                    assertEquals(step, context.resources.getQuantityString(R.plurals.sensor_step_unit, count))
                    assertEquals(
                        "$number $sampleNoun",
                        ReportPdfRenderer(context).labels(context).sampleCountValue(count),
                    )
                }
            }
            // Android quantity resources are selected with integers; no caller supplies a decimal quantity.
            assertEquals("other", rules.select(1.5))
            assertFileSizeAndPercentFormatting(localizedContext(base, locale), locale)
        }
    }

    @Test
    fun localeListsPreservePolishResolutionAndEveryExistingLanguagePriority() {
        assertLocaleListResolutions(
            InstrumentationRegistry.getInstrumentation().targetContext,
            mapOf(
                "pl" to "Pełna diagnostyka",
                "pl-PL,en" to "Pełna diagnostyka",
                "ja-JP,pl,en" to "Pełna diagnostyka",
                "en,pl" to "Full Check",
                "fi,pl" to "Full Check",
                "es,pl" to "Comprobación completa",
                "pt-BR,pl" to "Verificação completa",
                "de,pl" to "Gesamtcheck",
                "fr,pl" to "Vérification complète",
                "id,pl" to "Pemeriksaan lengkap",
                "sv,pl" to "Fullständig kontroll",
                "nb,pl" to "Fullstendig sjekk",
                "da,pl" to "Fuld kontrol",
                "it,pl" to "Controllo completo",
                "ja-JP,en" to "Full Check",
            ),
        )
    }

    @Test
    fun androidPdfTypefacesHavePolishGlyphs() {
        val fonts = listOf(Typeface.DEFAULT, Typeface.MONOSPACE, Typeface.create("sans-serif-medium", Typeface.NORMAL))
        fonts.forEach { font ->
            val paint = Paint().apply { typeface = font }
            "ąćęłńóśźżĄĆĘŁŃÓŚŹŻ".forEach { letter -> assertTrue(letter.toString(), paint.hasGlyph(letter.toString())) }
        }
    }
}
