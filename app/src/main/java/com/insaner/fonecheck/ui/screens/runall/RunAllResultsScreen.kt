@file:Suppress("MatchingDeclarationName")

package com.insaner.fonecheck.ui.screens.runall

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.CategoryTestResult
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.TestResult
import com.insaner.fonecheck.domain.model.TestStatus
import com.insaner.fonecheck.localization.evidenceLabelResource
import com.insaner.fonecheck.localization.evidenceReasonStringRes
import com.insaner.fonecheck.localization.shouldShowEvidenceReason
import com.insaner.fonecheck.localization.stableTextStringRes
import com.insaner.fonecheck.navigation.CategoryRetest
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.DisclosureHeader
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.SegmentedBar
import com.insaner.fonecheck.ui.components.StatusLamp
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.WindowLabel
import com.insaner.fonecheck.ui.components.WindowReading
import com.insaner.fonecheck.ui.components.statusLabel
import com.insaner.fonecheck.ui.format.formatUiDateTime
import com.insaner.fonecheck.ui.format.uiFileSize
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.screens.report.ReportDetailPresentation
import com.insaner.fonecheck.ui.screens.report.ReportDetailPresenter
import com.insaner.fonecheck.ui.screens.report.stableCodeFallback
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.toSemanticTone

enum class ReportResultMode {
    COMPLETED_RUN,
    SAVED_REPORT,
}

