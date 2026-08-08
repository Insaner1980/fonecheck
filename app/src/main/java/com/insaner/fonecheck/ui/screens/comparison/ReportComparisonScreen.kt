package com.insaner.fonecheck.ui.screens.comparison

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.comparison.AttentionChange
import com.insaner.fonecheck.domain.comparison.CategoryComparison
import com.insaner.fonecheck.domain.comparison.EvidenceChange
import com.insaner.fonecheck.domain.comparison.EvidenceComparison
import com.insaner.fonecheck.domain.comparison.ReportComparison
import com.insaner.fonecheck.domain.comparison.ScoreComparison
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.SectionBox
import com.insaner.fonecheck.ui.components.StandardCard
import com.insaner.fonecheck.ui.components.TestSectionCard
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral400
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun ReportComparisonRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportComparisonViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReportComparisonScreen(
        state = state,
        onRetry = viewModel::retry,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun ReportComparisonScreen(
    state: ReportComparisonState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        ReportComparisonState.Loading ->
            ComparisonMessage(
                message = stringResource(R.string.comparison_loading),
                showProgress = true,
                onRetry = null,
                onBack = null,
                modifier = modifier,
            )

        is ReportComparisonState.Content ->
            ComparisonContent(state.comparison, onBack, modifier)

        ReportComparisonState.NotFound ->
            ComparisonMessage(
                message = stringResource(R.string.comparison_not_found),
                showProgress = false,
                onRetry = null,
                onBack = onBack,
                modifier = modifier,
            )

        is ReportComparisonState.Unavailable ->
            ComparisonMessage(
                message = stringResource(R.string.comparison_unavailable),
                showProgress = false,
                onRetry = onRetry,
                onBack = onBack,
                modifier = modifier,
            )

        ReportComparisonState.Error ->
            ComparisonMessage(
                message = stringResource(R.string.comparison_error),
                showProgress = false,
                onRetry = onRetry,
                onBack = onBack,
                modifier = modifier,
            )
    }
}

@Composable
private fun ComparisonContent(
    comparison: ReportComparison,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    var expandedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    val dateFormatter =
        remember {
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
        }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.comparison_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.comparison_description),
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { ReportPairCard(comparison, dateFormatter) }
        item { ScoreCard(comparison) }
        item {
            StandardCard {
                Text(
                    text = stringResource(R.string.comparison_disclaimer),
                    modifier = Modifier.padding(18.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                comparison.categories.forEach { category ->
                    Box(modifier = Modifier.testTag("comparison_category")) {
                        ComparisonCategoryCard(
                            category = category,
                            isExpanded = expandedCategory == category.categoryId.stableId,
                            onClick = {
                                expandedCategory =
                                    category.categoryId.stableId.takeUnless { it == expandedCategory }
                            },
                        )
                    }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.report_back))
            }
        }
    }
}

