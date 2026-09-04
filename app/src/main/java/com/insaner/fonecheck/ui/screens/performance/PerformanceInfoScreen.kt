package com.insaner.fonecheck.ui.screens.performance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.insaner.fonecheck.domain.model.CpuCoreFrequency
import com.insaner.fonecheck.domain.model.PerformanceBenchmarkResult
import com.insaner.fonecheck.domain.model.PerformanceInfo
import com.insaner.fonecheck.domain.observation.DeviceObservation
import com.insaner.fonecheck.domain.observation.DeviceObservationClassifier
import com.insaner.fonecheck.domain.observation.MeasurementKind
import com.insaner.fonecheck.domain.observation.MeasurementOutcome
import com.insaner.fonecheck.localization.thermalStatusStringRes
import com.insaner.fonecheck.ui.TopBarActionRegistry
import com.insaner.fonecheck.ui.components.CaptureTimestamp
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ObservationReasonNote
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.RegisterRefreshTopBarAction
import com.insaner.fonecheck.ui.components.ScreenLoadingNote
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.WindowBar
import com.insaner.fonecheck.ui.components.WindowFigure
import com.insaner.fonecheck.ui.components.WindowLabel
import com.insaner.fonecheck.ui.components.confidenceLabel
import com.insaner.fonecheck.ui.format.uiFileSize
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme

@Composable
fun PerformanceInfoScreen(
    modifier: Modifier = Modifier,
    topBarActionRegistry: TopBarActionRegistry = TopBarActionRegistry.NoOp,
    viewModel: PerformanceInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RegisterRefreshTopBarAction(
        contentDescriptionResId = R.string.perf_refresh_info,
        enabled = !state.isInfoLoading,
        onRefresh = viewModel::refreshInfo,
        topBarActionRegistry = topBarActionRegistry,
    )

    TestScreenContent(
        modifier = modifier,
        liveStateUpdatedAtEpochMillis = state.info?.capturedAt?.toEpochMilli(),
    ) {
        if (state.isInfoLoading && state.info == null) {
            item { ScreenLoadingNote(message = stringResource(R.string.perf_info_loading)) }
        }
        state.info?.let { info ->
            // The instrument leads: memory is the one reading on this screen that is a share of
            // a fixed capacity, so its window opens the screen and the inventories follow.
            item { RamInfoSection(info) }
            item { CpuInfoSection(info) }
            item { GpuInfoSection(info) }
        }
        state.infoError?.let {
            item {
                Note(
                    text = stringResource(R.string.perf_info_error_description),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                )
            }
        }
        item {
            BenchmarkSection(
                state = state,
                onStart = viewModel::startBenchmark,
                onCancel = viewModel::cancelBenchmark,
            )
        }
    }
}

@Composable
private fun CpuInfoSection(info: PerformanceInfo) {
    val coreClassification = classifyReading(MeasurementKind.CPU, info.cpuCores > 0)
    PerformanceSection(
        label = stringResource(R.string.perf_cpu_title),
        trailing = confidenceLabel(info.cpuConfidence),
    ) {
        LongValueRow(
            label = stringResource(R.string.perf_cpu_model),
            value = performanceValueOrNull(info.cpuModel),
        )
        DataRow(
            label = stringResource(R.string.perf_cpu_architecture),
            value = performanceValueOrNull(info.cpuArchitecture),
        )
        DataRow(
            label = stringResource(R.string.perf_cpu_cores),
            value = info.cpuCores.takeIf { it > 0 }?.let { uiNumber(it) },
            showDivider = coreClassification.reason == null,
        )
        ObservationReasonNote(coreClassification)
        if (coreClassification.reason != null) HairlineRule()
        info.cpuFrequencies.forEach { CoreFrequencyRow(it) }
    }
}

@Composable
private fun RamInfoSection(info: PerformanceInfo) {
    val totalRamClassification = classifyReading(MeasurementKind.RAM, info.totalRamBytes?.let { it > 0L } == true)
    PerformanceSection(
        label = stringResource(R.string.perf_ram_title),
        trailing = confidenceLabel(info.ramConfidence),
    ) {
        RamUsageReadout(total = info.totalRamBytes, available = info.availableRamBytes)
        DataRow(
            label = stringResource(R.string.perf_ram_total),
            value = info.totalRamBytes?.let { uiFileSize(it) },
            showDivider = totalRamClassification.reason == null,
        )
        ObservationReasonNote(totalRamClassification)
        if (totalRamClassification.reason != null) HairlineRule()
        DataRow(
            label = stringResource(R.string.perf_ram_available),
            value = info.availableRamBytes?.let { uiFileSize(it) },
        )
    }
}

/**
 * How much of the device's memory is in use. Both figures come from the same reading, so the share
 * between them is a real proportion rather than two unrelated numbers.
 *
 * Drawn only when both are present: a bar with one end missing would be an invented reading.
 */
