package com.insaner.fonecheck.ui.screens.battery

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.math.abs

// ── State classes ────────────────────────────────────────────────────────────────

data class BasicBatteryState(
    val level: Int = 0,
    val voltage: Int = 0,
    val temperatureTenths: Int = 0,
    val temperatureCelsius: Float = 0f,
    val healthStatus: Int = BatteryManager.BATTERY_HEALTH_UNKNOWN,
    val technology: String? = null,
    val isCharging: Boolean = false,
)

data class ChargingState(
    val status: Int = BatteryManager.BATTERY_STATUS_UNKNOWN,
    val plugType: Int = 0,
    val chargingCurrentMa: Int? = null,
    val chargingCurrentConfidence: Confidence = Confidence.UNAVAILABLE,
    val manufacturerNote: String? = null,
)

data class HealthState(
    val healthStatusRaw: Int = BatteryManager.BATTERY_HEALTH_UNKNOWN,
    val healthStatusLabel: Int = R.string.batt_health_unknown,
    val cycleCount: Int? = null,
    val cycleCountConfidence: Confidence = Confidence.UNAVAILABLE,
    val healthPercentage: Int? = null,
    val healthPercentageConfidence: Confidence = Confidence.UNAVAILABLE,
)

data class CapacityState(
    val designCapacityMah: Double? = null,
    val designCapacityConfidence: Confidence = Confidence.UNAVAILABLE,
)

enum class ManufacturerProfile {
    SAMSUNG,
    ONEPLUS,
    GOOGLE_PIXEL,
    GENERIC,
}

data class ManufacturerState(
    val profile: ManufacturerProfile = ManufacturerProfile.GENERIC,
    val manufacturerName: String = "",
    val notes: List<String> = emptyList(),
)

enum class BatterySection {
    BASIC,
    CHARGING,
    HEALTH,
    CAPACITY,
    MANUFACTURER,
}

data class BatteryTestState(
    val basic: BasicBatteryState = BasicBatteryState(),
    val charging: ChargingState = ChargingState(),
    val health: HealthState = HealthState(),
    val capacity: CapacityState = CapacityState(),
    val manufacturer: ManufacturerState = ManufacturerState(),
    val expandedSection: BatterySection? = null,
)

// ── ViewModel ────────────────────────────────────────────────────────────────────

