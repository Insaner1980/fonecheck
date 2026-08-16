package com.insaner.fonecheck.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.repository.ReportReadFailure
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.navigation.DiagnosticDestination
import com.insaner.fonecheck.navigation.History
import com.insaner.fonecheck.navigation.Report
import com.insaner.fonecheck.navigation.Settings
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.SegmentedBar
import com.insaner.fonecheck.ui.components.StandardCard
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.theme.Aqua40
import com.insaner.fonecheck.ui.theme.Aqua80
import com.insaner.fonecheck.ui.theme.Coral80
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.JetBrainsMono
import com.insaner.fonecheck.ui.theme.Lavender40
import com.insaner.fonecheck.ui.theme.Lavender80
import com.insaner.fonecheck.ui.theme.Neutral100
import com.insaner.fonecheck.ui.theme.Neutral300
import com.insaner.fonecheck.ui.theme.Neutral50
import com.insaner.fonecheck.ui.theme.Neutral600
import com.insaner.fonecheck.ui.theme.Neutral700
import com.insaner.fonecheck.ui.theme.Neutral800
import com.insaner.fonecheck.ui.theme.Neutral850
import com.insaner.fonecheck.ui.theme.Neutral900
import com.insaner.fonecheck.ui.theme.Neutral950
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.toSemanticTone
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(
    onNavigate: (Any) -> Unit,
    onRunAllTests: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val latestFullCheck by viewModel.latestFullCheck.collectAsStateWithLifecycle()

    HomeContent(
        latestFullCheck = latestFullCheck,
        onNavigate = onNavigate,
        onRunAllTests = onRunAllTests,
        onRetryLatestFullCheck = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
internal fun HomeContent(
    latestFullCheck: LatestFullCheckState,
    onNavigate: (Any) -> Unit,
    onRunAllTests: () -> Unit,
    modifier: Modifier = Modifier,
    onRetryLatestFullCheck: () -> Unit = {},
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columnCount = homeGridColumnCount(maxWidth.value)
        val horizontalPadding = if (maxWidth < 600.dp) 16.dp else 24.dp
        val itemSpacing = if (maxWidth < 600.dp) 12.dp else 16.dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding =
                PaddingValues(
                    start = horizontalPadding,
                    top = 12.dp,
                    end = horizontalPadding,
                    bottom = 32.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HomeBrandHeader(
                    onHistory = { onNavigate(History) },
                    onSettings = { onNavigate(Settings) },
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                LatestFullCheckSection(
                    state = latestFullCheck,
                    onOpenReport = { reportId -> onNavigate(Report(reportId)) },
                    onRetry = onRetryLatestFullCheck,
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                PrimaryButton(
                    label = stringResource(R.string.home_run_all),
                    onClick = onRunAllTests,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)) {
                    Text(
                        text = stringResource(R.string.home_categories_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.home_categories_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(
                items = diagnosticDestinations,
                key = { it.labelResId },
            ) { category ->
                CategoryGridItem(
                    category = category,
                    label = stringResource(category.labelResId),
                    onClick = { onNavigate(category.route) },
                )
            }
        }
    }
}

internal fun homeGridColumnCount(availableWidthDp: Float): Int =
    when {
        availableWidthDp < 600f -> 2
        availableWidthDp < 840f -> 3
        else -> 4
    }

internal fun latestReportUsesStackedLayout(
    availableWidthDp: Float,
    fontScale: Float,
): Boolean = availableWidthDp < 312f || fontScale > 1.3f

@Composable
private fun HomeBrandHeader(
    onHistory: () -> Unit,
    onSettings: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val fontScale = LocalDensity.current.fontScale
        val stackActions = maxWidth < 480.dp && fontScale > 1.3f
        if (stackActions) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeBrand()
                HeaderActions(
                    onHistory = onHistory,
                    onSettings = onSettings,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HomeBrand(modifier = Modifier.weight(1f))
                HeaderActions(onHistory = onHistory, onSettings = onSettings)
            }
        }
    }
}

@Composable
private fun HomeBrand(modifier: Modifier = Modifier) {
    val appName = stringResource(R.string.app_name)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.fonecheck_mark),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            contentScale = ContentScale.Fit,
        )
        // The wordmark stays neutral: the primary action is the one element on this screen that
        // carries the accent.
        Text(
            text =
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = FonecheckTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                        ),
                    ) {
                        append(appName.take(4))
                    }
                    withStyle(
                        SpanStyle(
                            color = FonecheckTheme.colors.textPrimary,
                            fontWeight = FontWeight.Medium,
                        ),
                    ) {
                        append(appName.drop(4))
                    }
                },
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp, lineHeight = 32.sp),
            maxLines = 1,
        )
    }
}

