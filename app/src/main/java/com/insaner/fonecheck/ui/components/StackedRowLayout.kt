package com.insaner.fonecheck.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity

/**
 * Whether a label-and-value row has to stack instead of sitting side by side.
 *
 * Above this font scale a row cannot hold both halves: the value takes the width it needs and the
 * label is left with too little, so the text layout breaks a word rather than a line. Finnish makes
 * that immediate — `KIIHTYVYYSANTURI` split as `KIIHTYVYY` / `SANTURI`, `LÄMPÖTILANHALLINNAN TILA`
 * interleaved with its own trailing value. Stacking is the only layout that keeps both halves whole.
 *
 * The threshold is a layout decision, not an accessibility one: nothing is hidden below it and
 * nothing is added above it. It is shared so that a section header, a data row and a disclosure row
 * all change shape at the same point, instead of one wrapping while its neighbour still fits.
 */
@Composable
@ReadOnlyComposable
fun stackedRowLayout(): Boolean = LocalDensity.current.fontScale > LARGE_FONT_SCALE_THRESHOLD

/** The point at which a two-column row stops fitting a Finnish compound word beside its value. */
const val LARGE_FONT_SCALE_THRESHOLD = 1.3f
