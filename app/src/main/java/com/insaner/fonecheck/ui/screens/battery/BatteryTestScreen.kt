package com.insaner.fonecheck.ui.screens.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.ui.components.ConfidenceBadge
import com.insaner.fonecheck.ui.components.DetailInfoRow
import com.insaner.fonecheck.ui.components.SectionBox
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.TestSectionCard
import com.insaner.fonecheck.ui.theme.Blue400
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.JetBrainsMono
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400
import com.insaner.fonecheck.ui.theme.readableStatusColor

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun BatteryTestScreen(
    modifier: Modifier = Modifier,
    viewModel: BatteryTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    TestScreenContent(modifier = modifier) {
        // Basic Info
        item {
            val healthLabel = stringResource(state.health.healthStatusLabel)
            val batteryLevel = state.basic.level
            val levelText =
                batteryLevel?.let { stringResource(R.string.batt_value_percent, it) }
                    ?: stringResource(R.string.device_value_unavailable)
            TestSectionCard(
                icon = stringResource(R.string.batt_icon_basic),
                title = stringResource(R.string.batt_basic_title),
                statusText = "$levelText \u2022 $healthLabel",
                statusColor =
                    when {
                        batteryLevel == null -> Neutral500
                        batteryLevel > 50 -> Green400
                        batteryLevel > 20 -> Yellow400
                        else -> Red400
                    },
                isExpanded = state.expandedSection == BatterySection.BASIC,
                onClick = { viewModel.toggleSection(BatterySection.BASIC) },
            ) {
                BasicDetails(state.basic, viewModel)
            }
        }

        // Charging
        item {
            TestSectionCard(
                icon = stringResource(R.string.batt_icon_charging),
                title = stringResource(R.string.batt_charging_title),
                statusText = stringResource(viewModel.getChargingStatusLabel(state.charging.status)),
                statusColor =
                    when {
                        state.basic.isCharging -> Green400
                        else -> Neutral500
                    },
                isExpanded = state.expandedSection == BatterySection.CHARGING,
                onClick = { viewModel.toggleSection(BatterySection.CHARGING) },
            ) {
                ChargingDetails(state.charging, viewModel)
            }
        }

        // Health
        item {
            TestSectionCard(
                icon = stringResource(R.string.batt_icon_health),
                title = stringResource(R.string.batt_health_title),
                statusText = stringResource(state.health.healthStatusLabel),
                statusColor =
                    when (state.health.healthStatusRaw) {
                        android.os.BatteryManager.BATTERY_HEALTH_GOOD -> Green400
                        android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN -> Neutral500
                        else -> Red400
                    },
                isExpanded = state.expandedSection == BatterySection.HEALTH,
                onClick = { viewModel.toggleSection(BatterySection.HEALTH) },
            ) {
                HealthDetails(state.health)
            }
        }

        // Manufacturer
        item {
            TestSectionCard(
                icon = stringResource(R.string.batt_icon_manufacturer),
                title = stringResource(R.string.batt_manufacturer_title),
                statusText = state.manufacturer.manufacturerName,
                statusColor = Blue400,
                isExpanded = state.expandedSection == BatterySection.MANUFACTURER,
                onClick = { viewModel.toggleSection(BatterySection.MANUFACTURER) },
            ) {
                ManufacturerDetails(state.manufacturer)
            }
        }
    }
}

@Composable
private fun InfoRowWithConfidence(
    label: String,
    value: String,
    confidence: Confidence,
    valueColor: Color? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.weight(1.5f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Medium,
                    ),
                color = readableStatusColor(valueColor ?: MaterialTheme.colorScheme.onSurface),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(6.dp))
            ConfidenceBadge(confidence = confidence)
        }
    }
}

// ── Basic Details ───────────────────────────────────────────────────────────────

@Composable
private fun BasicDetails(
    basic: BasicBatteryState,
    viewModel: BatteryTestViewModel,
) {
    SectionBox {
        val level = basic.level
        DetailInfoRow(
            label = stringResource(R.string.batt_level),
            value =
                level?.let { stringResource(R.string.batt_value_percent, it) }
                    ?: stringResource(R.string.device_value_unavailable),
            valueColor =
                when {
                    level == null -> Neutral500
                    level > 50 -> Green400
                    level > 20 -> Yellow400
                    else -> Red400
                },
        )
        DetailInfoRow(
            label = stringResource(R.string.batt_voltage),
            value =
                basic.voltageMv?.let { stringResource(R.string.batt_value_millivolts, it) }
                    ?: stringResource(R.string.device_value_unavailable),
        )
        val temperature = basic.temperatureCelsius
        DetailInfoRow(
            label = stringResource(R.string.batt_temperature),
            value =
                temperature?.let { stringResource(R.string.batt_value_celsius, it) }
                    ?: stringResource(R.string.device_value_unavailable),
            valueColor =
                when {
                    temperature == null -> Neutral500
                    temperature < 35f -> Green400
                    temperature < 45f -> Yellow400
                    else -> Red400
                },
        )
        DetailInfoRow(
            label = stringResource(R.string.batt_health_label),
            value = stringResource(viewModel.getHealthLabel(basic.healthStatus)),
            valueColor =
                when (basic.healthStatus) {
                    android.os.BatteryManager.BATTERY_HEALTH_GOOD -> Green400
                    android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN -> Neutral500
                    else -> Red400
                },
        )
        basic.technology?.let {
            DetailInfoRow(
                label = stringResource(R.string.batt_technology),
                value = it,
            )
        }
    }
}

