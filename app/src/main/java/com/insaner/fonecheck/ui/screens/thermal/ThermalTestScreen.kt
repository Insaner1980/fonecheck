package com.insaner.fonecheck.ui.screens.thermal

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.localization.thermalStatusStringRes
import com.insaner.fonecheck.ui.components.InfoCard
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400

@Composable
fun ThermalTestScreen(
    modifier: Modifier = Modifier,
    viewModel: ThermalTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ThermalMonitoringEffect(
        onStartMonitoring = viewModel::startMonitoring,
        onStopMonitoring = viewModel::stopMonitoring,
    )

    TestScreenContent(modifier = modifier) {
        item {
            InfoCard(
                title = stringResource(R.string.thermal_status_title),
                confidence = state.statusConfidence,
            ) {
                InfoRow(
                    label = stringResource(R.string.thermal_status_label),
                    value = stringResource(thermalStatusStringRes(state.status)),
                    valueColor = thermalStatusColor(state.severity),
                )
                InfoRow(
                    label = stringResource(R.string.thermal_severity_label),
                    value = thermalSeverityLabel(state.severity),
                    valueColor = thermalStatusColor(state.severity),
                )
                InfoRow(
                    label = stringResource(R.string.thermal_monitoring_label),
                    value =
                        stringResource(
                            if (state.isMonitoring) {
                                R.string.thermal_monitoring_live
                            } else {
                                R.string.thermal_monitoring_paused
                            },
                        ),
                )
                Text(
                    text =
                        stringResource(
                            if (state.statusApiSupported) {
                                R.string.thermal_status_note
                            } else {
                                R.string.thermal_status_unsupported
                            },
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        state.error?.let { error ->
            item {
                ScreenStateCard(
                    type = ScreenStateType.UNAVAILABLE,
                    message = thermalErrorLabel(error),
                    actionLabel = stringResource(R.string.thermal_refresh),
                    onAction = viewModel::refresh,
                )
            }
        }

        item {
            val headroom = state.headroom
            InfoCard(
                title = stringResource(R.string.thermal_headroom_title),
                confidence = state.headroomConfidence,
            ) {
                InfoRow(
                    label = stringResource(R.string.thermal_headroom_current),
                    value =
                        when {
                            !state.headroomApiSupported ->
                                stringResource(R.string.thermal_headroom_requires_api30)
                            headroom != null ->
                                stringResource(R.string.thermal_headroom_value, headroom)
                            else -> stringResource(R.string.device_value_unavailable)
                        },
                )
                Text(
                    text = stringResource(R.string.thermal_headroom_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        item {
            InfoCard(
                title = stringResource(R.string.thermal_battery_title),
                confidence = state.batteryTemperatureConfidence,
            ) {
                InfoRow(
                    label = stringResource(R.string.thermal_battery_temperature),
                    value =
                        state.batteryTemperatureCelsius?.let {
                            stringResource(R.string.batt_value_celsius, it)
                        } ?: stringResource(R.string.device_value_unavailable),
                )
                Text(
                    text = stringResource(R.string.thermal_battery_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        item {
            InfoCard(title = stringResource(R.string.thermal_observation_title)) {
                Text(
                    text = stringResource(R.string.thermal_observation_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Button(
                onClick = viewModel::refresh,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(stringResource(R.string.thermal_refresh))
            }
        }
    }
}

@Composable
private fun thermalSeverityLabel(severity: ThermalSeverityCode): String =
    stringResource(
        when (severity) {
            ThermalSeverityCode.NORMAL -> R.string.thermal_severity_normal
            ThermalSeverityCode.LIGHT -> R.string.perf_thermal_light
            ThermalSeverityCode.MODERATE -> R.string.perf_thermal_moderate
            ThermalSeverityCode.SEVERE -> R.string.perf_thermal_severe
            ThermalSeverityCode.CRITICAL -> R.string.perf_thermal_critical
            ThermalSeverityCode.UNAVAILABLE -> R.string.device_value_unavailable
        },
    )

private fun thermalStatusColor(severity: ThermalSeverityCode): Color =
    when (severity) {
        ThermalSeverityCode.NORMAL -> Green400
        ThermalSeverityCode.LIGHT,
        ThermalSeverityCode.MODERATE,
        -> Yellow400

        ThermalSeverityCode.SEVERE,
        ThermalSeverityCode.CRITICAL,
        -> Red400

        ThermalSeverityCode.UNAVAILABLE -> Neutral500
    }

@Composable
private fun thermalErrorLabel(error: ThermalErrorCode): String =
    stringResource(
        when (error) {
            ThermalErrorCode.STATUS_UNAVAILABLE -> R.string.thermal_status_read_failed
            ThermalErrorCode.LISTENER_REGISTRATION_FAILED -> R.string.thermal_listener_failed
        },
    )
