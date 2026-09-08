package com.insaner.fonecheck.localization

import java.util.Locale

enum class AppLanguage(
    val languageTag: String,
) {
    SYSTEM(""),
    ENGLISH("en"),
    FINNISH("fi"),
    SPANISH("es"),
    ;

    companion object {
        fun fromLocale(locale: Locale?): AppLanguage =
            entries.firstOrNull { it.languageTag == locale?.language } ?: SYSTEM
    }
}
