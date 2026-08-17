package com.insaner.fonecheck.ui.format

import android.content.res.Configuration
import android.text.format.Formatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

/** Uses the UI language without borrowing regional number conventions from the device locale. */
fun uiLanguageLocale(locale: Locale): Locale = Locale.forLanguageTag(locale.language)

fun formatUiNumber(
    value: Number,
    locale: Locale,
    minimumFractionDigits: Int = 0,
    maximumFractionDigits: Int = minimumFractionDigits,
    grouping: Boolean = false,
): String =
    NumberFormat
        .getNumberInstance(uiLanguageLocale(locale))
        .apply {
            this.minimumFractionDigits = minimumFractionDigits
            this.maximumFractionDigits = maximumFractionDigits
            isGroupingUsed = grouping
        }.format(value)

fun formatUiScientificNumber(
    value: Number,
    locale: Locale,
    fractionDigits: Int,
): String {
    val pattern = "0.${"0".repeat(fractionDigits)}E0"
    return DecimalFormat(pattern, DecimalFormatSymbols(uiLanguageLocale(locale)))
        .format(value)
        .replace('E', 'e')
}

@Composable
fun uiNumber(
    value: Number,
    minimumFractionDigits: Int = 0,
    maximumFractionDigits: Int = minimumFractionDigits,
    grouping: Boolean = false,
): String {
    val locale = uiLanguageLocale(LocalLocale.current.platformLocale)
    return remember(value, locale, minimumFractionDigits, maximumFractionDigits, grouping) {
        formatUiNumber(
            value = value,
            locale = locale,
            minimumFractionDigits = minimumFractionDigits,
            maximumFractionDigits = maximumFractionDigits,
            grouping = grouping,
        )
    }
}

@Composable
fun uiScientificNumber(
    value: Number,
    fractionDigits: Int,
): String {
    val locale = uiLanguageLocale(LocalLocale.current.platformLocale)
    return remember(value, locale, fractionDigits) {
        formatUiScientificNumber(value, locale, fractionDigits)
    }
}

@Composable
fun uiFileSize(bytes: Long): String {
    val context = LocalContext.current
    val locale = uiLanguageLocale(LocalLocale.current.platformLocale)
    return remember(context, bytes, locale) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        Formatter.formatFileSize(context.createConfigurationContext(configuration), bytes)
    }
}
