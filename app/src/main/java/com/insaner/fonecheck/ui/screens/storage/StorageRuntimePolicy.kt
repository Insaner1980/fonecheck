package com.insaner.fonecheck.ui.screens.storage

object StorageRuntimePolicy {
    fun usagePercent(
        totalBytes: Long,
        availableBytes: Long,
    ): Double? {
        if (totalBytes <= 0L) return null
        val boundedAvailable = availableBytes.coerceIn(0L, totalBytes)
        return (totalBytes - boundedAvailable).toDouble() * 100.0 / totalBytes
    }

    fun hasBenchmarkSpace(
        availableBytes: Long,
        dataSizeBytes: Long,
        reserveBytes: Long,
    ): Boolean =
        availableBytes >= 0L &&
            dataSizeBytes > 0L &&
            reserveBytes >= 0L &&
            availableBytes >= dataSizeBytes &&
            availableBytes - dataSizeBytes >= reserveBytes
}
