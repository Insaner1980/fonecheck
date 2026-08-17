package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * The single numeric claim that represents a category at a glance. The measured [value] and its
 * [unit] lead the row; the raw values behind that claim stay visible in smaller monospace text.
 *
 * This component deliberately has no unavailable state. A screen only calls it when one measured
 * value honestly carries the weight of the category headline; otherwise the readout is omitted.
 */
@Composable
fun HeadlineReadout(
    value: String,
    unit: String,
    rawValues: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = value,
            style = FonecheckTheme.type.readout,
            color = FonecheckTheme.colors.textPrimary,
            modifier = Modifier.alignByBaseline(),
        )
        Text(
            text = unit,
            style = FonecheckTheme.type.readoutUnit,
            color = FonecheckTheme.colors.textMuted,
            modifier =
                Modifier
                    .alignByBaseline()
                    .padding(start = FonecheckTheme.spacing.xs),
        )
        Spacer(modifier = Modifier.width(FonecheckTheme.spacing.md).weight(1f))
        Text(
            text = rawValues,
            style = FonecheckTheme.type.sectionLabel,
            color = FonecheckTheme.colors.textMuted,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.alignByBaseline(),
        )
    }
}
