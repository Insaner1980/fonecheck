package com.insaner.fonecheck.ui.screens.display

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.Yellow400
import kotlinx.coroutines.delay

internal const val DISPLAY_TOUCH_GRID_TAG = "display_touch_grid"
internal const val DISPLAY_EXIT_BUTTON_TAG = "display_exit_button"

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun DisplayTestScreen(
    modifier: Modifier = Modifier,
    onFullscreenChange: (Boolean) -> Unit = {},
    viewModel: DisplayTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val isFullscreen = state.visual.isActive || state.touch.isActive
    val currentOnFullscreenChange by rememberUpdatedState(onFullscreenChange)

    DisposableEffect(isFullscreen) {
        currentOnFullscreenChange(isFullscreen)
        onDispose { currentOnFullscreenChange(false) }
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
                onTouchCells = { cells -> viewModel.recordTouch(cells = cells) },
                onPointerChange = { pointers -> viewModel.recordTouch(activePointers = pointers) },
                onReset = viewModel::resetTouchTest,
                onComplete = viewModel::completeTouchTest,
                onExit = viewModel::stopTouchTest,
            )
            BackHandler(onBack = viewModel::stopTouchTest)
        }

        else ->
            DisplayOverview(
                state = state,
                viewModel = viewModel,
                modifier = modifier,
            )
    }
}

