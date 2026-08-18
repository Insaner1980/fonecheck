package com.insaner.fonecheck.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
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
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.localization.stableTextStringRes
import com.insaner.fonecheck.navigation.History
import com.insaner.fonecheck.navigation.Report
import com.insaner.fonecheck.navigation.Settings
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.CategoryNavigationRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.SegmentedBar
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.toSemanticTone
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

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
    LazyColumn(
        modifier = modifier.fillMaxSize().background(FonecheckTheme.colors.background),
        contentPadding =
            PaddingValues(
                start = FonecheckTheme.spacing.md,
                top = FonecheckTheme.spacing.sm,
                end = FonecheckTheme.spacing.md,
                bottom = FonecheckTheme.spacing.xl,
            ),
    ) {
        item {
            Box(modifier = Modifier.padding(bottom = FonecheckTheme.spacing.md)) {
                HomeBrandHeader(
                    onHistory = { onNavigate(History) },
                    onSettings = { onNavigate(Settings) },
                )
            }
        }

        item {
            Box(modifier = Modifier.padding(bottom = FonecheckTheme.spacing.md)) {
                LatestFullCheckSection(
                    state = latestFullCheck,
                    onOpenReport = { reportId -> onNavigate(Report(reportId)) },
                    onRetry = onRetryLatestFullCheck,
                )
            }
        }

        item {
            Box(modifier = Modifier.padding(bottom = FonecheckTheme.spacing.lg)) {
                PrimaryButton(
                    label = stringResource(R.string.home_run_all),
                    onClick = onRunAllTests,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            Column {
                SectionHeader(
                    label = stringResource(R.string.home_categories_title),
                    trailing = diagnosticDestinations.size.toString(),
                )
                diagnosticDestinations.forEachIndexed { index, destination ->
                    val result = homeCategoryRowResult(latestFullCheck, destination.category)
                    CategoryNavigationRow(
                        label = stringResource(destination.labelResId),
                        value = result.value,
                        tone = result.tone,
                        onClick = { onNavigate(destination.route) },
                        modifier = Modifier.testTag("home_category_${destination.category.stableId}"),
                        showDivider = index < diagnosticDestinations.lastIndex,
                    )
                }
            }
        }
    }
}

internal fun latestReportUsesStackedLayout(
    availableWidthDp: Float,
    fontScale: Float,
): Boolean = availableWidthDp < 312f || fontScale > 1.3f

internal const val HOME_LOADING_INDICATOR_DELAY_MILLIS = 300L

private val homeTimestampFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm", Locale.ROOT)

internal fun homeUiLanguageLocale(locale: Locale): Locale = uiLanguageLocale(locale)

internal fun formatHomeCompletedAt(
    value: Instant,
    zoneId: ZoneId,
): String = homeTimestampFormatter.withZone(zoneId).format(value)

@Composable
private fun HomeBrandHeader(
    onHistory: () -> Unit,
    onSettings: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val fontScale = LocalDensity.current.fontScale
        val stackActions = latestReportUsesStackedLayout(maxWidth.value, fontScale)
        if (stackActions) {
            Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm)) {
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
                horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
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
        horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.fonecheck_mark),
            contentDescription = null,
            modifier = Modifier.size(FonecheckTheme.spacing.xxl),
        )
        Text(
            text = appName,
            modifier = Modifier.semantics { heading() },
            style = FonecheckTheme.type.screenTitle,
            color = FonecheckTheme.colors.textPrimary,
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
        horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onHistory) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = stringResource(R.string.home_history_content_description),
                tint = FonecheckTheme.colors.textPrimary,
            )
        }
        IconButton(onClick = onSettings) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.home_settings_content_description),
                tint = FonecheckTheme.colors.textPrimary,
            )
        }
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
    val showLoadingIndicator by
        produceState(initialValue = false, key1 = loading) {
            if (loading) {
                delay(HOME_LOADING_INDICATOR_DELAY_MILLIS)
                value = true
            }
        }
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
        if (showLoadingIndicator) {
            Box(modifier = Modifier.testTag("home_latest_loading_indicator")) {
                IndeterminateRule()
            }
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

private data class HomeCategoryRowResult(
    val value: String?,
    val tone: SemanticTone,
)

internal fun stableHomeHeadlineSource(categoryId: DiagnosticCategoryId): String? =
    when (categoryId) {
        DiagnosticCategoryId.DEVICE -> "report.device.model"
        DiagnosticCategoryId.PERFORMANCE -> "performance.cpu"
        DiagnosticCategoryId.CAMERA -> "camera.inventory"
        DiagnosticCategoryId.SENSORS -> "sensors.inventory"
        DiagnosticCategoryId.BATTERY -> "battery.health"
        else -> null
    }

private val stableBatteryHealthCodes =
    setOf(
        "good",
        "dead",
        "over_voltage",
        "unspecified_failure",
    )

@Composable
private fun homeCategoryRowResult(
    state: LatestFullCheckState,
    categoryId: DiagnosticCategoryId,
): HomeCategoryRowResult {
    val report = (state as? LatestFullCheckState.Available)?.report
        ?: return HomeCategoryRowResult(null, SemanticTone.NEUTRAL)
    val category = report.categories.firstOrNull { it.categoryId == categoryId }
        ?: return HomeCategoryRowResult(null, SemanticTone.NEUTRAL)
    val tone = category.aggregateStatus.toSemanticTone()
    val headline =
        if (category.aggregateStatus == DiagnosticStatus.PASS || category.aggregateStatus == DiagnosticStatus.INFO) {
            stableHomeHeadline(report, categoryId)
        } else {
            null
        }
    return HomeCategoryRowResult(
        value = headline ?: homeCategoryStatusLabel(category.aggregateStatus),
        tone = tone,
    )
}

@Composable
private fun stableHomeHeadline(
    report: DiagnosticReport,
    categoryId: DiagnosticCategoryId,
): String? {
    val source = stableHomeHeadlineSource(categoryId) ?: return null
    if (source == "report.device.model") {
        return report.device.model.takeUnless { it == DeviceInfo.UNAVAILABLE }
    }
    val evidence =
        report.categories
            .firstOrNull { it.categoryId == categoryId }
            ?.evidence
            ?.firstOrNull { it.checkId.value == source }
            ?: return null
    return when (source) {
        "performance.cpu" ->
            (evidence.value as? EvidenceValue.IntValue)?.value?.let { count ->
                pluralStringResource(R.plurals.home_category_cpu_cores, count, count)
            }
        "camera.inventory" ->
            (evidence.value as? EvidenceValue.IntValue)?.value?.let { count ->
                pluralStringResource(R.plurals.home_category_cameras, count, count)
            }
        "sensors.inventory" ->
            (evidence.value as? EvidenceValue.IntValue)?.value?.let { count ->
                pluralStringResource(R.plurals.home_category_sensors, count, count)
            }
        "battery.health" ->
            (evidence.value as? EvidenceValue.StableTextCodeValue)
                ?.value
                ?.takeIf(stableBatteryHealthCodes::contains)
                ?.let { stableTextStringRes(it) }
                ?.let { stringResource(it) }
        else -> null
    }
}

@Composable
private fun homeCategoryStatusLabel(status: DiagnosticStatus): String =
    stringResource(
        when (status) {
            DiagnosticStatus.PASS -> R.string.run_all_status_pass
            DiagnosticStatus.WARNING -> R.string.run_all_status_warning
            DiagnosticStatus.FAIL -> R.string.run_all_status_fail
            DiagnosticStatus.INFO -> R.string.run_all_status_info
            DiagnosticStatus.NOT_AVAILABLE -> R.string.run_all_status_unavailable
            DiagnosticStatus.NOT_TESTED -> R.string.run_all_status_not_tested
        },
    )

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
    val locale = homeUiLanguageLocale(LocalLocale.current.platformLocale)
    val numberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val percentFormat = remember(locale) { NumberFormat.getPercentInstance(locale) }
    val completedAtValue =
        remember(report.completedAt) {
            formatHomeCompletedAt(report.completedAt, ZoneId.systemDefault())
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
 * Hair spaces balance the slash between the figure and its total without separating their shared
 * baseline into independently measured text nodes.
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
                append('/')
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
            color = FonecheckTheme.colors.background,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(FonecheckTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
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

private data class HomeReportPresentation(
    val totalCategories: Int,
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