@Composable
private fun HeaderActions(
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FonecheckHeaderIconButton(
            contentDescription = stringResource(R.string.home_history_content_description),
            onClick = onHistory,
        ) { color ->
            HistoryGlyph(color, Modifier.size(26.dp))
        }
        FonecheckHeaderIconButton(
            contentDescription = stringResource(R.string.home_settings_content_description),
            onClick = onSettings,
        ) { color ->
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(27.dp),
            )
        }
    }
}

@Composable
private fun FonecheckHeaderIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable (Color) -> Unit,
) {
    val lavender = Lavender80
    val outerShape = RoundedCornerShape(17.dp)
    val innerShape = RoundedCornerShape(15.dp)
    Box(
        modifier =
            Modifier
                .size(52.dp)
                .shadow(8.dp, outerShape, ambientColor = Neutral950, spotColor = Neutral950)
                .clip(outerShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Lavender80.copy(alpha = 0.52f),
                            Neutral950,
                        ),
                    ),
                ).clickable(role = Role.Button, onClick = onClick)
                .semantics {
                    this.contentDescription = contentDescription
                    role = Role.Button
                }.padding(2.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(innerShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Neutral850,
                                Neutral900,
                            ),
                        ),
                    ).border(
                        1.dp,
                        Lavender80.copy(alpha = 0.22f),
                        innerShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            content(lavender)
        }
    }
}

