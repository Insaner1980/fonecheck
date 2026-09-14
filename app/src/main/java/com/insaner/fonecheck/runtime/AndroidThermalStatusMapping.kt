package com.insaner.fonecheck.runtime

import android.os.PowerManager
import com.insaner.fonecheck.domain.model.ThermalStatusCode

fun thermalStatusCodeFromAndroid(rawStatus: Int): ThermalStatusCode =
    when (rawStatus) {
        PowerManager.THERMAL_STATUS_NONE -> ThermalStatusCode.NONE
        PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatusCode.LIGHT
        PowerManager.THERMAL_STATUS_MODERATE -> ThermalStatusCode.MODERATE
        PowerManager.THERMAL_STATUS_SEVERE -> ThermalStatusCode.SEVERE
        PowerManager.THERMAL_STATUS_CRITICAL -> ThermalStatusCode.CRITICAL
        PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalStatusCode.EMERGENCY
        PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatusCode.SHUTDOWN
        else -> ThermalStatusCode.UNAVAILABLE
    }
