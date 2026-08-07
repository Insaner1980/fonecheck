package com.insaner.fonecheck.ui.screens.display

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DetailInfoRow
import com.insaner.fonecheck.ui.components.SectionBox
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.TestSectionCard
import com.insaner.fonecheck.ui.theme.Blue400
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400
import kotlinx.coroutines.delay

internal const val DISPLAY_TOUCH_GRID_TAG = "display_touch_grid"
internal const val DISPLAY_EXIT_BUTTON_TAG = "display_exit_button"

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun DisplayTestScreen(
    modifier: Modifier = Modifier,
    onFullscreenChanged: (Boolean) -> Unit = {},
    viewModel: DisplayTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val isFullscreen = state.visual.isActive || state.touch.isActive

    DisposableEffect(isFullscreen) {
        onFullscreenChanged(isFullscreen)
        onDispose { onFullscreenChanged(false) }
    }
    LaunchedEffect(state.visual.isActive) {
        if (state.visual.isActive) {
            delay(DisplayTestViewModel.VISUAL_TEST_TIMEOUT_MS)
            viewModel.stopVisualTest()
        }
    }

    when {
        state.visual.isActive -> {
            VisualTestOverlay(
                state = state.visual,
                onPrevious = viewModel::previousVisualPattern,
                onNext = viewModel::nextVisualPattern,
                onResult = viewModel::completeVisualTest,
                onExit = viewModel::stopVisualTest,
            )
            BackHandler(onBack = viewModel::stopVisualTest)
        }

        state.touch.isActive -> {
            TouchTestOverlay(
                state = state.touch,
                onCellsTouched = { cells -> viewModel.recordTouch(cells = cells) },
                onPointersChanged = { pointers -> viewModel.recordTouch(activePointers = pointers) },
                onReset = viewModel::resetTouchTest,
                onComplete = viewModel::completeTouchTest,
                onExit = viewModel::stopTouchTest,
            )
            BackHandler(onBack = viewModel::stopTouchTest)
        }

        else -> DisplayOverview(modifier, state, viewModel)
    }
}

