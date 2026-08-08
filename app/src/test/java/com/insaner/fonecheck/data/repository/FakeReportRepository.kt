package com.insaner.fonecheck.data.repository

import com.insaner.fonecheck.domain.model.DiagnosticReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeReportRepository(
    var insertFailuresRemaining: Int = 0,
) : ReportRepository {
    val insertAttempts = mutableListOf<DiagnosticReport>()
    private val reports = linkedMapOf<String, DiagnosticReport>()

    override suspend fun insert(report: DiagnosticReport) {
        insertAttempts += report
        if (insertFailuresRemaining > 0) {
            insertFailuresRemaining -= 1
            error("insert_failed")
        }
        check(report.stableId !in reports) { "duplicate_report" }
        reports[report.stableId] = report
    }

    override fun observeSummaries(): Flow<List<SavedReportSummary>> = flowOf(emptyList())

    override suspend fun getById(id: String): ReportLoadResult =
        reports[id]?.let(ReportLoadResult::Available) ?: ReportLoadResult.NotFound

    override suspend fun getForComparison(
        firstReportId: String,
        secondReportId: String,
    ): ReportComparisonLoad =
        ReportComparisonLoad(
            first = getById(firstReportId),
            second = getById(secondReportId),
        )

    override suspend fun delete(id: String) {
        reports.remove(id)
    }

    override suspend fun deleteAll() {
        reports.clear()
    }
}
