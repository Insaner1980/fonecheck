package com.insaner.fonecheck.ui.screens.thermal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.localization.thermalStatusStringRes
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FonecheckTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.lg),
    ) {
        ThermalStatusSection(state)

        state.error?.let { error ->
            Note(
                text = thermalErrorLabel(error),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
            )
        }

        ThermalHeadroomSection(state)
        ThermalBatterySection(state)

        ThermalSection(label = stringResource(R.string.thermal_observation_title)) {
            Note(stringResource(R.string.thermal_observation_note))
            HairlineRule()
        }

        PrimaryButton(
            label = stringResource(R.string.thermal_refresh),
            onClick = viewModel::refresh,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ThermalStatusSection(state: ThermalTestState) {
    ThermalSection(
        label = stringResource(R.string.thermal_status_title),
        trailing =
            state.capturedAt?.let {
                stringResource(R.string.device_captured_at, formatMeasurementTime(it))
            },
    ) {
        DataRow(
            label = stringResource(R.string.thermal_status_label),
            value =
                state.status
                    .takeUnless { it == ThermalStatusCode.UNAVAILABLE }
                    ?.let { stringResource(thermalStatusStringRes(it)) },
            tone = thermalStatusTone(state.severity),
        )
        DataRow(
            label = stringResource(R.string.thermal_severity_label),
            value =
                state.severity
                    .takeUnless { it == ThermalSeverityCode.UNAVAILABLE }
                    ?.let { thermalSeverityLabel(it) },
            tone = thermalStatusTone(state.severity),
        )
        DataRow(
            label = stringResource(R.string.thermal_monitoring_label),
            value =
                stringResource(
                    if (state.isMonitoring) {
                        R.string.thermal_monitoring_live
                    } else {
                        R.string.thermal_monitoring_paused
                    },
                ),
            showDivider = false,
        )
        Note(confidenceLabel(state.statusConfidence))
        Note(
            stringResource(
                if (state.statusApiSupported) {
                    R.string.thermal_status_note
                } else {
                    R.string.thermal_status_unsupported
                },
            ),
        )
        HairlineRule()
    }
}

@Composable
private fun ThermalHeadroomSection(state: ThermalTestState) {
    ThermalSection(
        label = stringResource(R.string.thermal_headroom_title),
        trailing = confidenceLabel(state.headroomConfidence),
    ) {
        DataRow(
            label = stringResource(R.string.thermal_headroom_current),
            value =
                state.headroom?.takeIf { state.headroomApiSupported }?.let {
                    stringResource(R.string.thermal_headroom_value, it)
                },
            showDivider = false,
        )
        if (!state.headroomApiSupported) {
            Note(stringResource(R.string.thermal_headroom_requires_api30))
        }
        Note(stringResource(R.string.thermal_headroom_note))
        HairlineRule()
    }
}

@Composable
private fun ThermalBatterySection(state: ThermalTestState) {
    ThermalSection(
        label = stringResource(R.string.thermal_battery_title),
        trailing = confidenceLabel(state.batteryTemperatureConfidence),
    ) {
        DataRow(
            label = stringResource(R.string.thermal_battery_temperature),
            value =
                state.batteryTemperatureCelsius?.let {
                    stringResource(R.string.batt_value_celsius, it)
                },
            showDivider = false,
        )
        Note(stringResource(R.string.thermal_battery_note))
        HairlineRule()
    }
}

@Composable
private fun ThermalSection(
    label: String,
    trailing: String? = null,
    content: @Composable () -> Unit,
) {
    Column {
        SectionHeader(label = label, trailing = trailing)
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

private fun thermalStatusTone(severity: ThermalSeverityCode): SemanticTone =
    when (severity) {
        ThermalSeverityCode.NORMAL -> SemanticTone.PASS
        ThermalSeverityCode.LIGHT,
        ThermalSeverityCode.MODERATE,
        -> SemanticTone.ATTENTION

        ThermalSeverityCode.SEVERE,
        ThermalSeverityCode.CRITICAL,
        -> SemanticTone.FAIL

        ThermalSeverityCode.UNAVAILABLE -> SemanticTone.NEUTRAL
    }

@Composable
private fun thermalErrorLabel(error: ThermalErrorCode): String =
    stringResource(
        when (error) {
            ThermalErrorCode.STATUS_UNAVAILABLE -> R.string.thermal_status_read_failed
            ThermalErrorCode.LISTENER_REGISTRATION_FAILED -> R.string.thermal_listener_failed
        },
    )

private val measurementTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm", Locale.ROOT)

private fun formatMeasurementTime(
    value: Instant,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = measurementTimeFormatter.withZone(zoneId).format(value)
