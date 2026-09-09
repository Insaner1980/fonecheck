package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/** One full-width option in a single-selection group. */
@Composable
fun SelectionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = FonecheckTheme.spacing.minTouchTarget)
                    .padding(vertical = FonecheckTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = FonecheckTheme.type.rowLabel,
                color = FonecheckTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(FonecheckTheme.spacing.md))
            Box(
                modifier = Modifier.size(PanelToggleSize).clearAndSetSemantics { },
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = FonecheckTheme.colors.textPrimary,
                        modifier = Modifier.size(PanelToggleSize),
                    )
                }
            }
        }
        HairlineRule()
    }
}
