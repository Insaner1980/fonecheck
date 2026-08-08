package com.insaner.fonecheck.data.repository

import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.ReportKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeReportRepository(
    var insertFailuresRemaining: Int = 0,
    var getByIdFailuresRemaining: Int = 0,
    var getByIdOverride: ReportLoadResult? = null,
    var deleteFailuresRemaining: Int = 0,
) : ReportRepository {
    val insertAttempts = mutableListOf<DiagnosticReport>()
    private val reports = linkedMapOf<String, DiagnosticReport>()
    private val summaries = MutableStateFlow<List<SavedReportSummary>>(emptyList())
    var summariesFlowOverride: Flow<List<SavedReportSummary>>? = null

    override suspend fun insert(report: DiagnosticReport) {
        insertAttempts += report
        if (insertFailuresRemaining > 0) {
            insertFailuresRemaining -= 1
            error("insert_failed")
        }
        check(report.stableId !in reports) { "duplicate_report" }
        reports[report.stableId] = report
        publishSummaries()
    }

    override fun observeSummaries(): Flow<List<SavedReportSummary>> = summariesFlowOverride ?: summaries

    override suspend fun getById(id: String): ReportLoadResult {
        if (getByIdFailuresRemaining > 0) {
            getByIdFailuresRemaining -= 1
            error("load_failed")
        }
        return getByIdOverride
            ?: reports[id]?.let(ReportLoadResult::Available)
            ?: ReportLoadResult.NotFound
    }

    override suspend fun getForComparison(
        firstReportId: String,
        secondReportId: String,
    ): ReportComparisonLoad =
        ReportComparisonLoad(
            first = getById(firstReportId),
            second = getById(secondReportId),
        )

    override suspend fun delete(id: String) {
        if (deleteFailuresRemaining > 0) {
            deleteFailuresRemaining -= 1
            error("delete_failed")
        }
        reports.remove(id)
        publishSummaries()
    }

    override suspend fun deleteAll() {
        reports.clear()
        publishSummaries()
    }

    private fun publishSummaries() {
        summaries.value =
            reports.values
                .sortedByDescending(DiagnosticReport::completedAt)
                .map { report ->
                    val evidence = report.categories.flatMap { it.evidence }
                    SavedReportSummary(
                        stableId = report.stableId,
                        kind = report.kind,
                        categoryId =
                            if (report.kind == ReportKind.CATEGORY_ONLY) {
                                report.categories.single().categoryId
                            } else {
                                null
                            },
                        completedAt = report.completedAt,
                        reportSchemaVersion = report.schemaVersion.value,
                        scoreVersion = report.score.version.value,
                        scoreValue = report.score.value,
                        scoreState = report.score.state,
                        coveragePercentage = report.coverage.percentage,
                        warningCount = evidence.count { it.status == DiagnosticStatus.WARNING },
                        failureCount = evidence.count { it.status == DiagnosticStatus.FAIL },
                        unavailableReason = null,
                    )
                }
    }
}
