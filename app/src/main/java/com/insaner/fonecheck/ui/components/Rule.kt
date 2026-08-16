package com.insaner.fonecheck.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * A hairline divider: one physical pixel at any density, so it stays a hairline instead of thickening
 * on high-density screens the way a 1dp line would.
 */
@Composable
fun HairlineRule(modifier: Modifier = Modifier) {
    val thickness = with(LocalDensity.current) { 1f.toDp() }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(thickness)
                .background(FonecheckTheme.colors.ruleHairline),
    )
}

/** The full-weight rule that sits under a section label. Used nowhere else. */
@Composable
fun StrongRule(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(FonecheckTheme.spacing.ruleThickness)
                .background(FonecheckTheme.colors.ruleStrong),
    )
}

/** How much of the track the travelling segment covers. */
private const val SWEEP_FRACTION = 0.28f
private const val SWEEP_DURATION_MILLIS = 1_100

/**
 * A hairline that is waiting for something: a muted segment travelling along an otherwise ordinary
 * [HairlineRule]. Indeterminate by nature — it reports that work is in progress, never how far it
 * has got.
 *
 * It replaces a spinner, which has no place in a surface with no cards and no elevation. Purely
 * decorative: the line beside it states what is loading, so this is hidden from screen readers.
 */
@Composable
fun IndeterminateRule(modifier: Modifier = Modifier) {
    val thickness = with(LocalDensity.current) { 1f.toDp() }
    val track = FonecheckTheme.colors.ruleHairline
    val sweep = FonecheckTheme.colors.textMuted
    val transition = rememberInfiniteTransition(label = "indeterminate rule")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = SWEEP_DURATION_MILLIS, easing = LinearEasing),
            ),
        label = "sweep",
    )
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(thickness)
                .clearAndSetSemantics { },
    ) {
        drawRect(color = track)
        val sweepWidth = size.width * SWEEP_FRACTION
        drawRect(
            color = sweep,
            topLeft = Offset(x = (size.width + sweepWidth) * progress - sweepWidth, y = 0f),
            size = Size(width = sweepWidth, height = size.height),
        )
    }
}
