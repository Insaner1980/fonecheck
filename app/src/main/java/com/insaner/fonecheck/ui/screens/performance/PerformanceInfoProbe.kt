package com.insaner.fonecheck.ui.screens.performance

import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CpuCoreFrequency
import com.insaner.fonecheck.domain.model.PerformanceInfo
import java.util.Locale

internal object PerformanceInfoConfidence {
    fun cpu(frequencies: List<CpuCoreFrequency>): Confidence =
        if (
            frequencies.any { frequency ->
                frequency.currentMhz != null || frequency.minMhz != null || frequency.maxMhz != null
            }
        ) {
            Confidence.HIGH
        } else {
            Confidence.LOW
        }
}

internal data class GlInfo(
    val version: String,
    val renderer: String,
    val vendor: String,
)

internal interface GlInfoSession : AutoCloseable {
    fun version(): String

    fun renderer(): String

    fun vendor(): String
}

internal fun readGlInfo(openSession: () -> GlInfoSession): GlInfo {
    val session =
        try {
            openSession()
        } catch (_: Exception) {
            return unavailableGlInfo()
        }
    return try {
        GlInfo(
            version = normalizePerformanceText(session.version()),
            renderer = normalizePerformanceText(session.renderer()),
            vendor = normalizePerformanceText(session.vendor()),
        )
    } catch (_: Exception) {
        unavailableGlInfo()
    } finally {
        try {
            session.close()
        } catch (_: Exception) {
            // The captured values remain usable if platform cleanup itself reports an error.
        }
    }
}

internal fun normalizePerformanceText(value: String?): String {
    val normalized = value?.trim().orEmpty()
    return normalized.takeUnless {
        it.isEmpty() || it.lowercase(Locale.ROOT) in setOf("unknown", "n/a", "null")
    } ?: PerformanceInfo.UNAVAILABLE
}

private fun unavailableGlInfo() =
    GlInfo(
        version = PerformanceInfo.UNAVAILABLE,
        renderer = PerformanceInfo.UNAVAILABLE,
        vendor = PerformanceInfo.UNAVAILABLE,
    )
