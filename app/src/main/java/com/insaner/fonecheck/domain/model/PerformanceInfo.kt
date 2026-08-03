package com.insaner.fonecheck.domain.model

data class PerformanceInfo(
    // CPU
    val cpuModel: String,
    val cpuArchitecture: String,
    val cpuCores: Int,
    val cpuFrequencies: List<CpuCoreFrequency>,
    val cpuConfidence: Confidence,
    // RAM
    val totalRam: String,
    val availableRam: String,
    val ramConfidence: Confidence,
    // GPU
    val glEsVersion: String,
    val glRenderer: String,
    val glVendor: String,
    val vulkanSupported: Boolean,
    val gpuConfidence: Confidence,
)

data class CpuCoreFrequency(
    val coreIndex: Int,
    val currentMhz: String,
    val minMhz: String,
    val maxMhz: String,
)
