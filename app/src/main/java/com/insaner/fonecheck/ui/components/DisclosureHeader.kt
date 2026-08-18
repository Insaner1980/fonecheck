package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.contentColor

/**
 * Opens and closes a disclosure section while keeping its real summary visible in the header.
 * Expansion state is exposed to accessibility services instead of being drawn as content.
 *
 * Use a strong divider for top-level screen sections and a hairline divider for repeated rows
 * inside one section.
 */
@Composable
fun DisclosureHeader(
    label: String,
    summary: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: SemanticTone = SemanticTone.NEUTRAL,
    strongDivider: Boolean = true,
) {
    val locale = LocalLocale.current.platformLocale
    val expansionState =
        stringResource(
            if (expanded) R.string.accessibility_expanded else R.string.accessibility_collapsed,
        )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick, role = Role.Button)
                .semantics(mergeDescendants = true) {
                    stateDescription = expansionState
                },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = FonecheckTheme.spacing.minTouchTarget)
                    .padding(bottom = FonecheckTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label.uppercase(locale),
                style = FonecheckTheme.type.sectionLabel,
                color = FonecheckTheme.colors.textMuted,
                modifier =
                    Modifier
                        .weight(1f)
                        .semantics {
                            heading()
                            contentDescription = label
                        },
            )
            Spacer(modifier = Modifier.width(FonecheckTheme.spacing.sm))
            Text(
                text = summary,
                style = FonecheckTheme.type.rowValue,
                color = tone.contentColor(),
            )
            Spacer(modifier = Modifier.width(FonecheckTheme.spacing.xs))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = FonecheckTheme.colors.textMuted,
            )
        }
        if (strongDivider) {
            StrongRule()
        } else {
            HairlineRule()
        }
    }
}
