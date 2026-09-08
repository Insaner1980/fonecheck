package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.FonecheckType

/** The approved shared geometry. Debug specimens can still compare the earlier geometry. */
data class SegmentGeometry(
    val digitAdvance: Float = 0.78f,
    val pointAdvance: Float = 0.16f,
    val horizontalInset: Float = 0.55f,
    val outerGap: Float = 0.55f,
    val bevel: Float = 0.5f,
    val pointRadius: Float = 0.05f,
    val pointOffset: Float = -0.10f,
)

/**
 * A flat seven-segment rendering for the numeric part of a featured instrument reading.
 *
 * Pointed bevels and narrow internal gaps group each digit, with wider spacing between digits.
 * Inactive segments are hidden by default for the active-only readability trial.
 *
 * Digits keep fixed-width cells while decimal marks, signs and ratio separators use narrower
 * cells. Overflow scrolls manually, preserving the requested accessible text size and precision.
 * [value] is exposed as one text node; the individual segments have no semantics.
 */
@Composable
fun SegmentedNumber(
    value: String,
    modifier: Modifier = Modifier,
    style: TextStyle = FonecheckType.readout,
    color: Color = FonecheckTheme.colors.windowText,
    inactiveColor: Color = Color.Transparent,
    geometry: SegmentGeometry = SegmentGeometry(),
    showOverflowHint: Boolean = true,
) {
    val density = LocalDensity.current
    val figureHeight = style.figureHeight()
    val requestedHeight = with(density) { figureHeight.toDp() }
    val requestedWidth = requestedHeight * segmentedWidth(value, geometry)
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = modifier) {
        val overflow = requestedWidth > maxWidth
        Column {
            Canvas(
                modifier =
                    Modifier
                        .horizontalScroll(scrollState, enabled = overflow)
                        .width(requestedWidth)
                        .height(requestedHeight)
                        .semantics { text = AnnotatedString(value) },
            ) {
                drawSegmentedValue(
                    value = value,
                    activeColor = color,
                    inactiveColor = inactiveColor,
                    geometry = geometry,
                )
            }
            if (overflow && showOverflowHint) ReadoutScrollHint()
        }
    }
}

@Composable
internal fun ReadoutScrollHint() {
    Text(
        text = stringResource(R.string.readout_scroll_hint),
        style = FonecheckTheme.type.note,
        color = FonecheckTheme.colors.windowText,
        // The scroll node already exposes the full value and accessibility scroll actions.
        modifier = Modifier.padding(top = FonecheckTheme.spacing.sm).clearAndSetSemantics { },
    )
}

internal data class SegmentedFigureParts(
    val number: String,
    val suffix: String?,
)

/** Splits `2.07 GB` into the segment-rendered number and the normally typeset unit. */
internal fun segmentedFigureParts(value: String): SegmentedFigureParts? {
    val trimmed = value.trimSegmentSpaces()
    if (trimmed.none(Char::isSegmentDigit)) return null

    val numberEnd = trimmed.indexOfFirst { !it.isSegmentCharacter() }.let { if (it < 0) trimmed.length else it }
    val number = trimmed.substring(0, numberEnd).trimEnd(Char::isSegmentSpace)
    if (number.none(Char::isSegmentDigit)) return null

    return SegmentedFigureParts(
        number = number,
        suffix = trimmed.substring(numberEnd).trimSegmentSpaces().ifEmpty { null },
    )
}

