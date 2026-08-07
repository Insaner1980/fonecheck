package com.insaner.fonecheck.domain.model

import java.time.Instant

enum class ReportKind {
    FULL_CHECK,
    CATEGORY_ONLY,
}

@JvmInline
value class ScoreVersion(
    val value: Int,
) {
    init {
        require(value > 0) { "Score version must be positive." }
    }

    fun isCompatibleWith(other: ScoreVersion): Boolean = this == other

    companion object {
        val CURRENT = ScoreVersion(1)
    }
}

@JvmInline
value class ReportSchemaVersion(
    val value: Int,
) {
    init {
        require(value > 0) { "Report schema version must be positive." }
    }

    companion object {
        val CURRENT = ReportSchemaVersion(1)
    }
}

enum class ScoreState {
    INCOMPLETE,
    PARTIAL,
    COMPLETE,
}

data class ScoreSummary(
    val version: ScoreVersion,
    val value: Int?,
    val state: ScoreState,
)

data class CoverageSummary(
    val applicableCount: Int,
    val completedCount: Int,
    val notTestedCount: Int,
    val unavailableCount: Int,
    val percentage: Int,
)

data class DiagnosticCategoryResult(
    val categoryId: DiagnosticCategoryId,
    val aggregateStatus: DiagnosticStatus,
    val evidence: List<DiagnosticEvidence>,
)

data class ReportDeviceContext(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val product: String,
    val androidRelease: String,
    val apiLevel: Int,
    val securityPatch: String?,
)

data class ReportAppContext(
    val versionName: String,
    val versionCode: Long,
)

data class DiagnosticReport(
    val stableId: String,
    val kind: ReportKind,
    val startedAt: Instant,
    val completedAt: Instant,
    val device: ReportDeviceContext,
    val app: ReportAppContext,
    val categories: List<DiagnosticCategoryResult>,
    val score: ScoreSummary,
    val coverage: CoverageSummary,
    val schemaVersion: ReportSchemaVersion,
)