@Composable
private fun ReportPairCard(
    comparison: ReportComparison,
    dateFormatter: DateTimeFormatter,
) {
    StandardCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionBox {
                InfoRow(stringResource(R.string.comparison_first_report), comparison.beforeId)
                Text(dateFormatter.format(comparison.beforeCompletedAt))
                Text(
                    stringResource(
                        R.string.comparison_app_version,
                        comparison.beforeApp.versionName,
                        comparison.beforeApp.versionCode,
                    ),
                )
                Text(
                    stringResource(
                        R.string.comparison_report_version,
                        comparison.beforeSchemaVersion.value,
                    ),
                )
            }
            SectionBox {
                InfoRow(stringResource(R.string.comparison_second_report), comparison.afterId)
                Text(dateFormatter.format(comparison.afterCompletedAt))
                Text(
                    stringResource(
                        R.string.comparison_app_version,
                        comparison.afterApp.versionName,
                        comparison.afterApp.versionCode,
                    ),
                )
                Text(
                    stringResource(
                        R.string.comparison_report_version,
                        comparison.afterSchemaVersion.value,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ScoreCard(comparison: ReportComparison) {
    StandardCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.comparison_score),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            when (val score = comparison.score) {
                is ScoreComparison.Compatible -> {
                    InfoRow(
                        label = stringResource(R.string.comparison_score),
                        value =
                            stringResource(
                                R.string.comparison_score_value,
                                score.before?.toString() ?: "—",
                                score.after?.toString() ?: "—",
                            ),
                    )
                    Text(
                        score.delta?.let { stringResource(R.string.comparison_score_delta, it) }
                            ?: stringResource(R.string.comparison_score_no_delta),
                    )
                }

                is ScoreComparison.Incompatible ->
                    Text(
                        stringResource(
                            R.string.comparison_score_incompatible,
                            score.beforeVersion.value,
                            score.afterVersion.value,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
            }
            InfoRow(
                label = stringResource(R.string.comparison_coverage),
                value =
                    stringResource(
                        R.string.comparison_coverage_value,
                        comparison.coverage.before,
                        comparison.coverage.after,
                    ),
            )
            Text(
                comparison.coverage.delta?.let {
                    stringResource(R.string.comparison_coverage_delta, it)
                } ?: stringResource(R.string.comparison_coverage_incompatible),
            )
        }
    }
}

@Composable
private fun ComparisonCategoryCard(
    category: CategoryComparison,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val label =
        diagnosticDestinations
            .firstOrNull { it.category == category.categoryId }
            ?.let { stringResource(it.labelResId) }
            ?: category.categoryId.stableId
    TestSectionCard(
        icon = category.categoryId.stableId.take(2).uppercase(Locale.ROOT),
        title = label,
        statusText =
            stringResource(
                R.string.comparison_category_status,
                statusLabel(category.beforeStatus),
                statusLabel(category.afterStatus),
            ),
        statusColor = statusColor(category.afterStatus),
        isExpanded = isExpanded,
        onClick = onClick,
        modifier = Modifier.testTag("comparison_category_${category.categoryId.stableId}"),
    ) {
        if (category.evidence.isEmpty()) {
            Text(
                stringResource(R.string.comparison_no_checks),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                category.evidence.forEach { EvidenceRow(it) }
            }
        }
    }
}

@Composable
private fun EvidenceRow(evidence: EvidenceComparison) {
    SectionBox {
        Text(
            text = evidence.checkId,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
        InfoRow(
            label = stringResource(changeLabel(evidence.change)),
            value =
                stringResource(
                    R.string.comparison_category_status,
                    statusLabel(evidence.before?.status),
                    statusLabel(evidence.after?.status),
                ),
            valueColor = statusColor(evidence.after?.status),
        )
        if (evidence.attentionChange != AttentionChange.NONE) {
            Text(
                text = stringResource(attentionLabel(evidence.attentionChange)),
                style = MaterialTheme.typography.bodySmall,
                color = statusColor(evidence.after?.status),
            )
        }
    }
}

@Composable
private fun statusLabel(status: DiagnosticStatus?): String =
    stringResource(
        when (status) {
            DiagnosticStatus.PASS -> R.string.run_all_status_pass
            DiagnosticStatus.WARNING -> R.string.run_all_status_warning
            DiagnosticStatus.FAIL -> R.string.run_all_status_fail
            DiagnosticStatus.INFO -> R.string.run_all_status_info
            DiagnosticStatus.NOT_AVAILABLE -> R.string.run_all_status_unavailable
            DiagnosticStatus.NOT_TESTED -> R.string.run_all_status_not_tested
            null -> R.string.comparison_missing
        },
    )

private fun statusColor(status: DiagnosticStatus?): Color =
    when (status) {
        DiagnosticStatus.PASS -> Green400
        DiagnosticStatus.WARNING -> Yellow400
        DiagnosticStatus.FAIL -> Red400
        else -> Neutral400
    }

private fun changeLabel(change: EvidenceChange): Int =
    when (change) {
        EvidenceChange.UNCHANGED -> R.string.comparison_change_unchanged
        EvidenceChange.ADDED -> R.string.comparison_change_added
        EvidenceChange.REMOVED -> R.string.comparison_change_removed
        EvidenceChange.STATUS_CHANGED -> R.string.comparison_change_status
        EvidenceChange.VALUE_CHANGED -> R.string.comparison_change_value
        EvidenceChange.NEWLY_AVAILABLE -> R.string.comparison_change_newly_available
        EvidenceChange.NEWLY_UNAVAILABLE -> R.string.comparison_change_newly_unavailable
        EvidenceChange.NOT_RUN -> R.string.comparison_change_not_run
    }

private fun attentionLabel(change: AttentionChange): Int =
    when (change) {
        AttentionChange.APPEARED -> R.string.comparison_attention_appeared
        AttentionChange.RESOLVED -> R.string.comparison_attention_resolved
        AttentionChange.CHANGED -> R.string.comparison_attention_changed
        AttentionChange.NONE -> error("No label for unchanged attention state")
    }

@Composable
private fun ComparisonMessage(
    message: String,
    showProgress: Boolean,
    onRetry: (() -> Unit)?,
    onBack: (() -> Unit)?,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showProgress) CircularProgressIndicator(modifier = Modifier.padding(bottom = 20.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        onRetry?.let {
            Button(
                onClick = it,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            ) {
                Text(stringResource(R.string.report_retry))
            }
        }
        onBack?.let {
            OutlinedButton(
                onClick = it,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.report_back))
            }
        }
    }
}
