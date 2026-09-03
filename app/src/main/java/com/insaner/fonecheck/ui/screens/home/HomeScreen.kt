package com.insaner.fonecheck.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
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
import com.insaner.fonecheck.navigation.History
import com.insaner.fonecheck.navigation.Report
import com.insaner.fonecheck.navigation.Settings
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.IconBoxButton
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.InstrumentActionButton
import com.insaner.fonecheck.ui.components.InstrumentTickRule
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.WindowBar
import com.insaner.fonecheck.ui.components.WindowLabel
import com.insaner.fonecheck.ui.components.WindowReading
import com.insaner.fonecheck.ui.format.formatTechnicalUiDateTime
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.FonecheckType
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigate: (Any) -> Unit,
    onRunAllTests: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val latestFullCheck by viewModel.latestFullCheck.collectAsStateWithLifecycle()
    val currentTime = remember(latestFullCheck) { Instant.now() }

    HomeContent(
        latestFullCheck = latestFullCheck,
        onNavigate = onNavigate,
        onRunAllTests = onRunAllTests,
        onRetryLatestFullCheck = viewModel::retry,
        modifier = modifier,
        currentTime = currentTime,
    )
}

@Composable
internal fun HomeContent(
    latestFullCheck: LatestFullCheckState,
    onNavigate: (Any) -> Unit,
    onRunAllTests: () -> Unit,
    modifier: Modifier = Modifier,
    onRetryLatestFullCheck: () -> Unit = {},
    currentTime: Instant = Instant.now(),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(FonecheckTheme.colors.panel),
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
                    currentTime = currentTime,
                )
            }
        }

        item {
            Box(modifier = Modifier.padding(bottom = FonecheckTheme.spacing.lg)) {
                InstrumentActionButton(
                    label = stringResource(R.string.home_start_full_check),
                    onClick = onRunAllTests,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            Column {
                HomeStatusPanel(
                    state = latestFullCheck,
                    onNavigate = onNavigate,
                )
                if (latestFullCheck is LatestFullCheckState.Available) {
                    Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
                    InstrumentTickRule()
                }
            }
        }
    }
}

internal fun latestReportUsesStackedLayout(
    availableWidthDp: Float,
    fontScale: Float,
): Boolean = availableWidthDp < 312f || fontScale > HOME_LARGE_FONT_SCALE_THRESHOLD

internal const val HOME_LOADING_INDICATOR_DELAY_MILLIS = 300L

internal val HOME_REPORT_STALE_AFTER: Duration = Duration.ofHours(24)

internal data class HomeReportRecency(
    val isStale: Boolean,
    val elapsedDays: Long,
)

internal fun homeReportRecency(
    completedAt: Instant,
    currentTime: Instant,
): HomeReportRecency {
    val age = Duration.between(completedAt, currentTime).coerceAtLeast(Duration.ZERO)
    return HomeReportRecency(
        isStale = age >= HOME_REPORT_STALE_AFTER,
        elapsedDays = age.toDays(),
    )
}

internal fun homeUiLanguageLocale(locale: Locale): Locale = uiLanguageLocale(locale)

internal fun formatHomeCompletedAt(
    value: Instant,
    locale: Locale,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = formatTechnicalUiDateTime(value, locale, zoneId)

internal fun homePaddedCount(value: Int): String = value.toString().padStart(2, '0')

/** Matches the rule `SectionHeader` draws, for the stacked variant that cannot delegate to it. */
private val HomeStackedRuleThickness = 3.dp

@Composable
private fun HomeBrandHeader(
    onHistory: () -> Unit,
    onSettings: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeBrand(modifier = Modifier.weight(1f))
            HeaderActions(onHistory = onHistory, onSettings = onSettings)
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        InstrumentTickRule()
    }
}

@Composable
private fun HomeBrand(modifier: Modifier = Modifier) {
    val appName = stringResource(R.string.app_name)
    val density = LocalDensity.current
    Text(
        text = appName,
        modifier = modifier.semantics { heading() },
        style =
            FonecheckTheme.type.screenTitle.copy(
                fontSize = with(density) { 28.dp.toSp() },
                lineHeight = with(density) { 34.dp.toSp() },
            ),
        color = FonecheckTheme.colors.textPrimary,
        maxLines = 1,
    )
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
        IconBoxButton(
            imageVector = Icons.AutoMirrored.Filled.List,
            contentDescription = stringResource(R.string.home_history_content_description),
            onClick = onHistory,
        )
        IconBoxButton(
            imageVector = Icons.Filled.Settings,
            contentDescription = stringResource(R.string.home_settings_content_description),
            onClick = onSettings,
        )
    }
}

