package com.insaner.fonecheck.localization

import android.content.Context
import android.content.res.Configuration
import android.icu.text.PluralRules
import android.os.LocaleList
import android.text.format.Formatter
import com.insaner.fonecheck.R
import com.insaner.fonecheck.export.PdfReportLabels
import com.insaner.fonecheck.ui.format.formatUiNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.text.NumberFormat
import java.util.Locale

internal fun localizedContext(
    base: Context,
    locale: Locale,
): Context =
    base.createConfigurationContext(
        Configuration(base.resources.configuration).apply { setLocale(locale) },
    )

internal fun assertLocaleListResolutions(
    base: Context,
    expectedByTags: Map<String, String>,
) {
    expectedByTags.forEach { (tags, expected) ->
        val configuration =
            Configuration(base.resources.configuration).apply { setLocales(LocaleList.forLanguageTags(tags)) }
        assertEquals(
            tags,
            expected,
            base.createConfigurationContext(configuration).getString(R.string.full_check_title),
        )
    }
}

internal fun assertPdfDecimalFormatting(
    context: Context,
    labels: PdfReportLabels,
    expectedPage: String,
) {
    assertEquals(expectedPage, context.getString(R.string.pdf_page, 1, 2))
    assertEquals("-12,5", labels.numberValue(-12.5))
    assertEquals("12,5%", context.getString(R.string.report_coverage_value, labels.numberValue(12.5)))
}

internal fun assertIntegerOneOtherQuantities(
    locale: Locale,
    assertResources: (Int, Boolean, String) -> Unit,
) {
    val rules = PluralRules.forLocale(locale)
    assertEquals(setOf("one", "other"), rules.keywords)
    // These callers use integers; decimal plural rules can differ between languages.
    listOf(0, 1, 2, 1234, 1_000_000).forEach { count ->
        val singular = count == 1
        assertEquals(if (singular) "one" else "other", rules.select(count.toDouble()))
        assertResources(count, singular, formatUiNumber(count, locale))
    }
}

internal fun assertZeroOneAndMillionQuantities(
    locale: Locale,
    assertResources: (Int, String) -> Unit,
) {
    val rules = PluralRules.forLocale(locale)
    // Older ICU can select other instead of many for exact millions.
    listOf(0, 1, 2, 1234, 1_000_000).forEach { count ->
        val quantity = rules.select(count.toDouble())
        if (count <= 1) assertEquals("one", quantity)
        if (count in listOf(2, 1234)) assertEquals("other", quantity)
        if (count == 1_000_000) assertTrue(quantity in setOf("many", "other"))
        assertResources(count, quantity)
    }
}

internal fun assertFileSizeAndPercentFormatting(
    context: Context,
    locale: Locale,
) {
    listOf(1_500_000L to "1,5", 0L to "0").forEach { (bytes, expected) ->
        assertTrue(Formatter.formatFileSize(context, bytes).contains(expected))
    }
    val percent = NumberFormat.getPercentInstance(locale).apply { maximumFractionDigits = 1 }
    assertEquals("12,5", percent.format(0.125).removeSuffix("%").trim())
}
