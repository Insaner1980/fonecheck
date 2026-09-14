package com.insaner.fonecheck.domain.model

data class CategoryTestResult(
    val category: TestCategory,
    val status: DiagnosticStatus,
    val summary: String,
    val results: List<TestResult>,
)