@Composable
private fun HistoryGlyph(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.25.dp.toPx()
        drawArc(
            color = color,
            startAngle = -52f,
            sweepAngle = 292f,
            useCenter = false,
            topLeft = Offset(strokeWidth, strokeWidth),
            size = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.22f, size.height * 0.28f),
            end = Offset(size.width * 0.22f, size.height * 0.48f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.22f, size.height * 0.28f),
            end = Offset(size.width * 0.40f, size.height * 0.31f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x, size.height * 0.30f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = center,
            end = Offset(size.width * 0.68f, size.height * 0.58f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun LatestFullCheckSection(
    state: LatestFullCheckState,
    onOpenReport: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val unavailableValue = stringResource(R.string.value_unavailable_short)
    when (state) {
        LatestFullCheckState.Loading ->
            LatestFullCheckMessage(
                message = stringResource(R.string.home_latest_loading),
                tag = "home_latest_loading",
                loading = true,
            )
        LatestFullCheckState.Empty ->
            LatestFullCheckMessage(
                message = stringResource(R.string.home_latest_empty_title),
                tag = "home_latest_empty",
                trailing = unavailableValue,
            )
        is LatestFullCheckState.Unavailable ->
            LatestFullCheckMessage(
                message =
                    stringResource(
                        when (state.reason) {
                            ReportReadFailure.CORRUPT_DATA -> R.string.report_corrupt
                            ReportReadFailure.UNSUPPORTED_SCHEMA_VERSION -> R.string.report_unsupported
                        },
                    ),
                tag = "home_latest_unavailable",
                trailing = unavailableValue,
            )
        LatestFullCheckState.Error ->
            LatestFullCheckMessage(
                message = stringResource(R.string.home_latest_error_message),
                tag = "home_latest_error",
                trailing = unavailableValue,
                actionLabel = stringResource(R.string.home_latest_retry),
                onAction = onRetry,
                assertive = true,
            )
        is LatestFullCheckState.Available ->
            LatestFullCheckReadout(
                report = state.report,
                onClick = { onOpenReport(state.report.stableId) },
            )
    }
}

/**
 * Every state that is not a finished report: the section header, one line saying what is going on,
 * and for a failed load the action that retries it. No zeroes and no invented figures.
 */
@Composable
private fun LatestFullCheckMessage(
    message: String,
    tag: String,
    trailing: String? = null,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    assertive: Boolean = false,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(tag)
                .semantics {
                    liveRegion = if (assertive) LiveRegionMode.Assertive else LiveRegionMode.Polite
                },
    ) {
        SectionHeader(
            label = stringResource(R.string.home_latest_title),
            trailing = trailing,
        )
        if (loading) {
            IndeterminateRule()
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        Text(
            text = message,
            style = FonecheckTheme.type.rowLabel,
            color = FonecheckTheme.colors.textSecondary,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            SecondaryButton(label = actionLabel, onClick = onAction)
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        HairlineRule()
    }
}

/** One segment per category, coloured by that category's own result. */
internal fun latestCheckSegments(report: DiagnosticReport): List<SemanticTone> =
    report.categories.map { it.aggregateStatus.toSemanticTone() }

/**
 * The finished report as a readout: how many categories passed, the shape of the run as a segment
 * per category, and the two figures that qualify it. The whole block opens the full report.
 */
@Composable
private fun LatestFullCheckReadout(
    report: DiagnosticReport,
    onClick: () -> Unit,
) {
    val presentation = remember(report) { HomeReportPresentation.from(report) }
    val segments = remember(report) { latestCheckSegments(report) }
    val locale = LocalConfiguration.current.locales[0]
    val numberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val percentFormat = remember(locale) { NumberFormat.getPercentInstance(locale) }
    // The short form, not the medium one: the header label beside it is already long in Finnish.
    val dateFormatter =
        remember(locale) {
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault())
        }
    val coverageValue =
        stringResource(
            R.string.home_latest_coverage_value,
            percentFormat.format(report.coverage.percentage / 100.0),
        )
    val attentionSummary =
        if (presentation.attentionCount == 0) {
            stringResource(R.string.home_latest_no_attention)
        } else {
            pluralStringResource(
                R.plurals.home_latest_attention_summary,
                presentation.attentionCount,
                presentation.attentionCount,
            )
        }
    val attentionTone =
        when {
            presentation.failureItemCount > 0 -> SemanticTone.FAIL
            presentation.attentionCount > 0 -> SemanticTone.ATTENTION
            else -> SemanticTone.PASS
        }
    val passedDescription =
        stringResource(
            R.string.home_latest_passed_description,
            numberFormat.format(presentation.passCount),
            numberFormat.format(presentation.totalCategories),
        )
    val categoryStatusDescription =
        stringResource(
            R.string.home_latest_category_statuses,
            numberFormat.format(presentation.passCount),
            numberFormat.format(presentation.warningCategoryCount),
            numberFormat.format(presentation.failureCategoryCount),
            numberFormat.format(presentation.infoCount),
            numberFormat.format(presentation.notAvailableCount),
            numberFormat.format(presentation.notTestedCount),
        )
    val completedAtValue = dateFormatter.format(report.completedAt)
    // The verdict word no longer appears on the screen, but it still opens the spoken description.
    val cardStateDescription =
        stringResource(
            R.string.home_latest_state_description,
            reportStatusText(report, presentation),
            passedDescription,
            coverageValue,
            attentionSummary,
            stringResource(R.string.home_latest_completed_at, completedAtValue),
            categoryStatusDescription,
        )

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val fontScale = LocalDensity.current.fontScale
        val stacked = latestReportUsesStackedLayout(maxWidth.value, fontScale)
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("home_latest_report_card")
                    .semantics { stateDescription = cardStateDescription }
                    .clickable(role = Role.Button, onClick = onClick),
        ) {
            SectionHeader(
                label = stringResource(R.string.home_latest_title),
                trailing = completedAtValue,
            )
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            PassedReadout(
                passed = numberFormat.format(presentation.passCount),
                total =
                    stringResource(
                        R.string.home_latest_passed_total,
                        numberFormat.format(presentation.totalCategories),
                    ),
                label = stringResource(R.string.home_latest_passed_label),
                stacked = stacked,
            )
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            SegmentedBar(segments = segments)
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            LatestCheckInfoLine(
                coverage = coverageValue,
                attention = attentionSummary,
                attentionTone = attentionTone,
            )
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            HairlineRule()
        }
    }
}

/**
 * A hair space between the figure and its total. A full space is too wide at readout size, and the
 * two are one run of text so that they share a baseline without being laid out against each other.
 */
private const val FIGURE_GAP = "\u2009"

@Composable
private fun PassedReadout(
    passed: String,
    total: String,
    label: String,
    stacked: Boolean,
) {
    val figures =
        buildAnnotatedString {
            withStyle(
                FonecheckTheme.type.readout
                    .toSpanStyle()
                    .copy(color = FonecheckTheme.colors.textPrimary),
            ) {
                append(passed)
            }
            withStyle(
                FonecheckTheme.type.readoutUnit
                    .toSpanStyle()
                    .copy(color = FonecheckTheme.colors.textMuted),
            ) {
                append(FIGURE_GAP)
                append(total)
            }
        }
    if (stacked) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = figures, style = FonecheckTheme.type.readout, maxLines = 1)
            Text(
                text = label,
                style = FonecheckTheme.type.rowLabel,
                color = FonecheckTheme.colors.textSecondary,
            )
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = figures,
                modifier = Modifier.alignByBaseline(),
                style = FonecheckTheme.type.readout,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = label,
                modifier = Modifier.alignByBaseline(),
                style = FonecheckTheme.type.rowLabel,
                color = FonecheckTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun LatestCheckInfoLine(
    coverage: String,
    attention: String,
    attentionTone: SemanticTone,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Coverage carries no verdict, so it is not a StatusText and there is no tone for a muted
        // micro label. Drawn from tokens here; extracted when the detail screens need the same line.
        Text(
            text = coverage.uppercase(LocalLocale.current.platformLocale),
            style = FonecheckTheme.type.sectionLabel,
            color = FonecheckTheme.colors.textMuted,
            modifier = Modifier.semantics { contentDescription = coverage },
        )
        StatusText(text = attention, tone = attentionTone)
    }
}

