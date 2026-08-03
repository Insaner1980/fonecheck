package com.insaner.fonecheck.domain.model

data class TestResult(
    val id: String,
    val name: String,
    val status: TestStatus,
    val detail: String?,
    val confidence: Confidence,
    val timestamp: Long,
)