internal fun segmentMask(character: Char): Int =
    when (character) {
        '0' -> TOP or UPPER_RIGHT or LOWER_RIGHT or BOTTOM or LOWER_LEFT or UPPER_LEFT
        '1' -> UPPER_RIGHT or LOWER_RIGHT
        '2' -> TOP or UPPER_RIGHT or MIDDLE or LOWER_LEFT or BOTTOM
        '3' -> TOP or UPPER_RIGHT or MIDDLE or LOWER_RIGHT or BOTTOM
        '4' -> UPPER_LEFT or MIDDLE or UPPER_RIGHT or LOWER_RIGHT
        '5' -> TOP or UPPER_LEFT or MIDDLE or LOWER_RIGHT or BOTTOM
        '6' -> TOP or UPPER_LEFT or MIDDLE or LOWER_LEFT or LOWER_RIGHT or BOTTOM
        '7' -> TOP or UPPER_RIGHT or LOWER_RIGHT
        '8' -> ALL_SEGMENTS
        '9' -> TOP or UPPER_LEFT or UPPER_RIGHT or MIDDLE or LOWER_RIGHT or BOTTOM
        'e', 'E' -> TOP or UPPER_LEFT or MIDDLE or LOWER_LEFT or BOTTOM
        else -> 0
    }

internal fun TextStyle.figureHeight(): TextUnit =
    when {
        lineHeight != TextUnit.Unspecified -> lineHeight
        fontSize != TextUnit.Unspecified -> fontSize
        else -> FonecheckType.readout.lineHeight
    }

private fun Char.isSegmentCharacter(): Boolean =
    isSegmentDigit() ||
        isSegmentSpace() ||
        this == 'e' ||
        this == 'E' ||
        this == '+' ||
        this == '-' ||
        this == UNICODE_MINUS ||
        this == '.' ||
        this == ',' ||
        this == '/' ||
        this == ':'

private fun Char.isSegmentDigit(): Boolean = this in '0'..'9'

private fun Char.isSegmentSpace(): Boolean =
    isWhitespace() ||
        this == NON_BREAKING_SPACE ||
        this == NARROW_NO_BREAK_SPACE

private fun String.trimSegmentSpaces(): String = trim(Char::isSegmentSpace)

internal fun segmentedWidth(
    value: String,
    geometry: SegmentGeometry = SegmentGeometry(),
): Float = value.sumOf { it.advance(geometry).toDouble() }.toFloat()

private fun Char.advance(geometry: SegmentGeometry): Float =
    when {
        isSegmentDigit() || this == 'e' || this == 'E' -> geometry.digitAdvance
        isSegmentSpace() -> SPACE_ADVANCE
        this == '.' || this == ',' -> geometry.pointAdvance
        this == ':' -> POINT_ADVANCE
        this == '/' -> SLASH_ADVANCE
        else -> SIGN_ADVANCE
    }

private fun DrawScope.drawSegmentedValue(
    value: String,
    activeColor: Color,
    inactiveColor: Color,
    geometry: SegmentGeometry,
) {
    var left = 0f
    value.forEach { character ->
        val advance = character.advance(geometry) * size.height
        when {
            character.isSegmentDigit() || character == 'e' || character == 'E' ->
                drawDigit(
                    character = character,
                    left = left,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    geometry = geometry,
                )

            character == '.' -> drawPoint(left, activeColor, geometry)
            character == ',' -> drawComma(left, activeColor, geometry)
            character == ':' -> drawColon(left, activeColor)
            character == '/' -> drawSlash(left, activeColor)
            character == '+' -> drawSign(left, activeColor, drawVertical = true)
            character == '-' || character == UNICODE_MINUS -> drawSign(left, activeColor, drawVertical = false)
        }
        left += advance
    }
}

private fun DrawScope.drawDigit(
    character: Char,
    left: Float,
    activeColor: Color,
    inactiveColor: Color,
    geometry: SegmentGeometry,
) {
    val activeMask = segmentMask(character)
    DIGIT_SEGMENTS.forEach { segment ->
        if (activeMask and segment != 0) {
            drawDigitSegment(segment, left, activeColor, activeMask, geometry)
        } else if (inactiveColor.alpha > 0f) {
            drawDigitSegment(segment, left, inactiveColor, ALL_SEGMENTS, geometry)
        }
    }
}

