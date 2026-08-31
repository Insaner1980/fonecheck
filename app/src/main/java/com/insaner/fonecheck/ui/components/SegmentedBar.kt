package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.fillColor

/**
 * One segment per item, each coloured by that item's own result — the shape of a run at a glance,
 * without a chart.
 *
 * The bar is a picture of a count that is always stated in words beside it, so it is hidden from
 * screen readers rather than read out as a row of unlabelled boxes.
 */
@Composable
fun SegmentedBar(
    segments: List<SemanticTone>,
    modifier: Modifier = Modifier,
) {
    if (segments.isEmpty()) {
        return
    }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(FonecheckTheme.spacing.segmentHeight)
                .clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.segmentGap),
    ) {
        segments.forEach { tone ->
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(tone.fillColor()),
            )
        }
    }
}
