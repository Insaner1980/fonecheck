package com.insaner.fonecheck.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.navigation.DiagnosticDestination
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import kotlin.math.sqrt

private val HomeStatusCategoryLampSize = 20.dp
private val HomeStatusLegendLampSize = 16.dp

@Composable
internal fun HomeStatusPanel(
    state: LatestFullCheckState,
    onNavigate: (Any) -> Unit,
) {
    val report = (state as? LatestFullCheckState.Available)?.report
    val density = LocalDensity.current
    val spacing = FonecheckTheme.spacing
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = FonecheckTheme.type.rowValue.copy(fontSize = 12.sp, lineHeight = 16.sp)
    val labels =
        diagnosticDestinations.associate { destination ->
            destination.category to stringResource(destination.labelResId).uppercase(LocalLocale.current.platformLocale)
        }
    val longestNameWidth =
        labels.values.maxOf { label ->
            textMeasurer
                .measure(
                    text = AnnotatedString(label),
                    style = labelStyle,
                    maxLines = 1,
                    softWrap = false,
                ).size.width
        }
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // Row weights can differ by one pixel; use the narrower cell and the actual rounded insets.
        val columnGap = spacing.xs
        val innerPadding = spacing.sm
        val labelOffset = HomeStatusCategoryLampSize + spacing.sm
        val gap = with(density) { columnGap.roundToPx() }
        val cellPadding = with(density) { innerPadding.roundToPx() } * 2
        val nameInset = with(density) { labelOffset.roundToPx() }
        val columnLayout =
            HomeStatusColumnLayout(
                columns =
                    homeStatusGridColumnCount(
                        twoColumnNameWidthPx = (constraints.maxWidth - gap) / 2 - cellPadding - nameInset,
                        longestNameWidthPx = longestNameWidth,
                    ),
                weight = 1f,
                gap = columnGap,
                innerPadding = innerPadding,
                labelOffset = labelOffset,
            )
        Column {
            HomeSectionHeader(
                label = stringResource(R.string.home_status_panel_title),
                trailing =
                    pluralStringResource(
                        R.plurals.home_status_channel_count,
                        diagnosticDestinations.size,
                        uiNumber(diagnosticDestinations.size),
                    ).uppercase(LocalLocale.current.platformLocale),
            )
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.xs),
            ) {
                homeStatusPanelRows(diagnosticDestinations, columnLayout.columns).forEach { rowDestinations ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(columnLayout.gap),
                    ) {
                        rowDestinations.forEach { destination ->
                            val status =
                                report
                                    ?.categories
                                    ?.firstOrNull { it.categoryId == destination.category }
                                    ?.aggregateStatus
                            HomeStatusCell(
                                destination = destination,
                                labelStyle = labelStyle,
                                status = status,
                                onClick = { onNavigate(destination.route) },
                                columnLayout = columnLayout,
                                modifier =
                                    Modifier
                                        .weight(columnLayout.weight)
                                        .testTag("home_category_${destination.category.stableId}"),
                            )
                        }
                        repeat(columnLayout.columns - rowDestinations.size) {
                            Spacer(
                                modifier =
                                    Modifier
                                        .weight(columnLayout.weight)
                                        .heightIn(
                                            min = FonecheckTheme.spacing.minTouchTarget,
                                        ),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.xl))
            HomeStatusLegend(columnLayout)
        }
    }
}

private data class HomeStatusColumnLayout(
    val columns: Int,
    val weight: Float,
    val gap: Dp,
    val innerPadding: Dp,
    val labelOffset: Dp,
)

@Composable
private fun HomeStatusCell(
    destination: DiagnosticDestination,
    labelStyle: TextStyle,
    status: DiagnosticStatus?,
    onClick: () -> Unit,
    columnLayout: HomeStatusColumnLayout,
    modifier: Modifier = Modifier,
) {
    val statusLabel = homeStatusLabel(status)
    val label = stringResource(destination.labelResId).uppercase(LocalLocale.current.platformLocale)
    val colors = FonecheckTheme.colors
    Column(
        modifier =
            modifier
                .semantics(mergeDescendants = true) { stateDescription = statusLabel }
                .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = FonecheckTheme.spacing.minTouchTarget,
                    ).padding(
                        horizontal = columnLayout.innerPadding,
                        vertical = FonecheckTheme.spacing.sm,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeStatusLamp(
                status = status,
                lampSize = HomeStatusCategoryLampSize,
            )
            Spacer(modifier = Modifier.width(columnLayout.labelOffset - HomeStatusCategoryLampSize))
            Text(
                text = label,
                style = labelStyle,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
            )
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(FonecheckTheme.spacing.ruleThickness)
                    .background(colors.rule),
        )
    }
}