@Composable
private fun DisplayOverview(
    modifier: Modifier,
    state: DisplayTestState,
    viewModel: DisplayTestViewModel,
) {
    TestScreenContent(modifier = modifier) {
        item {
            TestSectionCard(
                icon = "DSP",
                title = stringResource(R.string.display_info_title),
                statusText = "${state.info.widthPx}×${state.info.heightPx}",
                statusColor = Blue400,
                isExpanded = state.expandedSection == DisplaySection.INFO,
                onClick = { viewModel.toggleSection(DisplaySection.INFO) },
            ) {
                DisplayInfoDetails(state.info)
            }
        }
        item {
            TestSectionCard(
                icon = "PXL",
                title = stringResource(R.string.display_visual_title),
                statusText = manualResultLabel(state.visual.result),
                statusColor = manualResultColor(state.visual.result),
                isExpanded = state.expandedSection == DisplaySection.VISUAL,
                onClick = { viewModel.toggleSection(DisplaySection.VISUAL) },
            ) {
                TestLaunchSection(
                    description = stringResource(R.string.display_visual_description),
                    onStart = viewModel::startVisualTest,
                )
            }
        }
        item {
            TestSectionCard(
                icon = "TCH",
                title = stringResource(R.string.display_touch_title),
                statusText =
                    if (state.touch.isComplete) {
                        stringResource(R.string.display_status_complete)
                    } else {
                        stringResource(R.string.display_status_ready)
                    },
                statusColor = if (state.touch.isComplete) Green400 else Neutral500,
                isExpanded = state.expandedSection == DisplaySection.TOUCH,
                onClick = { viewModel.toggleSection(DisplaySection.TOUCH) },
            ) {
                SectionBox {
                    Text(
                        text = stringResource(R.string.display_touch_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (state.touch.isComplete) {
                        Spacer(Modifier.height(8.dp))
                        DetailInfoRow(
                            stringResource(R.string.display_touch_cells),
                            "${state.touch.touchedCells.size} / ${touchCellCount()}",
                        )
                        DetailInfoRow(
                            stringResource(R.string.display_multi_touch_peak),
                            state.touch.maxPointerCount.toString(),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::startTouchTest,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.display_start_test))
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayInfoDetails(info: DisplayInfoState) {
    SectionBox {
        DetailInfoRow(
            stringResource(R.string.display_resolution),
            stringResource(
                R.string.display_resolution_value,
                info.widthPx,
                info.heightPx,
                resolutionSourceLabel(info.resolutionSource),
            ),
        )
        DetailInfoRow(stringResource(R.string.display_density), "${info.densityDpi} dpi")
        DetailInfoRow(stringResource(R.string.display_refresh_rate), "%.0f Hz".format(info.refreshRate))
        DetailInfoRow(
            stringResource(R.string.display_hdr),
            supportedLabel(info.hdrSupported),
            valueColor = if (info.hdrSupported) Green400 else Neutral500,
        )
        DetailInfoRow(
            stringResource(R.string.display_wide_color),
            supportedLabel(info.wideColorGamut),
            valueColor = if (info.wideColorGamut) Green400 else Neutral500,
        )
        DetailInfoRow(stringResource(R.string.display_brightness), "${info.currentBrightness} / 255")
        DetailInfoRow(
            stringResource(R.string.display_auto_brightness),
            stringResource(if (info.autoBrightness) R.string.status_enabled else R.string.status_disabled),
        )
    }
}

@Composable
private fun TestLaunchSection(
    description: String,
    onStart: () -> Unit,
) {
    SectionBox {
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.display_start_test))
        }
    }
}

@Composable
private fun VisualTestOverlay(
    state: VisualTestState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onResult: (Boolean) -> Unit,
    onExit: () -> Unit,
) {
    val pattern = DisplayPattern.entries[state.patternIndex]
    val isLast = state.patternIndex == DisplayPattern.entries.lastIndex
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .displayPatternBackground(pattern),
    ) {
        Surface(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onExit,
                        modifier = Modifier.testTag(DISPLAY_EXIT_BUTTON_TAG),
                    ) {
                        Text(stringResource(R.string.display_exit_test))
                    }
                    Text(
                        text = "${state.patternIndex + 1} / ${DisplayPattern.entries.size}",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Text(
                    text = patternLabel(pattern),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (isLast) {
                    Text(
                        text = stringResource(R.string.display_visual_question),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { onResult(false) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.display_found_problem))
                        }
                        Button(
                            onClick = { onResult(true) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.display_looks_good))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onPrevious,
                            enabled = state.patternIndex > 0,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.display_previous_pattern))
                        }
                        Button(
                            onClick = onNext,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.display_next_pattern))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TouchTestOverlay(
    state: TouchTestState,
    onCellsTouched: (Set<Int>) -> Unit,
    onPointersChanged: (Map<Long, TouchPoint>) -> Unit,
    onReset: () -> Unit,
    onComplete: () -> Unit,
    onExit: () -> Unit,
) {
    val gridDescription = stringResource(R.string.display_touch_grid_description)
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.display_touch_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            TextButton(
                onClick = onExit,
                modifier = Modifier.testTag(DISPLAY_EXIT_BUTTON_TAG),
            ) {
                Text(stringResource(R.string.display_exit_test))
            }
        }
        Text(
            text =
                stringResource(
                    R.string.display_touch_progress,
                    state.touchedCells.size,
                    touchCellCount(),
                    state.activePointers.size,
                    state.maxPointerCount,
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag(DISPLAY_TOUCH_GRID_TAG)
                    .semantics { contentDescription = gridDescription }
                    .pointerInput(onCellsTouched, onPointersChanged) {
                        awaitEachGesture {
                            do {
                                val event = awaitPointerEvent()
                                val pointers =
                                    event.changes
                                        .filter { it.pressed }
                                        .associate { change ->
                                            change.id.value to
                                                change.position.toTouchPoint(size.width, size.height)
                                        }
                                onPointersChanged(pointers)
                                val cells =
                                    event.changes
                                        .filter { it.pressed || it.previousPressed }
                                        .flatMapTo(mutableSetOf()) { change ->
                                            TouchGridGeometry.cellsAlongSegment(
                                                start =
                                                    if (change.previousPressed) {
                                                        change.previousPosition.toTouchPoint(size.width, size.height)
                                                    } else {
                                                        change.position.toTouchPoint(size.width, size.height)
                                                    },
                                                end = change.position.toTouchPoint(size.width, size.height),
                                                columns = DisplayTestViewModel.TOUCH_GRID_COLS,
                                                rows = DisplayTestViewModel.TOUCH_GRID_ROWS,
                                            )
                                        }
                                if (cells.isNotEmpty()) onCellsTouched(cells)
                                event.changes.forEach { it.consume() }
                            } while (event.changes.any { it.pressed })
                            onPointersChanged(emptyMap())
                        }
                    },
        ) {
            drawTouchGrid(state)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.display_touch_reset))
            }
            Button(
                onClick = onComplete,
                enabled = state.touchedCells.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.display_touch_complete))
            }
        }
    }
}

