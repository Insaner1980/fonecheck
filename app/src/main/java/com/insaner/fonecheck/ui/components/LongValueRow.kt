package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.contentColor

/**
 * A measured value too long for one line: build fingerprints, identifiers, serial numbers. The label
 * goes on its own line as a small uppercase monospace label, the value sits below it across the full
 * width, left aligned.
 *
 * The value wraps only after a hyphen, dot or comma, so a token is never cut in the middle, and it
 * is never right-aligned — a wrapped value read from the right edge is unreadable.
 *
 * A null [value] behaves as in [DataRow]: [unavailableLabel] in muted text, [tone] ignored.
 */
// CPD-OFF
// DataRow and LongValueRow intentionally expose the same row API.
@Composable
fun LongValueRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    tone: SemanticTone = SemanticTone.NEUTRAL,
    unavailableLabel: String = stringResource(R.string.value_unavailable_short),
    showDivider: Boolean = true,
) {
    // CPD-ON
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(LocalLocale.current.platformLocale),
            style = FonecheckTheme.type.sectionLabel,
            color = FonecheckTheme.colors.textMuted,
            modifier =
                Modifier
                    .padding(top = FonecheckTheme.spacing.sm)
                    .semantics { contentDescription = label },
        )
        Text(
            text = if (value == null) unavailableLabel else withTokenBreakOpportunities(value),
            style = FonecheckTheme.type.rowValue,
            color = if (value == null) FonecheckTheme.colors.textMuted else tone.contentColor(),
            textAlign = TextAlign.Start,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = FonecheckTheme.spacing.xs, bottom = FonecheckTheme.spacing.sm)
                    // The break hints are invisible but would still be spelled out, so read the original.
                    .semantics { contentDescription = value ?: unavailableLabel },
        )
        if (showDivider) {
            HairlineRule()
        }
    }
}

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
