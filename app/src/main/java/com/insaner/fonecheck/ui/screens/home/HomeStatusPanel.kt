package com.insaner.fonecheck.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.navigation.DiagnosticDestination
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusLamp
import com.insaner.fonecheck.ui.components.StatusLampLegendSize
import com.insaner.fonecheck.ui.components.StatusLampSize
import com.insaner.fonecheck.ui.components.statusLabel
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme

@Composable
internal fun HomeStatusPanel(
    state: LatestFullCheckState,
    onNavigate: (Any) -> Unit,
) {
    val report = (state as? LatestFullCheckState.Available)?.report
    val density = LocalDensity.current
    val spacing = FonecheckTheme.spacing
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = FonecheckTheme.type.rowLabel
    val labels =
        diagnosticDestinations.associate { destination ->
            destination.category to stringResource(destination.labelResId)
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
        val labelOffset = StatusLampSize + spacing.sm
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
            SectionHeader(
                label = stringResource(R.string.home_status_panel_title),
                trailing =
                    pluralStringResource(
                        R.plurals.home_status_channel_count,
                        diagnosticDestinations.size,
                        uiNumber(diagnosticDestinations.size),
                    ),
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
    val statusDescription = statusLabel(status)
    val label = stringResource(destination.labelResId)
    val colors = FonecheckTheme.colors
    Column(
        modifier =
            modifier
                .semantics(mergeDescendants = true) { stateDescription = statusDescription }
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
            StatusLamp(
                status = status,
                lampSize = StatusLampSize,
            )
            Spacer(modifier = Modifier.width(columnLayout.labelOffset - StatusLampSize))
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
        StatusLamp(
            status = status,
            lampSize = StatusLampLegendSize,
        )
        Spacer(modifier = Modifier.width(labelOffset - StatusLampLegendSize))
        Text(
            text = statusLabel(status).uppercase(LocalLocale.current.platformLocale),
            style = FonecheckTheme.type.sectionLabel,
            color = FonecheckTheme.colors.textMuted,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
        )
    }
}
