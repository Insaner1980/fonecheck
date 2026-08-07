package com.insaner.fonecheck.data.repository

import com.insaner.fonecheck.data.local.ReportDao
import com.insaner.fonecheck.data.local.ReportEntity
import com.insaner.fonecheck.data.local.ReportSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomReportRepository(
    private val reportDao: ReportDao,
) : ReportRepository {
    override suspend fun insert(report: DiagnosticReport) {
        val payloadJson = ReportPayloadCodec.encode(report)
        reportDao.insert(report.toEntity(payloadJson))
    }

    override fun observeSummaries(): Flow<List<SavedReportSummary>> =
        reportDao.observeSummaries().map { summaries -> summaries.map(ReportSummary::toDomain) }

    override suspend fun getById(id: String): ReportLoadResult =
        reportDao.getById(id)?.toLoadResult() ?: ReportLoadResult.NotFound

    override suspend fun getForComparison(
        firstReportId: String,
        secondReportId: String,
    ): ReportComparisonLoad =
        ReportComparisonLoad(
            first = getById(firstReportId),
            second = getById(secondReportId),
        )

    override suspend fun delete(id: String) {
        reportDao.deleteById(id)
    }

    override suspend fun deleteAll() {
        reportDao.deleteAll()
    }
}

private fun DiagnosticReport.toEntity(payloadJson: String): ReportEntity {
    require(stableId.isNotBlank()) { "Report ID must not be blank." }
    require(startedAt <= completedAt) { "Report start must not be after completion." }
    require(schemaVersion == ReportSchemaVersion.CURRENT) { "Only the current report schema can be inserted." }
    require(categories.map { it.categoryId }.distinct().size == categories.size) {
        "A report must not contain duplicate categories."
    }
    require(categories.all { category -> category.evidence.all { it.categoryId == category.categoryId } }) {
        "Evidence must belong to its containing category."
    }
    val categoryId =
        when (kind) {
            ReportKind.FULL_CHECK -> null
            ReportKind.CATEGORY_ONLY -> {
                require(categories.size == 1) { "A category-only report must contain exactly one category." }
                categories.single().categoryId.stableId
            }
        }
    val evidence = categories.flatMap { it.evidence }
    return ReportEntity(
        id = stableId,
        reportKindCode = kind.stableCode(),
        categoryId = categoryId,
        startedAtEpochMillis = startedAt.toEpochMilli(),
        completedAtEpochMillis = completedAt.toEpochMilli(),
        reportSchemaVersion = schemaVersion.value,
        scoreVersion = score.version.value,
        scoreValue = score.value,
        scoreStateCode = score.state.stableCode(),
        coveragePercentage = coverage.percentage,
        applicableCount = coverage.applicableCount,
        completedCount = coverage.completedCount,
        notTestedCount = coverage.notTestedCount,
        unavailableCount = coverage.unavailableCount,
        warningCount = evidence.count { it.status == DiagnosticStatus.WARNING },
        failureCount = evidence.count { it.status == DiagnosticStatus.FAIL },
        payloadJson = payloadJson,
    )
}

private fun ReportEntity.toLoadResult(): ReportLoadResult {
    if (reportSchemaVersion != ReportSchemaVersion.CURRENT.value) {
        return ReportLoadResult.Unavailable(id, ReportReadFailure.UNSUPPORTED_SCHEMA_VERSION)
    }
    return try {
        val report = ReportPayloadCodec.decode(payloadJson)
        if (report.toEntity(payloadJson) != this) {
            ReportLoadResult.Unavailable(id, ReportReadFailure.CORRUPT_DATA)
        } else {
            ReportLoadResult.Available(report)
        }
    } catch (_: Exception) {
        ReportLoadResult.Unavailable(id, ReportReadFailure.CORRUPT_DATA)
    }
}

private fun ReportSummary.toDomain(): SavedReportSummary {
    val domainKind = enumFromStableCodeOrNull<ReportKind>(reportKindCode)
    val domainCategoryId =
        categoryId?.let { stableId ->
            DiagnosticCategoryId.entries.firstOrNull { it.stableId == stableId }
        }
    val domainScoreState = enumFromStableCodeOrNull<ScoreState>(scoreStateCode)
    val metadataIsValid =
        reportSchemaVersion > 0 &&
            domainKind != null &&
            domainScoreState != null &&
            coveragePercentage in 0..100 &&
            warningCount >= 0 &&
            failureCount >= 0 &&
            when (domainKind) {
                ReportKind.FULL_CHECK -> categoryId == null
                ReportKind.CATEGORY_ONLY -> categoryId != null && domainCategoryId != null
                null -> false
            } &&
            when (domainScoreState) {
                ScoreState.INCOMPLETE -> scoreValue == null
                ScoreState.PARTIAL,
                ScoreState.COMPLETE,
                -> scoreValue != null && scoreValue in 0..100
                null -> false
            }
    val unavailableReason =
        when {
            reportSchemaVersion != ReportSchemaVersion.CURRENT.value -> ReportReadFailure.UNSUPPORTED_SCHEMA_VERSION
            !metadataIsValid -> ReportReadFailure.CORRUPT_DATA
            else -> null
        }
    return SavedReportSummary(
        stableId = id,
        kind = domainKind,
        categoryId = domainCategoryId,
        completedAt = Instant.ofEpochMilli(completedAtEpochMillis),
        reportSchemaVersion = reportSchemaVersion,
        scoreVersion = scoreVersion,
        scoreValue = scoreValue,
        scoreState = domainScoreState,
        coveragePercentage = coveragePercentage,
        warningCount = warningCount,
        failureCount = failureCount,
        unavailableReason = unavailableReason,
    )
}