@Composable
private fun LatestFullCheckStateCard(
    title: String,
    message: String,
    tag: String,
    isLoading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    assertive: Boolean = false,
) {
    LayeredReportSurface(
        modifier =
            Modifier
                .testTag(tag)
                .semantics {
                    liveRegion = if (assertive) LiveRegionMode.Assertive else LiveRegionMode.Polite
                },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Aqua80,
                        strokeWidth = 2.dp,
                    )
                }
                Text(
                    text = title,
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    color = Neutral100,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral300,
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
@Suppress("kotlin:S3776") // Responsive layout and accessibility variants are explicit presentation branches.
private fun LatestFullCheckCard(
    report: DiagnosticReport,
    onClick: () -> Unit,
) {
    val presentation = remember(report) { HomeReportPresentation.from(report) }
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val numberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val percentFormat = remember(locale) { NumberFormat.getPercentInstance(locale) }
    val dateFormatter =
        remember(locale) {
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault())
        }
    val completedAt =
        stringResource(
            R.string.home_latest_completed_at,
            dateFormatter.format(report.completedAt),
        )
    val categoriesValue =
        stringResource(
            R.string.home_latest_categories_value,
            numberFormat.format(presentation.completedCategories),
            numberFormat.format(presentation.totalCategories),
        )
    val coverageValue = percentFormat.format(report.coverage.percentage / 100.0)
    val attentionValue = numberFormat.format(presentation.attentionCount)
    val attentionSummary =
        if (presentation.attentionCount == 0) {
            stringResource(R.string.home_latest_no_attention)
        } else {
            pluralStringResource(
                R.plurals.home_latest_attention_count,
                presentation.attentionCount,
                presentation.attentionCount,
            )
        }
    val attentionStateDescription =
        if (presentation.attentionCount == 0) {
            attentionSummary
        } else {
            pluralStringResource(
                R.plurals.home_latest_attention_summary,
                presentation.attentionCount,
                presentation.attentionCount,
            )
        }
    val statusText = reportStatusText(report, presentation)
    val reportSummary =
        ReportSummaryContent(
            statusText = statusText,
            attentionSummary = attentionSummary,
            completedAt = completedAt,
            attentionCount = presentation.attentionCount,
            failureCount = presentation.failureItemCount,
        )
    val categoryStatusDescription =
        stringResource(
            R.string.home_latest_category_statuses,
            numberFormat.format(presentation.passCount),
            numberFormat.format(presentation.warningCategoryCount),
            numberFormat.format(presentation.failureCategoryCount),
            numberFormat.format(presentation.infoCount),
            numberFormat.format(presentation.notAvailableCount),
            numberFormat.format(presentation.notTestedCount),
        )
    val scoreDescription =
        report.score.value?.let { score ->
            stringResource(R.string.home_latest_score_description, numberFormat.format(score))
        } ?: stringResource(
            R.string.home_latest_no_score_description,
            stringResource(scoreStateLabel(report.score.state)),
        )
    val cardStateDescription =
        stringResource(
            R.string.home_latest_state_description,
            statusText,
            scoreDescription,
            attentionStateDescription,
            categoryStatusDescription,
        )

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val fontScale = LocalDensity.current.fontScale
        val stacked = latestReportUsesStackedLayout(maxWidth.value, fontScale)
        val cardShape = RoundedCornerShape(if (maxWidth < 600.dp) 28.dp else 34.dp)
        val horizontalPadding = if (maxWidth < 600.dp) 20.dp else 32.dp
        val verticalPadding = if (maxWidth < 600.dp) 26.dp else 34.dp
        val gaugeSize =
            when {
                stacked -> minOf(240.dp, 180.dp * fontScale)
                maxWidth >= 600.dp -> 220.dp
                maxWidth >= 400.dp -> 172.dp
                else -> 132.dp
            }
        Card(
            onClick = onClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("home_latest_report_card")
                    .semantics { stateDescription = cardStateDescription },
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp, pressedElevation = 4.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Neutral800,
                                    Neutral900,
                                ),
                            ),
                        ).border(
                            width = 1.dp,
                            color = Lavender80.copy(alpha = 0.44f),
                            shape = cardShape,
                        ).padding(horizontal = horizontalPadding, vertical = verticalPadding),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                if (stacked) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        FonecheckScoreGauge(
                            score = report.score.value,
                            scoreState = report.score.state,
                            modifier = Modifier.size(gaugeSize),
                        )
                        ReportSummary(
                            content = reportSummary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 188.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FonecheckScoreGauge(
                            score = report.score.value,
                            scoreState = report.score.state,
                            modifier = Modifier.size(gaugeSize),
                        )
                        ReportSummary(
                            content = reportSummary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Lavender80.copy(alpha = 0.32f),
                                        Neutral300.copy(alpha = 0.20f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                )

                ReportMetrics(
                    stacked = stacked,
                    categoriesValue = categoriesValue,
                    coverageValue = coverageValue,
                    attentionValue = attentionValue,
                    attentionCount = presentation.attentionCount,
                    failureCount = presentation.failureItemCount,
                )
            }
        }
    }
}

