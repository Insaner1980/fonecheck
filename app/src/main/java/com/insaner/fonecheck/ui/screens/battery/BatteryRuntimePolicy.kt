package com.insaner.fonecheck.ui.screens.battery

import com.insaner.fonecheck.domain.model.Confidence
import kotlin.math.abs

object BatteryLevelNormalizer {
    fun normalize(
        level: Int?,
        scale: Int?,
    ): Int? {
        if (level == null || scale == null) return null
        if (level < 0 || scale <= 0) return null
        if (level > scale) return null
        return ((level.toLong() * 100L) / scale).toInt()
    }
}

object BatteryTemperatureNormalizer {
    fun normalize(rawTenthsCelsius: Int?): Float? =
        rawTenthsCelsius
            ?.takeIf { it in MIN_TEMPERATURE_TENTHS..MAX_TEMPERATURE_TENTHS }
            ?.div(10f)

    private const val MIN_TEMPERATURE_TENTHS = -500
    private const val MAX_TEMPERATURE_TENTHS = 1_000
}

enum class BatteryFlowStatus {
    CHARGING,
    DISCHARGING,
    IDLE,
    UNKNOWN,
}

enum class BatteryCurrentDirection {
    CHARGING,
    DISCHARGING,
    IDLE,
}

data class BatteryCurrentReading(
    val magnitudeMa: Double,
    val direction: BatteryCurrentDirection,
    val signNormalized: Boolean,
    val confidence: Confidence = Confidence.LOW,
)

object BatteryCurrentNormalizer {
    fun normalize(
        rawMicroAmps: Int?,
        status: BatteryFlowStatus,
    ): BatteryCurrentReading? {
        if (rawMicroAmps == null || rawMicroAmps == Int.MIN_VALUE) return null
        val magnitudeMicroAmps = abs(rawMicroAmps.toLong())
        if (magnitudeMicroAmps > MAX_PLAUSIBLE_CURRENT_MICROAMPS) return null

        val apiDirection =
            when {
                rawMicroAmps > 0 -> BatteryCurrentDirection.CHARGING
                rawMicroAmps < 0 -> BatteryCurrentDirection.DISCHARGING
                else -> BatteryCurrentDirection.IDLE
            }
        val observedDirection =
            when (status) {
                BatteryFlowStatus.CHARGING -> BatteryCurrentDirection.CHARGING
                BatteryFlowStatus.DISCHARGING -> BatteryCurrentDirection.DISCHARGING
                BatteryFlowStatus.IDLE, BatteryFlowStatus.UNKNOWN -> apiDirection
            }
        return BatteryCurrentReading(
            magnitudeMa = magnitudeMicroAmps / MICROAMPS_PER_MILLIAMP,
            direction = observedDirection,
            signNormalized = observedDirection != apiDirection && apiDirection != BatteryCurrentDirection.IDLE,
        )
    }

    // A phone battery current above 20 A is treated as an invalid OEM/fuel-gauge reading.
    private const val MAX_PLAUSIBLE_CURRENT_MICROAMPS = 20_000_000L
    private const val MICROAMPS_PER_MILLIAMP = 1_000.0
}

object BatteryCycleCountNormalizer {
    fun isSupported(sdkInt: Int): Boolean = sdkInt >= ANDROID_14_API_LEVEL

    fun normalize(
        sdkInt: Int,
        rawCycleCount: Int?,
    ): Int? = rawCycleCount?.takeIf { isSupported(sdkInt) && it >= 0 }

    private const val ANDROID_14_API_LEVEL = 34
}

object BatteryManufacturerPolicy {
    fun profileFor(manufacturer: String): ManufacturerProfile {
        val normalized = manufacturer.lowercase()
        return when {
            normalized.contains("samsung") -> ManufacturerProfile.SAMSUNG
            normalized.contains("oneplus") -> ManufacturerProfile.ONEPLUS
            normalized.contains("google") -> ManufacturerProfile.GOOGLE_PIXEL
            else -> ManufacturerProfile.GENERIC
        }
    }

    fun currentConfidence(profile: ManufacturerProfile): Confidence =
        when (profile) {
            ManufacturerProfile.SAMSUNG,
            ManufacturerProfile.ONEPLUS,
            ManufacturerProfile.GOOGLE_PIXEL,
            ManufacturerProfile.GENERIC,
            -> Confidence.LOW
        }
}