@Composable
private fun DisplayOverview(
    state: DisplayTestState,
    viewModel: DisplayTestViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FonecheckTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.lg),
    ) {
        DisplaySection(label = stringResource(R.string.display_info_title)) {
            DisplayInfoDetails(state.info)
        }
        DisplaySection(label = stringResource(R.string.display_visual_title)) {
            DataRow(
                label = stringResource(R.string.display_status_label),
                value = manualResultLabel(state.visual.result),
                tone = manualResultTone(state.visual.result),
            )
            Note(stringResource(R.string.display_visual_description))
            PrimaryButton(
                label = stringResource(R.string.display_start_test),
                onClick = viewModel::startVisualTest,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        DisplaySection(label = stringResource(R.string.display_touch_title)) {
            DataRow(
                label = stringResource(R.string.display_status_label),
                value =
                    stringResource(
                        if (state.touch.isComplete) {
                            R.string.display_status_complete
                        } else {
                            R.string.display_status_ready
                        },
                    ),
                tone = if (state.touch.isComplete) SemanticTone.PASS else SemanticTone.NEUTRAL,
            )
            if (state.touch.isComplete) {
                DataRow(
                    label = stringResource(R.string.display_touch_cells),
                    value =
                        stringResource(
                            R.string.display_value_ratio,
                            uiNumber(state.touch.touchedCells.size),
                            uiNumber(touchCellCount()),
                        ),
                )
                DataRow(
                    label = stringResource(R.string.display_multi_touch_peak),
                    value = uiNumber(state.touch.maxPointerCount),
                )
            }
            Note(stringResource(R.string.display_touch_desc))
            PrimaryButton(
                label = stringResource(R.string.display_start_test),
                onClick = viewModel::startTouchTest,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DisplayInfoDetails(info: DisplayInfoState) {
    DataRow(
        label = stringResource(R.string.display_resolution),
        value =
            stringResource(
                R.string.display_resolution_value,
                uiNumber(info.widthPx),
                uiNumber(info.heightPx),
                resolutionSourceLabel(info.resolutionSource),
            ),
    )
    DataRow(
        label = stringResource(R.string.display_density),
        value = stringResource(R.string.display_density_value, uiNumber(info.densityDpi)),
    )
    DataRow(
        label = stringResource(R.string.display_refresh_rate),
        value = stringResource(R.string.display_refresh_rate_value, uiNumber(info.refreshRate)),
    )
    DataRow(
        label = stringResource(R.string.display_hdr),
        value = supportedLabel(info.hdrSupported),
    )
    DataRow(
        label = stringResource(R.string.display_wide_color),
        value = supportedLabel(info.wideColorGamut),
    )
    DataRow(
        label = stringResource(R.string.display_brightness),
        value =
            stringResource(
                R.string.display_value_ratio,
                uiNumber(info.currentBrightness),
                uiNumber(255),
            ),
    )
    DataRow(
        label = stringResource(R.string.display_auto_brightness),
        value = stringResource(if (info.autoBrightness) R.string.status_enabled else R.string.status_disabled),
    )
}

@Composable
private fun DisplaySection(
    label: String,
    content: @Composable () -> Unit,
) {
    Column {
        SectionHeader(label)
        content()
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
            color = FonecheckTheme.colors.background,
        ) {
            Column(
                modifier = Modifier.padding(FonecheckTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SecondaryButton(
                        label = stringResource(R.string.display_exit_test),
                        onClick = onExit,
                        modifier = Modifier.testTag(DISPLAY_EXIT_BUTTON_TAG),
                    )
                    Text(
                        text =
                            stringResource(
                                R.string.display_value_ratio,
                                uiNumber(state.patternIndex + 1),
                                uiNumber(DisplayPattern.entries.size),
                            ),
                        style = FonecheckTheme.type.sectionLabel,
                        color = FonecheckTheme.colors.textMuted,
                    )
                }
                Text(
                    text = patternLabel(pattern),
                    style = FonecheckTheme.type.screenTitle,
                    color = FonecheckTheme.colors.textPrimary,
                )
                if (isLast) {
                    Note(stringResource(R.string.display_visual_question))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
                    ) {
                        SecondaryButton(
                            label = stringResource(R.string.display_found_problem),
                            onClick = { onResult(false) },
                            modifier = Modifier.weight(1f),
                        )
                        PrimaryButton(
                            label = stringResource(R.string.display_looks_good),
                            onClick = { onResult(true) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
                    ) {
                        SecondaryButton(
                            label = stringResource(R.string.display_previous_pattern),
                            onClick = onPrevious,
                            enabled = state.patternIndex > 0,
                            modifier = Modifier.weight(1f),
                        )
                        PrimaryButton(
                            label = stringResource(R.string.display_next_pattern),
                            onClick = onNext,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TouchTestOverlay(
    state: TouchTestState,
    onTouchCells: (Set<Int>) -> Unit,
    onPointerChange: (Map<Long, TouchPoint>) -> Unit,
    onReset: () -> Unit,
    onComplete: () -> Unit,
    onExit: () -> Unit,
) {
    val gridDescription = stringResource(R.string.display_touch_grid_description)
    val gridActionLabel = stringResource(R.string.display_touch_accessible_action)
    val progressDescription =
        stringResource(
            R.string.display_touch_progress,
            uiNumber(state.touchedCells.size),
            uiNumber(touchCellCount()),
            uiNumber(state.activePointers.size),
            uiNumber(state.maxPointerCount),
        )
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(FonecheckTheme.colors.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(FonecheckTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.display_touch_title),
                modifier = Modifier.semantics { heading() },
                style = FonecheckTheme.type.screenTitle,
                color = FonecheckTheme.colors.textPrimary,
            )
            SecondaryButton(
                label = stringResource(R.string.display_exit_test),
                onClick = onExit,
                modifier = Modifier.testTag(DISPLAY_EXIT_BUTTON_TAG),
            )
        }
        Text(
            text = progressDescription,
            style = FonecheckTheme.type.sectionLabel,
            color = FonecheckTheme.colors.textMuted,
        )
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag(DISPLAY_TOUCH_GRID_TAG)
                    .semantics {
                        contentDescription = gridDescription
                        stateDescription = progressDescription
                        role = Role.Button
                        onClick(label = gridActionLabel) {
                            onTouchCells(setOf(0))
                            true
                        }
                    }.pointerInput(onTouchCells, onPointerChange) {
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
                                onPointerChange(pointers)
                                val cells =
                                    event.changes
                                        .filter { it.pressed || it.previousPressed }
                                        .flatMap { change ->
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
                                        }.toSet()
                                if (cells.isNotEmpty()) onTouchCells(cells)
                                event.changes.forEach { it.consume() }
                            } while (event.changes.any { it.pressed })
                            onPointerChange(emptyMap())
                        }
                    },
        ) {
            drawTouchGrid(state)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
        ) {
            SecondaryButton(
                label = stringResource(R.string.display_touch_reset),
                onClick = onReset,
                modifier = Modifier.weight(1f),
            )
            PrimaryButton(
                label = stringResource(R.string.display_touch_complete),
                onClick = onComplete,
                enabled = state.touchedCells.isNotEmpty(),
                modifier = Modifier.weight(1f),
            )
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

private fun manualResultTone(value: Boolean?): SemanticTone =
    when (value) {
        true -> SemanticTone.PASS
        false -> SemanticTone.FAIL
        null -> SemanticTone.NEUTRAL
    }

@Composable
private fun supportedLabel(supported: Boolean): String =
    stringResource(if (supported) R.string.conn_supported else R.string.conn_not_supported)
