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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.insaner.fonecheck.domain.observation.DeviceObservation
import com.insaner.fonecheck.domain.observation.DeviceObservationClassifier
import com.insaner.fonecheck.domain.observation.MeasurementKind
import com.insaner.fonecheck.domain.observation.MeasurementOutcome
import com.insaner.fonecheck.ui.TopBarActionRegistry
import com.insaner.fonecheck.ui.classification.classifyDisplayConfirmation
import com.insaner.fonecheck.ui.components.ButtonRow
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.ManualResultButtons
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ObservationReasonNote
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.RegisterRefreshTopBarAction
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.WindowBar
import com.insaner.fonecheck.ui.components.WindowFigure
import com.insaner.fonecheck.ui.components.WindowLabel
import com.insaner.fonecheck.ui.components.shouldShowObservationReason
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Yellow400
import com.insaner.fonecheck.ui.theme.toSemanticTone
import kotlinx.coroutines.delay

internal const val DISPLAY_TOUCH_GRID_TAG = "display_touch_grid"
internal const val DISPLAY_EXIT_BUTTON_TAG = "display_exit_button"

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun DisplayTestScreen(
    modifier: Modifier = Modifier,
    onFullscreenChange: (Boolean) -> Unit = {},
    topBarActionRegistry: TopBarActionRegistry = TopBarActionRegistry.NoOp,
    viewModel: DisplayTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val isFullscreen = state.visual.isActive || state.touch.isActive
    val currentOnFullscreenChange by rememberUpdatedState(onFullscreenChange)

    RegisterRefreshTopBarAction(
        contentDescriptionResId = R.string.display_refresh_info,
        enabled = true,
        onRefresh = viewModel::refreshInfo,
        topBarActionRegistry = topBarActionRegistry,
    )

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
    val liveStateUpdatedAtEpochMillis = remember(state) { System.currentTimeMillis() }
    TestScreenContent(
        modifier = modifier,
        liveStateUpdatedAtEpochMillis = liveStateUpdatedAtEpochMillis,
    ) {
        item {
            DisplaySection(label = stringResource(R.string.display_info_title)) {
                BrightnessReadout(state.info)
                DisplayInfoDetails(state.info)
            }
        }
        item {
            DisplayVisualSummary(
                state = state.visual,
                onStart = viewModel::startVisualTest,
            )
        }
        item {
            DisplayTouchSummary(
                isComplete = state.touch.isComplete,
                touchedCellCount = state.touch.touchedCells.size,
                maxPointerCount = state.touch.maxPointerCount,
                onStart = viewModel::startTouchTest,
            )
        }
    }
}

