package com.insaner.fonecheck.ui.screens.home

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class DeviceSummary(
    val modelName: String = "",
    val androidVersion: String = "",
    val batteryLevel: Int = 0,
)

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val _summary = MutableStateFlow(DeviceSummary())
        val summary: StateFlow<DeviceSummary> = _summary

        init {
            loadSummary()
        }

        private fun loadSummary() {
            val context = getApplication<Application>()
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val batteryPercent = if (scale > 0) (level * 100) / scale else level

            _summary.value =
                DeviceSummary(
                    modelName = "${Build.MANUFACTURER} ${Build.MODEL}",
                    androidVersion = "Android ${Build.VERSION.RELEASE}",
                    batteryLevel = batteryPercent,
                )
        }
    }
