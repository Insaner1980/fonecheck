package com.insaner.fonecheck.localization

import android.graphics.Paint
import android.graphics.Typeface
import android.icu.text.PluralRules
import android.text.format.Formatter
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
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
class TurkishResourcesTest {
    @Test
    fun turkishRegionsResolveDiagnosticsAccessibilityAndPdfText() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val english = localizedContext(base, Locale.ENGLISH)
        listOf("tr", "tr-TR").forEach { tag ->
            val context = localizedContext(base, Locale.forLanguageTag(tag))
            assertEquals(AppLanguage.TURKISH, AppLanguage.fromLocale(context.resources.configuration.locales[0]))
            mapOf(
                R.string.full_check_title to "Tam kontrol",
                R.string.settings_language_turkish to "Türkçe",
                R.string.onboarding_title to "Başlarken",
                R.string.home_cat_storage to "Depolama",
                R.string.perf_ram_title to "RAM",
                R.string.history_title to "Rapor geçmişi",
                R.string.report_saved_title to "Kaydedilen rapor",
                R.string.comparison_title to "Rapor karşılaştırması",
                R.string.export_title to "Raporu dışa aktar",
                R.string.thermal_headroom_title to "Termal marj",
                R.string.vibration_primitives_supported to "Desteklenen temel öğeler",
                R.string.vibration_effects_supported to "Desteklenen efektler",
                R.string.permission_status_denied to "İzin reddedildi",
                R.string.permission_status_not_requested to "İzin istenmedi",
                R.string.accessibility_expanded to "Genişletildi",
                R.string.accessibility_collapsed to "Daraltıldı",
                R.string.home_settings_content_description to "Ayarları aç",
                R.string.button_status_timed_out to "Zaman aşımı",
                R.string.biometric_cancelled to "İptal edildi",
                R.string.sensor_status_skipped to "Atlandı",
                R.string.conn_not_supported to "Desteklenmiyor",
            ).forEach { (id, expected) -> assertEquals(expected, context.getString(id)) }
            assertRetestCategoryTitles(context, "Yeniden test: ")
            assertObservationReasonsTranslated(english, context)
            assertEquals(
                "Bu test sonuç alınmadan iptal edildi.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_CANCELLED)),
            )
            val labels = ReportPdfRenderer(context).labels(context)
            assertEquals("fonecheck tanılama raporu", labels.title)
            assertEquals("Tamamlanma oranı", labels.coverage)
            assertEquals("Güvenilirlik", labels.confidence)
            assertEquals("Yüksek güvenilirlik", labels.confidenceName(Confidence.HIGH))
            assertEquals("Düşük güvenilirlik", labels.confidenceName(Confidence.LOW))
            mapOf(
                DiagnosticStatus.PASS to "Başarılı",
                DiagnosticStatus.FAIL to "Başarısız",
                DiagnosticStatus.WARNING to "Uyarı",
                DiagnosticStatus.INFO to "Bilgi",
                DiagnosticStatus.NOT_AVAILABLE to "Kullanılamıyor",
                DiagnosticStatus.NOT_TESTED to "Ölçülmedi",
            ).forEach { (status, expected) -> assertEquals(expected, labels.statusName(status)) }
            assertEquals(
                setOf("Otomatik ölçüm", "Android API", "Kullanıcı onayı", "Hesaplanan değer", "Tahmin"),
                EvidenceSource.entries.map(labels.sourceName).toSet(),
            )
            mapOf(
                "battery.health" to "Android pil durumu",
                "performance.ram" to "RAM",
                "camera.capture_dimensions" to "Son test fotoğrafının boyutları",
            ).forEach { (id, expected) ->
                assertEquals(expected, context.getString(requireNotNull(evidenceLabelResource(id)).stringResId))
            }
            assertEquals("Şarj oluyor", labels.stableTextName("charging"))
            assertEquals("Şarj azalıyor", labels.stableTextName("discharging"))
            assertEquals("-12,5", labels.numberValue(-12.5))
            assertEquals("Sayfa 1 / 2", context.getString(R.string.pdf_page, 1, 2))
        }
    }

    @Test
    fun turkishCountsUseSingularNounsAndPercentSignsPrecedeNumbers() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("tr", "tr-TR").forEach { tag ->
            val locale = uiLanguageLocale(Locale.forLanguageTag(tag))
            assertEquals("tr", locale.toLanguageTag())
            val context = localizedContext(base, locale)
            val rules = PluralRules.forLocale(locale)
            assertEquals(setOf("one", "other"), rules.keywords)
            listOf(0, 1, 2, 5, 10, 1234, 1_000_000).forEach { count ->
                assertEquals(if (count == 1) "one" else "other", rules.select(count.toDouble()))
                val number = formatUiNumber(count, locale)
                assertPluralResourceValues(
                    context,
                    mapOf(
                        R.plurals.sensor_samples to "$number örnek",
                        R.plurals.home_latest_days_ago to "$count gün önce",
                        R.plurals.home_status_channel_count to "$number kategori",
                        R.plurals.home_latest_evidence_attention_summary to "$count bulgu dikkat gerektiriyor",
                        R.plurals.conn_gps_more_sats to "+ $number uydu daha",
                    ),
                    count,
                    number,
                )
                assertEquals("adım", context.resources.getQuantityString(R.plurals.sensor_step_unit, count))
                assertEquals("$number örnek", ReportPdfRenderer(context).labels(context).sampleCountValue(count))
            }
            // Quantity resource callers supply integers, not decimal quantities.
            assertEquals("other", rules.select(1.5))
            listOf(0L to "0", 1_500_000L to "1,5").forEach { (bytes, expected) ->
                assertTrue(Formatter.formatFileSize(context, bytes).contains(expected))
            }
            assertEquals("%12,5", context.getString(R.string.report_coverage_value, "12,5"))
            val percent = NumberFormat.getPercentInstance(locale).apply { maximumFractionDigits = 1 }
            assertEquals("%12,5", percent.format(0.125))
        }
    }

    @Test
    fun localeListsPreserveEveryExistingLanguageAndTurkishRegionalFallback() {
        assertLocaleListResolutions(
            InstrumentationRegistry.getInstrumentation().targetContext,
            mapOf(
                "tr" to "Tam kontrol",
                "tr-TR,en" to "Tam kontrol",
                "ja-JP,tr,en" to "Tam kontrol",
                "en,tr" to "Full Check",
                "fi,tr" to "Full Check",
                "es,tr" to "Comprobación completa",
                "pt-BR,tr" to "Verificação completa",
                "de,tr" to "Gesamtcheck",
                "fr,tr" to "Vérification complète",
                "id,tr" to "Pemeriksaan lengkap",
                "sv,tr" to "Fullständig kontroll",
                "nb,tr" to "Fullstendig sjekk",
                "da,tr" to "Fuld kontrol",
                "it,tr" to "Controllo completo",
                "pl,tr" to "Pełna diagnostyka",
                "ja-JP,en" to "Full Check",
            ),
        )
    }

    @Test
    fun androidPdfTypefacesHaveTurkishGlyphs() {
        val fonts = listOf(Typeface.DEFAULT, Typeface.MONOSPACE, Typeface.create("sans-serif-medium", Typeface.NORMAL))
        fonts.forEach { font ->
            val paint = Paint().apply { typeface = font }
            "çğıİöşüÇĞIİÖŞÜ".forEach { letter -> assertTrue(letter.toString(), paint.hasGlyph(letter.toString())) }
        }
    }
}
