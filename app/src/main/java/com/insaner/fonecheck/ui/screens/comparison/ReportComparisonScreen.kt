package com.insaner.fonecheck.ui.screens.comparison

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.DisclosureHeader
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ReportStateScreen
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.toSemanticTone
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
            ReportStateScreen(
                type = ScreenStateType.LOADING,
                message = stringResource(R.string.comparison_loading),
                onRetry = null,
                onBack = null,
                modifier = modifier,
            )

        is ReportComparisonState.Content ->
            ComparisonContent(state.comparison, modifier)

        ReportComparisonState.NotFound ->
            ReportStateScreen(
                type = ScreenStateType.EMPTY,
                message = stringResource(R.string.comparison_not_found),
                onRetry = null,
                onBack = onBack,
                modifier = modifier,
            )

        is ReportComparisonState.Unavailable ->
            ReportStateScreen(
                type = ScreenStateType.UNAVAILABLE,
                message = stringResource(R.string.comparison_unavailable),
                onRetry = onRetry,
                onBack = onBack,
                modifier = modifier,
            )

        ReportComparisonState.Error ->
            ReportStateScreen(
                type = ScreenStateType.ERROR,
                message = stringResource(R.string.comparison_error),
                onRetry = onRetry,
                onBack = onBack,
                modifier = modifier,
            )
    }
}

@Composable
private fun ComparisonContent(
    comparison: ReportComparison,
    modifier: Modifier = Modifier,
) {
    var expandedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    val locale = uiLanguageLocale(LocalLocale.current.platformLocale)
    val dateFormatter =
        remember(locale) {
            DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault())
        }

    TestScreenContent(modifier = modifier) {
        item { Note(stringResource(R.string.comparison_description)) }
        item { ReportPairSections(comparison, dateFormatter) }
        item { ScoreSection(comparison) }
        item {
            Column {
                SectionHeader(stringResource(R.string.comparison_limitations))
                Note(stringResource(R.string.comparison_disclaimer))
            }
        }
        item { SectionHeader(stringResource(R.string.comparison_categories)) }
        items(
            items = comparison.categories,
            key = { it.categoryId.stableId },
        ) { category ->
            Box(modifier = Modifier.testTag("comparison_category")) {
                ComparisonCategorySection(
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

@Composable
private fun ReportPairSections(
    comparison: ReportComparison,
    dateFormatter: DateTimeFormatter,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.lg)) {
        ReportMetadataSection(
            label = stringResource(R.string.comparison_first_report),
            reportId = comparison.beforeId,
            completedAt = dateFormatter.format(comparison.beforeCompletedAt),
            appVersion =
                stringResource(
                    R.string.report_app_version_value,
                    comparison.beforeApp.versionName,
                    uiNumber(comparison.beforeApp.versionCode),
                ),
            reportVersion = uiNumber(comparison.beforeSchemaVersion.value),
        )
        ReportMetadataSection(
            label = stringResource(R.string.comparison_second_report),
            reportId = comparison.afterId,
            completedAt = dateFormatter.format(comparison.afterCompletedAt),
            appVersion =
                stringResource(
                    R.string.report_app_version_value,
                    comparison.afterApp.versionName,
                    uiNumber(comparison.afterApp.versionCode),
                ),
            reportVersion = uiNumber(comparison.afterSchemaVersion.value),
        )
    }
}

@Composable
private fun ReportMetadataSection(
    label: String,
    reportId: String,
    completedAt: String,
    appVersion: String,
    reportVersion: String,
) {
    Column {
        SectionHeader(label)
        LongValueRow(
            label = stringResource(R.string.report_identifier),
            value = reportId,
        )
        LongValueRow(
            label = stringResource(R.string.report_completed_at),
            value = completedAt,
        )
        LongValueRow(
            label = stringResource(R.string.report_app_version),
            value = appVersion,
        )
        DataRow(
            label = stringResource(R.string.pdf_report_format),
            value = reportVersion,
        )
    }
}

@Composable
private fun ScoreSection(comparison: ReportComparison) {
    val unavailable = stringResource(R.string.value_unavailable_short)
    Column {
        SectionHeader(stringResource(R.string.comparison_score))
        when (val score = comparison.score) {
            is ScoreComparison.Compatible -> {
                DataRow(
                    label = stringResource(R.string.comparison_score),
                    value =
                        stringResource(
                            R.string.comparison_score_value,
                            score.before?.let { uiNumber(it) } ?: unavailable,
                            score.after?.let { uiNumber(it) } ?: unavailable,
                        ),
                )
                Note(
                    score.delta?.let {
                        stringResource(R.string.comparison_score_delta, signedUiNumber(it))
                    } ?: stringResource(R.string.comparison_score_no_delta),
                )
            }

            is ScoreComparison.Incompatible ->
                Note(
                    stringResource(
                        R.string.comparison_score_incompatible,
                        uiNumber(score.beforeVersion.value),
                        uiNumber(score.afterVersion.value),
                    ),
                )
        }
        DataRow(
            label = stringResource(R.string.comparison_coverage),
            value =
                stringResource(
                    R.string.comparison_coverage_value,
                    uiNumber(comparison.coverage.before),
                    uiNumber(comparison.coverage.after),
                ),
        )
        Note(
            comparison.coverage.delta?.let {
                stringResource(R.string.comparison_coverage_delta, signedUiNumber(it))
            } ?: stringResource(R.string.comparison_coverage_incompatible),
        )
    }
}

@Composable
private fun signedUiNumber(value: Int): String =
    if (value > 0) "+${uiNumber(value)}" else uiNumber(value)

@Composable
private fun ComparisonCategorySection(
    category: CategoryComparison,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val label =
        diagnosticDestinations
            .firstOrNull { it.category == category.categoryId }
            ?.let { stringResource(it.labelResId) }
            ?: category.categoryId.stableId
    Column {
        DisclosureHeader(
            label = label,
            summary =
                stringResource(
                    R.string.comparison_category_status,
                    statusLabel(category.beforeStatus),
                    statusLabel(category.afterStatus),
                ),
            expanded = isExpanded,
            onClick = onClick,
            tone = category.afterStatus?.toSemanticTone() ?: SemanticTone.NEUTRAL,
            modifier = Modifier.testTag("comparison_category_${category.categoryId.stableId}"),
        )
        if (isExpanded) {
            Column(modifier = Modifier.padding(top = FonecheckTheme.spacing.sm)) {
                if (category.evidence.isEmpty()) {
                    Note(stringResource(R.string.comparison_no_checks))
                } else {
                    category.evidence.forEach { EvidenceRows(it) }
                }
            }
        }
    }
}

@Composable
private fun EvidenceRows(evidence: EvidenceComparison) {
    LongValueRow(
        label = stringResource(R.string.comparison_check),
        value = evidence.checkId,
    )
    DataRow(
        label = stringResource(changeLabel(evidence.change)),
        value =
            stringResource(
                R.string.comparison_category_status,
                statusLabel(evidence.before?.status),
                statusLabel(evidence.after?.status),
            ),
        tone = evidence.after?.status?.toSemanticTone() ?: SemanticTone.NEUTRAL,
    )
    if (evidence.attentionChange != AttentionChange.NONE) {
        StatusText(
            text = stringResource(attentionLabel(evidence.attentionChange)),
            tone = evidence.after?.status?.toSemanticTone() ?: SemanticTone.NEUTRAL,
            modifier = Modifier.padding(vertical = FonecheckTheme.spacing.sm),
        )
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
