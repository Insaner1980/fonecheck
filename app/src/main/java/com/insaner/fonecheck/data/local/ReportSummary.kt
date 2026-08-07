package com.insaner.fonecheck.data.local

data class ReportSummary(
    val id: String,
    val reportKindCode: String,
    val categoryId: String?,
    val completedAtEpochMillis: Long,
    val scoreVersion: Int,
    val scoreValue: Int?,
    val scoreStateCode: String,
    val coveragePercentage: Int,
    val warningCount: Int,
    val failureCount: Int,
)
