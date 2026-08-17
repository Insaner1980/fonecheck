@file:Suppress("MatchingDeclarationName")

package com.insaner.fonecheck.ui.screens.runall

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.CategoryTestResult
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
import com.insaner.fonecheck.domain.model.TestResult
import com.insaner.fonecheck.domain.model.TestStatus
import com.insaner.fonecheck.localization.evidenceLabelStringRes
import com.insaner.fonecheck.localization.evidenceReasonStringRes
import com.insaner.fonecheck.localization.stableTextStringRes
import com.insaner.fonecheck.navigation.CategoryRetest
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.ConfidenceBadge
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.StandardCard
import com.insaner.fonecheck.ui.components.StatusBadge
import com.insaner.fonecheck.ui.format.uiFileSize
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.screens.report.ReportDetailPresentation
import com.insaner.fonecheck.ui.screens.report.ReportDetailPresenter
import com.insaner.fonecheck.ui.screens.report.stableCodeFallback
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral400
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400
import com.insaner.fonecheck.ui.theme.readableStatusColor
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (mode == ReportResultMode.SAVED_REPORT) {
            item {
                ReportMetadataCard(report, presentation.durationMillis)
            }
        }

        item {
            ResultsSummaryCard(report, presentation)
        }

        if (saveStatus != ReportSaveStatus.SAVED) {
            item {
                ReportSaveCard(saveStatus, onRetrySave)
            }
        }

        if (attentionResults.isNotEmpty()) {
            item {
                ResultSectionTitle(
                    title = stringResource(R.string.run_all_needs_attention),
                    count = attentionResults.size,
                )
            }
            categoryResultItems(
                results = attentionResults,
                expandedCategoryName = expandedCategoryName,
                onExpandedChange = { expandedCategoryName = it },
                onOpenCategory = onOpenCategory,
                mode = mode,
            )
        }

        if (completedResults.isNotEmpty()) {
            item {
                ResultSectionTitle(
                    title = stringResource(R.string.run_all_completed),
                    count = completedResults.size,
                )
            }
            item {
                CompletedResultsCard(
                    results = completedResults,
                    expandedCategoryName = expandedCategoryName,
                    onToggle = { result ->
                        expandedCategoryName = toggleExpanded(expandedCategoryName, result)
                    },
                    onOpenCategory = onOpenCategory,
                    mode = mode,
                )
            }
        }

        if (incompleteResults.isNotEmpty()) {
            item {
                ResultSectionTitle(
                    title = stringResource(R.string.run_all_not_completed),
                    count = incompleteResults.size,
                )
            }
            categoryResultItems(
                results = incompleteResults,
                expandedCategoryName = expandedCategoryName,
                onExpandedChange = { expandedCategoryName = it },
                onOpenCategory = onOpenCategory,
                mode = mode,
            )
        }

        item {
            Button(
                onClick = onDone,
                enabled = saveStatus == ReportSaveStatus.SAVED,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 20.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    stringResource(
                        if (mode == ReportResultMode.SAVED_REPORT) {
                            R.string.report_back
                        } else {
                            R.string.run_all_done
                        },
                    ),
                )
            }
        }
    }
}

private fun LazyListScope.categoryResultItems(
    results: List<CategoryTestResult>,
    expandedCategoryName: String?,
    onExpandedChange: (String?) -> Unit,
    onOpenCategory: (Any) -> Unit,
    mode: ReportResultMode,
) {
    items(
        items = results,
        key = { it.category.name },
    ) { result ->
        ExpandedCategoryResult(
            result = result,
            isExpanded = expandedCategoryName == result.category.name,
            onToggle = {
                onExpandedChange(toggleExpanded(expandedCategoryName, result))
            },
            onOpenCategory = onOpenCategory,
            mode = mode,
        )
    }
}

private fun toggleExpanded(
    currentCategoryName: String?,
    result: CategoryTestResult,
): String? = if (currentCategoryName == result.category.name) null else result.category.name

