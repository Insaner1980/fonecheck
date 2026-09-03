package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * A row of evenly spaced ticks, the way a measuring scale is printed on an instrument face.
 *
 * It marks the edge of a region rather than separating two rows: under the screen title, and above
 * the capture timestamp at the foot of a screen. Inside a section, the divider is a
 * [HairlineRule] or a [StrongRule] instead.
 *
 * The rule carries no meaning of its own, so it is hidden from screen readers.
 */
@Composable
fun InstrumentTickRule(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(FonecheckTheme.spacing.sm)
                .clearAndSetSemantics { },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        repeat(INSTRUMENT_TICK_COUNT) {
            Box(
                modifier =
                    Modifier
                        .width(TickWidth)
                        .height(FonecheckTheme.spacing.sm)
                        .background(FonecheckTheme.colors.edge),
            )
        }
    }
}

private const val INSTRUMENT_TICK_COUNT = 32
private val TickWidth = 3.dp
