package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.contentColor

/**
 * One measured value: a sans label on the left, a monospace value on the right, a hairline rule
 * below. This is the default row of the app.
 *
 * A null [value] means the app could not read it. The row then draws [unavailableLabel] in muted
 * text and ignores [tone], because a value that was never read carries no verdict. The default label
 * is a plain "n/a"; a screen that knows a more accurate word passes its own, and a [Note] underneath
 * gives the reason.
 *
 * [onValueLongClick] makes the full row a long-press target only when [value] is present. The
 * optional interaction preserves the default non-interactive behavior on existing screens.
 *
 * The value is measured first, up to `FonecheckTheme.spacing.rowValueMaxWidth`, and the label takes
 * everything left over. A short value therefore leaves the label room to stay on one line, which is
 * what keeps a long compound label — Finnish is full of them — from wrapping three ways beside a
 * four-letter reading.
 *
 * The value stays on one line and is ellipsised rather than clipped, so a shortened reading is
 * always visibly shortened. That ellipsis is a defect, not a layout state: a value that reaches it
 * belongs in a [LongValueRow].
 */
@Composable
@Suppress("kotlin:S107") // A row's optional presentation and interaction roles form one stable UI API.
fun DataRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    tone: SemanticTone = SemanticTone.NEUTRAL,
    confidence: Confidence? = null,
    unavailableLabel: String = stringResource(R.string.value_unavailable_short),
    showDivider: Boolean = true,
    onValueLongClick: (() -> Unit)? = null,
    longClickLabel: String = stringResource(R.string.copy_value_action),
    contentVerticalPadding: Dp = FonecheckTheme.spacing.sm,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .valueLongPress(
                    label = longClickLabel,
                    onLongPress = onValueLongClick.takeIf { value != null },
                ),
    ) {
        // A value wider than its cap would be ellipsised beside the label, and an ellipsis is a
        // defect rather than a layout state. Stacking gives it the full width instead.
        val valueOutgrowsRow = valueExceedsRowValueWidth(value)
        if (stackedRowLayout() || valueOutgrowsRow) {
            StackedDataRow(
                label = label,
                value = value,
                tone = tone,
                confidence = confidence,
                unavailableLabel = unavailableLabel,
                contentVerticalPadding = contentVerticalPadding,
            )
        } else {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = contentVerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = FonecheckTheme.type.rowLabel,
                    color = FonecheckTheme.colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(FonecheckTheme.spacing.md))
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.widthIn(max = FonecheckTheme.spacing.rowValueMaxWidth),
                ) {
                    Text(
                        text = value ?: unavailableLabel,
                        style = FonecheckTheme.type.rowValue,
                        color = if (value == null) FonecheckTheme.colors.textMuted else tone.contentColor(),
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    confidence?.let {
                        Text(
                            text = confidenceLabel(it),
                            style = FonecheckTheme.type.sectionLabel,
                            color = FonecheckTheme.colors.textMuted,
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(top = FonecheckTheme.spacing.xs),
                        )
                    }
                }
            }
        }
        if (showDivider) {
            HairlineRule()
        }
    }
}

/**
 * The same row once the font scale leaves no room for two columns: label on its own line, value
 * beneath it at full width. Nothing is ellipsised and no word is broken.
 */
@Composable
private fun StackedDataRow(
    label: String,
    value: String?,
    tone: SemanticTone,
    confidence: Confidence?,
    unavailableLabel: String,
    contentVerticalPadding: Dp,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = contentVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.xs),
    ) {
        Text(
            text = label,
            style = FonecheckTheme.type.rowLabel,
            color = FonecheckTheme.colors.textSecondary,
        )
        Text(
            text = value ?: unavailableLabel,
            style = FonecheckTheme.type.rowValue,
            color = if (value == null) FonecheckTheme.colors.textMuted else tone.contentColor(),
        )
        confidence?.let {
            Text(
                text = confidenceLabel(it),
                style = FonecheckTheme.type.sectionLabel,
                color = FonecheckTheme.colors.textMuted,
            )
        }
    }
}
