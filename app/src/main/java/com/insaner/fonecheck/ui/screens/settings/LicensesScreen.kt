package com.insaner.fonecheck.ui.screens.settings

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.theme.FonecheckTheme

@Composable
fun LicensesScreen(modifier: Modifier = Modifier) {
    val resources = LocalResources.current
    val notices =
        remember(resources) {
            val input = resources.openRawResource(R.raw.third_party_notices)
            input.bufferedReader().use { it.readText() }
        }
    TestScreenContent(modifier = modifier) {
        item { Note(stringResource(R.string.licenses_description)) }
        item { SectionHeader(stringResource(R.string.licenses_notices_heading)) }
        item {
            SelectionContainer {
                Text(
                    text = notices,
                    style = FonecheckTheme.type.note,
                    color = FonecheckTheme.colors.textSecondary,
                )
            }
        }
    }
}
