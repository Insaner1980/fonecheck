package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.contentColor

// CPD-OFF
// DataRow and LongValueRow intentionally expose the same row API.

/**
 * A measured value that may be too long for one line: build fingerprints, identifiers, serial
 * numbers. The component keeps values that fit in the regular [DataRow] layout and changes to the
 * two-line form only when measurement proves that the value needs more width. Null placeholders
 * always use [DataRow]. Call sites must represent other unavailable placeholder states as null.
 *
 * The value wraps only after a hyphen, dot or comma, so a token is never cut in the middle, and it
 * is never right-aligned — a wrapped value read from the right edge is unreadable.
 *
 * A null [value] behaves as in [DataRow]: [unavailableLabel] in muted text, [tone] ignored.
 * [onValueLongClick] follows the same optional interaction contract as [DataRow].
 */
@Composable
fun LongValueRow(
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
    // CPD-ON
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val labelWidth =
        textMeasurer
            .measure(
                text = AnnotatedString(label),
                style = FonecheckTheme.type.rowLabel,
                maxLines = 1,
                softWrap = false,
            ).size.width
    val valueWidth =
        value?.let {
            textMeasurer
                .measure(
                    text = AnnotatedString(it),
                    style = FonecheckTheme.type.rowValue,
                    maxLines = 1,
                    softWrap = false,
                ).size.width
        }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val useLongLayout =
            shouldUseLongValueLayout(
                rowWidth = constraints.maxWidth,
                labelWidth = labelWidth,
                labelMaxWidth =
                    with(density) {
                        FonecheckTheme.spacing.rowLabelMaxWidth.roundToPx()
                    },
                valueWidth = valueWidth,
                rowGap = with(density) { FonecheckTheme.spacing.md.roundToPx() },
            )
        if (useLongLayout && value != null) {
            LongValueLayout(
                label = label,
                value = value,
                tone = tone,
                confidence = confidence,
                showDivider = showDivider,
                onValueLongClick = onValueLongClick,
                longClickLabel = longClickLabel,
                contentVerticalPadding = contentVerticalPadding,
            )
        } else {
            DataRow(
                label = label,
                value = value,
                tone = tone,
                confidence = confidence,
                unavailableLabel = unavailableLabel,
                showDivider = showDivider,
                onValueLongClick = onValueLongClick,
                longClickLabel = longClickLabel,
                contentVerticalPadding = contentVerticalPadding,
            )
        }
    }
}

@Composable
private fun LongValueLayout(
    label: String,
    value: String,
    tone: SemanticTone,
    confidence: Confidence?,
    showDivider: Boolean,
    onValueLongClick: (() -> Unit)?,
    longClickLabel: String,
    contentVerticalPadding: Dp,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .valueLongPress(
                    label = longClickLabel,
                    onLongPress = onValueLongClick,
                ),
    ) {
        Text(
            text = label,
            style = FonecheckTheme.type.rowLabel,
            color = FonecheckTheme.colors.textMuted,
            modifier =
                Modifier
                    .padding(top = contentVerticalPadding)
                    .semantics { contentDescription = label },
        )
        Text(
            text = withTokenBreakOpportunities(value),
            style = FonecheckTheme.type.rowValue,
            color = tone.contentColor(),
            textAlign = TextAlign.Start,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = FonecheckTheme.spacing.xs,
                        bottom = if (confidence == null) contentVerticalPadding else FonecheckTheme.spacing.xs,
                    )
                    // The break hints are invisible but would still be spelled out, so read the original.
                    .semantics { contentDescription = value },
        )
        confidence?.let {
            Text(
                text = confidenceLabel(it),
                style = FonecheckTheme.type.sectionLabel,
                color = FonecheckTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = contentVerticalPadding),
            )
        }
        if (showDivider) {
            HairlineRule()
        }
    }
}

internal fun shouldUseLongValueLayout(
    rowWidth: Int,
    labelWidth: Int,
    labelMaxWidth: Int,
    valueWidth: Int?,
    rowGap: Int,
): Boolean =
    valueWidth != null &&
        valueWidth > (rowWidth - labelWidth.coerceAtMost(labelMaxWidth) - rowGap).coerceAtLeast(0)

private const val ZERO_WIDTH_SPACE = '\u200B'

private const val BREAK_AFTER = "-.,"

/**
 * Marks the only places a long value may wrap. A zero-width space after each separator is a break
 * opportunity the layout can use; the runs between separators contain none, so they stay whole.
 *
 * A single run longer than the line still has to break somewhere — that is a limit of text layout,
 * not a choice.
 */
internal fun withTokenBreakOpportunities(value: String): String =
    buildString(value.length) {
        value.forEachIndexed { index, char ->
            append(char)
            if (char in BREAK_AFTER && index < value.lastIndex) {
                append(ZERO_WIDTH_SPACE)
            }
        }
    }
