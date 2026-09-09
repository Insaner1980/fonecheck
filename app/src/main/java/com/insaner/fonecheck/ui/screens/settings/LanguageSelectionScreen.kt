package com.insaner.fonecheck.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.insaner.fonecheck.localization.AppLanguage
import com.insaner.fonecheck.ui.components.SelectionRow
import com.insaner.fonecheck.ui.components.TestScreenContent
import java.text.Collator

@Composable
fun LanguageSelectionRoute(modifier: Modifier = Modifier) {
    var selectedLanguage by remember {
        mutableStateOf(AppLanguage.fromLocale(AppCompatDelegate.getApplicationLocales()[0]))
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        selectedLanguage = AppLanguage.fromLocale(AppCompatDelegate.getApplicationLocales()[0])
    }
    LanguageSelectionScreen(
        selectedLanguage = selectedLanguage,
        onLanguage = { language ->
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.languageTag))
            selectedLanguage = language
        },
        modifier = modifier,
    )
}

@Composable
fun LanguageSelectionScreen(
    selectedLanguage: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = AppLanguage.entries.associateWith { language -> stringResource(language.labelResId) }
    val locale = LocalLocale.current.platformLocale
    val languages =
        remember(labels, locale) {
            val collator = Collator.getInstance(locale)
            listOf(AppLanguage.SYSTEM) +
                AppLanguage.entries
                    .filterNot { it == AppLanguage.SYSTEM }
                    .sortedWith { first, second ->
                        collator.compare(labels.getValue(first), labels.getValue(second))
                    }
        }

    TestScreenContent(modifier = modifier) {
        item {
            Column(modifier = Modifier.selectableGroup()) {
                languages.forEach { language ->
                    SelectionRow(
                        label = labels.getValue(language),
                        selected = language == selectedLanguage,
                        onClick = { onLanguage(language) },
                        modifier = Modifier.testTag("settings_language_${language.name.lowercase()}"),
                    )
                }
            }
        }
    }
}
