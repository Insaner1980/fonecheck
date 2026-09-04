package com.insaner.fonecheck.domain.comparison

import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreVersion
import java.time.Instant

enum class EvidenceChange {
    UNCHANGED,
    ADDED,
    REMOVED,
    STATUS_CHANGED,
    VALUE_CHANGED,
    NEWLY_AVAILABLE,
    NEWLY_UNAVAILABLE,
    NOT_RUN,
}

enum class AttentionChange {
    NONE,
    APPEARED,
    RESOLVED,
    CHANGED,
}

sealed interface ScoreComparison {
    data class Compatible(
        val before: Int?,
        val after: Int?,
        val delta: Int?,
        val beforeState: ScoreState,
        val afterState: ScoreState,
        val version: ScoreVersion,
    ) : ScoreComparison

    data class Incompatible(
        val beforeVersion: ScoreVersion,
        val afterVersion: ScoreVersion,
    ) : ScoreComparison
}

fun ScoreComparison.deltaOrNull(): Int? = (this as? ScoreComparison.Compatible)?.delta

data class CoverageComparison(
    val before: Int,
    val after: Int,
    val delta: Int?,
)

data class EvidenceComparison(
    val checkId: String,
    val before: DiagnosticEvidence?,
    val after: DiagnosticEvidence?,
    val change: EvidenceChange,
    val attentionChange: AttentionChange,
)

data class CategoryComparison(
    val categoryId: DiagnosticCategoryId,
    val beforeStatus: DiagnosticStatus?,
    val afterStatus: DiagnosticStatus?,
    val evidence: List<EvidenceComparison>,
)

data class ReportComparison(
    val beforeId: String,
    val afterId: String,
    val beforeCompletedAt: Instant,
    val afterCompletedAt: Instant,
    val beforeApp: ReportAppContext,
    val afterApp: ReportAppContext,
    val beforeSchemaVersion: ReportSchemaVersion,
    val afterSchemaVersion: ReportSchemaVersion,
    val score: ScoreComparison,
    val coverage: CoverageComparison,
    val categories: List<CategoryComparison>,
)

class IncompatibleReportScopeException : IllegalArgumentException("Reports have incompatible scopes.")

fun reportScopesAreComparable(
    firstKind: ReportKind?,
    firstCategoryId: DiagnosticCategoryId?,
    secondKind: ReportKind?,
    secondCategoryId: DiagnosticCategoryId?,
): Boolean =
    when {
        firstKind == ReportKind.FULL_CHECK && secondKind == ReportKind.FULL_CHECK -> true
        firstKind == ReportKind.CATEGORY_ONLY && secondKind == ReportKind.CATEGORY_ONLY ->
            firstCategoryId != null && firstCategoryId == secondCategoryId
        else -> false
    }

object ReportComparisonEngine {
    fun compare(
        before: DiagnosticReport,
        after: DiagnosticReport,
    ): ReportComparison {
        if (
            !reportScopesAreComparable(
                firstKind = before.kind,
                firstCategoryId = before.comparisonCategoryId(),
                secondKind = after.kind,
                secondCategoryId = after.comparisonCategoryId(),
            )
        ) {
            throw IncompatibleReportScopeException()
        }
        before.requireValidComparisonKeys()
        after.requireValidComparisonKeys()
        val score =
            if (before.score.version.isCompatibleWith(after.score.version)) {
                ScoreComparison.Compatible(
                    before = before.score.value,
                    after = after.score.value,
                    delta = before.score.value?.let { old -> after.score.value?.minus(old) },
                    beforeState = before.score.state,
                    afterState = after.score.state,
                    version = before.score.version,
                )
            } else {
                ScoreComparison.Incompatible(before.score.version, after.score.version)
            }
        val compatibleSchema = before.schemaVersion == after.schemaVersion

        return ReportComparison(
            beforeId = before.stableId,
            afterId = after.stableId,
            beforeCompletedAt = before.completedAt,
            afterCompletedAt = after.completedAt,
            beforeApp = before.app,
            afterApp = after.app,
            beforeSchemaVersion = before.schemaVersion,
            afterSchemaVersion = after.schemaVersion,
            score = score,
            coverage =
                CoverageComparison(
                    before = before.coverage.percentage,
                    after = after.coverage.percentage,
                    delta =
                        if (compatibleSchema) {
                            after.coverage.percentage - before.coverage.percentage
                        } else {
                            null
                        },
                ),
            categories = comparisonCategories(before).map { compareCategory(it, before, after) },
        )
    }