@Composable
private fun LatestFullCheckSection(
    state: LatestFullCheckState,
    onOpenReport: (String) -> Unit,
    onRetry: () -> Unit,
    currentTime: Instant,
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
                currentTime = currentTime,
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
        HairlineRule(color = FonecheckTheme.colors.rule)
    }
}

/**
 * The finished report as a readout: how many categories passed, the shape of the run as a segment
 * per category, and the two figures that qualify it. The whole block opens the full report.
 */
@Composable
private fun LatestFullCheckReadout(
    report: DiagnosticReport,
    onClick: () -> Unit,
    currentTime: Instant,
) {
    val presentation = remember(report) { HomeReportPresentation.from(report) }
    val recency = remember(report.completedAt, currentTime) { homeReportRecency(report.completedAt, currentTime) }
    val locale = homeUiLanguageLocale(LocalLocale.current.platformLocale)
    val numberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val percentFormat = remember(locale) { NumberFormat.getPercentInstance(locale) }
    val completedAtValue =
        remember(report.completedAt, locale) {
            formatHomeCompletedAt(report.completedAt, locale)
        }
    val coverageValue =
        stringResource(
            R.string.home_latest_coverage_value,
            percentFormat.format(report.coverage.percentage / 100.0),
        )
    val elapsedDays = recency.elapsedDays.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    val elapsedValue =
        if (recency.isStale) {
            pluralStringResource(R.plurals.home_latest_days_ago, elapsedDays, elapsedDays)
        } else {
            null
        }
    val completedDescription = stringResource(R.string.home_latest_completed_at, completedAtValue)
    val timingDescription =
        if (elapsedValue != null) {
            stringResource(
                R.string.home_latest_past_state_description,
                elapsedValue,
                completedAtValue,
            )
        } else {
            completedDescription
        }
    val attentionSummary =
        if (presentation.attentionCount == 0) {
            stringResource(R.string.home_latest_no_attention)
        } else {
            pluralStringResource(
                R.plurals.home_latest_evidence_attention_summary,
                presentation.attentionCount,
                presentation.attentionCount,
            )
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
            timingDescription,
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
                label =
                    stringResource(
                        if (recency.isStale) {
                            R.string.home_latest_past_title
                        } else {
                            R.string.home_latest_title
                        },
                    ),
                trailing = elapsedValue ?: completedAtValue,
            )
            if (recency.isStale) {
                Text(
                    text = completedDescription,
                    style = FonecheckTheme.type.note,
                    color = FonecheckTheme.colors.textMuted,
                    modifier = Modifier.padding(top = FonecheckTheme.spacing.xs),
                )
            }
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            ReadoutWindow {
                WindowLabel(text = stringResource(R.string.home_latest_passed_label))
                Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
                // Home raises the readout above its role size: this figure is the point of the screen.
                WindowReading(
                    value = homePaddedCount(presentation.passCount),
                    unit =
                        stringResource(
                            R.string.home_latest_passed_total,
                            numberFormat.format(presentation.totalCategories),
                        ),
                    style = FonecheckType.readout.copy(fontSize = 56.sp, lineHeight = 60.sp),
                    stacked = stacked,
                )
                Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
                LatestCheckInfoLine(
                    coverage = coverageValue,
                    coveragePercentage = report.coverage.percentage,
                    attention = attentionSummary,
                )
            }
        }
    }
}

@Composable
private fun LatestCheckInfoLine(
    coverage: String,
    coveragePercentage: Int,
    attention: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        WindowLabel(text = coverage)
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        WindowBar(coveragePercentage)
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.xs))
        WindowLabel(text = attention)
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
            color = FonecheckTheme.colors.panel,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(FonecheckTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
            ) {
                HomeBrandHeader(onHistory = {}, onSettings = {})
                LatestFullCheckReadout(
                    report = previewDiagnosticReport(),
                    onClick = {},
                    currentTime = Instant.parse("2026-08-20T13:18:00Z"),
                )
                InstrumentActionButton(
                    label = stringResource(R.string.home_start_full_check),
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
