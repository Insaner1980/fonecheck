package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

/**
 * An information screen that is still reading: a travelling rule with the reason beneath it.
 *
 * The message is announced politely rather than assertively — the screen is working, not reporting
 * a problem, and it must not interrupt what a screen reader is already saying.
 *
 * This replaces the spinner. A surface with no cards and no elevation has nowhere to put one.
 */
@Composable
fun ScreenLoadingNote(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        IndeterminateRule()
        Note(
            text = message,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}