// ── Charging Details ────────────────────────────────────────────────────────────

@Composable
private fun ChargingDetails(
    charging: ChargingState,
    viewModel: BatteryTestViewModel,
) {
    SectionBox {
        DetailInfoRow(
            label = stringResource(R.string.batt_charging_status),
            value = stringResource(viewModel.getChargingStatusLabel(charging.status)),
        )
        DetailInfoRow(
            label = stringResource(R.string.batt_plug_type),
            value = stringResource(viewModel.getPlugTypeLabel(charging.plugType)),
        )
        if (charging.chargingCurrentMa != null) {
            InfoRowWithConfidence(
                label = stringResource(R.string.batt_charging_current),
                value = stringResource(R.string.batt_value_milliamps, charging.chargingCurrentMa),
                confidence = charging.chargingCurrentConfidence,
            )
            DetailInfoRow(
                label = stringResource(R.string.batt_current_direction),
                value = currentDirectionLabel(charging.currentDirection),
            )
            Text(
                text =
                    stringResource(
                        if (charging.currentSignNormalized) {
                            R.string.batt_current_sign_normalized
                        } else {
                            R.string.batt_current_caveat
                        },
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        } else {
            InfoRowWithConfidence(
                label = stringResource(R.string.batt_charging_current),
                value = stringResource(R.string.device_value_unavailable),
                confidence = Confidence.UNAVAILABLE,
            )
        }
        charging.manufacturerNote?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = Yellow400,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// ── Health Details ──────────────────────────────────────────────────────────────

@Composable
private fun HealthDetails(health: HealthState) {
    SectionBox {
        DetailInfoRow(
            label = stringResource(R.string.batt_health_status),
            value = stringResource(health.healthStatusLabel),
            valueColor =
                when (health.healthStatusRaw) {
                    android.os.BatteryManager.BATTERY_HEALTH_GOOD -> Green400
                    android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN -> Neutral500
                    else -> Red400
                },
        )

        if (health.cycleCountSupported) {
            if (health.cycleCount != null) {
                InfoRowWithConfidence(
                    label = stringResource(R.string.batt_cycle_count),
                    value = health.cycleCount.toString(),
                    confidence = health.cycleCountConfidence,
                )
            } else {
                InfoRowWithConfidence(
                    label = stringResource(R.string.batt_cycle_count),
                    value = stringResource(R.string.device_value_unavailable),
                    confidence = Confidence.UNAVAILABLE,
                )
            }
        } else {
            InfoRowWithConfidence(
                label = stringResource(R.string.batt_cycle_count),
                value = stringResource(R.string.batt_requires_api34),
                confidence = Confidence.UNAVAILABLE,
            )
        }
    }
}

// ── Manufacturer Details ────────────────────────────────────────────────────────

@Composable
private fun ManufacturerDetails(manufacturer: ManufacturerState) {
    SectionBox {
        DetailInfoRow(
            label = stringResource(R.string.batt_mfr_name),
            value = manufacturer.manufacturerName,
        )
        InfoRowWithConfidence(
            label = stringResource(R.string.batt_mfr_profile),
            value =
                when (manufacturer.profile) {
                    ManufacturerProfile.SAMSUNG -> stringResource(R.string.batt_mfr_samsung)
                    ManufacturerProfile.ONEPLUS -> stringResource(R.string.batt_mfr_oneplus)
                    ManufacturerProfile.GOOGLE_PIXEL -> stringResource(R.string.batt_mfr_pixel)
                    ManufacturerProfile.GENERIC -> stringResource(R.string.batt_mfr_generic)
                },
            confidence = manufacturer.profileConfidence,
        )
        manufacturer.notes.forEach { note ->
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = Yellow400,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun currentDirectionLabel(direction: BatteryCurrentDirection): String =
    stringResource(
        when (direction) {
            BatteryCurrentDirection.CHARGING -> R.string.batt_current_direction_charging
            BatteryCurrentDirection.DISCHARGING -> R.string.batt_current_direction_discharging
            BatteryCurrentDirection.IDLE -> R.string.batt_current_direction_idle
        },
    )
