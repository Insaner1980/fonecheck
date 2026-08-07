package com.insaner.fonecheck.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reports",
    indices = [Index(value = ["completedAtEpochMillis"])],
)
data class ReportEntity(
    @PrimaryKey val id: String,
    val reportKindCode: String,
    val categoryId: String?,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long,
    val reportSchemaVersion: Int,
    val scoreVersion: Int,
    val scoreValue: Int?,
    val scoreStateCode: String,
    val coveragePercentage: Int,
    val applicableCount: Int,
    val completedCount: Int,
    val notTestedCount: Int,
    val unavailableCount: Int,
    val warningCount: Int,
    val failureCount: Int,
    val payloadJson: String,
) {
    init {
        require(id.isNotBlank()) { "Report id must not be blank." }
        require(payloadJson.isNotBlank()) { "Report payload must not be blank." }
        require(reportKindCode == "full_check" || reportKindCode == "category_only") {
            "Unsupported report kind code."
        }
        require(
            (reportKindCode == "full_check" && categoryId == null) ||
                (reportKindCode == "category_only" && !categoryId.isNullOrBlank()),
        ) { "Category id must be present only for category-only reports." }
        require(startedAtEpochMillis <= completedAtEpochMillis) {
            "Report start must not be after completion."
        }
        require(reportSchemaVersion > 0) { "Report schema version must be positive." }
        require(scoreVersion > 0) { "Score version must be positive." }
        require(
            scoreStateCode == "incomplete" ||
                scoreStateCode == "partial" ||
                scoreStateCode == "complete",
        ) { "Unsupported score state code." }
        if (scoreStateCode == "incomplete") {
            require(scoreValue == null) { "Incomplete reports must not have a score." }
        } else {
            require(scoreValue != null && scoreValue in 0..100) {
                "Partial and complete reports must have a score from 0 to 100."
            }
        }
        require(coveragePercentage in 0..100) { "Coverage must be from 0 to 100." }
        require(
            applicableCount >= 0 &&
                completedCount >= 0 &&
                notTestedCount >= 0 &&
                unavailableCount >= 0 &&
                warningCount >= 0 &&
                failureCount >= 0,
        ) { "Report counts must not be negative." }
        require(completedCount + notTestedCount == applicableCount) {
            "Completed and not-tested counts must equal the applicable count."
        }
    }
}
