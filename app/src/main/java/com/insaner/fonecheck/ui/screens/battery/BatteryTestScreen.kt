package com.insaner.fonecheck.ui.screens.battery

import android.os.Build
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
            TestSectionCard(
                icon = "BAT",
                title = stringResource(R.string.batt_basic_title),
                statusText = "${state.basic.level}% \u2022 $healthLabel",
                statusColor =
                    when {
                        state.basic.level > 50 -> Green400
                        state.basic.level > 20 -> Yellow400
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
                icon = "CHG",
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
                icon = "HP",
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

        // Capacity
        item {
            val capacityText =
                state.capacity.designCapacityMah?.let {
                    "${it.toInt()} mAh"
                } ?: stringResource(R.string.confidence_unavailable)
            TestSectionCard(
                icon = "CAP",
                title = stringResource(R.string.batt_capacity_title),
                statusText = capacityText,
                statusColor = if (state.capacity.designCapacityMah != null) Green400 else Neutral500,
                isExpanded = state.expandedSection == BatterySection.CAPACITY,
                onClick = { viewModel.toggleSection(BatterySection.CAPACITY) },
            ) {
                CapacityDetails(state.capacity)
            }
        }

        // Manufacturer
        item {
            TestSectionCard(
                icon = "MFR",
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
                color = valueColor ?: MaterialTheme.colorScheme.onSurface,
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
        DetailInfoRow(
            label = stringResource(R.string.batt_level),
            value = "${basic.level}%",
            valueColor =
                when {
                    basic.level > 50 -> Green400
                    basic.level > 20 -> Yellow400
                    else -> Red400
                },
        )
        DetailInfoRow(
            label = stringResource(R.string.batt_voltage),
            value = "${basic.voltage} mV",
        )
        DetailInfoRow(
            label = stringResource(R.string.batt_temperature),
            value = "%.1f \u00B0C".format(basic.temperatureCelsius),
            valueColor =
                when {
                    basic.temperatureCelsius < 35f -> Green400
                    basic.temperatureCelsius < 45f -> Yellow400
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
                value = "${charging.chargingCurrentMa} mA",
                confidence = charging.chargingCurrentConfidence,
            )
        } else {
            InfoRowWithConfidence(
                label = stringResource(R.string.batt_charging_current),
                value = "\u2014",
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (health.cycleCount != null) {
                InfoRowWithConfidence(
                    label = stringResource(R.string.batt_cycle_count),
                    value = "${health.cycleCount}",
                    confidence = health.cycleCountConfidence,
                )
            } else {
                InfoRowWithConfidence(
                    label = stringResource(R.string.batt_cycle_count),
                    value = "\u2014",
                    confidence = Confidence.UNAVAILABLE,
                )
            }

            if (health.healthPercentage != null) {
                InfoRowWithConfidence(
                    label = stringResource(R.string.batt_health_percentage),
                    value = "${health.healthPercentage}%",
                    confidence = health.healthPercentageConfidence,
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

// ── Capacity Details ────────────────────────────────────────────────────────────

@Composable
private fun CapacityDetails(capacity: CapacityState) {
    SectionBox {
        if (capacity.designCapacityMah != null) {
            InfoRowWithConfidence(
                label = stringResource(R.string.batt_design_capacity),
                value = "${capacity.designCapacityMah.toInt()} mAh",
                confidence = capacity.designCapacityConfidence,
            )
        } else {
            InfoRowWithConfidence(
                label = stringResource(R.string.batt_design_capacity),
                value = "\u2014",
                confidence = Confidence.UNAVAILABLE,
            )
            Text(
                text = stringResource(R.string.batt_capacity_unavailable_note),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
                modifier = Modifier.padding(top = 4.dp),
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
        DetailInfoRow(
            label = stringResource(R.string.batt_mfr_profile),
            value =
                when (manufacturer.profile) {
                    ManufacturerProfile.SAMSUNG -> "Samsung"
                    ManufacturerProfile.ONEPLUS -> "OnePlus"
                    ManufacturerProfile.GOOGLE_PIXEL -> "Google Pixel"
                    ManufacturerProfile.GENERIC -> stringResource(R.string.batt_mfr_generic)
                },
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
