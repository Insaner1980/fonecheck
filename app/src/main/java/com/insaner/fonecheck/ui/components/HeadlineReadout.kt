package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * The single numeric claim that represents a category at a glance. The measured [value] and its
 * [unit] lead the row; the supporting values behind that claim stay visible in smaller monospace
 * text.
 *
 * This component deliberately has no unavailable state. A screen only calls it when one measured
 * value honestly carries the weight of the category headline; otherwise the readout is omitted.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun HeadlineReadout(
    value: String,
    unit: String,
    supportingLines: List<String>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.xs),
    ) {
        Row {
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
        }
        Column(horizontalAlignment = Alignment.End) {
            supportingLines.forEach { line ->
                Text(
                    text = line,
                    style = FonecheckTheme.type.sectionLabel,
                    color = FonecheckTheme.colors.textMuted,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
