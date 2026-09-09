package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/** A full-width settings row that opens a related screen. */
@Composable
fun NavigationRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stacked = stackedRowLayout()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = FonecheckTheme.spacing.minTouchTarget)
                    .padding(vertical = FonecheckTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = FonecheckTheme.type.rowLabel,
                    color = FonecheckTheme.colors.textSecondary,
                )
                if (stacked) {
                    Text(
                        text = value,
                        style = FonecheckTheme.type.rowValue,
                        color = FonecheckTheme.colors.textPrimary,
                        modifier = Modifier.padding(top = FonecheckTheme.spacing.xs),
                    )
                }
            }
            if (!stacked) {
                Text(
                    text = value,
                    style = FonecheckTheme.type.rowValue,
                    color = FonecheckTheme.colors.textPrimary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.widthIn(max = FonecheckTheme.spacing.rowValueMaxWidth),
                )
                Spacer(modifier = Modifier.width(FonecheckTheme.spacing.sm))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = FonecheckTheme.colors.textMuted,
                modifier = Modifier.size(PanelToggleSize),
            )
        }
        HairlineRule()
    }
}
