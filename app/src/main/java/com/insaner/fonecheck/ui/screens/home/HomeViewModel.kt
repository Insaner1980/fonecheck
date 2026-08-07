package com.insaner.fonecheck.ui.screens.home

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.insaner.fonecheck.ui.screens.battery.BatteryLevelNormalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class DeviceSummary(
    val modelName: String = "",
    val androidVersion: String = "",
    val batteryLevel: Int? = null,
)

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val _summary = MutableStateFlow(DeviceSummary())
        val summary: StateFlow<DeviceSummary> = _summary.asStateFlow()

        init {
            loadSummary()
        }

        private fun loadSummary() {
            val context = getApplication<Application>()
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level =
                batteryIntent
                    ?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    ?.takeIf { batteryIntent.hasExtra(BatteryManager.EXTRA_LEVEL) }
            val scale =
                batteryIntent
                    ?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    ?.takeIf { batteryIntent.hasExtra(BatteryManager.EXTRA_SCALE) }
            val batteryPercent = BatteryLevelNormalizer.normalize(level, scale)

            _summary.value =
                DeviceSummary(
                    modelName = "${Build.MANUFACTURER} ${Build.MODEL}",
                    androidVersion = "Android ${Build.VERSION.RELEASE}",
                    batteryLevel = batteryPercent,
                )
        }
    }
