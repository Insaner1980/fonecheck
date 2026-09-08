package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.FonecheckType

/**
 * The lit window of the instrument: a dark recess behind a heavy frame, holding the reading that
 * the section exists to deliver.
 *
 * The interior is dark in both themes, so everything inside it is drawn from the `window` colour
 * roles rather than from the surrounding text ramp. Use [WindowLabel], [WindowFigure],
 * [WindowUnit] and [WindowBar] inside it; ordinary rows and notes belong outside, on the panel.
 *
 * A screen opens a window for the one value that carries the section. A section whose meaning is a
 * list of rows does not get a window.
 */
@Composable
fun ReadoutWindow(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(FonecheckTheme.colors.windowBg)
                .border(WindowFrameWidth, FonecheckTheme.colors.windowFrame)
                .padding(FonecheckTheme.spacing.md),
        content = content,
    )
}

/**
 * The caption above or below a reading inside a [ReadoutWindow].
 *
 * Pass [text] in the casing that should be drawn and announced.
 */
@Composable
fun WindowLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = FonecheckTheme.type.sectionLabel,
        color = FonecheckTheme.colors.windowDim,
        modifier = modifier.semantics { contentDescription = text },
    )
}

/**
 * The reading itself: the largest thing in the window.
 *
 * A leading numeric value is drawn by [SegmentedNumber]. A following unit keeps the existing
 * window-unit typography, while non-numeric states such as `n/a` keep the normal readout type.
 *
 * [style] defaults to the `readout` role. A screen raises it only when the window is the whole
 * point of the screen, as Home does for the passed-category count.
 *
 * [alert] marks a reading that has passed the threshold its section defines. It only ever changes
 * the colour, never the digits: the figure keeps stating what was actually measured.
 */
@Composable
fun WindowFigure(
    value: String,
    modifier: Modifier = Modifier,
    style: TextStyle = FonecheckType.readout,
    alert: Boolean = false,
    geometry: SegmentGeometry = SegmentGeometry(),
    showOverflowHint: Boolean = true,
) {
    val color = if (alert) FonecheckTheme.colors.windowAlert else FonecheckTheme.colors.windowText
    val parts = segmentedFigureParts(value)
    if (parts == null) {
        Text(
            text = value,
            style = style,
            color = color,
            modifier = modifier,
            maxLines = 1,
        )
    } else if (parts.suffix != null) {
        WindowReading(
            value,
            unit = null,
            modifier = modifier,
            style = style,
            alert = alert,
            geometry = geometry,
        )
    } else {
        SegmentedNumber(
            value = parts.number,
            modifier = modifier,
            style = style,
            color = color,
            geometry = geometry,
            showOverflowHint = showOverflowHint,
        )
    }
}

/**
 * The unit or denominator beside a [WindowFigure] — `°C`, `hPa`, `of 14`.
 *
 * Pass [text] in the casing that should be drawn.
 */
@Composable
fun WindowUnit(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = windowUnitStyle(),
        color = FonecheckTheme.colors.windowText.copy(alpha = 0.85f),
        modifier = modifier,
    )
}

@Composable
private fun windowUnitStyle(): TextStyle =
    FonecheckTheme.type.readoutUnit.copy(fontSize = FonecheckTheme.type.readoutUnit.fontSize * 1.1f)

/**
 * A [WindowFigure] and the [WindowUnit] that qualifies it: `82` and `/ 100`, `-9,81` and `m/s²`,
 * `08` and `of 14`.
 *
 * Side by side the unit is lifted off the bottom edge so it reads against the figure rather than
 * below it. The pair stacks above the shared font-scale threshold or when its measured natural
 * width exceeds the available space. A figure that still overflows scrolls without shrinking.
 *
 * [unit] is null where there is nothing to qualify. A reading the app could not take has no
 * denominator, and `n/a / 100` would state one anyway.
 */
