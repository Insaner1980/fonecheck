package com.insaner.fonecheck.data.repository

import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ScoreState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface ReportRepository {
    suspend fun insert(report: DiagnosticReport)

    fun observeSummaries(): Flow<List<SavedReportSummary>>

    suspend fun getById(id: String): ReportLoadResult

    suspend fun getForComparison(
        firstReportId: String,
        secondReportId: String,
    ): ReportComparisonLoad

    suspend fun delete(id: String)

    suspend fun deleteAll()
}

suspend fun ReportRepository.insertOrConfirm(report: DiagnosticReport): Boolean =
    try {
        insert(report)
        true
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        val existing = runCatching { getById(report.stableId) }.getOrNull()
        existing is ReportLoadResult.Available && existing.report == report
    }

data class SavedReportSummary(
    val stableId: String,
    val kind: ReportKind?,
    val categoryId: DiagnosticCategoryId?,
    val completedAt: Instant,
    val reportSchemaVersion: Int,
    val scoreVersion: Int,
    val scoreValue: Int?,
    val scoreState: ScoreState?,
    val coveragePercentage: Int,
    val warningCount: Int,
    val failureCount: Int,
    val unavailableReason: ReportReadFailure?,
)

enum class ReportReadFailure {
    UNSUPPORTED_SCHEMA_VERSION,
    CORRUPT_DATA,
}

sealed interface ReportLoadResult {
    data class Available(
        val report: DiagnosticReport,
    ) : ReportLoadResult

    data object NotFound : ReportLoadResult

    data class Unavailable(
        val reportId: String,
        val reason: ReportReadFailure,
    ) : ReportLoadResult
}

data class ReportComparisonLoad(
    val first: ReportLoadResult,
    val second: ReportLoadResult,
)
