package com.insaner.fonecheck.ui.format

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val finnishUiDateTimeFormatter =
    DateTimeFormatter.ofPattern("d.M.uuuu 'klo' HH.mm", Locale.forLanguageTag("fi"))

private val technicalUiDateTimeFormatter =
    DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm", Locale.ROOT)

internal fun formatUiDateTime(
    value: Instant,
    locale: Locale,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val uiLocale = uiLanguageLocale(locale)
    val formatter =
        if (uiLocale.language == "fi") {
            finnishUiDateTimeFormatter
        } else {
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(uiLocale)
        }
    return formatter.withZone(zoneId).format(value)
}

internal fun formatTechnicalUiDateTime(
    value: Instant,
    locale: Locale,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val formatter =
        if (uiLanguageLocale(locale).language == "fi") {
            finnishUiDateTimeFormatter
        } else {
            technicalUiDateTimeFormatter
        }
    return formatter.withZone(zoneId).format(value)
}

internal fun formatPdfDateTime(
    value: Instant,
    locale: Locale,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val offset = DateTimeFormatter.ofPattern("xxx", Locale.ROOT).withZone(zoneId).format(value)
    return "${formatUiDateTime(value, locale, zoneId)} UTC$offset"
}