@Composable
fun WindowReading(
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
    style: TextStyle = FonecheckType.readout,
    stacked: Boolean = stackedRowLayout(),
    alert: Boolean = false,
    geometry: SegmentGeometry = SegmentGeometry(),
) {
    val parts = segmentedFigureParts(value)
    val figure = parts?.number ?: value
    val companion =
        when {
            parts?.suffix == null -> unit
            unit == null -> parts.suffix
            else -> "${parts.suffix} $unit"
        }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val unitStyle = windowUnitStyle()
    val figureWidth =
        if (parts != null) {
            with(density) { (style.figureHeight().toDp() * segmentedWidth(figure, geometry)).roundToPx() }
        } else {
            textMeasurer.measure(figure, style = style, softWrap = false).size.width
        }
    val unitWidth = companion?.let { textMeasurer.measure(it, style = unitStyle, softWrap = false).size.width } ?: 0
    val gap = with(density) { FonecheckTheme.spacing.sm.roundToPx() }

    BoxWithConstraints(modifier = modifier.semantics(mergeDescendants = true) { }) {
        val figureOverflows = parts != null && figureWidth > constraints.maxWidth
        if (stacked || (companion != null && figureWidth.toLong() + gap + unitWidth > constraints.maxWidth)) {
            Column {
                WindowFigure(
                    value = figure,
                    style = style,
                    alert = alert,
                    geometry = geometry,
                    showOverflowHint = false,
                )
                companion?.let { WindowUnit(text = it) }
                if (figureOverflows) ReadoutScrollHint()
            }
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                WindowFigure(
                    value = figure,
                    style = style,
                    alert = alert,
                    geometry = geometry,
                    modifier = Modifier.weight(1f, fill = false),
                )
                companion?.let {
                    Spacer(modifier = Modifier.width(FonecheckTheme.spacing.sm))
                    WindowUnit(
                        text = it,
                        modifier = Modifier.padding(bottom = FonecheckTheme.spacing.sm),
                    )
                }
            }
        }
    }
}

/**
 * One labelled value inside a [ReadoutWindow], for a reading that is a short list rather than a
 * single figure — the three axes of an accelerometer, or a reading and its accuracy.
 *
 * A window holding only these is still a window: the values belong together and are read together.
 * A section whose meaning is a list of unrelated rows belongs on the panel, as [DataRow]s.
 */
@Composable
fun WindowRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val labelText =
        @Composable {
            Text(
                text = label,
                style = FonecheckTheme.type.sectionLabel,
                color = FonecheckTheme.colors.windowDim,
                modifier = Modifier.semantics { contentDescription = label },
            )
        }
    val valueText =
        @Composable {
            Text(
                text = value,
                style = FonecheckTheme.type.rowValue,
                color = FonecheckTheme.colors.windowText,
                maxLines = 1,
            )
        }
    // Above the shared threshold the label and the value no longer fit across one window, and the
    // value holds a single line without ellipsising — side by side it would be clipped.
    if (stackedRowLayout()) {
        Column(modifier = modifier.fillMaxWidth()) {
            labelText()
            valueText()
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            labelText()
            valueText()
        }
    }
}

/**
 * A proportion drawn inside a [ReadoutWindow]: a filled length against an unlit track.
 *
 * The bar repeats a figure that the window states in words, so it is hidden from screen readers.
 * [percentage] is clamped, because a bar that overruns its own track tells the reader nothing.
 */
@Composable
fun WindowBar(
    percentage: Int,
    modifier: Modifier = Modifier,
    height: Dp = WindowBarHeight,
) {
    val fraction = percentage.coerceIn(0, 100) / 100f
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .background(FonecheckTheme.colors.windowTrack)
                .clearAndSetSemantics { },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(fraction)
                    .height(height)
                    .background(FonecheckTheme.colors.windowText),
        )
    }
}

private val WindowFrameWidth = 3.dp
private val WindowBarHeight = 12.dp
