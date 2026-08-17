package com.insaner.fonecheck.ui.screens.battery

import android.os.BatteryManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone

@Composable
fun BatteryTestScreen(
    modifier: Modifier = Modifier,
    viewModel: BatteryTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FonecheckTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.lg),
    ) {
        BatterySection(label = stringResource(R.string.batt_basic_title)) {
            BasicDetails(state.basic, viewModel)
        }
        BatterySection(label = stringResource(R.string.batt_charging_title)) {
            ChargingDetails(state.charging, viewModel)
        }
        BatterySection(label = stringResource(R.string.batt_health_title)) {
            HealthDetails(state.health)
        }
        BatterySection(label = stringResource(R.string.batt_manufacturer_title)) {
            ManufacturerDetails(state.manufacturer)
        }
    }
}

@Composable
private fun BasicDetails(
    basic: BasicBatteryState,
    viewModel: BatteryTestViewModel,
) {
    DataRow(
        label = stringResource(R.string.batt_level),
        value = basic.level?.let { stringResource(R.string.batt_value_percent, it) },
    )
    DataRow(
        label = stringResource(R.string.batt_voltage),
        value = basic.voltageMv?.let { stringResource(R.string.batt_value_millivolts, it) },
    )
    DataRow(
        label = stringResource(R.string.batt_temperature),
        value =
            basic.temperatureCelsius?.let {
                stringResource(R.string.batt_value_celsius, uiNumber(it, 1, 1))
            },
        tone = batteryTemperatureTone(basic.temperatureCelsius),
    )
    DataRow(
        label = stringResource(R.string.batt_health_label),
        value = stringResource(viewModel.getHealthLabel(basic.healthStatus)),
        tone = batteryHealthTone(basic.healthStatus),
    )
    DataRow(
        label = stringResource(R.string.batt_technology),
        value = basic.technology,
    )
}

@Composable
private fun ChargingDetails(
    charging: ChargingState,
    viewModel: BatteryTestViewModel,
) {
    DataRow(
        label = stringResource(R.string.batt_charging_status),
        value = stringResource(viewModel.getChargingStatusLabel(charging.status)),
    )
    DataRow(
        label = stringResource(R.string.batt_plug_type),
        value = stringResource(viewModel.getPlugTypeLabel(charging.plugType)),
    )
    DataRow(
        label = stringResource(R.string.batt_charging_current),
        value =
            charging.chargingCurrentMa?.let {
                stringResource(R.string.batt_value_milliamps, uiNumber(it, 1, 1))
            },
        showDivider = charging.chargingCurrentMa == null,
    )
    charging.chargingCurrentMa?.let {
        Note(confidenceLabel(charging.chargingCurrentConfidence))
        DataRow(
            label = stringResource(R.string.batt_current_direction),
            value = currentDirectionLabel(charging.currentDirection),
            showDivider = false,
        )
        Note(
            stringResource(
                if (charging.currentSignNormalized) {
                    R.string.batt_current_sign_normalized
                } else {
                    R.string.batt_current_caveat
                },
            ),
        )
        charging.manufacturerNote?.let { note -> Note(note) }
        HairlineRule()
    }
}

@Composable
private fun HealthDetails(health: HealthState) {
    DataRow(
        label = stringResource(R.string.batt_health_status),
        value = stringResource(health.healthStatusLabel),
        tone = batteryHealthTone(health.healthStatusRaw),
        showDivider = false,
    )
    Note(stringResource(R.string.batt_health_source_note))
    HairlineRule()

    DataRow(
        label = stringResource(R.string.batt_cycle_count),
        value = health.cycleCount?.toString(),
        showDivider = health.cycleCountSupported && health.cycleCount == null,
    )
    if (!health.cycleCountSupported) {
        Note(stringResource(R.string.batt_requires_api34))
        HairlineRule()
    } else {
        health.cycleCount?.let {
            Note(confidenceLabel(health.cycleCountConfidence))
            HairlineRule()
        }
    }
}

@Composable
private fun ManufacturerDetails(manufacturer: ManufacturerState) {
    DataRow(
        label = stringResource(R.string.batt_mfr_name),
        value = manufacturer.manufacturerName.takeIf(String::isNotBlank),
    )
    DataRow(
        label = stringResource(R.string.batt_mfr_profile),
        value = manufacturerProfileLabel(manufacturer.profile),
        showDivider = false,
    )
    Note(confidenceLabel(manufacturer.profileConfidence))
    manufacturer.notes.forEach { note -> Note(note) }
    HairlineRule()
}

@Composable
private fun BatterySection(
    label: String,
    content: @Composable () -> Unit,
) {
    Column {
        SectionHeader(label)
        content()
    }
}

@Composable
private fun confidenceLabel(confidence: Confidence): String =
    stringResource(
        when (confidence) {
            Confidence.HIGH -> R.string.confidence_high
            Confidence.LOW -> R.string.confidence_low
            Confidence.UNAVAILABLE -> R.string.confidence_unavailable
        },
    )

@Composable
private fun manufacturerProfileLabel(profile: ManufacturerProfile): String =
    stringResource(
        when (profile) {
            ManufacturerProfile.SAMSUNG -> R.string.batt_mfr_samsung
            ManufacturerProfile.ONEPLUS -> R.string.batt_mfr_oneplus
            ManufacturerProfile.GOOGLE_PIXEL -> R.string.batt_mfr_pixel
            ManufacturerProfile.GENERIC -> R.string.batt_mfr_generic
        },
    )

@Composable
private fun currentDirectionLabel(direction: BatteryCurrentDirection): String =
    stringResource(
        when (direction) {
            BatteryCurrentDirection.CHARGING -> R.string.batt_current_direction_charging
            BatteryCurrentDirection.DISCHARGING -> R.string.batt_current_direction_discharging
            BatteryCurrentDirection.IDLE -> R.string.batt_current_direction_idle
        },
    )

private fun batteryTemperatureTone(temperatureCelsius: Float?): SemanticTone =
    when {
        temperatureCelsius == null -> SemanticTone.NEUTRAL
        temperatureCelsius < 35f -> SemanticTone.PASS
        temperatureCelsius < 45f -> SemanticTone.ATTENTION
        else -> SemanticTone.FAIL
    }

private fun batteryHealthTone(healthStatus: Int): SemanticTone =
    when (healthStatus) {
        BatteryManager.BATTERY_HEALTH_GOOD -> SemanticTone.PASS
        BatteryManager.BATTERY_HEALTH_UNKNOWN -> SemanticTone.NEUTRAL
        else -> SemanticTone.FAIL
    }
