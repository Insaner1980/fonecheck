package com.insaner.fonecheck.ui.screens.thermal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.domain.observation.DeviceObservation
import com.insaner.fonecheck.domain.observation.DeviceObservationClassifier
import com.insaner.fonecheck.localization.thermalStatusStringRes
import com.insaner.fonecheck.ui.TopBarActionRegistry
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ObservationReasonNote
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.RegisterRefreshTopBarAction
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.ThermalHeadroomGauge
import com.insaner.fonecheck.ui.components.WindowFigure
import com.insaner.fonecheck.ui.components.WindowLabel
import com.insaner.fonecheck.ui.components.confidenceLabel
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.toSemanticTone

@Composable
fun ThermalTestScreen(
    modifier: Modifier = Modifier,
    topBarActionRegistry: TopBarActionRegistry = TopBarActionRegistry.NoOp,
    viewModel: ThermalTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RegisterRefreshTopBarAction(
        contentDescriptionResId = R.string.thermal_refresh,
        enabled = true,
        onRefresh = viewModel::refresh,
        topBarActionRegistry = topBarActionRegistry,
    )
    ThermalMonitoringEffect(
        onStartMonitoring = viewModel::startMonitoring,
        onStopMonitoring = viewModel::stopMonitoring,
    )

    TestScreenContent(
        modifier = modifier,
        liveStateUpdatedAtEpochMillis = state.capturedAt?.toEpochMilli(),
    ) {
        // The instrument leads. Both readings are windows, so the screen opens with what it
        // measured; the rows and the caveats that qualify them follow underneath.
        item { ThermalHeadroomSection(state) }
        item { ThermalBatterySection(state) }
        item { ThermalStatusSection(state) }

        state.error?.let { error ->
            item {
                Note(
                    text = thermalErrorLabel(error),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                )
            }
        }

        item {
            ThermalSection(label = stringResource(R.string.thermal_observation_title)) {
                Note(stringResource(R.string.thermal_observation_note))
                HairlineRule()
            }
        }
    }
}

@Composable
private fun ThermalStatusSection(state: ThermalTestState) {
    val classification =
        DeviceObservationClassifier.classify(DeviceObservation.Thermal(state.status))
    ThermalSection(
        label = stringResource(R.string.thermal_status_title),
        trailing = confidenceLabel(state.statusConfidence),
    ) {
        DataRow(
            label = stringResource(R.string.thermal_status_label),
            value =
                state.status
                    .takeUnless { it == ThermalStatusCode.UNAVAILABLE }
                    ?.let { stringResource(thermalStatusStringRes(it)) },
            tone = classification.toSemanticTone(),
        )
        DataRow(
            label = stringResource(R.string.thermal_severity_label),
            value =
                state.severity
                    .takeUnless { it == ThermalSeverityCode.UNAVAILABLE }
                    ?.let { thermalSeverityLabel(it) },
            tone = classification.toSemanticTone(),
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
        ObservationReasonNote(classification)
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
    val headroom = state.headroom?.takeIf { state.headroomApiSupported }
    val unavailable = stringResource(R.string.value_unavailable_short)
    val reading =
        headroom?.let { stringResource(R.string.thermal_headroom_value, uiNumber(it, 2, 2)) }
    ThermalSection(
        label = stringResource(R.string.thermal_headroom_title),
        trailing = confidenceLabel(state.headroomConfidence),
    ) {
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        ReadoutWindow {
            WindowLabel(text = stringResource(R.string.thermal_headroom_current))
            ThermalHeadroomGauge(headroom = headroom)
            // The dial is hidden from screen readers; this figure is the reading they get.
            WindowFigure(
                value = reading ?: unavailable,
                // Over the threshold the arc has already turned; the figure follows it.
                alert = headroom != null && headroom > HEADROOM_THRESHOLD,
            )
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        if (!state.headroomApiSupported) {
            Note(stringResource(R.string.thermal_headroom_requires_api30))
        }
        Note(stringResource(R.string.thermal_headroom_note))
        HairlineRule()
    }
}

/** A headroom of 1.0 is the device's severe-throttling threshold. */
private const val HEADROOM_THRESHOLD = 1f

@Composable
private fun ThermalBatterySection(state: ThermalTestState) {
    ThermalSection(
        label = stringResource(R.string.thermal_battery_title),
        trailing = confidenceLabel(state.batteryTemperatureConfidence),
    ) {
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        ReadoutWindow {
            WindowLabel(text = stringResource(R.string.thermal_battery_temperature))
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            WindowFigure(
                value =
                    state.batteryTemperatureCelsius?.let {
                        stringResource(R.string.batt_value_celsius, uiNumber(it, 1, 1))
                    } ?: stringResource(R.string.value_unavailable_short),
            )
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        Note(stringResource(R.string.thermal_battery_note))
        HairlineRule()
    }
}

@Composable
private fun ThermalSection(
    label: String,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        SectionHeader(label = label, trailing = trailing)
        content()
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

@Composable
private fun thermalErrorLabel(error: ThermalErrorCode): String =
    stringResource(
        when (error) {
            ThermalErrorCode.STATUS_UNAVAILABLE -> R.string.thermal_status_read_failed
            ThermalErrorCode.LISTENER_REGISTRATION_FAILED -> R.string.thermal_listener_failed
        },
    )