private fun DrawScope.drawDigitSegment(
    segment: Int,
    left: Float,
    color: Color,
    activeMask: Int,
    geometry: SegmentGeometry,
) {
    val height = size.height
    val width = height * DIGIT_WIDTH
    val thickness = height * SEGMENT_THICKNESS
    val halfHeight = height / 2f
    val horizontalLeft = left + thickness * geometry.horizontalInset
    val horizontalRight = left + width - thickness * geometry.horizontalInset
    val leftCenter = left + thickness / 2f
    val rightCenter = left + width - thickness / 2f
    // Without a horizontal bar, let the vertical strokes carry the digit's full outline.
    // This gives 1 and 7 a stronger stem while preserving a small segment break.
    val upperTop = if (activeMask and TOP != 0) thickness * geometry.outerGap else 0f
    val centerGap =
        if (activeMask and MIDDLE != 0) thickness * CENTER_SEGMENT_GAP else thickness * STEM_GAP
    val upperBottom = halfHeight - centerGap
    val lowerTop = halfHeight + centerGap
    val lowerBottom =
        if (activeMask and BOTTOM != 0) height - thickness * geometry.outerGap else height

    when (segment) {
        TOP ->
            drawHorizontalSegment(
                horizontalLeft,
                horizontalRight,
                thickness / 2f,
                thickness,
                color,
                geometry.bevel,
            )
        UPPER_RIGHT -> drawVerticalSegment(rightCenter, upperTop, upperBottom, thickness, color, geometry.bevel)
        LOWER_RIGHT -> drawVerticalSegment(rightCenter, lowerTop, lowerBottom, thickness, color, geometry.bevel)
        BOTTOM ->
            drawHorizontalSegment(
                horizontalLeft,
                horizontalRight,
                height - thickness / 2f,
                thickness,
                color,
                geometry.bevel,
            )
        LOWER_LEFT -> drawVerticalSegment(leftCenter, lowerTop, lowerBottom, thickness, color, geometry.bevel)
        UPPER_LEFT -> drawVerticalSegment(leftCenter, upperTop, upperBottom, thickness, color, geometry.bevel)
        MIDDLE -> drawHorizontalSegment(horizontalLeft, horizontalRight, halfHeight, thickness, color, geometry.bevel)
    }
}

private fun DrawScope.drawHorizontalSegment(
    startX: Float,
    endX: Float,
    centerY: Float,
    thickness: Float,
    color: Color,
    bevelRatio: Float,
) {
    val halfThickness = thickness / 2f
    val bevel = thickness * bevelRatio
    val path =
        Path().apply {
            moveTo(startX, centerY - halfThickness + bevel)
            lineTo(startX + bevel, centerY - halfThickness)
            lineTo(endX - bevel, centerY - halfThickness)
            lineTo(endX, centerY - halfThickness + bevel)
            lineTo(endX, centerY + halfThickness - bevel)
            lineTo(endX - bevel, centerY + halfThickness)
            lineTo(startX + bevel, centerY + halfThickness)
            lineTo(startX, centerY + halfThickness - bevel)
            close()
        }
    drawPath(path, color)
}

private fun DrawScope.drawVerticalSegment(
    centerX: Float,
    startY: Float,
    endY: Float,
    thickness: Float,
    color: Color,
    bevelRatio: Float,
) {
    val halfThickness = thickness / 2f
    val bevel = thickness * bevelRatio
    val path =
        Path().apply {
            moveTo(centerX - halfThickness + bevel, startY)
            lineTo(centerX + halfThickness - bevel, startY)
            lineTo(centerX + halfThickness, startY + bevel)
            lineTo(centerX + halfThickness, endY - bevel)
            lineTo(centerX + halfThickness - bevel, endY)
            lineTo(centerX - halfThickness + bevel, endY)
            lineTo(centerX - halfThickness, endY - bevel)
            lineTo(centerX - halfThickness, startY + bevel)
            close()
        }
    drawPath(path, color)
}

