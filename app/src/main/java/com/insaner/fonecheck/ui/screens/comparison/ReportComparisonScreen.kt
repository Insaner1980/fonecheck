package com.insaner.fonecheck.ui.screens.comparison

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import com.insaner.fonecheck.localization.evidenceLabelResource
import com.insaner.fonecheck.localization.stableCodeDisplayText
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.DisclosureHeader
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.ReportStateScreen
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusLamp
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.WindowLabel
import com.insaner.fonecheck.ui.components.WindowReading
import com.insaner.fonecheck.ui.components.WindowRow
import com.insaner.fonecheck.ui.components.statusLabel
import com.insaner.fonecheck.ui.format.formatUiDateTime
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.screens.runall.evidenceDetail
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import com.insaner.fonecheck.ui.theme.toSemanticTone

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

        is ReportComparisonState.Issues -> {
            val hasUnreadableReport =
                state.first?.let { it != ComparisonReportIssue.NOT_FOUND } == true ||
                    state.second?.let { it != ComparisonReportIssue.NOT_FOUND } == true
            ReportStateScreen(
                type = if (hasUnreadableReport) ScreenStateType.UNAVAILABLE else ScreenStateType.EMPTY,
                message = comparisonIssueMessage(state),
                onRetry = onRetry.takeIf { hasUnreadableReport },
                onBack = onBack,
                modifier = modifier,
            )
        }

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
    val beforeCompletedAt =
        remember(comparison.beforeCompletedAt, locale) {
            formatUiDateTime(comparison.beforeCompletedAt, locale)
        }
    val afterCompletedAt =
        remember(comparison.afterCompletedAt, locale) {
            formatUiDateTime(comparison.afterCompletedAt, locale)
        }

    TestScreenContent(modifier = modifier) {
        item { Note(stringResource(R.string.comparison_description)) }
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

        // Which two reports these numbers came from. Provenance, so it follows the reading.
        item { ReportPairSections(comparison, beforeCompletedAt, afterCompletedAt) }
    }
}

@Composable
private fun ReportPairSections(
    comparison: ReportComparison,
    beforeCompletedAt: String,
    afterCompletedAt: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.lg)) {
        ReportMetadataSection(
            label = stringResource(R.string.comparison_first_report),
            reportId = comparison.beforeId,
            completedAt = beforeCompletedAt,
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
            completedAt = afterCompletedAt,
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

/**
 * What the comparison exists to answer: did it get better or worse, and by how much.
 *
 * The change is the figure, and the two readings behind it stay in the window beneath it. Two
 * reports scored under different score versions have no comparable change at all, so the figure
 * reads unavailable and the note underneath says why rather than showing a difference that would
 * mean nothing.
 */
@Composable
private fun ScoreSection(comparison: ReportComparison) {
    val unavailable = stringResource(R.string.value_unavailable_short)
    val score = comparison.score
    val compatible = score as? ScoreComparison.Compatible
    Column {
        SectionHeader(stringResource(R.string.comparison_score))
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        ReadoutWindow {
            WindowLabel(text = stringResource(R.string.comparison_score_change))
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            WindowReading(
                value = compatible?.delta?.let { signedUiNumber(it) } ?: unavailable,
                unit = null,
            )
            compatible?.let {
                Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
                WindowRow(
                    label = stringResource(R.string.comparison_score),
                    value =
                        stringResource(
                            R.string.comparison_score_value,
                            it.before?.let { value -> uiNumber(value) } ?: unavailable,
                            it.after?.let { value -> uiNumber(value) } ?: unavailable,
                        ),
                )
            }
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            WindowRow(
                label = stringResource(R.string.comparison_coverage),
                value =
                    stringResource(
                        R.string.comparison_coverage_value,
                        uiNumber(comparison.coverage.before),
                        uiNumber(comparison.coverage.after),
                    ),
            )
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        when (score) {
            is ScoreComparison.Compatible ->
                if (score.delta == null) {
                    Note(stringResource(R.string.comparison_score_no_delta))
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
        Note(
            comparison.coverage.delta?.let {
                stringResource(R.string.comparison_coverage_delta, signedUiNumber(it))
            } ?: stringResource(R.string.comparison_coverage_incompatible),
        )
    }
}

@Composable
private fun signedUiNumber(value: Int): String = if (value > 0) "+${uiNumber(value)}" else uiNumber(value)

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
                    comparisonStatusLabel(category.beforeStatus),
                    comparisonStatusLabel(category.afterStatus),
                ),
            expanded = isExpanded,
            onClick = onClick,
            tone = category.afterStatus?.toSemanticTone() ?: SemanticTone.NEUTRAL,
            // Repeated rows inside one section; the categories header above keeps the panel edge.
            strongDivider = false,
            leading = { StatusLamp(status = category.afterStatus) },
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
        // The report screen names its checks; showing a raw `battery.health` here instead was the
        // same evidence in two vocabularies.
        value =
            evidenceLabelResource(evidence.checkId)?.let { resource ->
                resource.formatArgument?.let { argument ->
                    stringResource(resource.stringResId, uiNumber(argument))
                } ?: stringResource(resource.stringResId)
            } ?: stableCodeDisplayText(evidence.checkId.substringAfter('.')),
    )
    DataRow(
        label = stringResource(changeLabel(evidence.change)),
        value =
            stringResource(
                R.string.comparison_category_status,
                comparisonStatusLabel(evidence.before?.status),
                comparisonStatusLabel(evidence.after?.status),
            ),
        tone = evidence.after?.status?.toSemanticTone() ?: SemanticTone.NEUTRAL,
    )
    LongValueRow(
        label = stringResource(R.string.comparison_first_report),
        value = evidence.before?.let { evidenceDetail(it) },
        unavailableLabel =
            stringResource(
                if (evidence.before == null) R.string.comparison_missing else R.string.value_unavailable_short,
            ),
    )
    LongValueRow(
        label = stringResource(R.string.comparison_second_report),
        value = evidence.after?.let { evidenceDetail(it) },
        unavailableLabel =
            stringResource(
                if (evidence.after == null) R.string.comparison_missing else R.string.value_unavailable_short,
            ),
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
private fun comparisonIssueMessage(state: ReportComparisonState.Issues): String {
    val firstMessage =
        state.first?.let { issue ->
            stringResource(
                R.string.comparison_report_issue,
                stringResource(R.string.comparison_first_report),
                stringResource(issue.messageResId),
            )
        }
    val secondMessage =
        state.second?.let { issue ->
            stringResource(
                R.string.comparison_report_issue,
                stringResource(R.string.comparison_second_report),
                stringResource(issue.messageResId),
            )
        }
    return listOfNotNull(firstMessage, secondMessage).joinToString(separator = "\n")
}

private val ComparisonReportIssue.messageResId: Int
    get() =
        when (this) {
            ComparisonReportIssue.NOT_FOUND -> R.string.report_not_found
            ComparisonReportIssue.UNSUPPORTED_SCHEMA_VERSION -> R.string.report_unsupported
            ComparisonReportIssue.CORRUPT_DATA -> R.string.report_corrupt
        }

/**
 * The shared status word, except that a null here means the report does not carry the check at all
 * — missing rather than unavailable. That one case is the only reason this screen has its own
 * function; the six statuses come from [statusLabel] like everywhere else.
 */
@Composable
private fun comparisonStatusLabel(status: DiagnosticStatus?): String =
    if (status == null) stringResource(R.string.comparison_missing) else statusLabel(status)

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