@Composable
private fun ResultsSummaryCard(
    report: DiagnosticReport,
    presentation: ReportDetailPresentation,
) {
    val score = report.score.value
    val counts = presentation.counts
    val scoreColor =
        when {
            score == null -> Neutral400
            score >= 85 -> Green400
            score >= 65 -> Yellow400
            else -> Red400
        }

    StandardCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.run_all_results_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.run_all_results_description),
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = score?.toString() ?: "—",
                        style = MaterialTheme.typography.displayMedium,
                        color = readableStatusColor(scoreColor),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text =
                            stringResource(R.string.run_all_overall_score) +
                                " · " +
                                scoreStateLabel(report.score.state),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SummaryCount(
                        stringResource(R.string.report_pass_count, counts.pass),
                        Green400,
                    )
                    SummaryCount(
                        stringResource(R.string.report_info_count, counts.info),
                        MaterialTheme.colorScheme.primary,
                    )
                    SummaryCount(
                        pluralStringResource(R.plurals.run_all_warning_count, counts.warning, counts.warning),
                        Yellow400,
                    )
                    SummaryCount(
                        pluralStringResource(R.plurals.run_all_failed_count, counts.fail, counts.fail),
                        Red400,
                    )
                    SummaryCount(
                        pluralStringResource(
                            R.plurals.run_all_unavailable_count,
                            counts.notAvailable,
                            counts.notAvailable,
                        ),
                        Neutral400,
                    )
                    SummaryCount(
                        stringResource(R.string.report_not_tested_count, counts.notTested),
                        Neutral400,
                    )
                }
            }
            Text(
                text =
                    stringResource(R.string.report_coverage) +
                        ": " +
                        stringResource(
                            R.string.report_coverage_value,
                            report.coverage.percentage,
                            report.coverage.completedCount,
                            report.coverage.applicableCount,
                        ),
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReportMetadataCard(
    report: DiagnosticReport,
    durationMillis: Long,
) {
    val dateFormatter =
        remember {
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(uiLanguageLocale(LocalLocale.current.platformLocale))
                .withZone(ZoneId.systemDefault())
        }
    val durationSeconds = durationMillis / 1_000L
    val deviceName =
        listOf(report.device.manufacturer, report.device.model)
            .filter(String::isNotBlank)
            .distinct()
            .joinToString(" ")

    StandardCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.report_saved_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.report_saved_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            InfoRow(
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
            InfoRow(
                label = stringResource(R.string.report_device),
                value = deviceName,
            )
            InfoRow(
                label = stringResource(R.string.report_android),
                value =
                    stringResource(
                        R.string.report_android_value,
                        report.device.androidRelease,
                        report.device.apiLevel,
                    ),
            )
            InfoRow(
                label = stringResource(R.string.report_app_version),
                value =
                    stringResource(
                        R.string.report_app_version_value,
                        report.app.versionName,
                        report.app.versionCode,
                    ),
            )
            InfoRow(
                label = stringResource(R.string.report_completed_at),
                value = dateFormatter.format(report.completedAt),
            )
            InfoRow(
                label = stringResource(R.string.report_duration),
                value =
                    stringResource(
                        R.string.report_duration_value,
                        durationSeconds / 60L,
                        durationSeconds % 60L,
                    ),
            )
            InfoRow(
                label = stringResource(R.string.report_identifier),
                value = report.stableId,
            )
        }
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
private fun ReportSaveCard(
    status: ReportSaveStatus,
    onRetry: () -> Unit,
) {
    StandardCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text =
                    stringResource(
                        if (status == ReportSaveStatus.FAILED) {
                            R.string.run_all_save_failed
                        } else {
                            R.string.run_all_saving_report
                        },
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (status == ReportSaveStatus.FAILED) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.run_all_retry_save))
                }
            }
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
        reason = reason?.let { reasonLabel(it) },
    )

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
    evidenceLabelStringRes(checkId)?.let { stringResource(it) } ?: checkId

@Composable
private fun evidenceDetail(evidence: DiagnosticEvidence): String? =
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
                "samples" -> pluralStringResource(R.plurals.sensor_samples, value.value, value.value)
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

@Composable
private fun SummaryCount(
    text: String,
    color: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = readableStatusColor(color),
    )
}

