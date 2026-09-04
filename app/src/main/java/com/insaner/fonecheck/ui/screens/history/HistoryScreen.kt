package com.insaner.fonecheck.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.repository.SavedReportSummary
import com.insaner.fonecheck.domain.comparison.reportScopesAreComparable
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateScreen
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.format.formatUiDateTime
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme

@Composable
fun HistoryRoute(
    onOpen: (String) -> Unit,
    onCompare: (String, String) -> Unit,
    onExport: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HistoryScreen(
        state = state,
        onRetry = viewModel::retry,
        onOpen = onOpen,
        onCompare = onCompare,
        onExport = onExport,
        onDelete = viewModel::delete,
        modifier = modifier,
    )
}

@Composable
@Suppress("kotlin:S3776") // The list owns its loading, empty, selection, and dialog states.
fun HistoryScreen(
    state: HistoryState,
    onRetry: () -> Unit,
    onOpen: (String) -> Unit,
    onCompare: (String, String) -> Unit,
    onExport: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var compareBaseId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val locale = uiLanguageLocale(LocalLocale.current.platformLocale)
    val comparableReports =
        state.reports
            .filter { it.unavailableReason == null && it.hasValidComparisonScope() }
    val comparableReportIds = comparableReports.map(SavedReportSummary::stableId).toSet()
    val compareBase = comparableReports.firstOrNull { it.stableId == compareBaseId }
    LaunchedEffect(state.reports) {
        val reportIds = state.reports.map(SavedReportSummary::stableId).toSet()
        if (compareBaseId !in comparableReportIds) compareBaseId = null
        if (pendingDeleteId !in reportIds) pendingDeleteId = null
    }

    if (state.isLoading && state.reports.isEmpty()) {
        HistoryLoading(modifier)
    } else {
        TestScreenContent(modifier = modifier) {
            item { Note(stringResource(R.string.history_description)) }
            state.error?.let {
                item { HistoryErrorCard(error = it, onRetry = onRetry) }
            }
            compareBaseId?.let {
                item { Note(stringResource(R.string.history_compare_prompt)) }
            }
            if (state.reports.isEmpty() && state.error == null) {
                item { HistoryEmpty() }
            } else {
                items(
                    items = state.reports,
                    key = SavedReportSummary::stableId,
                ) { report ->
                    val completedAt =
                        remember(report.completedAt, locale) {
                            formatUiDateTime(report.completedAt, locale)
                        }
                    HistoryReportSection(
                        report = report,
                        completedAt = completedAt,
                        isCompareBase = compareBaseId == report.stableId,
                        isCompareEnabled =
                            report.unavailableReason == null &&
                                report.hasValidComparisonScope() &&
                                (
                                    compareBase == null ||
                                        compareBase.stableId == report.stableId ||
                                        compareBase.hasComparableScopeWith(report)
                                ),
                        isDeleting = report.stableId in state.deletingReportIds,
                        onOpen = { onOpen(report.stableId) },
                        onCompare = {
                            val base = comparableReports.firstOrNull { it.stableId == compareBaseId }
                            when {
                                base == null -> compareBaseId = report.stableId
                                base.stableId == report.stableId -> compareBaseId = null
                                base.hasComparableScopeWith(report) -> {
                                    compareBaseId = null
                                    onCompare(base.stableId, report.stableId)
                                }
                            }
                        },
                        onExport = { onExport(report.stableId) },
                        onDelete = { pendingDeleteId = report.stableId },
                    )
                }
            }
        }
    }

    pendingDeleteId?.let { reportId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            shape = RectangleShape,
            containerColor = FonecheckTheme.colors.panel,
            tonalElevation = 0.dp,
            title = {
                Text(
                    text = stringResource(R.string.history_delete_title),
                    style = FonecheckTheme.type.screenTitle,
                    color = FonecheckTheme.colors.textPrimary,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.history_delete_message, reportId),
                    style = FonecheckTheme.type.note,
                    color = FonecheckTheme.colors.textMuted,
                )
            },
            confirmButton = {
                PrimaryButton(
                    label = stringResource(R.string.history_delete_confirm),
                    onClick = {
                        pendingDeleteId = null
                        onDelete(reportId)
                    },
                    modifier = Modifier.testTag("history_confirm_delete"),
                )
            },
            dismissButton = {
                SecondaryButton(
                    label = stringResource(R.string.history_delete_cancel),
                    onClick = { pendingDeleteId = null },
                )
            },
        )
    }
}

@Composable
private fun HistoryLoading(modifier: Modifier = Modifier) {
    ScreenStateScreen(
        type = ScreenStateType.LOADING,
        message = stringResource(R.string.history_loading),
        modifier = modifier,
    )
}

@Composable
private fun HistoryEmpty() {
    ScreenStateCard(
        type = ScreenStateType.EMPTY,
        message = stringResource(R.string.history_empty),
    )
}

