package com.insaner.fonecheck.domain.model

import java.time.Instant

@JvmInline
value class DiagnosticSnapshotVersion(
    val value: Int,
) {
    init {
        require(value > 0) { "Diagnostic snapshot version must be positive." }
    }

    companion object {
        val CURRENT = DiagnosticSnapshotVersion(1)
    }
}

data class DiagnosticCategorySnapshot(
    val version: DiagnosticSnapshotVersion,
    val categoryId: DiagnosticCategoryId,
    val evidence: List<DiagnosticEvidence>,
) {
    init {
        require(evidence.isNotEmpty()) { "A diagnostic snapshot must contain evidence." }
        require(evidence.all { it.categoryId == categoryId }) {
            "Snapshot evidence must belong to its category."
        }
        require(evidence.map { it.checkId }.distinct().size == evidence.size) {
            "A diagnostic snapshot must not contain duplicate check IDs."
        }
    }
}

data class ReportAssemblyRequest(
    val stableId: String,
    val kind: ReportKind,
    val startedAt: Instant,
    val completedAt: Instant,
    val device: ReportDeviceContext,
    val app: ReportAppContext,
    val snapshots: List<DiagnosticCategorySnapshot>,
)

object ReportAssembler {
    fun assemble(request: ReportAssemblyRequest): DiagnosticReport {
        require(request.stableId.isNotBlank()) { "Report ID must not be blank." }
        require(request.startedAt <= request.completedAt) { "Report start must not be after completion." }
        require(request.snapshots.all { it.version == DiagnosticSnapshotVersion.CURRENT }) {
            "Only the current diagnostic snapshot version can be assembled."
        }
        require(request.snapshots.map { it.categoryId }.distinct().size == request.snapshots.size) {
            "A report must not contain duplicate category snapshots."
        }
        val orderedSnapshots = request.orderedSnapshots()
        val categories =
            orderedSnapshots.map { snapshot ->
                DiagnosticCategoryResult(
                    categoryId = snapshot.categoryId,
                    aggregateStatus = aggregate(snapshot.evidence),
                    evidence = snapshot.evidence,
                )
            }
        val calculation = ScoreCalculator.calculate(categories)
        return DiagnosticReport(
            stableId = request.stableId,
            kind = request.kind,
            startedAt = request.startedAt,
            completedAt = request.completedAt,
            device = request.device,
            app = request.app,
            categories = categories,
            score = calculation.score,
            coverage = calculation.coverage,
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
    }

    private fun ReportAssemblyRequest.orderedSnapshots(): List<DiagnosticCategorySnapshot> =
        when (kind) {
            ReportKind.FULL_CHECK -> {
                val snapshotsByCategory = snapshots.associateBy(DiagnosticCategorySnapshot::categoryId)
                require(snapshotsByCategory.keys == DiagnosticCatalog.categories.toSet()) {
                    "A full report must contain exactly one snapshot for every catalog category."
                }
                DiagnosticCatalog.categories.map { snapshotsByCategory.getValue(it) }
            }

            ReportKind.CATEGORY_ONLY -> {
                require(snapshots.size == 1) { "A category-only report must contain exactly one snapshot." }
                snapshots
            }
        }

    private fun aggregate(evidence: List<DiagnosticEvidence>): DiagnosticStatus =
        when {
            evidence.any { it.status == DiagnosticStatus.FAIL } -> DiagnosticStatus.FAIL
            evidence.any { it.status == DiagnosticStatus.WARNING } -> DiagnosticStatus.WARNING
            evidence.any { it.status == DiagnosticStatus.PASS } -> DiagnosticStatus.PASS
            evidence.all { it.status == DiagnosticStatus.NOT_AVAILABLE } -> DiagnosticStatus.NOT_AVAILABLE
            evidence.any { it.status == DiagnosticStatus.NOT_TESTED } -> DiagnosticStatus.NOT_TESTED
            else -> DiagnosticStatus.INFO
        }
}
