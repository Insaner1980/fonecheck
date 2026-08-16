package com.insaner.fonecheck.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.contentColor

/** A compact category destination with its latest saved result. */
@Composable
fun CategoryNavigationRow(
    @DrawableRes iconResId: Int,
    label: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: SemanticTone = SemanticTone.NEUTRAL,
    unavailableLabel: String = stringResource(R.string.value_unavailable_short),
    showDivider: Boolean = true,
) {
    val displayedValue = value ?: unavailableLabel
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .heightIn(min = FonecheckTheme.spacing.minTouchTarget)
                    .semantics(mergeDescendants = true) { stateDescription = displayedValue }
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(vertical = FonecheckTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                tint = FonecheckTheme.colors.textSecondary,
                modifier = Modifier.size(FonecheckTheme.spacing.lg),
            )
            Spacer(modifier = Modifier.width(FonecheckTheme.spacing.sm))
            Text(
                text = label,
                style = FonecheckTheme.type.rowLabel,
                color = FonecheckTheme.colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(FonecheckTheme.spacing.sm))
            Text(
                text = displayedValue,
                style = FonecheckTheme.type.rowValue,
                color = if (value == null) FonecheckTheme.colors.textMuted else tone.contentColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(FonecheckTheme.spacing.xs))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = FonecheckTheme.colors.textMuted,
                modifier = Modifier.size(FonecheckTheme.spacing.lg),
            )
        }
        if (showDivider) {
            HairlineRule()
        }
    }
}
