package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * A caveat under a row: why a value is missing, how it was derived, what it does not mean. Small,
 * muted and set in the sans face so it reads as commentary rather than as another measurement.
 */
@Composable
fun Note(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = FonecheckTheme.type.note,
        color = FonecheckTheme.colors.textMuted,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = FonecheckTheme.spacing.xs, bottom = FonecheckTheme.spacing.sm),
    )
}
