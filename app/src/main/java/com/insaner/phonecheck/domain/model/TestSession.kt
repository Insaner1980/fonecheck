package com.insaner.phonecheck.domain.model

data class TestSession(
    val id: String,
    val timestamp: Long,
    val deviceInfo: DeviceInfo,
    val results: List<TestResult>,
    val overallScore: Int,
)
