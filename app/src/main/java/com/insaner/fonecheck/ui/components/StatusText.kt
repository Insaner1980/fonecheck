package com.insaner.fonecheck.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.contentColor

/**
 * A short verdict — PASS, ATTENTION, FAIL — as monospace uppercase text in its semantic colour. No
 * pill, no tinted background: the colour alone carries the meaning.
 *
 * Pass [text] in its natural casing; the component uppercases what it draws and keeps the original
 * wording for screen readers.
 */
@Composable
fun StatusText(
    text: String,
    tone: SemanticTone,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(LocalLocale.current.platformLocale),
        style = FonecheckTheme.type.sectionLabel,
        color = tone.contentColor(),
        modifier = modifier.semantics { contentDescription = text },
    )
}