@Composable
private fun LayeredReportSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent,
        shadowElevation = 9.dp,
    ) {
        Box(
            modifier =
                Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Lavender80.copy(alpha = 0.68f),
                                Neutral700,
                                Neutral950,
                            ),
                        ),
                    ).padding(7.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(23.dp))
                        .background(Brush.linearGradient(listOf(Neutral850, Neutral900)))
                        .border(
                            1.dp,
                            Neutral600.copy(alpha = 0.72f),
                            RoundedCornerShape(23.dp),
                        ),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ReportSummary(
    content: ReportSummaryContent,
    modifier: Modifier = Modifier,
) {
    val attentionColor =
        when {
            content.failureCount > 0 -> Red400
            content.attentionCount > 0 -> Coral80
            else -> Green400
        }
    val statusColor =
        when {
            content.failureCount > 0 -> Red400
            content.attentionCount > 0 -> Coral80
            else -> Aqua80
        }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.home_latest_title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            color = Lavender80,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = content.statusText,
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    lineHeight = 36.sp,
                    shadow = Shadow(Neutral950.copy(alpha = 0.72f), Offset(0f, 2f), 3f),
                ),
            color = statusColor,
            fontWeight = FontWeight.Bold,
            maxLines = 3,
        )
        ReportSummaryRow(
            text = content.attentionSummary,
            kind =
                if (content.attentionCount > 0) {
                    SummaryGlyphKind.Attention
                } else {
                    SummaryGlyphKind.Clear
                },
            accent = attentionColor,
        )
        ReportSummaryRow(
            text = content.completedAt,
            kind = SummaryGlyphKind.Calendar,
            accent = Lavender80,
        )
    }
}

private data class ReportSummaryContent(
    val statusText: String,
    val attentionSummary: String,
    val completedAt: String,
    val attentionCount: Int,
    val failureCount: Int,
)

private enum class SummaryGlyphKind {
    Attention,
    Clear,
    Calendar,
}

@Composable
private fun ReportSummaryRow(
    text: String,
    kind: SummaryGlyphKind,
    accent: Color,
) {
    val icon =
        when (kind) {
            SummaryGlyphKind.Attention -> Icons.Rounded.Warning
            SummaryGlyphKind.Clear -> Icons.Rounded.CheckCircle
            SummaryGlyphKind.Calendar -> Icons.Rounded.DateRange
        }
    Row(
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral300,
        )
    }
}

