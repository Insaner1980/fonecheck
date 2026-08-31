package com.insaner.fonecheck.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.observation.ObservationClassification
import com.insaner.fonecheck.domain.observation.ObservationState

/**
 * The visual role a value carries. This is the whole colour vocabulary available to screens: they
 * pass a tone, never a [Color].
 */
enum class SemanticTone {
    NEUTRAL,
    PASS,
    ATTENTION,
    FAIL,
}

/**
 * The single mapping point from a diagnostic outcome to its visual role. A diagnostic status says
 * what was measured; a tone says how it is drawn. Statuses that report no verdict stay neutral.
 */
fun DiagnosticStatus.toSemanticTone(): SemanticTone =
    when (this) {
        DiagnosticStatus.PASS -> SemanticTone.PASS
        DiagnosticStatus.WARNING -> SemanticTone.ATTENTION
        DiagnosticStatus.FAIL -> SemanticTone.FAIL
        DiagnosticStatus.INFO,
        DiagnosticStatus.NOT_AVAILABLE,
        DiagnosticStatus.NOT_TESTED,
        -> SemanticTone.NEUTRAL
    }

fun ObservationClassification.toSemanticTone(): SemanticTone =
    when (state) {
        ObservationState.PASS -> SemanticTone.PASS
        ObservationState.FAULT -> SemanticTone.FAIL
        ObservationState.NOTED -> SemanticTone.ATTENTION
        ObservationState.NOT_MEASURED -> SemanticTone.NEUTRAL
    }

/** Colour for text drawn in this tone. Every value here clears 4.5:1 against the background. */
@Composable
@ReadOnlyComposable
fun SemanticTone.contentColor(): Color {
    val colors = LocalFonecheckColors.current
    return when (this) {
        SemanticTone.NEUTRAL -> colors.textPrimary
        SemanticTone.PASS -> colors.pass
        SemanticTone.ATTENTION -> colors.attention
        SemanticTone.FAIL -> colors.fail
    }
}

/** Colour for a filled shape in this tone. Never put text on top of these. */
@Composable
@ReadOnlyComposable
fun SemanticTone.fillColor(): Color {
    val colors = LocalFonecheckColors.current
    return when (this) {
        SemanticTone.NEUTRAL -> colors.segmentTrack
        SemanticTone.PASS -> colors.pass
        SemanticTone.ATTENTION -> colors.attentionFill
        SemanticTone.FAIL -> colors.fail
    }
}
