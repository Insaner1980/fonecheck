package com.insaner.fonecheck.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The 8dp spacing grid, read through [FonecheckTheme.spacing]. Structure comes from dividers and
 * spacing, so these are the only distances a screen is allowed to name.
 */
@Immutable
object FonecheckSpacing {
    /** Half step. Gaps inside a single row only, never for layout rhythm. */
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp

    /** Full-weight rule under a section label. The hairline rule is one physical pixel instead. */
    val ruleThickness: Dp = 1.dp

    /** Small corner radius, for controls only. Nothing else in the app is rounded. */
    val controlRadius: Dp = 4.dp

    val minTouchTarget: Dp = 48.dp

    /**
     * How wide a row label may grow before it wraps. A label is sized to its own text, so this cap
     * is what guarantees the value the rest of the row instead of a fixed share of it.
     */
    val rowLabelMaxWidth: Dp = 160.dp

    val segmentHeight: Dp = 4.dp
    val segmentGap: Dp = 2.dp
}
