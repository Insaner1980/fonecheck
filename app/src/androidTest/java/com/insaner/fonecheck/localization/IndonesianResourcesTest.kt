package com.insaner.fonecheck.localization

import android.icu.text.PluralRules
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.observation.ObservationReason
import com.insaner.fonecheck.export.ReportPdfRenderer
import com.insaner.fonecheck.ui.format.formatUiNumber
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class IndonesianResourcesTest {
    @Test
    fun modernAndLegacyLocalesResolveIndonesianUiAccessibilityAndPdf() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val english = localizedContext(base, Locale.ENGLISH)
        listOf("id", "id-ID", "id-SG", "in", "in-ID").forEach { tag ->
            val context = localizedContext(base, Locale.forLanguageTag(tag))
            assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromLocale(context.resources.configuration.locales[0]))
            mapOf(
                R.string.full_check_title to "Pemeriksaan lengkap",
                R.string.onboarding_title to "Memulai",
                R.string.settings_language to "Bahasa",
                R.string.settings_language_indonesian to "Bahasa Indonesia",
                R.string.home_cat_battery to "Baterai",
                R.string.perf_ram_title to "RAM",
                R.string.home_cat_storage to "Penyimpanan",
                R.string.history_title to "Riwayat laporan",
                R.string.report_saved_title to "Laporan tersimpan",
                R.string.comparison_title to "Perbandingan laporan",
                R.string.export_title to "Ekspor laporan",
                R.string.report_retest to "Uji ulang dan simpan",
                R.string.accessibility_expanded to "Diperluas",
                R.string.accessibility_collapsed to "Diciutkan",
                R.string.permission_status_denied to "Izin ditolak",
                R.string.value_unavailable_short to "n/a",
                R.string.home_settings_content_description to "Buka setelan",
                R.string.readout_scroll_hint to "Gulir secara horizontal untuk membaca seluruh nilai.",
            ).forEach { (id, expected) -> assertEquals(tag, expected, context.getString(id)) }
            val labels = ReportPdfRenderer(context).labels(context)
            assertEquals("Laporan diagnostik fonecheck", labels.title)
            assertEquals("Belum diukur", labels.statusName(DiagnosticStatus.NOT_TESTED))
            assertEquals("Tidak tersedia", labels.statusName(DiagnosticStatus.NOT_AVAILABLE))
            assertEquals("Gagal", labels.statusName(DiagnosticStatus.FAIL))
            assertEquals("Peringatan", labels.statusName(DiagnosticStatus.WARNING))
            assertEquals("Informasi", labels.statusName(DiagnosticStatus.INFO))
            assertEquals("Pemeriksaan tidak dapat diselesaikan", labels.reasonName(EvidenceReasonCode.ERROR))
            assertEquals("Waktu habis", labels.reasonName(EvidenceReasonCode.TIMEOUT))
            assertEquals("Pemeriksaan belum diselesaikan", labels.reasonName(EvidenceReasonCode.NOT_RUN))
            assertEquals("Cakupan pemeriksaan", labels.coverage)
            assertEquals("Tingkat keyakinan", labels.confidence)
            assertEquals(
                EvidenceSource.entries.size,
                EvidenceSource.entries
                    .map(labels.sourceName)
                    .toSet()
                    .size,
            )
            mapOf(
                "battery.health" to "Status baterai Android",
                "battery.level" to "Tingkat daya",
                "performance.ram" to "RAM",
                "camera.capture_dimensions" to "Dimensi gambar uji terakhir",
            ).forEach { (checkId, expected) ->
                val resource = requireNotNull(evidenceLabelResource(checkId))
                assertEquals(expected, context.getString(resource.stringResId))
            }
            assertEquals("Mengisi daya", labels.stableTextName("charging"))
            assertEquals("Menggunakan daya", labels.stableTextName("discharging"))
            assertPdfDecimalFormatting(context, labels, "Halaman 1 / 2")
            ObservationReason.entries.forEach { reason ->
                val resource = observationReasonStringRes(reason)
                assertNotEquals(reason.name, english.getString(resource), context.getString(resource))
            }
            assertEquals(
                "Pengujian ini dibatalkan sebelum hasil diperoleh.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_CANCELLED)),
            )
            assertEquals(
                "Pengujian ini dilewati. Jalankan untuk memperoleh hasil.",
                context.getString(observationReasonStringRes(ObservationReason.TEST_SKIPPED)),
            )
            assertTrue(context.getString(R.string.licenses_component_inventory).contains("Pemberitahuan pihak ketiga"))
        }
    }

    @Test
    fun allIntegerQuantitiesUseOtherAndFormattingUsesIndonesian() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val locale = uiLanguageLocale(Locale.forLanguageTag("id-SG"))
        assertEquals("id", locale.toLanguageTag())
        val context = localizedContext(base, locale)
        val rules = PluralRules.forLocale(locale)
        assertEquals(setOf("other"), rules.keywords)
        listOf(0, 1, 2, 1234, 1_000_000).forEach { count ->
            assertEquals("other", rules.select(count.toDouble()))
            val number = formatUiNumber(count, locale)
            assertEquals(
                "$number sampel",
                context.resources.getQuantityString(R.plurals.sensor_samples, count, number),
            )
            assertEquals(
                "$count hari lalu",
                context.resources.getQuantityString(R.plurals.home_latest_days_ago, count, count),
            )
            assertEquals(
                "$number kategori",
                context.resources.getQuantityString(R.plurals.home_status_channel_count, count, number),
            )
            assertEquals(
                "+ $number lagi",
                context.resources.getQuantityString(R.plurals.conn_gps_more_sats, count, number),
            )
            assertEquals(
                "$count bukti perlu perhatian",
                context.resources.getQuantityString(R.plurals.home_latest_evidence_attention_summary, count, count),
            )
        }
        assertFileSizeAndPercentFormatting(context, locale)
        assertEquals("-1.234,5", formatUiNumber(-1234.5, locale, 1, 1, grouping = true))
    }

    @Test
    fun localeListsPreserveIndonesianSelectionAndExistingLanguageFallbacks() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        assertLocaleListResolutions(
            base,
            mapOf(
                "id-ID,en" to "Pemeriksaan lengkap",
                "in-ID,en" to "Pemeriksaan lengkap",
                "ja-JP,id,en" to "Pemeriksaan lengkap",
                "en,id" to "Full Check",
                "fi,id" to "Full Check",
                "es,id" to "Comprobación completa",
                "pt-BR,id" to "Verificação completa",
                "de,id" to "Gesamtcheck",
                "fr,id" to "Vérification complète",
                "ja-JP,en" to "Full Check",
            ),
        )
    }
}