private fun DrawScope.drawTouchGrid(state: TouchTestState) {
    val columns = DisplayTestViewModel.TOUCH_GRID_COLS
    val rows = DisplayTestViewModel.TOUCH_GRID_ROWS
    val cellWidth = size.width / columns
    val cellHeight = size.height / rows
    for (row in 0 until rows) {
        for (column in 0 until columns) {
            val index = row * columns + column
            drawRect(
                color = if (index in state.touchedCells) Green400.copy(alpha = 0.55f) else Color(0xFF343B48),
                topLeft = Offset(column * cellWidth + 1f, row * cellHeight + 1f),
                size = Size(cellWidth - 2f, cellHeight - 2f),
            )
        }
    }
    state.activePointers.values.forEach { point ->
        drawCircle(
            color = Yellow400,
            radius = 18.dp.toPx(),
            center = Offset(point.xFraction * size.width, point.yFraction * size.height),
        )
    }
}

internal fun Modifier.displayPatternBackground(pattern: DisplayPattern): Modifier =
    if (pattern == DisplayPattern.GRAY_GRADIENT) {
        background(Brush.horizontalGradient(listOf(Color.Black, Color.Gray, Color.White)))
    } else {
        background(pattern.color)
    }

private val DisplayPattern.color: Color
    get() =
        when (this) {
            DisplayPattern.RED -> Color.Red
            DisplayPattern.GREEN -> Color.Green
            DisplayPattern.BLUE -> Color.Blue
            DisplayPattern.WHITE -> Color.White
            DisplayPattern.BLACK -> Color.Black
            DisplayPattern.GRAY_GRADIENT -> Color.Transparent
        }

private fun Offset.toTouchPoint(
    width: Int,
    height: Int,
): TouchPoint =
    TouchPoint(
        xFraction = x / width.coerceAtLeast(1),
        yFraction = y / height.coerceAtLeast(1),
    )

private fun touchCellCount(): Int = DisplayTestViewModel.TOUCH_GRID_COLS * DisplayTestViewModel.TOUCH_GRID_ROWS

@Composable
private fun resolutionSourceLabel(value: DisplayResolutionSource): String =
    stringResource(
        when (value) {
            DisplayResolutionSource.APP_WINDOW -> R.string.display_resolution_source_window
            DisplayResolutionSource.DISPLAY_MODE -> R.string.display_resolution_source_mode
            DisplayResolutionSource.PHYSICAL_METRICS -> R.string.display_resolution_source_physical
        },
    )

@Composable
private fun patternLabel(value: DisplayPattern): String =
    stringResource(
        when (value) {
            DisplayPattern.RED -> R.string.display_pattern_red
            DisplayPattern.GREEN -> R.string.display_pattern_green
            DisplayPattern.BLUE -> R.string.display_pattern_blue
            DisplayPattern.WHITE -> R.string.display_pattern_white
            DisplayPattern.BLACK -> R.string.display_pattern_black
            DisplayPattern.GRAY_GRADIENT -> R.string.display_pattern_gray_gradient
        },
    )

@Composable
private fun manualResultLabel(value: Boolean?): String =
    stringResource(
        when (value) {
            true -> R.string.display_status_passed
            false -> R.string.display_status_issue
            null -> R.string.display_status_ready
        },
    )

private fun manualResultColor(value: Boolean?): Color =
    when (value) {
        true -> Green400
        false -> Red400
        null -> Neutral500
    }

@Composable
private fun supportedLabel(supported: Boolean): String =
    stringResource(if (supported) R.string.conn_supported else R.string.conn_not_supported)