@Composable
private fun ResultSectionTitle(
    title: String,
    count: Int,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExpandedCategoryResult(
    result: CategoryTestResult,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpenCategory: (Any) -> Unit,
    mode: ReportResultMode,
) {
    val destination = diagnosticDestinations.first { it.category == result.category }
    CategoryResultCard(
        result = result,
        title = stringResource(destination.labelResId),
        iconResId = destination.iconResId,
        isExpanded = isExpanded,
        onToggle = onToggle,
        onOpen = {
            onOpenCategory(
                if (mode == ReportResultMode.SAVED_REPORT) {
                    CategoryRetest(result.category.stableId)
                } else {
                    destination.route
                },
            )
        },
        mode = mode,
    )
}

@Composable
private fun CompletedResultsCard(
    results: List<CategoryTestResult>,
    expandedCategoryName: String?,
    onToggle: (CategoryTestResult) -> Unit,
    onOpenCategory: (Any) -> Unit,
    mode: ReportResultMode,
) {
    StandardCard {
        results.forEachIndexed { index, result ->
            val destination = diagnosticDestinations.first { it.category == result.category }
            CompactResultRow(
                result = result,
                title = stringResource(destination.labelResId),
                iconResId = destination.iconResId,
                isExpanded = expandedCategoryName == result.category.name,
                onToggle = { onToggle(result) },
                onOpen = {
                    onOpenCategory(
                        if (mode == ReportResultMode.SAVED_REPORT) {
                            CategoryRetest(result.category.stableId)
                        } else {
                            destination.route
                        },
                    )
                },
                mode = mode,
            )
            if (index < results.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun CompactResultRow(
    result: CategoryTestResult,
    title: String,
    iconResId: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    mode: ReportResultMode,
) {
    val statusText = statusLabel(result.status)
    val expansionState =
        stringResource(
            if (isExpanded) R.string.accessibility_expanded else R.string.accessibility_collapsed,
        )
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("report_category_${result.category.stableId}")
                .semantics {
                    role = Role.Button
                    stateDescription = "$statusText, $expansionState"
                }.clickable(onClick = onToggle)
                .padding(14.dp),
    ) {
        ResultHeader(
            result = result,
            title = title,
            iconResId = iconResId,
            showSummary = false,
        )
        if (isExpanded) {
            ResultDetails(result.results, onOpen, mode)
        }
    }
}

@Composable
private fun CategoryResultCard(
    result: CategoryTestResult,
    title: String,
    iconResId: Int,
    isExpanded: Boolean,
    mode: ReportResultMode,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
) {
    val statusText = statusLabel(result.status)
    val expansionState =
        stringResource(
            if (isExpanded) R.string.accessibility_expanded else R.string.accessibility_collapsed,
        )
    StandardCard(
        modifier =
            Modifier
                .testTag("report_category_${result.category.stableId}")
                .semantics { stateDescription = "$statusText, $expansionState" },
        onClick = onToggle,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ResultHeader(
                result = result,
                title = title,
                iconResId = iconResId,
                showSummary = true,
            )
            Text(
                text =
                    stringResource(
                        when {
                            mode == ReportResultMode.SAVED_REPORT && isExpanded ->
                                R.string.report_hide_saved_evidence
                            mode == ReportResultMode.SAVED_REPORT -> R.string.report_view_saved_evidence
                            isExpanded -> R.string.run_all_hide_details
                            else -> R.string.run_all_view_details
                        },
                    ),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (isExpanded) {
                ResultDetails(result.results, onOpen, mode)
            }
        }
    }
}

@Composable
private fun ResultHeader(
    result: CategoryTestResult,
    title: String,
    iconResId: Int,
    showSummary: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = FonecheckTheme.colors.textSecondary,
            modifier = Modifier.size(FonecheckTheme.spacing.lg),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            if (showSummary) {
                Text(
                    text = result.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        StatusBadge(
            text = statusLabel(result.status),
            color = statusColor(result.status),
        )
    }
}

@Composable
private fun ResultDetails(
    results: List<TestResult>,
    onOpen: () -> Unit,
    mode: ReportResultMode,
) {
    Column {
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (results.isEmpty()) {
            Text(
                text = stringResource(R.string.report_no_saved_evidence),
                modifier = Modifier.padding(vertical = 14.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            results.forEach { testResult ->
                ResultDetailRow(testResult)
            }
        }
        OutlinedButton(
            onClick = onOpen,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                stringResource(
                    if (mode == ReportResultMode.SAVED_REPORT) {
                        R.string.report_retest
                    } else {
                        R.string.run_all_open_test
                    },
                ),
            )
        }
    }
}

@Composable
private fun ResultDetailRow(result: TestResult) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            result.detail?.let { detail ->
                Text(
                    text = detail,
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            result.reason?.let { reason ->
                Text(
                    text = stringResource(R.string.report_evidence_reason, reason),
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                result.source?.let { source ->
                    Text(
                        text = sourceLabel(source),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ConfidenceBadge(confidence = result.confidence)
            }
        }
        StatusBadge(
            text = statusLabel(result.status),
            color = statusColor(result.status),
        )
    }
}

@Composable
private fun statusLabel(status: TestStatus): String =
    stringResource(
        when (status) {
            TestStatus.Pass -> R.string.run_all_status_pass
            is TestStatus.Warning -> R.string.run_all_status_warning
            is TestStatus.Fail -> R.string.run_all_status_fail
            is TestStatus.Info -> R.string.run_all_status_info
            TestStatus.NotAvailable -> R.string.run_all_status_unavailable
            TestStatus.NotTested -> R.string.run_all_status_not_tested
        },
    )

@Composable
private fun statusColor(status: TestStatus): Color =
    when (status) {
        TestStatus.Pass -> Green400
        is TestStatus.Warning -> Yellow400
        is TestStatus.Fail -> Red400
        is TestStatus.Info -> MaterialTheme.colorScheme.primary
        TestStatus.NotAvailable, TestStatus.NotTested -> Neutral400
    }