@Composable
private fun RamUsageReadout(
    total: Long?,
    available: Long?,
) {
    if (total == null || available == null || total <= 0L) return
    // The bar shows what the label names. A window captioned "available" whose bar filled up as
    // memory ran out would contradict its own figure.
    val availablePercent = ((available.toDouble() / total) * PERCENT).toInt().coerceIn(0, PERCENT)
    Column {
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        ReadoutWindow {
            WindowLabel(text = stringResource(R.string.perf_ram_available))
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            WindowFigure(value = uiFileSize(available))
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            WindowBar(percentage = availablePercent)
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
    }
}

private const val PERCENT = 100

@Composable
private fun GpuInfoSection(info: PerformanceInfo) {
    val rendererClassification =
        classifyReading(
            MeasurementKind.GPU,
            info.glRenderer != PerformanceInfo.UNAVAILABLE && info.glRenderer.isNotBlank(),
        )
    PerformanceSection(
        label = stringResource(R.string.perf_gpu_title),
        trailing = confidenceLabel(info.gpuConfidence),
    ) {
        LongValueRow(
            label = stringResource(R.string.perf_gpu_gles_version),
            value = performanceValueOrNull(info.glEsVersion),
        )
        DataRow(
            label = stringResource(R.string.perf_gpu_renderer),
            value = performanceValueOrNull(info.glRenderer),
            showDivider = rendererClassification.reason == null,
        )
        ObservationReasonNote(rendererClassification)
        if (rendererClassification.reason != null) HairlineRule()
        DataRow(
            label = stringResource(R.string.perf_gpu_vendor),
            value = performanceValueOrNull(info.glVendor),
        )
        DataRow(
            label = stringResource(R.string.perf_gpu_vulkan_feature),
            value = stringResource(if (info.vulkanFeatureDeclared) R.string.status_yes else R.string.status_no),
        )
    }
}

@Composable
private fun BenchmarkSection(
    state: PerformanceInfoState,
    onStart: () -> Unit,
    onCancel: () -> Unit,
) {
    PerformanceSection(label = stringResource(R.string.perf_benchmark_title)) {
        Note(stringResource(R.string.perf_benchmark_description))
        when (state.benchmarkPhase) {
            BenchmarkPhase.RUNNING -> {
                IndeterminateRule()
                SecondaryButton(
                    label = stringResource(R.string.perf_benchmark_cancel),
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            BenchmarkPhase.COMPLETED -> {
                state.benchmarkResult?.let { BenchmarkResultRows(it) }
                PrimaryButton(
                    label = stringResource(R.string.perf_benchmark_run_again),
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            BenchmarkPhase.CANCELLED,
            BenchmarkPhase.ERROR,
            -> {
                Note(
                    stringResource(
                        if (state.benchmarkPhase == BenchmarkPhase.CANCELLED) {
                            R.string.perf_benchmark_cancelled
                        } else if (state.benchmarkError == "benchmark_timeout") {
                            R.string.perf_benchmark_timeout
                        } else {
                            R.string.perf_benchmark_error
                        },
                    ),
                )
                PrimaryButton(
                    label = stringResource(R.string.perf_benchmark_start),
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            BenchmarkPhase.IDLE ->
                PrimaryButton(
                    label = stringResource(R.string.perf_benchmark_start),
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                )
        }
    }
}

@Composable
private fun BenchmarkResultRows(result: PerformanceBenchmarkResult) {
    DataRow(
        label = stringResource(R.string.perf_benchmark_cpu_rate),
        value =
            stringResource(
                R.string.perf_benchmark_cpu_rate_value,
                uiNumber(result.cpuOperationsPerSecond, grouping = true),
            ),
    )
    DataRow(
        label = stringResource(R.string.perf_benchmark_memory_rate),
        value =
            result.memoryMebibytesPerSecond?.let {
                stringResource(R.string.perf_benchmark_memory_rate_value, uiNumber(it, 1, 1))
            },
    )
    DataRow(
        label = stringResource(R.string.perf_benchmark_workload),
        value =
            stringResource(
                R.string.perf_benchmark_workload_value,
                uiNumber(result.memoryBytesProcessed / 1_048_576),
            ),
    )
    DataRow(
        label = stringResource(R.string.perf_benchmark_duration),
        value = stringResource(R.string.perf_benchmark_duration_value, uiNumber(result.durationMillis)),
    )
    DataRow(
        label = stringResource(R.string.perf_benchmark_thermal_before),
        value = stringResource(thermalStatusStringRes(result.thermalBefore)),
    )
    DataRow(
        label = stringResource(R.string.perf_benchmark_thermal_after),
        value = stringResource(thermalStatusStringRes(result.thermalAfter)),
    )
    CaptureTimestamp(result.capturedAt)
    if (result.error != null) {
        Note(stringResource(R.string.perf_benchmark_memory_unavailable))
    }
}

@Composable
private fun CoreFrequencyRow(frequency: CpuCoreFrequency) {
    // A frequency range is three numbers and two separators; it outgrows a side-by-side row.
    LongValueRow(
        label = stringResource(R.string.perf_cpu_core_label, uiNumber(frequency.coreIndex)),
        value =
            stringResource(
                R.string.perf_cpu_freq_range,
                frequencyValue(frequency.currentMhz),
                frequencyValue(frequency.minMhz),
                frequencyValue(frequency.maxMhz),
            ),
    )
}

@Composable
private fun frequencyValue(value: Long?): String =
    value?.let {
        stringResource(R.string.perf_cpu_frequency_value, uiNumber(it))
    } ?: stringResource(R.string.value_unavailable_short)

private fun performanceValueOrNull(value: String): String? =
    value.takeUnless { it == PerformanceInfo.UNAVAILABLE || it.isBlank() }

private fun classifyReading(
    kind: MeasurementKind,
    available: Boolean,
) = DeviceObservationClassifier.classify(
    DeviceObservation.Measurement(
        kind,
        if (available) MeasurementOutcome.MEASURED else MeasurementOutcome.UNAVAILABLE,
    ),
)

@Composable
private fun PerformanceSection(
    label: String,
    trailing: String? = null,
    content: @Composable () -> Unit,
) {
    Column {
        SectionHeader(label = label, trailing = trailing)
        content()
    }
}
