package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * How far through a sequence of steps the reader has got, in the window the panel keeps for a
 * reading.
 *
 * Position is the only bounded quantity a step screen has, and it is what the reader is asking when
 * a step takes a while. [label] states that position in words — `Quick check 3 of 15` — so the bar
 * beneath it repeats a figure instead of being the only carrier of one, and the label is announced
 * politely as it changes.
 *
 * This replaces the Material `LinearProgressIndicator`: a determinate bar in this app is a
 * [WindowBar] inside a window.
 */
@Composable
fun ProgressWindow(
    label: String,
    percentage: Int,
    modifier: Modifier = Modifier,
) {
    ReadoutWindow(modifier = modifier) {
        WindowLabel(
            text = label,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        WindowBar(percentage = percentage)
    }
}
