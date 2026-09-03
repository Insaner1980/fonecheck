package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import kotlin.math.ceil

/** The lamp beside a category row. */
val StatusLampSize: Dp = 20.dp

/** The smaller lamp used where the same vocabulary is being explained rather than reported. */
val StatusLampLegendSize: Dp = 16.dp

/**
 * A diagnostic verdict as a lit panel lamp: a filled square in the status colour, outlined in the
 * panel edge, carrying a drawn mark.
 *
 * The mark is what survives a colour-vision difference, so every status draws one — a tick, a
 * cross, a triangle, a bar, a ring. Colour and mark always agree.
 *
 * The lamp is decorative on its own: it repeats a verdict that the row around it already states in
 * words, so it is hidden from screen readers. A caller that draws a lamp without that neighbouring
 * text must supply the verdict itself, through [statusLabel].
 */
@Composable
fun StatusLamp(
    status: DiagnosticStatus?,
    modifier: Modifier = Modifier,
    lampSize: Dp = StatusLampSize,
) {
    val colors = FonecheckTheme.colors
    val background =
        when (status) {
            DiagnosticStatus.PASS -> colors.lampPass
            DiagnosticStatus.WARNING -> colors.lampNoted
            DiagnosticStatus.FAIL -> colors.lampFault
            DiagnosticStatus.INFO -> colors.lampInfo
            else -> colors.lampUnlit
        }
    val content =
        when (status) {
            DiagnosticStatus.PASS -> colors.lampPassInk
            DiagnosticStatus.WARNING -> colors.lampNotedInk
            DiagnosticStatus.FAIL -> colors.lampFaultInk
            DiagnosticStatus.INFO -> colors.lampInfoInk
            else -> colors.lampUnlitInk
        }
    val statusMarkSize = lampSize * (2f / 3f)
    val warningTriangleReferenceWidthPx =
        with(LocalDensity.current) {
            lampSize.roundToPx() - ceil(LampBorderWidth.toPx()).toInt() * 2
        }.toFloat()
    Box(
        modifier =
            modifier
                .size(lampSize)
                .background(background)
                .border(LampBorderWidth, colors.edge)
                .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        StatusIcon(
            status = status,
            tint = content,
            warningTriangleReferenceWidthPx = warningTriangleReferenceWidthPx,
            modifier = Modifier.size(statusMarkSize),
        )
    }
}

/** The spoken and written name of a status. The one mapping from a status to its word. */
@Composable
fun statusLabel(status: DiagnosticStatus?): String =
    stringResource(
        when (status) {
            DiagnosticStatus.PASS -> R.string.run_all_status_pass
            DiagnosticStatus.WARNING -> R.string.run_all_status_warning
            DiagnosticStatus.FAIL -> R.string.run_all_status_fail
            DiagnosticStatus.INFO -> R.string.run_all_status_info
            DiagnosticStatus.NOT_AVAILABLE -> R.string.status_not_available
            DiagnosticStatus.NOT_TESTED -> R.string.status_not_measured
            null -> R.string.value_unavailable_short
        },
    )

private fun statusImageVector(status: DiagnosticStatus?): ImageVector? =
    when (status) {
        DiagnosticStatus.PASS -> Icons.Filled.Check
        DiagnosticStatus.FAIL -> Icons.Filled.Close
        DiagnosticStatus.WARNING,
        DiagnosticStatus.INFO,
        DiagnosticStatus.NOT_TESTED,
        DiagnosticStatus.NOT_AVAILABLE,
        null,
        -> null
    }

/**
 * The mark alone, without the lamp around it.
 *
 * [warningTriangleReferenceWidthPx] is the inner width of the lamp that will hold this mark. The
 * warning triangle is sized from the lamp rather than from its own box, so that triangles drawn at
 * different lamp sizes stay in proportion to each other.
 */
@Composable
fun StatusIcon(
    status: DiagnosticStatus?,
    tint: Color,
    warningTriangleReferenceWidthPx: Float,
    modifier: Modifier = Modifier,
) {
    val iconModifier = modifier.clearAndSetSemantics { }
    val imageVector = statusImageVector(status)
    if (imageVector != null) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = iconModifier,
        )
    } else {
        Canvas(modifier = iconModifier) {
            val strokeWidth = size.minDimension * 0.12f
            val centerX = size.width / 2f
            when (status) {
                DiagnosticStatus.WARNING -> {
                    val triangleBase =
                        warningTriangleReferenceWidthPx * WARNING_TRIANGLE_BASE_PROPORTION
                    val triangleHeight = triangleBase * 0.866f
                    val triangleLeft = centerX - triangleBase / 2f
                    val triangleTop = (size.height - triangleHeight) / 2f
                    val triangle =
                        Path().apply {
                            moveTo(centerX, triangleTop)
                            lineTo(centerX + triangleBase / 2f, triangleTop + triangleHeight)
                            lineTo(triangleLeft, triangleTop + triangleHeight)
                            close()
                        }
                    drawPath(
                        path = triangle,
                        color = tint,
                    )
                }

                DiagnosticStatus.INFO -> {
                    drawCircle(
                        color = tint,
                        radius = strokeWidth * 0.58f,
                        center = Offset(centerX, size.height * 0.18f),
                    )
                    drawLine(
                        color = tint,
                        start = Offset(centerX, size.height * 0.44f),
                        end = Offset(centerX, size.height * 0.84f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Square,
                    )
                }

                DiagnosticStatus.NOT_AVAILABLE -> {
                    drawCircle(
                        color = tint,
                        radius = (size.minDimension - strokeWidth) / 2f,
                        style = Stroke(width = strokeWidth),
                    )
                    drawLine(
                        color = tint,
                        start = Offset(size.width * 0.24f, size.height * 0.76f),
                        end = Offset(size.width * 0.76f, size.height * 0.24f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Square,
                    )
                }

                DiagnosticStatus.NOT_TESTED,
                null,
                -> {
                    drawCircle(
                        color = tint,
                        radius = (size.minDimension - strokeWidth) / 2f,
                        style = Stroke(width = strokeWidth),
                    )
                }

                DiagnosticStatus.PASS,
                DiagnosticStatus.FAIL,
                -> Unit
            }
        }
    }
}

private val LampBorderWidth = 2.dp
private const val WARNING_TRIANGLE_BASE_PROPORTION = 0.8f
