package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * Opens a section: a small uppercase monospace label with a full-weight rule beneath it. The
 * optional [trailing] value sits on the same baseline, for a count, a coverage figure or a
 * timestamp.
 *
 * Pass [label] in its natural casing; the component uppercases what it draws and keeps the original
 * wording for screen readers. [trailing] is drawn exactly as given, because uppercasing a localised
 * value mangles it: a Finnish medium-form date becomes `11. ELOK. 2026`.
 *
 * The trailing value is measured first and never wraps; a label too long to fit beside it wraps
 * instead.
 *
 * The rule beneath is the heavy panel edge: a section is a region of the instrument face, not a
 * row. The lighter [ruleColor] and [ruleThickness] exist for a caller that has to place a header
 * inside an already-framed region.
 */
@Composable
fun SectionHeader(
    label: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    ruleColor: Color = FonecheckTheme.colors.edge,
    ruleThickness: Dp = SectionRuleThickness,
) {
    val locale = LocalLocale.current.platformLocale
    Column(modifier = modifier.fillMaxWidth()) {
        val labelText = label.uppercase(locale)
        val labelModifier =
            Modifier.semantics {
                heading()
                contentDescription = label
            }
        if (stackedRowLayout()) {
            // The row can no longer hold both halves. Side by side the trailing value takes the
            // width it needs and the label is left breaking a word; stacked, each keeps its own line.
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = FonecheckTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.xs),
            ) {
                SectionLabel(text = labelText, modifier = labelModifier)
                trailing?.let { SectionLabel(text = it) }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = FonecheckTheme.spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SectionLabel(
                    text = labelText,
                    modifier = labelModifier.weight(1f, fill = false),
                )
                trailing?.let {
                    SectionLabel(
                        text = it,
                        modifier = Modifier.padding(start = FonecheckTheme.spacing.sm),
                    )
                }
            }
        }
        StrongRule(color = ruleColor, thickness = ruleThickness)
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = FonecheckTheme.type.sectionLabel,
        color = FonecheckTheme.colors.textMuted,
        modifier = modifier,
    )
}

private val SectionRuleThickness = 3.dp
