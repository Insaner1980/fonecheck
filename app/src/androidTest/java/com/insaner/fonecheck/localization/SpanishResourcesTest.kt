package com.insaner.fonecheck.localization

import android.content.res.Configuration
import android.text.format.Formatter
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.export.ReportPdfRenderer
import com.insaner.fonecheck.ui.format.formatUiNumber
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class SpanishResourcesTest {
    @Test
    fun genericAndRegionalSpanishResolveUiAccessibilityEvidenceAndPdf() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val expected =
            mapOf(
                R.string.full_check_title to "Comprobación completa",
                R.string.onboarding_title to "Primeros pasos",
                R.string.settings_language to "Idioma",
                R.string.home_cat_battery to "Batería",
                R.string.home_cat_camera to "Cámara",
                R.string.history_title to "Historial de informes",
                R.string.report_saved_title to "Informe guardado",
                R.string.comparison_title to "Comparación de informes",
                R.string.export_title to "Exportar informe",
                R.string.report_retest to "Volver a comprobar y guardar",
                R.string.accessibility_expanded to "Expandido",
                R.string.accessibility_collapsed to "Contraído",
                R.string.permission_status_denied to "Permiso denegado",
                R.string.status_not_measured to "Sin medir",
                R.string.value_unavailable_short to "n/a",
            )
        listOf("es", "es-ES", "es-MX").forEach { tag ->
            val configuration =
                Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) }
            val context = base.createConfigurationContext(configuration)
            expected.forEach { (id, value) -> assertEquals(tag, value, context.getString(id)) }
            val labels = ReportPdfRenderer(context).labels(context)
            assertEquals("Informe de diagnóstico de fonecheck", labels.title)
            assertEquals("Sin medir", labels.statusName(DiagnosticStatus.NOT_TESTED))
            assertEquals("Fallo", labels.statusName(DiagnosticStatus.FAIL))
            assertEquals("No se pudo completar la comprobación", labels.reasonName(EvidenceReasonCode.ERROR))
            assertEquals("La comprobación no se completó", labels.reasonName(EvidenceReasonCode.NOT_RUN))
            assertEquals("Página 1 / 2", context.getString(R.string.pdf_page, 1, 2))
            assertEquals("-12,5", labels.numberValue(-12.5))
            assertEquals("12,5%", context.getString(R.string.report_coverage_value, labels.numberValue(12.5)))
        }
    }

    @Test
    fun spanishQuantitiesAndFileSizesFollowUiLanguage() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val locale = uiLanguageLocale(Locale.forLanguageTag("es-MX"))
        val configuration = Configuration(base.resources.configuration).apply { setLocale(locale) }
        val context = base.createConfigurationContext(configuration)
        listOf(0, 1, 2, 1234).forEach { count ->
            val number = formatUiNumber(count, locale)
            assertEquals(
                "$number ${if (count == 1) "muestra" else "muestras"}",
                context.resources.getQuantityString(R.plurals.sensor_samples, count, number),
            )
            assertEquals(
                "Hace $count ${if (count == 1) "día" else "días"}",
                context.resources.getQuantityString(R.plurals.home_latest_days_ago, count, count),
            )
            assertEquals(
                "$number ${if (count == 1) "categoría" else "categorías"}",
                context.resources.getQuantityString(R.plurals.home_status_channel_count, count, number),
            )
            assertEquals(
                "+ $number más",
                context.resources.getQuantityString(R.plurals.conn_gps_more_sats, count, number),
            )
            assertEquals(
                "$count ${if (count == 1) "observación requiere" else "observaciones requieren"} atención",
                context.resources.getQuantityString(R.plurals.home_latest_evidence_attention_summary, count, count),
            )
        }
        assertTrue(Formatter.formatFileSize(context, 1_500_000L).contains("1,5"))
        assertTrue(Formatter.formatFileSize(context, 0L).contains("0"))
    }
}
