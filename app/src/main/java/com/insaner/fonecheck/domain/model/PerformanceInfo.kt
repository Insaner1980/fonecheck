package com.insaner.fonecheck.domain.model

import java.time.Instant

data class PerformanceInfo(
    val cpuModel: String,
    val cpuArchitecture: String,
    val cpuCores: Int,
    val cpuFrequencies: List<CpuCoreFrequency>,
    val cpuConfidence: Confidence,
    val totalRamBytes: Long?,
    val availableRamBytes: Long?,
    val ramConfidence: Confidence,
    val glEsVersion: String,
    val glRenderer: String,
    val glVendor: String,
    val vulkanFeatureDeclared: Boolean,
    val gpuConfidence: Confidence,
    val capturedAt: Instant,
) {
    companion object {
        const val UNAVAILABLE = "unavailable"
    }
}

data class CpuCoreFrequency(
    val coreIndex: Int,
    val currentMhz: Long?,
    val minMhz: Long?,
    val maxMhz: Long?,
)

enum class ThermalStatusCode {
    NONE,
    LIGHT,
    MODERATE,
    SEVERE,
    CRITICAL,
    EMERGENCY,
    SHUTDOWN,
    UNAVAILABLE,
}

enum class BenchmarkErrorCode {
    MEMORY_ALLOCATION_FAILED,
}

data class PerformanceBenchmarkResult(
    val cpuOperationsPerSecond: Long,
    val memoryMebibytesPerSecond: Double?,
    val memoryBytesProcessed: Long,
    val durationMillis: Long,
    val thermalBefore: ThermalStatusCode,
    val thermalAfter: ThermalStatusCode,
    val capturedAt: Instant,
    val error: BenchmarkErrorCode? = null,
)
