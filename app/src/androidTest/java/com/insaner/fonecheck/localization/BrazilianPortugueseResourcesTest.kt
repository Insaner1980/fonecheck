package com.insaner.fonecheck.localization

import android.content.res.Configuration
import android.icu.text.PluralRules
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
import java.text.NumberFormat
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class BrazilianPortugueseResourcesTest {
    @Test
    fun brazilianConfigurationResolvesUiAccessibilityEvidenceAndPdf() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration =
            Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag("pt-BR")) }
        val context = base.createConfigurationContext(configuration)
        val expected =
            mapOf(
                R.string.full_check_title to "Verificação completa",
                R.string.onboarding_title to "Primeiros passos",
                R.string.settings_language to "Idioma",
                R.string.settings_language_portuguese_brazil to "Português (Brasil)",
                R.string.home_cat_battery to "Bateria",
                R.string.home_cat_camera to "Câmera",
                R.string.history_title to "Histórico de relatórios",
                R.string.report_saved_title to "Relatório salvo",
                R.string.comparison_title to "Comparação de relatórios",
                R.string.export_title to "Exportar relatório",
                R.string.report_retest to "Testar novamente e salvar",
                R.string.accessibility_expanded to "Expandido",
                R.string.accessibility_collapsed to "Recolhido",
                R.string.permission_status_denied to "Permissão negada",
                R.string.status_not_measured to "Não medido",
                R.string.value_unavailable_short to "n/a",
                R.string.readout_scroll_hint to "Role na horizontal para ler o valor completo.",
            )
        expected.forEach { (id, value) -> assertEquals(value, context.getString(id)) }
        val labels = ReportPdfRenderer(context).labels(context)
        assertEquals("Relatório de diagnóstico do fonecheck", labels.title)
        assertEquals("Não medido", labels.statusName(DiagnosticStatus.NOT_TESTED))
        assertEquals("Falha", labels.statusName(DiagnosticStatus.FAIL))
        assertEquals("Aviso", labels.statusName(DiagnosticStatus.WARNING))
        assertEquals("Informação", labels.statusName(DiagnosticStatus.INFO))
        assertEquals("Não foi possível concluir a verificação", labels.reasonName(EvidenceReasonCode.ERROR))
        assertEquals("A verificação não foi concluída", labels.reasonName(EvidenceReasonCode.NOT_RUN))
        assertPdfDecimalFormatting(context, labels, "Página 1 / 2")
        assertTrue(context.getString(R.string.licenses_component_inventory).contains("componentes de código aberto"))
    }

    @Test
    fun quantitiesUseBrazilianZeroAndOneAndRuntimeMillionRules() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val locale = uiLanguageLocale(Locale.forLanguageTag("pt-BR"))
        val context = localizedContext(base, locale)
        // API 26+ CLDR uses one for 0 and 1 in pt-BR. Newer ICU versions also select many for millions.
        val rules = PluralRules.forLocale(locale)
        listOf(0, 1, 2, 1234, 1_000_000).forEach { count ->
            val quantity = rules.select(count.toDouble())
            if (count <= 1) assertEquals("one", quantity)
            if (count in listOf(2, 1234)) assertEquals("other", quantity)
            if (count == 1_000_000) assertTrue(quantity in setOf("many", "other"))
            val singular = count <= 1
            val connector = if (quantity == "many") "de " else ""
            val number = formatUiNumber(count, locale)
            assertEquals(
                "$number $connector${if (singular) "amostra" else "amostras"}",
                context.resources.getQuantityString(R.plurals.sensor_samples, count, number),
            )
            assertEquals(
                "Há $count $connector${if (singular) "dia" else "dias"}",
                context.resources.getQuantityString(R.plurals.home_latest_days_ago, count, count),
            )
            assertEquals(
                "$number $connector${if (singular) "categoria" else "categorias"}",
                context.resources.getQuantityString(R.plurals.home_status_channel_count, count, number),
            )
            assertEquals(
                "+ $number a mais",
                context.resources.getQuantityString(R.plurals.conn_gps_more_sats, count, number),
            )
            assertEquals(
                "$count $connector${if (singular) "observação requer" else "observações requerem"} atenção",
                context.resources.getQuantityString(R.plurals.home_latest_evidence_attention_summary, count, count),
            )
        }
        listOf(1_500_000L to "1,5", 0L to "0").forEach { (bytes, expectedValue) ->
            assertTrue(Formatter.formatFileSize(context, bytes).contains(expectedValue))
        }
        val percent = NumberFormat.getPercentInstance(locale).apply { maximumFractionDigits = 1 }
        assertEquals("12,5%", percent.format(0.125))
    }

    @Test
    fun platformFallbackUsesTheAvailablePortugueseTranslationAndLocaleListOrder() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        assertLocaleListResolutions(
            base,
            mapOf(
                "pt" to "Verificação completa",
                "pt-PT" to "Verificação completa",
                "it-IT,pt-BR,en" to "Verificação completa",
                "es,pt-BR" to "Comprobación completa",
                "en,pt-BR" to "Full Check",
            ),
        )
    }
}
