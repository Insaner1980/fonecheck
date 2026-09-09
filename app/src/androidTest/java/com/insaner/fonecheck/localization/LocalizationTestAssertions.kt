package com.insaner.fonecheck.localization

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import com.insaner.fonecheck.R
import com.insaner.fonecheck.export.PdfReportLabels
import org.junit.Assert.assertEquals
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
