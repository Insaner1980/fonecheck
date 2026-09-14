package com.insaner.fonecheck.runtime

import android.os.BatteryManager
import com.insaner.fonecheck.domain.observation.BatteryHealthCode

fun batteryHealthCodeFromAndroid(rawHealth: Int): BatteryHealthCode =
    when (rawHealth) {
        BatteryManager.BATTERY_HEALTH_UNKNOWN -> BatteryHealthCode.UNKNOWN
        BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealthCode.GOOD
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealthCode.OVERHEAT
        BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealthCode.DEAD
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealthCode.OVER_VOLTAGE
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealthCode.UNSPECIFIED_FAILURE
        BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealthCode.COLD
        else -> BatteryHealthCode.OTHER
    }
