package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Thermal headroom as a panel dial.
 *
 * Headroom is a bounded fraction with a meaningful threshold — 1.0 is the device's severe
 * throttling point — which is what a dial reads better than a number does. It is the only value in
 * the app shaped that way; a reading with no threshold stays a [WindowBar] or a plain row.
 *
 * The scale is fixed at 0…1.0 and never rescales. `PowerManager.getThermalHeadroom()` does return
 * values above 1.0, and there the needle stops at the end of the arc rather than travelling past
 * it, while the whole arc turns to the alert colour. A dial that silently restretched its own scale
 * would be lying about what it is showing.
 *
 * [headroom] is null when the reading is unavailable — below Android 11, or when the platform
 * declined it. The dial then draws unlit, with no needle.
 *
 * The dial is decorative: it repeats a figure that the caller states in words directly beneath it,
 * so it is hidden from screen readers.
 */
@Composable
fun ThermalHeadroomGauge(
    headroom: Float?,
    modifier: Modifier = Modifier,
) {
    val colors = FonecheckTheme.colors
    val textMeasurer = rememberTextMeasurer()
    val scaleMinimumLabel = uiNumber(0)
    val scaleMaximumLabel = uiNumber(1.0, minimumFractionDigits = 1, maximumFractionDigits = 1)
    val scaleStyle =
        FonecheckTheme.type.sectionLabel.copy(color = colors.windowDim)
    val overThreshold = headroom != null && headroom > 1f
    val trackColor =
        when {
            headroom == null -> colors.windowOff
            overThreshold -> colors.windowAlert
            else -> colors.windowDim
        }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(GaugeHeight)
                    .clearAndSetSemantics { },
        ) {
            val radius = gaugeRadius()
            val pivot = gaugePivot(radius)
            val arcTopLeft = Offset(pivot.x - radius, pivot.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)
            val arcStyle = Stroke(width = ArcStroke.toPx(), cap = StrokeCap.Butt)

            drawArc(
                color = trackColor,
                startAngle = START_ANGLE_DEGREES,
                sweepAngle = SWEEP_ANGLE_DEGREES,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = arcStyle,
            )
            // The danger band is already the whole arc once the reading is over the threshold.
            if (headroom != null && !overThreshold) {
                drawArc(
                    color = colors.windowAlert,
                    startAngle = START_ANGLE_DEGREES + SWEEP_ANGLE_DEGREES * DANGER_BAND_START,
                    sweepAngle = SWEEP_ANGLE_DEGREES * (1f - DANGER_BAND_START),
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = arcStyle,
                )
            }
            drawScaleTicks(pivot = pivot, radius = radius, color = trackColor)
            if (headroom != null) {
                drawNeedle(
                    pivot = pivot,
                    radius = radius,
                    // Pinned, not rescaled: the arc keeps meaning 0…1.0 whatever the reading is.
                    fraction = headroom.coerceIn(0f, 1f),
                    color = if (overThreshold) colors.windowAlert else colors.windowText,
                )
            }
            drawScaleLabels(
                textMeasurer = textMeasurer,
                style = scaleStyle,
                pivot = pivot,
                radius = radius,
                minimumLabel = scaleMinimumLabel,
                maximumLabel = scaleMaximumLabel,
            )
        }
    }
}

private fun DrawScope.gaugeRadius(): Float {
    val horizontal = size.width / 2f - ArcStroke.toPx() - ScaleLabelInset.toPx()
    val vertical = size.height * PIVOT_HEIGHT_FRACTION - ArcStroke.toPx()
    return minOf(horizontal, vertical)
}

private fun DrawScope.gaugePivot(radius: Float): Offset = Offset(size.width / 2f, radius + ArcStroke.toPx())

private fun DrawScope.pointOnArc(
    pivot: Offset,
    radius: Float,
    fraction: Float,
): Offset {
    val radians = Math.toRadians((START_ANGLE_DEGREES + SWEEP_ANGLE_DEGREES * fraction).toDouble())
    return Offset(
        x = pivot.x + radius * cos(radians).toFloat(),
        y = pivot.y + radius * sin(radians).toFloat(),
    )
}

private fun DrawScope.drawScaleTicks(
    pivot: Offset,
    radius: Float,
    color: Color,
) {
    val tickLength = ArcStroke.toPx() * TICK_LENGTH_RATIO
    val inner = radius - ArcStroke.toPx() / 2f - tickLength
    val outer = radius - ArcStroke.toPx() / 2f
    repeat(TICK_COUNT) { index ->
        val fraction = index / (TICK_COUNT - 1f)
        drawLine(
            color = color,
            start = pointOnArc(pivot, inner, fraction),
            end = pointOnArc(pivot, outer, fraction),
            strokeWidth = TickStroke.toPx(),
            cap = StrokeCap.Butt,
        )
    }
}

private fun DrawScope.drawNeedle(
    pivot: Offset,
    radius: Float,
    fraction: Float,
    color: Color,
) {
    drawLine(
        color = color,
        start = pivot,
        end = pointOnArc(pivot, radius - ArcStroke.toPx() * NEEDLE_TIP_GAP_RATIO, fraction),
        strokeWidth = NeedleStroke.toPx(),
        cap = StrokeCap.Butt,
    )
    drawCircle(color = color, radius = NeedleHubRadius.toPx(), center = pivot)
}

private fun DrawScope.drawScaleLabels(
    textMeasurer: TextMeasurer,
    style: TextStyle,
    pivot: Offset,
    radius: Float,
    minimumLabel: String,
    maximumLabel: String,
) {
    listOf(0f to minimumLabel, 1f to maximumLabel).forEach { (fraction, label) ->
        val measured = textMeasurer.measure(label, style)
        val anchor = pointOnArc(pivot, radius, fraction)
        drawText(
            textLayoutResult = measured,
            topLeft =
                Offset(
                    x = anchor.x - measured.size.width / 2f,
                    y = anchor.y + ArcStroke.toPx(),
                ),
        )
    }
}

private const val START_ANGLE_DEGREES = 180f
private const val SWEEP_ANGLE_DEGREES = 180f

/** The last band of the scale, where the device is approaching its throttling threshold. */
private const val DANGER_BAND_START = 0.85f

private const val TICK_COUNT = 5
private const val TICK_LENGTH_RATIO = 0.9f
private const val NEEDLE_TIP_GAP_RATIO = 1.4f
private const val PIVOT_HEIGHT_FRACTION = 0.78f
private val GaugeHeight = 132.dp
private val ArcStroke = 8.dp
private val TickStroke = 2.dp
private val NeedleStroke = 3.dp
private val NeedleHubRadius = 5.dp
private val ScaleLabelInset = 10.dp