@Composable
private fun DisplayVisualSummary(
    state: VisualTestState,
    onStart: () -> Unit,
) {
    val classification = classifyDisplayResult(state.result)
    val valueExplainsReason = state.result == null
    val reasonVisible = shouldShowObservationReason(classification, valueExplainsReason)
    DisplaySection(label = stringResource(R.string.display_visual_title)) {
        DataRow(
            label = stringResource(R.string.display_status_label),
            value = manualResultLabel(state.result),
            tone = classification.toSemanticTone(),
            showDivider = !reasonVisible,
        )
        ObservationReasonNote(
            classification = classification,
            valueExplainsNotMeasuredState = valueExplainsReason,
        )
        if (reasonVisible) HairlineRule()
        Note(stringResource(R.string.display_visual_description))
        SecondaryButton(
            label = stringResource(R.string.display_start_test),
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DisplayTouchSummary(
    isComplete: Boolean,
    touchedCellCount: Int,
    maxPointerCount: Int,
    onStart: () -> Unit,
) {
    val classification =
        classifyMeasurement(
            if (isComplete) MeasurementOutcome.MEASURED else MeasurementOutcome.NOT_RUN,
        )
    val valueExplainsReason = !isComplete
    val reasonVisible = shouldShowObservationReason(classification, valueExplainsReason)
    DisplaySection(label = stringResource(R.string.display_touch_title)) {
        DataRow(
            label = stringResource(R.string.display_status_label),
            value =
                stringResource(
                    if (isComplete) {
                        R.string.display_status_complete
                    } else {
                        R.string.status_not_measured
                    },
                ),
            tone = classification.toSemanticTone(),
            showDivider = !reasonVisible,
        )
        ObservationReasonNote(
            classification = classification,
            valueExplainsNotMeasuredState = valueExplainsReason,
        )
        if (reasonVisible) HairlineRule()
        if (isComplete) {
            DataRow(
                label = stringResource(R.string.display_touch_cells),
                value =
                    stringResource(
                        R.string.display_value_ratio,
                        uiNumber(touchedCellCount),
                        uiNumber(touchCellCount()),
                    ),
            )
            DataRow(
                label = stringResource(R.string.display_multi_touch_peak),
                value = uiNumber(maxPointerCount),
            )
        }
        Note(stringResource(R.string.display_touch_desc))
        SecondaryButton(
            label = stringResource(R.string.display_start_test),
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Screen brightness against the scale Android reports it on. Bounded by a known maximum, so it
 * reads as a level rather than a bare number.
 */
@Composable
private fun BrightnessReadout(info: DisplayInfoState) {
    val percent = (info.currentBrightness * PERCENT / MAX_BRIGHTNESS).coerceIn(0, PERCENT)
    Column {
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        ReadoutWindow {
            WindowLabel(text = stringResource(R.string.display_brightness))
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            WindowFigure(
                value =
                    stringResource(
                        R.string.display_value_ratio,
                        uiNumber(info.currentBrightness),
                        uiNumber(MAX_BRIGHTNESS),
                    ),
            )
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            WindowBar(percentage = percent)
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
    }
}

/** The scale Android's brightness setting is reported on. */
private const val MAX_BRIGHTNESS = 255
private const val PERCENT = 100

@Composable
private fun DisplayInfoDetails(info: DisplayInfoState) {
    val resolutionClassification =
        DeviceObservationClassifier.classify(
            DeviceObservation.Measurement(
                MeasurementKind.DISPLAY,
                if (info.widthPx > 0 && info.heightPx > 0) {
                    MeasurementOutcome.MEASURED
                } else {
                    MeasurementOutcome.UNAVAILABLE
                },
            ),
        )
    LongValueRow(
        label = stringResource(R.string.display_resolution),
        value =
            if (info.widthPx > 0 && info.heightPx > 0) {
                stringResource(
                    R.string.display_resolution_value,
                    uiNumber(info.widthPx),
                    uiNumber(info.heightPx),
                    resolutionSourceLabel(info.resolutionSource),
                )
            } else {
                null
            },
        showDivider = resolutionClassification.reason == null,
    )
    ObservationReasonNote(resolutionClassification)
    if (resolutionClassification.reason != null) HairlineRule()
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
                    // The shared pair, so this screen cannot drift back to recommending a pass.
                    ManualResultButtons(
                        problemLabel = stringResource(R.string.display_found_problem),
                        passLabel = stringResource(R.string.display_looks_good),
                        onResult = onResult,
                    )
                } else {
                    ButtonRow { buttonModifier ->
                        SecondaryButton(
                            label = stringResource(R.string.display_previous_pattern),
                            onClick = onPrevious,
                            enabled = state.patternIndex > 0,
                            modifier = buttonModifier,
                        )
                        PrimaryButton(
                            label = stringResource(R.string.display_next_pattern),
                            onClick = onNext,
                            modifier = buttonModifier,
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
        DataRow(
            label = stringResource(R.string.display_touch_cells),
            value =
                stringResource(
                    R.string.display_value_ratio,
                    uiNumber(state.touchedCells.size),
                    uiNumber(touchCellCount()),
                ),
        )
        DataRow(
            label = stringResource(R.string.display_touch_active),
            value = uiNumber(state.activePointers.size),
        )
        DataRow(
            label = stringResource(R.string.display_touch_maximum),
            value = uiNumber(state.maxPointerCount),
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
        ButtonRow { buttonModifier ->
            SecondaryButton(
                label = stringResource(R.string.display_touch_reset),
                onClick = onReset,
                modifier = buttonModifier,
            )
            PrimaryButton(
                label = stringResource(R.string.display_touch_complete),
                onClick = onComplete,
                enabled = state.touchedCells.isNotEmpty(),
                modifier = buttonModifier,
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
            null -> R.string.status_not_measured
        },
    )

private fun classifyDisplayResult(value: Boolean?) =
    value?.let(::classifyDisplayConfirmation) ?: classifyMeasurement(MeasurementOutcome.NOT_RUN)

private fun classifyMeasurement(outcome: MeasurementOutcome) =
    DeviceObservationClassifier.classify(DeviceObservation.Measurement(MeasurementKind.GENERIC, outcome))

@Composable
private fun supportedLabel(supported: Boolean): String =
    stringResource(if (supported) R.string.conn_supported else R.string.conn_not_supported)
