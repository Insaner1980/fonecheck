package com.insaner.fonecheck.ui.screens.thermal

import com.insaner.fonecheck.domain.model.ThermalStatusCode

enum class ThermalSeverityCode {
    NORMAL,
    LIGHT,
    MODERATE,
    SEVERE,
    CRITICAL,
    UNAVAILABLE,
}

object ThermalRuntimePolicy {
    fun status(
        sdkInt: Int,
        rawStatus: Int?,
    ): ThermalStatusCode {
        if (sdkInt < ANDROID_10_API_LEVEL) return ThermalStatusCode.UNAVAILABLE
        return when (rawStatus) {
            0 -> ThermalStatusCode.NONE
            1 -> ThermalStatusCode.LIGHT
            2 -> ThermalStatusCode.MODERATE
            3 -> ThermalStatusCode.SEVERE
            4 -> ThermalStatusCode.CRITICAL
            5 -> ThermalStatusCode.EMERGENCY
            6 -> ThermalStatusCode.SHUTDOWN
            else -> ThermalStatusCode.UNAVAILABLE
        }
    }

    fun severity(status: ThermalStatusCode): ThermalSeverityCode =
        when (status) {
            ThermalStatusCode.NONE -> ThermalSeverityCode.NORMAL
            ThermalStatusCode.LIGHT -> ThermalSeverityCode.LIGHT
            ThermalStatusCode.MODERATE -> ThermalSeverityCode.MODERATE
            ThermalStatusCode.SEVERE -> ThermalSeverityCode.SEVERE
            ThermalStatusCode.CRITICAL,
            ThermalStatusCode.EMERGENCY,
            ThermalStatusCode.SHUTDOWN,
            -> ThermalSeverityCode.CRITICAL

            ThermalStatusCode.UNAVAILABLE -> ThermalSeverityCode.UNAVAILABLE
        }

    fun headroom(
        sdkInt: Int,
        rawHeadroom: Float?,
    ): Float? =
        rawHeadroom?.takeIf {
            sdkInt >= ANDROID_11_API_LEVEL && it.isFinite() && it >= 0f
        }

    fun batteryTemperature(rawTenthsCelsius: Int?): Float? =
        rawTenthsCelsius
            ?.takeIf { it in MIN_BATTERY_TEMPERATURE_TENTHS..MAX_BATTERY_TEMPERATURE_TENTHS }
            ?.div(10f)

    private const val ANDROID_10_API_LEVEL = 29
    private const val ANDROID_11_API_LEVEL = 30
    private const val MIN_BATTERY_TEMPERATURE_TENTHS = -500
    private const val MAX_BATTERY_TEMPERATURE_TENTHS = 1_000
}
