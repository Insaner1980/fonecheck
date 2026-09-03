package com.insaner.fonecheck.ui.screens.battery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.observation.DeviceObservation
import com.insaner.fonecheck.domain.observation.DeviceObservationClassifier
import com.insaner.fonecheck.ui.classification.classifyBatteryHealth
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ObservationReasonNote
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.WindowBar
import com.insaner.fonecheck.ui.components.WindowFigure
import com.insaner.fonecheck.ui.components.WindowLabel
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme

@Composable
fun BatteryTestScreen(
    modifier: Modifier = Modifier,
    viewModel: BatteryTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val liveStateUpdatedAtEpochMillis = remember(state) { System.currentTimeMillis() }
    val chargingStatusLabel = stringResource(viewModel.getChargingStatusLabel(state.charging.status))
    val plugTypeLabel = stringResource(viewModel.getPlugTypeLabel(state.charging.plugType))

    TestScreenContent(
        modifier = modifier,
        liveStateUpdatedAtEpochMillis = liveStateUpdatedAtEpochMillis,
    ) {
        item {
            BatterySection(label = stringResource(R.string.batt_basic_title)) {
                // Charge is the one figure this screen exists to report, and it is a bounded
                // percentage, so it gets the window and the bar.
                BatteryLevelReadout(state.basic.level)
                BasicDetails(state.basic)
            }
        }
        item {
            BatterySection(label = stringResource(R.string.batt_charging_title)) {
                ChargingDetails(state.charging, chargingStatusLabel, plugTypeLabel)
            }
        }
        item {
            BatterySection(label = stringResource(R.string.batt_health_title)) {
                HealthDetails(state.health)
            }
        }
        item {
            BatterySection(label = stringResource(R.string.batt_manufacturer_title)) {
                ManufacturerDetails(state.manufacturer)
            }
        }
    }
}

@Composable
private fun BatteryLevelReadout(level: Int?) {
    Column {
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        ReadoutWindow {
            WindowLabel(text = stringResource(R.string.batt_level))
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            WindowFigure(
                value =
                    level?.let { stringResource(R.string.batt_value_percent, it) }
                        ?: stringResource(R.string.value_unavailable_short),
            )
            level?.let {
                Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
                WindowBar(percentage = it)
            }
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
    }
}

@Composable
private fun BasicDetails(basic: BasicBatteryState) {
    val temperatureClassification =
        DeviceObservationClassifier.classify(
            DeviceObservation.BatteryTemperature(basic.temperatureCelsius),
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
        showDivider = false,
    )
    ObservationReasonNote(temperatureClassification)
    HairlineRule()
    DataRow(
        label = stringResource(R.string.batt_technology),
        value = basic.technology,
    )
}

@Composable
private fun ChargingDetails(
    charging: ChargingState,
    chargingStatusLabel: String,
    plugTypeLabel: String,
) {
    DataRow(
        label = stringResource(R.string.batt_charging_status),
        value = chargingStatusLabel,
    )
    DataRow(
        label = stringResource(R.string.batt_plug_type),
        value = plugTypeLabel,
    )
    DataRow(
        label = stringResource(R.string.batt_charging_current),
        value =
            charging.chargingCurrentMa?.let {
                stringResource(R.string.batt_value_milliamps, uiNumber(it, 1, 1))
            },
        confidence = charging.chargingCurrentConfidence.takeIf { charging.chargingCurrentMa != null },
        showDivider = charging.chargingCurrentMa == null,
    )
    charging.chargingCurrentMa?.let {
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
    val classification = classifyBatteryHealth(health.healthStatusRaw)
    DataRow(
        label = stringResource(R.string.batt_health_status),
        value = stringResource(health.healthStatusLabel),
        showDivider = false,
    )
    ObservationReasonNote(classification)
    Note(stringResource(R.string.batt_health_source_note))
    HairlineRule()

    DataRow(
        label = stringResource(R.string.batt_cycle_count),
        value = health.cycleCount?.let { uiNumber(it) },
        confidence = health.cycleCountConfidence.takeIf { health.cycleCountSupported && health.cycleCount != null },
        showDivider = health.cycleCountSupported,
    )
    if (!health.cycleCountSupported) {
        Note(stringResource(R.string.batt_requires_api34))
        HairlineRule()
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
        confidence = manufacturer.profileConfidence,
        showDivider = false,
    )
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