@HiltViewModel
class BatteryTestViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val context: Context get() = getApplication()
        private val batteryManager = context.getSystemService(BatteryManager::class.java)

        private val _state = MutableStateFlow(BatteryTestState())
        val state: StateFlow<BatteryTestState> = _state

        private var batteryReceiver: BroadcastReceiver? = null

        init {
            detectManufacturer()
            registerBatteryReceiver()
            refreshAdvancedInfo()
        }

        fun toggleSection(section: BatterySection) {
            val current = _state.value.expandedSection
            _state.value =
                _state.value.copy(
                    expandedSection = if (current == section) null else section,
                )
        }

        // ── Battery broadcast receiver ──────────────────────────────────────────────

        private fun registerBatteryReceiver() {
            val receiver =
                object : BroadcastReceiver() {
                    override fun onReceive(
                        ctx: Context?,
                        intent: Intent?,
                    ) {
                        intent?.let { parseBatteryIntent(it) }
                    }
                }
            batteryReceiver = receiver
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            // Sticky broadcast — returns current state immediately
            val stickyIntent = context.registerReceiver(receiver, filter)
            stickyIntent?.let { parseBatteryIntent(it) }
        }

        private fun parseBatteryIntent(intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            val levelPercent = if (scale > 0) (level * 100) / scale else level
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val isCharging =
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            _state.value =
                _state.value.copy(
                    basic =
                        BasicBatteryState(
                            level = levelPercent,
                            voltage = voltage,
                            temperatureTenths = temperature,
                            temperatureCelsius = temperature / 10.0f,
                            healthStatus = health,
                            technology = technology,
                            isCharging = isCharging,
                        ),
                    charging =
                        _state.value.charging.copy(
                            status = status,
                            plugType = plugged,
                        ),
                    health =
                        _state.value.health.copy(
                            healthStatusRaw = health,
                            healthStatusLabel = getHealthLabel(health),
                        ),
                )

            refreshChargingCurrent(isCharging)
        }

        // ── Charging current ────────────────────────────────────────────────────────

        private fun refreshChargingCurrent(isCharging: Boolean) {
            val rawMicroAmps = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

            if (rawMicroAmps == null || rawMicroAmps == Int.MIN_VALUE) {
                _state.value =
                    _state.value.copy(
                        charging =
                            _state.value.charging.copy(
                                chargingCurrentMa = null,
                                chargingCurrentConfidence = Confidence.UNAVAILABLE,
                                manufacturerNote = null,
                            ),
                    )
                return
            }

            val profile = _state.value.manufacturer.profile
            var currentMa = rawMicroAmps / 1000

            // OnePlus SUPERVOOC: current may be negative even when charging
            if (profile == ManufacturerProfile.ONEPLUS && isCharging && currentMa < 0) {
                currentMa = abs(currentMa)
            }

            val confidence =
                when (profile) {
                    ManufacturerProfile.GOOGLE_PIXEL -> Confidence.HIGH
                    ManufacturerProfile.SAMSUNG -> Confidence.LOW
                    ManufacturerProfile.ONEPLUS -> Confidence.LOW
                    ManufacturerProfile.GENERIC -> Confidence.LOW
                }

            val note =
                when (profile) {
                    ManufacturerProfile.SAMSUNG ->
                        context.getString(R.string.batt_mfr_samsung_current_note)
                    ManufacturerProfile.ONEPLUS ->
                        context.getString(R.string.batt_mfr_oneplus_current_note)
                    else -> null
                }

            _state.value =
                _state.value.copy(
                    charging =
                        _state.value.charging.copy(
                            chargingCurrentMa = currentMa,
                            chargingCurrentConfidence = confidence,
                            manufacturerNote = note,
                        ),
                )
        }

        // ── Advanced info (API 34+) ─────────────────────────────────────────────────

        private fun refreshAdvancedInfo() {
            var cycleCount: Int? = null
            var cycleCountConfidence = Confidence.UNAVAILABLE
            var healthPercentage: Int? = null
            var healthPercentageConfidence = Confidence.UNAVAILABLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // BATTERY_PROPERTY_CYCLE_COUNT = 6, added in API 34
                val cycles = batteryManager?.getIntProperty(6)
                if (cycles != null && cycles != Int.MIN_VALUE && cycles >= 0) {
                    cycleCount = cycles
                    cycleCountConfidence = Confidence.HIGH
                }
            }

            _state.value =
                _state.value.copy(
                    health =
                        _state.value.health.copy(
                            cycleCount = cycleCount,
                            cycleCountConfidence = cycleCountConfidence,
                            healthPercentage = healthPercentage,
                            healthPercentageConfidence = healthPercentageConfidence,
                        ),
                )
        }

        // ── Manufacturer detection ──────────────────────────────────────────────────

        private fun detectManufacturer() {
            val manufacturer = Build.MANUFACTURER.lowercase()
            val profile =
                when {
                    manufacturer.contains("samsung") -> ManufacturerProfile.SAMSUNG
                    manufacturer.contains("oneplus") -> ManufacturerProfile.ONEPLUS
                    manufacturer.contains("google") -> ManufacturerProfile.GOOGLE_PIXEL
                    else -> ManufacturerProfile.GENERIC
                }

            val notes =
                when (profile) {
                    ManufacturerProfile.SAMSUNG ->
                        listOf(
                            context.getString(R.string.batt_mfr_samsung_note1),
                            context.getString(R.string.batt_mfr_samsung_note2),
                        )
                    ManufacturerProfile.ONEPLUS ->
                        listOf(
                            context.getString(R.string.batt_mfr_oneplus_note1),
                            context.getString(R.string.batt_mfr_oneplus_note2),
                        )
                    ManufacturerProfile.GOOGLE_PIXEL ->
                        listOf(
                            context.getString(R.string.batt_mfr_pixel_note1),
                        )
                    ManufacturerProfile.GENERIC -> emptyList()
                }

            _state.value =
                _state.value.copy(
                    manufacturer =
                        ManufacturerState(
                            profile = profile,
                            manufacturerName = Build.MANUFACTURER,
                            notes = notes,
                        ),
                )
        }

        // ── Label helpers ───────────────────────────────────────────────────────────

        fun getHealthLabel(health: Int): Int =
            when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> R.string.batt_health_good
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.string.batt_health_overheat
                BatteryManager.BATTERY_HEALTH_DEAD -> R.string.batt_health_dead
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> R.string.batt_health_over_voltage
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> R.string.batt_health_failure
                BatteryManager.BATTERY_HEALTH_COLD -> R.string.batt_health_cold
                else -> R.string.batt_health_unknown
            }

        fun getChargingStatusLabel(status: Int): Int =
            when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> R.string.batt_status_charging
                BatteryManager.BATTERY_STATUS_DISCHARGING -> R.string.batt_status_discharging
                BatteryManager.BATTERY_STATUS_FULL -> R.string.batt_status_full
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> R.string.batt_status_not_charging
                else -> R.string.batt_status_unknown
            }

        fun getPlugTypeLabel(plugged: Int): Int =
            when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> R.string.batt_plug_ac
                BatteryManager.BATTERY_PLUGGED_USB -> R.string.batt_plug_usb
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> R.string.batt_plug_wireless
                else -> R.string.batt_plug_none
            }

        // ── Cleanup ─────────────────────────────────────────────────────────────────

        override fun onCleared() {
            super.onCleared()
            batteryReceiver?.let {
                try {
                    context.unregisterReceiver(it)
                } catch (_: Exception) {
                }
            }
        }
    }
