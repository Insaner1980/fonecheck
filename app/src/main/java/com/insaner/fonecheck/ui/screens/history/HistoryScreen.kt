package com.insaner.fonecheck.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.repository.SavedReportSummary
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.StandardCard
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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
    LaunchedEffect(state.reports) {
        val reportIds = state.reports.mapTo(mutableSetOf(), SavedReportSummary::stableId)
        if (compareBaseId !in reportIds) compareBaseId = null
        if (pendingDeleteId !in reportIds) pendingDeleteId = null
    }

    if (state.isLoading && state.reports.isEmpty()) {
        HistoryLoading(modifier)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.history_description),
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            state.error?.let {
                item { HistoryErrorCard(error = it, onRetry = onRetry) }
            }
            compareBaseId?.let {
                item {
                    Text(
                        text = stringResource(R.string.history_compare_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (state.reports.isEmpty() && state.error == null) {
                item { HistoryEmpty() }
            } else {
                items(
                    items = state.reports,
                    key = SavedReportSummary::stableId,
                ) { report ->
                    HistoryReportCard(
                        report = report,
                        isCompareBase = compareBaseId == report.stableId,
                        isDeleting = report.stableId in state.deletingReportIds,
                        onOpen = { onOpen(report.stableId) },
                        onCompare = {
                            val baseId = compareBaseId
                            when {
                                baseId == null -> compareBaseId = report.stableId
                                baseId == report.stableId -> compareBaseId = null
                                else -> {
                                    compareBaseId = null
                                    onCompare(baseId, report.stableId)
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
            title = { Text(stringResource(R.string.history_delete_title)) },
            text = { Text(stringResource(R.string.history_delete_message, reportId)) },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeleteId = null
                        onDelete(reportId)
                    },
                    modifier = Modifier.testTag("history_confirm_delete"),
                ) {
                    Text(stringResource(R.string.history_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(stringResource(R.string.history_delete_cancel))
                }
            },
        )
    }
}

@Composable
private fun HistoryLoading(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.history_loading),
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun HistoryEmpty() {
    StandardCard {
        Text(
            text = stringResource(R.string.history_empty),
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HistoryErrorCard(
    error: String,
    onRetry: () -> Unit,
) {
    StandardCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text =
                    stringResource(
                        if (error == "history_delete_failed") {
                            R.string.history_delete_error
                        } else {
                            R.string.history_error
                        },
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text(stringResource(R.string.history_retry))
            }
        }
    }
}

@Composable
private fun HistoryReportCard(
    report: SavedReportSummary,
    isCompareBase: Boolean,
    isDeleting: Boolean,
    onOpen: () -> Unit,
    onCompare: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateFormatter =
        remember {
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
        }
    val isAvailable = report.unavailableReason == null
    val validScore =
        isAvailable &&
            report.scoreValue in 0..100 &&
            (report.scoreState == ScoreState.COMPLETE || report.scoreState == ScoreState.PARTIAL)
    StandardCard {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = historyKindLabel(report),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = dateFormatter.format(report.completedAt),
                modifier = Modifier.padding(top = 3.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = report.stableId,
                modifier = Modifier.padding(top = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InfoRow(
                    label = stringResource(R.string.history_score),
                    value = report.scoreValue?.toString().takeIf { validScore } ?: "—",
                    modifier = Modifier.weight(1f),
                )
                InfoRow(
                    label = stringResource(R.string.history_coverage),
                    value = if (isAvailable) "${report.coveragePercentage}%" else "—",
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text =
                    stringResource(
                        R.string.history_issue_summary,
                        report.warningCount,
                        report.failureCount,
                    ),
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextButton(
                    onClick = onOpen,
                    modifier = Modifier.testTag("history_open_${report.stableId}"),
                ) {
                    Text(stringResource(R.string.history_open))
                }
                TextButton(
                    onClick = onCompare,
                    enabled = isAvailable,
                    modifier = Modifier.testTag("history_compare_${report.stableId}"),
                ) {
                    Text(
                        stringResource(
                            if (isCompareBase) {
                                R.string.history_compare_selected
                            } else {
                                R.string.history_compare
                            },
                        ),
                    )
                }
                TextButton(
                    onClick = onExport,
                    enabled = isAvailable,
                    modifier = Modifier.testTag("history_export_${report.stableId}"),
                ) {
                    Text(stringResource(R.string.history_export))
                }
                TextButton(
                    onClick = onDelete,
                    enabled = !isDeleting,
                    modifier = Modifier.testTag("history_delete_${report.stableId}"),
                ) {
                    Text(stringResource(R.string.history_delete))
                }
            }
        }
    }
}

@Composable
private fun historyKindLabel(report: SavedReportSummary): String {
    if (report.unavailableReason != null || report.kind == null) {
        return stringResource(R.string.history_unavailable_report)
    }
    if (report.kind == ReportKind.CATEGORY_ONLY) {
        val categoryLabel =
            report.categoryId
                ?.let { categoryId -> diagnosticDestinations.firstOrNull { it.category == categoryId } }
                ?.let { destination -> stringResource(destination.labelResId) }
                ?: stringResource(R.string.history_unavailable_report)
        return stringResource(R.string.history_category_retest, categoryLabel)
    }
    return stringResource(
        when (report.scoreState) {
            ScoreState.COMPLETE -> R.string.history_full_check
            ScoreState.PARTIAL -> R.string.history_partial_check
            ScoreState.INCOMPLETE -> R.string.history_incomplete_check
            null -> R.string.history_unavailable_report
        },
    )
}
