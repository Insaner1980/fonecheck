package com.insaner.fonecheck.localization

import androidx.annotation.StringRes
import com.insaner.fonecheck.R
import java.util.Locale

enum class AppLanguage(
    val languageTag: String,
    @StringRes val labelResId: Int,
) {
    SYSTEM("", R.string.settings_language_system),
    ENGLISH("en", R.string.settings_language_english),
    FINNISH("fi", R.string.settings_language_finnish),
    SPANISH("es", R.string.settings_language_spanish),
    PORTUGUESE_BRAZIL("pt-BR", R.string.settings_language_portuguese_brazil),
    GERMAN("de", R.string.settings_language_german),
    FRENCH("fr", R.string.settings_language_french),
    INDONESIAN("id", R.string.settings_language_indonesian),
    ;

    companion object {
        fun fromLocale(locale: Locale?): AppLanguage =
            entries.firstOrNull { it.languageTag == locale?.toLanguageTag() }
                // Older Android returns legacy "in" from Locale.language; BCP-47 keeps "id".
                ?: entries.firstOrNull { it.languageTag == locale?.toLanguageTag()?.substringBefore('-') }
                ?: SYSTEM
    }
}