@Composable
@Suppress("kotlin:S3776") // Report sections intentionally mirror the persisted result hierarchy.
fun RunAllResultsScreen(
    report: DiagnosticReport,
    saveStatus: ReportSaveStatus,
    onRetrySave: () -> Unit,
    onOpenCategory: (Any) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    mode: ReportResultMode = ReportResultMode.COMPLETED_RUN,
) {
    val presentation = remember(report) { ReportDetailPresenter.present(report) }
    val representedCategoryIds = remember(report) { report.categories.map { it.categoryId }.toSet() }
    val categories =
        presentation.categories.mapNotNull { category ->
            if (diagnosticDestinations.none { it.category == category.categoryId }) {
                null
            } else {
                category.toUiResult()
            }
        }
    val attentionResults =
        categories.filter {
            it.status is TestStatus.Fail || it.status is TestStatus.Warning
        }
    val completedResults =
        categories.filter {
            it.status == TestStatus.Pass || it.status is TestStatus.Info
        }
    val incompleteResults =
        categories.filter {
            it.status == TestStatus.NotAvailable || it.status == TestStatus.NotTested
        }
    var expandedCategoryName by rememberSaveable(attentionResults) {
        mutableStateOf(attentionResults.firstOrNull()?.category?.name)
    }

    TestScreenContent(modifier = modifier) {
        item {
            ResultsSummary(
                report = report,
                presentation = presentation,
                categoryTones = categories.map { it.status.semanticTone() },
            )
        }

        if (saveStatus != ReportSaveStatus.SAVED) {
            item {
                ReportSaveSection(saveStatus, onRetrySave)
            }
        }

        resultGroup(
            titleResId = R.string.run_all_needs_attention,
            results = attentionResults,
            expandedCategoryName = expandedCategoryName,
            onExpandedChange = { expandedCategoryName = it },
            onOpenCategory = onOpenCategory,
            mode = mode,
            representedCategoryIds = representedCategoryIds,
        )
        resultGroup(
            titleResId = R.string.run_all_completed,
            results = completedResults,
            expandedCategoryName = expandedCategoryName,
            onExpandedChange = { expandedCategoryName = it },
            onOpenCategory = onOpenCategory,
            mode = mode,
            representedCategoryIds = representedCategoryIds,
        )
        resultGroup(
            titleResId = R.string.run_all_not_completed,
            results = incompleteResults,
            expandedCategoryName = expandedCategoryName,
            onExpandedChange = { expandedCategoryName = it },
            onOpenCategory = onOpenCategory,
            mode = mode,
            representedCategoryIds = representedCategoryIds,
        )

        // Provenance last. A saved report used to open with seven rows of device and version
        // detail before it said how the phone had done.
        if (mode == ReportResultMode.SAVED_REPORT) {
            item {
                ReportMetadataSection(report, presentation.durationMillis)
            }
        }

        item {
            PrimaryButton(
                label =
                    stringResource(
                        if (mode == ReportResultMode.SAVED_REPORT) {
                            R.string.report_back
                        } else {
                            R.string.run_all_done
                        },
                    ),
                onClick = onDone,
                enabled = saveStatus == ReportSaveStatus.SAVED,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun LazyListScope.resultGroup(
    @StringRes titleResId: Int,
    results: List<CategoryTestResult>,
    expandedCategoryName: String?,
    onExpandedChange: (String?) -> Unit,
    onOpenCategory: (Any) -> Unit,
    mode: ReportResultMode,
    representedCategoryIds: Set<DiagnosticCategoryId>,
) {
    if (results.isEmpty()) return

    item {
        SectionHeader(
            label = stringResource(titleResId),
            trailing = uiNumber(results.size, grouping = true),
        )
    }
    items(
        items = results,
        key = { it.category.name },
    ) { result ->
        CategoryResult(
            result = result,
            isExpanded = expandedCategoryName == result.category.name,
            onToggle = {
                onExpandedChange(toggleExpanded(expandedCategoryName, result))
            },
            onOpenCategory = onOpenCategory,
            mode = mode,
            canOpenCategory = mode != ReportResultMode.SAVED_REPORT || result.category in representedCategoryIds,
        )
    }
}

private fun toggleExpanded(
    currentCategoryName: String?,
    result: CategoryTestResult,
): String? = if (currentCategoryName == result.category.name) null else result.category.name

@Composable
private fun ResultsSummary(
    report: DiagnosticReport,
    presentation: ReportDetailPresentation,
    categoryTones: List<SemanticTone>,
) {
    val counts = presentation.counts

    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm)) {
        Text(
            text = stringResource(R.string.run_all_results_title),
            style = FonecheckTheme.type.screenTitle,
            color = FonecheckTheme.colors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        Note(stringResource(R.string.run_all_results_description))
        ScoreReadout(report.score)
        // One segment per category, in the colour of that category: the shape of the run, drawn
        // beside the score it produced and counted in words underneath.
        SegmentedBar(segments = categoryTones)
        Column {
            StatusCount(DiagnosticStatus.PASS, counts.pass)
            StatusCount(DiagnosticStatus.INFO, counts.info)
            StatusCount(DiagnosticStatus.WARNING, counts.warning)
            StatusCount(DiagnosticStatus.FAIL, counts.fail)
            StatusCount(DiagnosticStatus.NOT_AVAILABLE, counts.notAvailable)
            StatusCount(DiagnosticStatus.NOT_TESTED, counts.notTested)
            DataRow(
                label = stringResource(R.string.report_coverage),
                value =
                    stringResource(
                        R.string.report_coverage_value,
                        uiNumber(report.coverage.percentage),
                    ),
            )
            DataRow(
                label = stringResource(R.string.report_checks),
                value =
                    stringResource(
                        R.string.report_checks_value,
                        uiNumber(report.coverage.completedCount),
                        uiNumber(report.coverage.applicableCount),
                    ),
            )
        }
    }
}

/**
 * The score in the window the panel keeps for a reading, with the state that says what it is worth
 * directly beneath it.
 *
 * A run with too little evidence has no score at all. The window says so rather than drawing a
 * zero, and it drops the unit with it: a reading that was never taken has no denominator.
 */
@Composable
private fun ScoreReadout(score: ScoreSummary) {
    val value = score.value
    ReadoutWindow {
        WindowLabel(text = stringResource(R.string.run_all_overall_score))
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        WindowReading(
            value = value?.let { uiNumber(it) } ?: stringResource(R.string.value_unavailable_short),
            unit = stringResource(R.string.report_score_unit).takeIf { value != null },
        )
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        WindowLabel(text = scoreStateLabel(score.state))
    }
}

/**
 * How many categories ended in one status. The label is the status word itself, taken from the one
 * mapping the app has for it, so the count reads in the same vocabulary as the rows below it.
 */
@Composable
private fun StatusCount(
    status: DiagnosticStatus,
    count: Int,
) {
    DataRow(
        label = statusLabel(status),
        value = uiNumber(count),
        tone = status.toSemanticTone(),
        modifier = Modifier.testTag("report_count_" + status.name),
    )
}

@Composable
private fun ReportMetadataSection(
    report: DiagnosticReport,
    durationMillis: Long,
) {
    val locale = uiLanguageLocale(LocalLocale.current.platformLocale)
    val completedAt =
        remember(report.completedAt, locale) {
            formatUiDateTime(report.completedAt, locale)
        }
    val durationSeconds = durationMillis / 1_000L
    val deviceName =
        listOf(report.device.manufacturer, report.device.model)
            .filter(String::isNotBlank)
            .distinct()
            .joinToString(" ")

    Column {
        SectionHeader(stringResource(R.string.report_saved_title))
        Note(stringResource(R.string.report_saved_description))
        DataRow(
            label = stringResource(R.string.report_kind),
            value =
                stringResource(
                    if (report.kind == ReportKind.FULL_CHECK) {
                        R.string.report_kind_full
                    } else {
                        R.string.report_kind_category
                    },
                ),
        )
        LongValueRow(
            label = stringResource(R.string.report_device),
            value = deviceName,
        )
        LongValueRow(
            label = stringResource(R.string.report_android),
            value =
                stringResource(
                    R.string.report_android_value,
                    report.device.androidRelease,
                    uiNumber(report.device.apiLevel),
                ),
        )
        LongValueRow(
            label = stringResource(R.string.report_app_version),
            value =
                stringResource(
                    R.string.report_app_version_value,
                    report.app.versionName,
                    uiNumber(report.app.versionCode),
                ),
        )
        LongValueRow(
            label = stringResource(R.string.report_completed_at),
            value = completedAt,
        )
        DataRow(
            label = stringResource(R.string.report_duration),
            value =
                stringResource(
                    R.string.report_duration_value,
                    uiNumber(durationSeconds / 60L),
                    uiNumber(durationSeconds % 60L),
                ),
        )
        LongValueRow(
            label = stringResource(R.string.report_identifier),
            value = report.stableId,
        )
    }
}

@Composable
private fun scoreStateLabel(state: ScoreState): String =
    stringResource(
        when (state) {
            ScoreState.COMPLETE -> R.string.report_score_complete
            ScoreState.PARTIAL -> R.string.report_score_partial
            ScoreState.INCOMPLETE -> R.string.report_score_incomplete
        },
    )

@Composable
private fun ReportSaveSection(
    status: ReportSaveStatus,
    onRetry: () -> Unit,
) {
    val failed = status == ReportSaveStatus.FAILED
    ScreenStateCard(
        type = if (failed) ScreenStateType.ERROR else ScreenStateType.LOADING,
        message =
            stringResource(
                if (failed) R.string.run_all_save_failed else R.string.run_all_saving_report,
            ),
        actionLabel = stringResource(R.string.run_all_retry_save).takeIf { failed },
        onAction = onRetry.takeIf { failed },
    )
}

@Composable
private fun CategoryResult(
    result: CategoryTestResult,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpenCategory: (Any) -> Unit,
    mode: ReportResultMode,
    canOpenCategory: Boolean,
) {
    val destination = diagnosticDestinations.first { it.category == result.category }
    val title = stringResource(destination.labelResId)
    val diagnosticStatus = result.status.toDiagnosticStatus()
    val status = statusLabel(diagnosticStatus)
    val route =
        if (mode == ReportResultMode.SAVED_REPORT) {
            CategoryRetest(result.category.stableId)
        } else {
            destination.route
        }
    val openAction =
        if (canOpenCategory) {
            { onOpenCategory(route) }
        } else {
            null
        }

    Column(
        modifier = Modifier.fillMaxWidth().testTag("report_category_${result.category.stableId}"),
    ) {
        DisclosureHeader(
            label = title,
            summary = status,
            expanded = isExpanded,
            onClick = onToggle,
            tone = diagnosticStatus.toSemanticTone(),
            // Repeated rows inside a group take the hairline; the group header keeps the panel edge.
            strongDivider = false,
            leading = { StatusLamp(status = diagnosticStatus) },
        )
        if (result.status != TestStatus.NotTested) {
            Note(text = result.summary)
        }
        if (isExpanded) {
            ResultDetails(
                results = result.results,
                onOpen = openAction,
                mode = mode,
            )
        }
    }
}

@Composable
private fun ResultDetails(
    results: List<TestResult>,
    onOpen: (() -> Unit)?,
    mode: ReportResultMode,
) {
    Column(
        modifier = Modifier.padding(top = FonecheckTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
    ) {
        val sectionReasons = repeatedConsecutiveReasons(results.map(TestResult::reason))
        sectionReasons.forEach { reason ->
            Note(text = stringResource(R.string.report_evidence_reason, reason))
        }
        if (results.isEmpty()) {
            Note(text = stringResource(R.string.report_no_saved_evidence))
        } else {
            results.forEach { result ->
                ResultDetail(
                    result = result,
                    showReason = result.reason !in sectionReasons,
                )
            }
        }
        onOpen?.let {
            SecondaryButton(
                label =
                    stringResource(
                        if (mode == ReportResultMode.SAVED_REPORT) {
                            R.string.report_retest
                        } else {
                            R.string.run_all_open_test
                        },
                    ),
                onClick = it,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ResultDetail(
    result: TestResult,
    showReason: Boolean,
) {
    val hasDetail = result.detail != null
    Column {
        DataRow(
            label = result.name,
            value = statusLabel(result.status.toDiagnosticStatus()),
            tone = result.status.semanticTone(),
            confidence = result.confidence.takeUnless { hasDetail },
        )
        result.detail?.let { detail ->
            LongValueRow(
                label = stringResource(R.string.report_evidence_value),
                value = detail,
                confidence = result.confidence,
            )
        }
        result.source?.let { source ->
            LongValueRow(
                label = stringResource(R.string.report_evidence_source),
                value = sourceLabel(source),
            )
        }
        result.reason?.takeIf { showReason }?.let { reason ->
            Note(text = stringResource(R.string.report_evidence_reason, reason))
        }
    }
}

@Composable
private fun DiagnosticCategoryResult.toUiResult(): CategoryTestResult =
    CategoryTestResult(
        category = categoryId,
        status = aggregateStatus.toLegacyStatus(),
        summary = categorySummary(aggregateStatus),
        results = evidence.map { it.toUiResult() },
    )

@Composable
private fun DiagnosticEvidence.toUiResult(): TestResult =
    TestResult(
        id = checkId.value,
        name = evidenceLabel(checkId.value),
        status = status.toLegacyStatus(),
        detail = evidenceDetail(this),
        confidence = confidence,
        timestamp = capturedAt.toEpochMilli(),
        source = source,
        reason =
            reason
                ?.takeIf { shouldShowEvidenceReason(status, it) }
                ?.let { reasonLabel(it) },
    )

internal fun repeatedConsecutiveReasons(reasons: List<String?>): Set<String> =
    buildSet {
        reasons.zipWithNext().forEach { (first, second) ->
            first?.takeIf { it == second }?.let(::add)
        }
    }

private fun DiagnosticStatus.toLegacyStatus(): TestStatus =
    when (this) {
        DiagnosticStatus.PASS -> TestStatus.Pass
        DiagnosticStatus.FAIL -> TestStatus.Fail()
        DiagnosticStatus.WARNING -> TestStatus.Warning()
        DiagnosticStatus.INFO -> TestStatus.Info("")
        DiagnosticStatus.NOT_AVAILABLE -> TestStatus.NotAvailable
        DiagnosticStatus.NOT_TESTED -> TestStatus.NotTested
    }

@Composable
private fun categorySummary(status: DiagnosticStatus): String =
    stringResource(
        when (status) {
            DiagnosticStatus.PASS -> R.string.run_all_summary_pass
            DiagnosticStatus.INFO -> R.string.run_all_summary_info
            DiagnosticStatus.WARNING -> R.string.run_all_summary_warning
            DiagnosticStatus.FAIL -> R.string.run_all_summary_fail
            DiagnosticStatus.NOT_AVAILABLE -> R.string.run_all_summary_unavailable
            DiagnosticStatus.NOT_TESTED -> R.string.run_all_summary_not_tested
        },
    )

@Composable
private fun evidenceLabel(checkId: String): String =
    evidenceLabelResource(checkId)?.let { resource ->
        resource.formatArgument?.let { argument ->
            stringResource(resource.stringResId, uiNumber(argument))
        } ?: stringResource(resource.stringResId)
    } ?: stableCodeFallback(checkId.substringAfter('.'))

@Composable
internal fun evidenceDetail(evidence: DiagnosticEvidence): String? =
    evidence.value?.let { evidenceValueLabel(it, evidence.unit) }

@Composable
private fun evidenceValueLabel(
    value: EvidenceValue,
    unit: EvidenceUnitCode?,
): String =
    when (value) {
        is EvidenceValue.BooleanValue ->
            stringResource(if (value.value) R.string.status_yes else R.string.status_no)

        is EvidenceValue.IntValue ->
            when (unit?.value) {
                "percent" -> stringResource(R.string.storage_percent_value, localizedNumber(value.value))
                "samples" ->
                    pluralStringResource(
                        R.plurals.sensor_samples,
                        value.value,
                        uiNumber(value.value),
                    )
                else -> localizedNumber(value.value)
            }

        is EvidenceValue.LongValue ->
            when (unit?.value) {
                "operations_per_second" ->
                    stringResource(R.string.perf_benchmark_cpu_rate_value, localizedNumber(value.value))
                "milliseconds" ->
                    stringResource(
                        R.string.conn_gps_fix_duration_format,
                        uiNumber(value.value / 1_000.0, 1, 1),
                    )
                "bytes" -> uiFileSize(value.value)
                else -> localizedNumber(value.value)
            }
        is EvidenceValue.DecimalValue -> localizedNumber(value.value)
        is EvidenceValue.DoubleValue ->
            when (unit?.value) {
                "celsius" -> stringResource(R.string.run_all_detail_temperature, uiNumber(value.value, 1, 1))
                "milliamperes" -> stringResource(R.string.batt_value_milliamps, uiNumber(value.value, 1, 1))
                "ratio" -> stringResource(R.string.thermal_headroom_value, uiNumber(value.value, 2, 2))
                "percent" -> stringResource(R.string.storage_percent_value, localizedNumber(value.value))
                "mebibytes_per_second" ->
                    stringResource(R.string.storage_rate_value, uiNumber(value.value, 1, 1))
                else -> localizedNumber(value.value)
            }

        is EvidenceValue.RawTextValue -> value.value
        is EvidenceValue.StableTextCodeValue -> stableTextLabel(value.value)
    }

@Composable
private fun localizedNumber(value: Number): String = uiNumber(value, maximumFractionDigits = 6, grouping = true)

@Composable
private fun stableTextLabel(code: String): String =
    stableTextStringRes(code)?.let { stringResource(it) } ?: stableCodeFallback(code)

@Composable
private fun reasonLabel(reason: EvidenceReasonCode): String =
    evidenceReasonStringRes(reason)?.let { stringResource(it) } ?: stableCodeFallback(reason.value)

@Composable
private fun sourceLabel(source: EvidenceSource): String =
    stringResource(
        when (source) {
            EvidenceSource.AUTOMATIC_MEASUREMENT -> R.string.report_source_automatic
            EvidenceSource.ANDROID_API -> R.string.report_source_android_api
            EvidenceSource.USER_CONFIRMATION -> R.string.report_source_user
            EvidenceSource.DERIVED -> R.string.report_source_derived
            EvidenceSource.ESTIMATE -> R.string.report_source_estimate
        },
    )

/**
 * The report is assembled from [DiagnosticStatus] and drawn from the legacy [TestStatus], so the
 * screen converts back once and reads the status word, the tone and the lamp from that one value.
 * The vocabulary has a single source rather than three parallel when blocks that can drift apart.
 */
private fun TestStatus.toDiagnosticStatus(): DiagnosticStatus =
    when (this) {
        TestStatus.Pass -> DiagnosticStatus.PASS
        is TestStatus.Warning -> DiagnosticStatus.WARNING
        is TestStatus.Fail -> DiagnosticStatus.FAIL
        is TestStatus.Info -> DiagnosticStatus.INFO
        TestStatus.NotAvailable -> DiagnosticStatus.NOT_AVAILABLE
        TestStatus.NotTested -> DiagnosticStatus.NOT_TESTED
    }

private fun TestStatus.semanticTone(): SemanticTone = toDiagnosticStatus().toSemanticTone()