@Composable
private fun HomeStatusLamp(
    status: DiagnosticStatus?,
    lampSize: Dp,
    modifier: Modifier = Modifier,
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
    val borderWidth = 2.dp
    Box(
        modifier =
            modifier
                .size(lampSize)
                .then(
                    if (status == DiagnosticStatus.WARNING) {
                        Modifier
                    } else {
                        Modifier
                            .background(background)
                            .border(borderWidth, colors.edge)
                    },
                ).clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        if (status == DiagnosticStatus.WARNING) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = borderWidth.toPx()
                val strokeInset = strokeWidth / 2f
                // Offset every outer edge by half the stroke so the sharp miter stays inside the lamp bounds.
                val halfWidth = size.width / 2f
                val slopedEdgeLength = sqrt(halfWidth * halfWidth + size.height * size.height)
                val apexInset = strokeInset * slopedEdgeLength / halfWidth
                val baseInset = strokeInset * (slopedEdgeLength + halfWidth) / size.height
                val triangle =
                    Path().apply {
                        moveTo(halfWidth, apexInset)
                        lineTo(size.width - baseInset, size.height - strokeInset)
                        lineTo(baseInset, size.height - strokeInset)
                        close()
                    }
                drawPath(
                    path = triangle,
                    color = background,
                )
                drawPath(
                    path = triangle,
                    color = colors.edge,
                    style =
                        Stroke(
                            width = strokeWidth,
                            join = StrokeJoin.Miter,
                        ),
                )
            }
        }
        HomeStatusIcon(
            status = status,
            tint = content,
            modifier = Modifier.size(statusMarkSize),
        )
    }
}

@Composable
internal fun homeStatusLabel(status: DiagnosticStatus?): String =
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

private fun homeStatusImageVector(status: DiagnosticStatus?): ImageVector? =
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

@Composable
internal fun HomeStatusIcon(
    status: DiagnosticStatus?,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val iconModifier = modifier.clearAndSetSemantics { }
    val imageVector = homeStatusImageVector(status)
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
                    drawLine(
                        color = tint,
                        start = Offset(centerX, size.height * 0.16f),
                        end = Offset(centerX, size.height * 0.62f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Square,
                    )
                    drawCircle(
                        color = tint,
                        radius = strokeWidth * 0.58f,
                        center = Offset(centerX, size.height * 0.84f),
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

@Composable
private fun HomeStatusLegend(columnLayout: HomeStatusColumnLayout) {
    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.xs)) {
        homeStatusPanelRows(
            items =
                listOf(
                    DiagnosticStatus.PASS,
                    DiagnosticStatus.FAIL,
                    DiagnosticStatus.WARNING,
                    DiagnosticStatus.INFO,
                    DiagnosticStatus.NOT_AVAILABLE,
                    DiagnosticStatus.NOT_TESTED,
                ),
            columns = columnLayout.columns,
        ).forEach { rowStatuses ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(columnLayout.gap),
            ) {
                rowStatuses.forEach { status ->
                    HomeLegendEntry(
                        status = status,
                        labelOffset = columnLayout.labelOffset,
                        modifier =
                            Modifier
                                .weight(columnLayout.weight)
                                .padding(horizontal = columnLayout.innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeLegendEntry(
    status: DiagnosticStatus,
    labelOffset: Dp,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        HomeStatusLamp(
            status = status,
            lampSize = HomeStatusLegendLampSize,
        )
        Spacer(modifier = Modifier.width(labelOffset - HomeStatusLegendLampSize))
        Text(
            text = homeStatusLabel(status).uppercase(LocalLocale.current.platformLocale),
            style = FonecheckTheme.type.sectionLabel,
            color = FonecheckTheme.colors.textMuted,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
        )
    }
}

@Composable
internal fun HomeRunButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(3.dp, FonecheckTheme.colors.edge),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = FonecheckTheme.colors.primaryActionBackground,
                contentColor = FonecheckTheme.colors.primaryActionContent,
            ),
        elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp,
                disabledElevation = 0.dp,
            ),
    ) {
        Text(
            text = label.uppercase(LocalLocale.current.platformLocale),
            style = FonecheckTheme.type.rowValue,
            textAlign = TextAlign.Center,
        )
    }
}