@Composable
private fun FonecheckScoreGauge(
    score: Int?,
    scoreState: ScoreState,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val scoreNumberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val scoreText =
        score?.let(scoreNumberFormat::format)
            ?: stringResource(R.string.home_latest_no_score)
    val scoreDescription =
        score?.let {
            stringResource(
                R.string.home_latest_score_description,
                scoreNumberFormat.format(it),
            )
        }
            ?: stringResource(
                R.string.home_latest_no_score_description,
                stringResource(scoreStateLabel(scoreState)),
            )
    val scoreTextStyle =
        if (score == null) {
            MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMono,
                shadow = Shadow(Neutral950.copy(alpha = 0.72f), Offset(0f, 2f), 3f),
            )
        } else {
            MaterialTheme.typography.displayLarge.copy(
                fontFamily = JetBrainsMono,
                fontSize = 56.sp,
                lineHeight = 64.sp,
                shadow = Shadow(Neutral950.copy(alpha = 0.72f), Offset(0f, 3f), 4f),
            )
        }

    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .testTag("home_score_gauge")
                .semantics { stateDescription = scoreDescription },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = size.minDimension * 0.105f
            val shadowWidth = strokeWidth + 3.dp.toPx()
            val arcTopLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val startAngle = 135f
            val totalSweep = 270f
            val scoreSweep = totalSweep * ((score ?: 0).coerceIn(0, 100) / 100f)

            drawArc(
                color = Neutral950.copy(alpha = 0.66f),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = arcTopLeft + Offset(0f, 2.dp.toPx()),
                size = arcSize,
                style = Stroke(width = shadowWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = Lavender40.copy(alpha = 0.38f),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            if (score != null && scoreSweep > 0f) {
                drawArc(
                    brush =
                        Brush.sweepGradient(
                            colors = listOf(Aqua40, Aqua80, Aqua80, Aqua40),
                            center = center,
                        ),
                    startAngle = startAngle,
                    sweepAngle = scoreSweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                drawArc(
                    color = Neutral50.copy(alpha = 0.24f),
                    startAngle = startAngle + 2f,
                    sweepAngle = (scoreSweep - 4f).coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = arcTopLeft + Offset(0f, -strokeWidth * 0.10f),
                    size = arcSize,
                    style = Stroke(width = 1.25.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }
        Text(
            text = scoreText,
            modifier = Modifier.padding(horizontal = 26.dp),
            style = scoreTextStyle,
            color = Neutral100,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = if (score == null) 2 else 1,
        )
    }
}

@Preview(name = "Score gauge - dark", widthDp = 180, heightDp = 216)
@Composable
private fun FonecheckScoreGaugeDarkPreview() {
    FonecheckScoreGaugePreview(darkTheme = true)
}

@Preview(name = "Score gauge - light", widthDp = 180, heightDp = 216)
@Composable
private fun FonecheckScoreGaugeLightPreview() {
    FonecheckScoreGaugePreview(darkTheme = false)
}

@Composable
private fun FonecheckScoreGaugePreview(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    FonecheckTheme(darkTheme = darkTheme) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                FonecheckScoreGauge(
                    score = 93,
                    scoreState = ScoreState.COMPLETE,
                    modifier = Modifier.size(180.dp),
                )
            }
        }
    }
}

@Preview(name = "Home top - dark", widthDp = 360, heightDp = 600)
@Composable
private fun HomeTopDarkPreview() {
    HomeTopPreview(darkTheme = true)
}

@Preview(name = "Home top - light", widthDp = 360, heightDp = 600)
@Composable
private fun HomeTopLightPreview() {
    HomeTopPreview(darkTheme = false)
}