@Composable
private fun HistoryErrorCard(
    error: String,
    onRetry: () -> Unit,
) {
    val isDeleteError = error == "history_delete_failed"
    ScreenStateCard(
        type = ScreenStateType.ERROR,
        message =
            stringResource(
                if (isDeleteError) {
                    R.string.history_delete_error
                } else {
                    R.string.history_error
                },
            ),
        actionLabel = stringResource(R.string.history_retry).takeUnless { isDeleteError },
        onAction = onRetry.takeUnless { isDeleteError },
    )
}

@Composable
@Suppress("kotlin:S107") // Report state and its four explicit row actions form one cohesive section API.
private fun HistoryReportSection(
    report: SavedReportSummary,
    completedAt: String,
    isCompareBase: Boolean,
    isCompareEnabled: Boolean,
    isDeleting: Boolean,
    onOpen: () -> Unit,
    onCompare: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
) {
    val isAvailable = report.unavailableReason == null
    val validScore =
        isAvailable &&
            report.scoreValue in 0..100 &&
            (report.scoreState == ScoreState.COMPLETE || report.scoreState == ScoreState.PARTIAL)

    Column {
        SectionHeader(
            label = historyKindLabel(report),
            trailing = historyCategoryLabel(report),
        )
        DataRow(
            label = stringResource(R.string.history_status),
            value = historyStatusLabel(report),
        )
        DataRow(
            label = stringResource(R.string.history_completed),
            value = completedAt,
        )
        LongValueRow(
            label = stringResource(R.string.report_identifier),
            value = report.stableId,
        )
        DataRow(
            label = stringResource(R.string.history_score),
            value = report.scoreValue?.let { uiNumber(it) }.takeIf { validScore },
        )
        DataRow(
            label = stringResource(R.string.history_coverage),
            value =
                stringResource(
                    R.string.history_coverage_value,
                    uiNumber(report.coveragePercentage),
                ).takeIf { isAvailable },
        )
        DataRow(
            label = stringResource(R.string.history_warnings),
            value = uiNumber(report.warningCount).takeIf { isAvailable },
        )
        DataRow(
            label = stringResource(R.string.history_issues),
            value = uiNumber(report.failureCount).takeIf { isAvailable },
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
        ) {
            PrimaryButton(
                label = stringResource(R.string.history_open),
                onClick = onOpen,
                modifier = Modifier.testTag("history_open_${report.stableId}"),
            )
            SecondaryButton(
                label =
                    stringResource(
                        if (isCompareBase) {
                            R.string.history_compare_selected
                        } else {
                            R.string.history_compare
                        },
                    ),
                onClick = onCompare,
                enabled = isCompareEnabled,
                modifier = Modifier.testTag("history_compare_${report.stableId}"),
            )
            SecondaryButton(
                label = stringResource(R.string.history_export),
                onClick = onExport,
                enabled = isAvailable,
                modifier = Modifier.testTag("history_export_${report.stableId}"),
            )
            SecondaryButton(
                label = stringResource(R.string.history_delete),
                onClick = onDelete,
                enabled = !isDeleting,
                modifier = Modifier.testTag("history_delete_${report.stableId}"),
            )
        }
    }
}

private fun SavedReportSummary.hasValidComparisonScope(): Boolean = hasComparableScopeWith(this)

private fun SavedReportSummary.hasComparableScopeWith(other: SavedReportSummary): Boolean =
    reportScopesAreComparable(
        firstKind = kind,
        firstCategoryId = categoryId,
        secondKind = other.kind,
        secondCategoryId = other.categoryId,
    )

@Composable
private fun historyKindLabel(report: SavedReportSummary): String {
    if (report.unavailableReason != null || report.kind == null) {
        return stringResource(R.string.history_unavailable_report)
    }
    if (report.kind == ReportKind.CATEGORY_ONLY) {
        return stringResource(R.string.history_category_retest)
    }
    return stringResource(R.string.history_full_check)
}

@Composable
private fun historyCategoryLabel(report: SavedReportSummary): String? {
    if (report.unavailableReason != null || report.kind != ReportKind.CATEGORY_ONLY) return null
    return report.categoryId
        ?.let { categoryId -> diagnosticDestinations.firstOrNull { it.category == categoryId } }
        ?.let { destination -> stringResource(destination.labelResId) }
        ?: stringResource(R.string.history_unavailable_report)
}

@Composable
private fun historyStatusLabel(report: SavedReportSummary): String {
    if (report.unavailableReason != null) {
        return stringResource(R.string.history_status_unavailable)
    }
    return stringResource(
        when (report.scoreState) {
            ScoreState.COMPLETE -> R.string.history_status_complete
            ScoreState.PARTIAL -> R.string.history_status_partial
            ScoreState.INCOMPLETE -> R.string.history_status_incomplete
            null -> R.string.history_status_unavailable
        },
    )
}
