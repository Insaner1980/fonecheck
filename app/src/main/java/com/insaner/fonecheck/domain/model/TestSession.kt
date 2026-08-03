package com.insaner.fonecheck.domain.model

data class TestSession(
    val id: String,
    val timestamp: Long,
    val deviceInfo: DeviceInfo,
    val categories: List<CategoryTestResult>,
    val overallScore: Int,
)