@Composable
private fun HomeTopPreview(darkTheme: Boolean) {
    FonecheckTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HomeBrandHeader(onHistory = {}, onSettings = {})
                LatestFullCheckReadout(report = previewDiagnosticReport(), onClick = {})
                PrimaryButton(
                    label = stringResource(R.string.home_run_all),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun previewDiagnosticReport(): DiagnosticReport {
    val completedAt = Instant.parse("2026-08-11T10:18:00Z")
    val warningCategory = DiagnosticCategoryId.DEVICE
    val warningEvidence =
        listOf("screen", "storage").map { check ->
            DiagnosticEvidence(
                categoryId = warningCategory,
                checkId = DiagnosticCheckId(warningCategory, "${warningCategory.stableId}.$check"),
                status = DiagnosticStatus.WARNING,
                confidence = Confidence.HIGH,
                source = EvidenceSource.AUTOMATIC_MEASUREMENT,
                applicability = Applicability.APPLICABLE,
                capturedAt = completedAt,
            )
        }
    return DiagnosticReport(
        stableId = "preview-full-check",
        kind = ReportKind.FULL_CHECK,
        startedAt = completedAt.minusSeconds(90),
        completedAt = completedAt,
        device =
            ReportDeviceContext(
                manufacturer = "fonecheck",
                model = "Preview",
                brand = "fonecheck",
                product = "preview",
                androidRelease = "16",
                apiLevel = 36,
                securityPatch = "2026-08-01",
            ),
        app = ReportAppContext(versionName = "1.0", versionCode = 1),
        categories =
            DiagnosticCategoryId.entries.map { category ->
                DiagnosticCategoryResult(
                    categoryId = category,
                    aggregateStatus =
                        if (category == warningCategory) {
                            DiagnosticStatus.WARNING
                        } else {
                            DiagnosticStatus.PASS
                        },
                    evidence = if (category == warningCategory) warningEvidence else emptyList(),
                )
            },
        score = ScoreSummary(version = ScoreVersion.CURRENT, value = 93, state = ScoreState.COMPLETE),
        coverage =
            CoverageSummary(
                applicableCount = 100,
                completedCount = 84,
                notTestedCount = 16,
                unavailableCount = 0,
                percentage = 84,
            ),
        schemaVersion = ReportSchemaVersion.CURRENT,
    )
}

@Composable
private fun ReportMetrics(
    stacked: Boolean,
    categoriesValue: String,
    coverageValue: String,
    attentionValue: String,
    attentionCount: Int,
    failureCount: Int,
) {
    val metrics =
        listOf(
            ReportMetric(
                label = stringResource(R.string.home_latest_categories),
                value = categoriesValue,
                accent = Lavender80,
                kind = ReportMetricKind.Categories,
            ),
            ReportMetric(
                label = stringResource(R.string.home_latest_coverage),
                value = coverageValue,
                accent = Aqua80,
                kind = ReportMetricKind.Coverage,
            ),
            ReportMetric(
                label = stringResource(R.string.home_latest_needs_attention),
                value = attentionValue,
                accent =
                    when {
                        failureCount > 0 -> Red400
                        attentionCount > 0 -> Coral80
                        else -> Green400
                    },
                kind =
                    if (attentionCount > 0) {
                        ReportMetricKind.Attention
                    } else {
                        ReportMetricKind.Clear
                    },
            ),
        )

    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            metrics.forEachIndexed { index, metric ->
                if (index > 0) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Neutral600.copy(alpha = 0.46f)),
                    )
                }
                ReportMetricItem(
                    metric = metric,
                    horizontal = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 92.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            metrics.forEachIndexed { index, metric ->
                if (index > 0) {
                    Box(
                        modifier =
                            Modifier
                                .width(1.dp)
                                .height(72.dp)
                                .background(Neutral600.copy(alpha = 0.56f)),
                    )
                }
                ReportMetricItem(
                    metric = metric,
                    horizontal = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private data class ReportMetric(
    val label: String,
    val value: String,
    val accent: Color,
    val kind: ReportMetricKind,
)

private enum class ReportMetricKind {
    Categories,
    Coverage,
    Attention,
    Clear,
}

@Composable
private fun ReportMetricItem(
    metric: ReportMetric,
    horizontal: Boolean,
    modifier: Modifier = Modifier,
) {
    val description =
        stringResource(
            R.string.home_latest_metric_state_description,
            metric.label,
            metric.value,
        )

    if (horizontal) {
        Row(
            modifier =
                modifier
                    .semantics { stateDescription = description }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReportMetricIcon(
                kind = metric.kind,
                accent = metric.accent,
                size = 36.dp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ReportMetricValue(metric.value)
                ReportMetricLabel(metric.label, TextAlign.Start)
            }
        }
    } else {
        Column(
            modifier =
                modifier
                    .semantics { stateDescription = description }
                    .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ReportMetricIcon(
                kind = metric.kind,
                accent = metric.accent,
                size = 34.dp,
            )
            ReportMetricValue(metric.value)
            ReportMetricLabel(metric.label, TextAlign.Center)
        }
    }
}

@Composable
private fun ReportMetricIcon(
    kind: ReportMetricKind,
    accent: Color,
    size: Dp,
) {
    when (kind) {
        ReportMetricKind.Categories ->
            Canvas(modifier = Modifier.size(size)) {
                val tileSize = this.size.minDimension * 0.35f
                val gap = this.size.minDimension * 0.12f
                val start = (this.size.minDimension - tileSize * 2 - gap) / 2
                val cornerRadius = CornerRadius(tileSize * 0.24f)
                repeat(2) { row ->
                    repeat(2) { column ->
                        val topLeft =
                            Offset(
                                x = start + column * (tileSize + gap),
                                y = start + row * (tileSize + gap),
                            )
                        drawRoundRect(
                            color = Neutral950.copy(alpha = 0.62f),
                            topLeft = topLeft + Offset(0f, 1.5.dp.toPx()),
                            size = Size(tileSize, tileSize),
                            cornerRadius = cornerRadius,
                        )
                        drawRoundRect(
                            brush = Brush.linearGradient(listOf(accent, Lavender40)),
                            topLeft = topLeft,
                            size = Size(tileSize, tileSize),
                            cornerRadius = cornerRadius,
                        )
                        drawRoundRect(
                            color = Neutral50.copy(alpha = 0.18f),
                            topLeft = topLeft,
                            size = Size(tileSize, tileSize),
                            cornerRadius = cornerRadius,
                            style = Stroke(width = 1.dp.toPx()),
                        )
                    }
                }
            }
        ReportMetricKind.Coverage ->
            Canvas(modifier = Modifier.size(size)) {
                val center = this.center
                val outerRadius = this.size.minDimension * 0.42f
                val innerRadius = this.size.minDimension * 0.25f
                val strokeWidth = 2.5.dp.toPx()
                drawCircle(
                    color = Neutral950.copy(alpha = 0.62f),
                    radius = outerRadius,
                    center = center + Offset(0f, 1.5.dp.toPx()),
                    style = Stroke(width = strokeWidth + 2.dp.toPx()),
                )
                drawCircle(
                    color = accent,
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = strokeWidth),
                )
                drawCircle(
                    color = accent.copy(alpha = 0.62f),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = strokeWidth),
                )
                drawLine(
                    color = accent,
                    start = center,
                    end = Offset(this.size.width * 0.72f, this.size.height * 0.28f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = accent,
                    radius = this.size.minDimension * 0.10f,
                    center = center,
                )
                drawCircle(
                    color = Neutral900,
                    radius = this.size.minDimension * 0.045f,
                    center = center,
                )
            }
        ReportMetricKind.Attention ->
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(size),
            )
        ReportMetricKind.Clear ->
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(size),
            )
    }
}

@Composable
private fun ReportMetricValue(value: String) {
    Text(
        text = value,
        style =
            MaterialTheme.typography.titleLarge.copy(
                fontFamily = JetBrainsMono,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                shadow = Shadow(Neutral950.copy(alpha = 0.72f), Offset(0f, 2f), 3f),
            ),
        color = Neutral100,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

@Composable
private fun ReportMetricLabel(
    label: String,
    textAlign: TextAlign,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = Neutral300,
        textAlign = textAlign,
        maxLines = 2,
    )
}

@Composable
private fun reportStatusText(
    report: DiagnosticReport,
    presentation: HomeReportPresentation,
): String =
    stringResource(
        when {
            report.score.state == ScoreState.INCOMPLETE -> R.string.report_score_incomplete
            presentation.failureItemCount > 0 -> R.string.home_latest_status_fail
            presentation.warningItemCount > 0 -> R.string.home_latest_status_warning
            report.score.state == ScoreState.PARTIAL -> R.string.report_score_partial
            presentation.notTestedCount > 0 -> R.string.home_latest_status_not_tested
            presentation.notAvailableCount > 0 -> R.string.home_latest_status_unavailable
            presentation.infoCount > 0 && presentation.passCount == 0 -> R.string.home_latest_status_info
            else -> R.string.home_latest_status_good
        },
    )

private fun scoreStateLabel(state: ScoreState): Int =
    when (state) {
        ScoreState.COMPLETE -> R.string.report_score_complete
        ScoreState.PARTIAL -> R.string.report_score_partial
        ScoreState.INCOMPLETE -> R.string.report_score_incomplete
    }

private data class HomeReportPresentation(
    val totalCategories: Int,
    val completedCategories: Int,
    val passCount: Int,
    val warningCategoryCount: Int,
    val failureCategoryCount: Int,
    val infoCount: Int,
    val notAvailableCount: Int,
    val notTestedCount: Int,
    val warningItemCount: Int,
    val failureItemCount: Int,
) {
    val attentionCount: Int = warningItemCount + failureItemCount

    companion object {
        fun from(report: DiagnosticReport): HomeReportPresentation {
            val categories = report.categories
            val evidence = categories.flatMap { it.evidence }
            return HomeReportPresentation(
                totalCategories = categories.size,
                completedCategories =
                    categories.count {
                        it.aggregateStatus == DiagnosticStatus.PASS ||
                            it.aggregateStatus == DiagnosticStatus.WARNING ||
                            it.aggregateStatus == DiagnosticStatus.FAIL ||
                            it.aggregateStatus == DiagnosticStatus.INFO
                    },
                passCount = categories.count { it.aggregateStatus == DiagnosticStatus.PASS },
                warningCategoryCount = categories.count { it.aggregateStatus == DiagnosticStatus.WARNING },
                failureCategoryCount = categories.count { it.aggregateStatus == DiagnosticStatus.FAIL },
                infoCount = categories.count { it.aggregateStatus == DiagnosticStatus.INFO },
                notAvailableCount = categories.count { it.aggregateStatus == DiagnosticStatus.NOT_AVAILABLE },
                notTestedCount = categories.count { it.aggregateStatus == DiagnosticStatus.NOT_TESTED },
                warningItemCount = evidence.count { it.status == DiagnosticStatus.WARNING },
                failureItemCount = evidence.count { it.status == DiagnosticStatus.FAIL },
            )
        }
    }
}

@Composable
private fun CategoryGridItem(
    category: DiagnosticDestination,
    label: String,
    onClick: () -> Unit,
) {
    StandardCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(126.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(category.imageResId),
                    contentDescription = null,
                    modifier = Modifier.size(118.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
