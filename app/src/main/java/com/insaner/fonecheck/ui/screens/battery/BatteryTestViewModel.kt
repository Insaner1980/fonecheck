package com.insaner.fonecheck.ui.screens.battery

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// ── State classes ────────────────────────────────────────────────────────────────

data class BasicBatteryState(
    val level: Int? = null,
    val voltageMv: Int? = null,
    val temperatureCelsius: Float? = null,
    val healthStatus: Int = BatteryManager.BATTERY_HEALTH_UNKNOWN,
    val technology: String? = null,
    val isCharging: Boolean = false,
)

data class ChargingState(
    val status: Int = BatteryManager.BATTERY_STATUS_UNKNOWN,
    val plugType: Int = 0,
    val chargingCurrentMa: Double? = null,
    val currentDirection: BatteryCurrentDirection = BatteryCurrentDirection.IDLE,
    val currentSignNormalized: Boolean = false,
    val chargingCurrentConfidence: Confidence = Confidence.UNAVAILABLE,
    val manufacturerNote: String? = null,
)

data class HealthState(
    val healthStatusRaw: Int = BatteryManager.BATTERY_HEALTH_UNKNOWN,
    val healthStatusLabel: Int = R.string.batt_health_unknown,
    val cycleCount: Int? = null,
    val cycleCountSupported: Boolean = false,
    val cycleCountConfidence: Confidence = Confidence.UNAVAILABLE,
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
    val profileConfidence: Confidence = Confidence.LOW,
    val notes: List<String> = emptyList(),
)

enum class BatterySection {
    BASIC,
    CHARGING,
    HEALTH,
    MANUFACTURER,
}

data class BatteryTestState(
    val basic: BasicBatteryState = BasicBatteryState(),
    val charging: ChargingState = ChargingState(),
    val health: HealthState = HealthState(),
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
        val state: StateFlow<BatteryTestState> = _state.asStateFlow()

        private var batteryReceiver: BroadcastReceiver? = null

        init {
            detectManufacturer()
            registerBatteryReceiver()
        }

        fun toggleSection(section: BatterySection) {
            _state.update { current ->
                current.copy(
                    expandedSection = if (current.expandedSection == section) null else section,
                )
            }
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
            val stickyIntent =
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED,
                )
            stickyIntent?.let { parseBatteryIntent(it) }
        }

        @Suppress("InlinedApi")
        private fun parseBatteryIntent(intent: Intent) {
            val level =
                intent
                    .getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    .takeIf { intent.hasExtra(BatteryManager.EXTRA_LEVEL) }
            val scale =
                intent
                    .getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    .takeIf { intent.hasExtra(BatteryManager.EXTRA_SCALE) }
            val levelPercent = BatteryLevelNormalizer.normalize(level, scale)
            val voltage =
                intent
                    .getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                    .takeIf { intent.hasExtra(BatteryManager.EXTRA_VOLTAGE) && it > 0 }
            val temperatureCelsius =
                intent
                    .getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                    .takeIf { intent.hasExtra(BatteryManager.EXTRA_TEMPERATURE) }
                    ?.div(10.0f)
            val health =
                intent.getIntExtra(
                    BatteryManager.EXTRA_HEALTH,
                    BatteryManager.BATTERY_HEALTH_UNKNOWN,
                )
            val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf(String::isNotBlank)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val isCharging =
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val cycleCountSupported = BatteryCycleCountNormalizer.isSupported(Build.VERSION.SDK_INT)
            val rawCycleCount =
                if (cycleCountSupported && intent.hasExtra(BatteryManager.EXTRA_CYCLE_COUNT)) {
                    intent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
                } else {
                    null
                }
            val cycleCount = BatteryCycleCountNormalizer.normalize(Build.VERSION.SDK_INT, rawCycleCount)

            _state.update { current ->
                current.copy(
                    basic =
                        BasicBatteryState(
                            level = levelPercent,
                            voltageMv = voltage,
                            temperatureCelsius = temperatureCelsius,
                            healthStatus = health,
                            technology = technology,
                            isCharging = isCharging,
                        ),
                    charging =
                        current.charging.copy(
                            status = status,
                            plugType = plugged,
                        ),
                    health =
                        current.health.copy(
                            healthStatusRaw = health,
                            healthStatusLabel = getHealthLabel(health),
                            cycleCount = cycleCount,
                            cycleCountSupported = cycleCountSupported,
                            cycleCountConfidence =
                                if (cycleCount != null) Confidence.HIGH else Confidence.UNAVAILABLE,
                        ),
                )
            }

            refreshChargingCurrent(status.toBatteryFlowStatus())
        }

        // ── Charging current ────────────────────────────────────────────────────────

        private fun refreshChargingCurrent(status: BatteryFlowStatus) {
            val rawMicroAmps = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            val reading = BatteryCurrentNormalizer.normalize(rawMicroAmps, status)
            _state.update { current ->
                val profile = current.manufacturer.profile
                val note =
                    when (profile) {
                        ManufacturerProfile.SAMSUNG ->
                            context.getString(R.string.batt_mfr_samsung_current_note)
                        ManufacturerProfile.ONEPLUS ->
                            context.getString(R.string.batt_mfr_oneplus_current_note)
                        ManufacturerProfile.GOOGLE_PIXEL ->
                            context.getString(R.string.batt_mfr_pixel_current_note)
                        else -> null
                    }
                current.copy(
                    charging =
                        current.charging.copy(
                            chargingCurrentMa = reading?.magnitudeMa,
                            currentDirection = reading?.direction ?: BatteryCurrentDirection.IDLE,
                            currentSignNormalized = reading?.signNormalized ?: false,
                            chargingCurrentConfidence =
                                if (reading != null) {
                                    BatteryManufacturerPolicy.currentConfidence(profile)
                                } else {
                                    Confidence.UNAVAILABLE
                                },
                            manufacturerNote = note.takeIf { reading != null },
                        ),
                )
            }
        }

        // ── Manufacturer detection ──────────────────────────────────────────────────

        private fun detectManufacturer() {
            val profile = BatteryManufacturerPolicy.profileFor(Build.MANUFACTURER)

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

            _state.update { current ->
                current.copy(
                    manufacturer =
                        ManufacturerState(
                            profile = profile,
                            manufacturerName = Build.MANUFACTURER,
                            profileConfidence = BatteryManufacturerPolicy.currentConfidence(profile),
                            notes = notes,
                        ),
                )
            }
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
            batteryReceiver?.let {
                try {
                    context.unregisterReceiver(it)
                } catch (_: IllegalArgumentException) {
                    // The receiver was already unregistered.
                }
            }
            batteryReceiver = null
        }

        private fun Int.toBatteryFlowStatus(): BatteryFlowStatus =
            when (this) {
                BatteryManager.BATTERY_STATUS_CHARGING -> BatteryFlowStatus.CHARGING
                BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryFlowStatus.DISCHARGING
                BatteryManager.BATTERY_STATUS_FULL,
                BatteryManager.BATTERY_STATUS_NOT_CHARGING,
                -> BatteryFlowStatus.IDLE

                else -> BatteryFlowStatus.UNKNOWN
            }
    }