    private fun compareCategory(
        categoryId: DiagnosticCategoryId,
        before: DiagnosticReport,
        after: DiagnosticReport,
    ): CategoryComparison {
        val beforeCategory = before.categories.find { it.categoryId == categoryId }
        val afterCategory = after.categories.find { it.categoryId == categoryId }
        val beforeEvidence = beforeCategory?.evidence?.associateBy { it.checkId.value }.orEmpty()
        val afterEvidence = afterCategory?.evidence?.associateBy { it.checkId.value }.orEmpty()

        return CategoryComparison(
            categoryId = categoryId,
            beforeStatus = beforeCategory?.aggregateStatus,
            afterStatus = afterCategory?.aggregateStatus,
            evidence =
                (beforeEvidence.keys + afterEvidence.keys)
                    .sorted()
                    .map { checkId ->
                        val old = beforeEvidence[checkId]
                        val new = afterEvidence[checkId]
                        EvidenceComparison(
                            checkId = checkId,
                            before = old,
                            after = new,
                            change = classifyChange(old, new),
                            attentionChange = classifyAttention(old?.status, new?.status),
                        )
                    },
        )
    }

    private fun classifyChange(
        before: DiagnosticEvidence?,
        after: DiagnosticEvidence?,
    ): EvidenceChange =
        when {
            before == null -> EvidenceChange.ADDED
            after == null -> EvidenceChange.REMOVED
            before.status != DiagnosticStatus.NOT_TESTED &&
                after.status == DiagnosticStatus.NOT_TESTED -> EvidenceChange.NOT_RUN
            before.status == DiagnosticStatus.NOT_AVAILABLE &&
                after.status != DiagnosticStatus.NOT_AVAILABLE -> EvidenceChange.NEWLY_AVAILABLE
            before.status != DiagnosticStatus.NOT_AVAILABLE &&
                after.status == DiagnosticStatus.NOT_AVAILABLE -> EvidenceChange.NEWLY_UNAVAILABLE
            before.status != after.status -> EvidenceChange.STATUS_CHANGED
            before.withoutTimestamp() != after.withoutTimestamp() -> EvidenceChange.VALUE_CHANGED
            else -> EvidenceChange.UNCHANGED
        }

    private fun classifyAttention(
        before: DiagnosticStatus?,
        after: DiagnosticStatus?,
    ): AttentionChange {
        val beforeNeedsAttention = before == DiagnosticStatus.WARNING || before == DiagnosticStatus.FAIL
        val afterNeedsAttention = after == DiagnosticStatus.WARNING || after == DiagnosticStatus.FAIL
        return when {
            !beforeNeedsAttention && afterNeedsAttention -> AttentionChange.APPEARED
            beforeNeedsAttention && !afterNeedsAttention -> AttentionChange.RESOLVED
            beforeNeedsAttention && afterNeedsAttention && before != after -> AttentionChange.CHANGED
            else -> AttentionChange.NONE
        }
    }

    private fun DiagnosticEvidence.withoutTimestamp() = copy(capturedAt = java.time.Instant.EPOCH)

    private fun DiagnosticReport.comparisonCategoryId(): DiagnosticCategoryId? =
        if (kind == ReportKind.CATEGORY_ONLY) categories.singleOrNull()?.categoryId else null

    private fun comparisonCategories(report: DiagnosticReport): List<DiagnosticCategoryId> =
        when (report.kind) {
            ReportKind.FULL_CHECK -> DiagnosticCatalog.categories
            ReportKind.CATEGORY_ONLY -> listOf(requireNotNull(report.comparisonCategoryId()))
        }

    private fun DiagnosticReport.requireValidComparisonKeys() {
        require(categories.map { it.categoryId }.distinct().size == categories.size) {
            "A report must not contain duplicate categories."
        }
        categories.forEach { category ->
            require(category.evidence.all { it.categoryId == category.categoryId }) {
                "Evidence must belong to its containing category."
            }
            require(
                category.evidence
                    .map { it.checkId }
                    .distinct()
                    .size == category.evidence.size,
            ) {
                "A report category must not contain duplicate check IDs."
            }
        }
    }
}