private fun DrawScope.drawPoint(
    left: Float,
    color: Color,
    geometry: SegmentGeometry,
) {
    val radius = size.height * geometry.pointRadius
    drawCircle(
        color = color,
        radius = radius,
        center = Offset((left + size.height * geometry.pointOffset).coerceAtLeast(0f) + radius, size.height - radius),
    )
}

private fun DrawScope.drawColon(
    left: Float,
    color: Color,
) {
    val radius = size.height * POINT_RADIUS
    drawCircle(color, radius, Offset(left + radius, size.height * ONE_THIRD))
    drawCircle(color, radius, Offset(left + radius, size.height * TWO_THIRDS))
}

private fun DrawScope.drawComma(
    left: Float,
    color: Color,
    geometry: SegmentGeometry,
) {
    val radius = size.height * geometry.pointRadius
    val origin = (left + size.height * geometry.pointOffset).coerceAtLeast(0f)
    val center = Offset(origin + radius, size.height - radius * 2.5f)
    drawCircle(color, radius, center)
    drawLine(
        color = color,
        start = center,
        end = Offset(origin + radius * 0.5f, size.height - radius * 0.5f),
        strokeWidth = radius,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawSlash(
    left: Float,
    color: Color,
) {
    val height = size.height
    val width = height * SLASH_WIDTH
    val thickness = height * SLASH_THICKNESS
    drawLine(
        color = color,
        start = Offset(left + thickness / 2f, height - thickness / 2f),
        end = Offset(left + width - thickness / 2f, thickness / 2f),
        strokeWidth = thickness,
        cap = StrokeCap.Butt,
    )
}

private fun DrawScope.drawSign(
    left: Float,
    color: Color,
    drawVertical: Boolean,
) {
    val height = size.height
    val width = height * SIGN_WIDTH
    val thickness = height * SEGMENT_THICKNESS
    drawLine(
        color = color,
        start = Offset(left + thickness / 2f, height / 2f),
        end = Offset(left + width - thickness / 2f, height / 2f),
        strokeWidth = thickness,
        cap = StrokeCap.Square,
    )
    if (drawVertical) {
        drawLine(
            color = color,
            start = Offset(left + width / 2f, height * PLUS_TOP),
            end = Offset(left + width / 2f, height * (PLUS_TOP + PLUS_HEIGHT)),
            strokeWidth = thickness,
            cap = StrokeCap.Square,
        )
    }
}

private const val TOP = 1 shl 0
private const val UPPER_RIGHT = 1 shl 1
private const val LOWER_RIGHT = 1 shl 2
private const val BOTTOM = 1 shl 3
private const val LOWER_LEFT = 1 shl 4
private const val UPPER_LEFT = 1 shl 5
private const val MIDDLE = 1 shl 6
private const val ALL_SEGMENTS = (1 shl 7) - 1
private val DIGIT_SEGMENTS =
    intArrayOf(TOP, UPPER_RIGHT, LOWER_RIGHT, BOTTOM, LOWER_LEFT, UPPER_LEFT, MIDDLE)

private const val DIGIT_WIDTH = 0.62f
private const val SPACE_ADVANCE = 0.16f
private const val POINT_ADVANCE = 0.24f
private const val SLASH_WIDTH = 0.30f
private const val SLASH_ADVANCE = 0.38f
private const val SIGN_WIDTH = 0.30f
private const val SIGN_ADVANCE = 0.40f
private const val SEGMENT_THICKNESS = 0.14f
private const val SLASH_THICKNESS = 0.10f
private const val CENTER_SEGMENT_GAP = 0.55f
private const val STEM_GAP = 0.025f
private const val POINT_RADIUS = 0.07f
private const val PLUS_TOP = 0.32f
private const val PLUS_HEIGHT = 0.36f
private const val ONE_THIRD = 0.34f
private const val TWO_THIRDS = 0.70f
private const val UNICODE_MINUS = '\u2212'
private const val NON_BREAKING_SPACE = '\u00A0'
private const val NARROW_NO_BREAK_SPACE = '\u202F'
