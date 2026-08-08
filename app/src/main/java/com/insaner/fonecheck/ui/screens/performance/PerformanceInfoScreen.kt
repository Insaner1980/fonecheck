package com.insaner.fonecheck.ui.screens.performance

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.CpuCoreFrequency
import com.insaner.fonecheck.domain.model.PerformanceBenchmarkResult
import com.insaner.fonecheck.domain.model.PerformanceInfo
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.ui.components.InfoCard
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.theme.JetBrainsMono
import com.insaner.fonecheck.ui.theme.Neutral600
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun PerformanceInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: PerformanceInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.isInfoLoading && state.info == null) {
            ScreenStateCard(
                type = ScreenStateType.LOADING,
                message = stringResource(R.string.perf_info_loading),
            )
        }
        state.info?.let { info ->
            CpuInfoCard(info)
            RamInfoCard(info)
            GpuInfoCard(info)
        }
        state.infoError?.let {
            ScreenStateCard(
                type = ScreenStateType.ERROR,
                message = stringResource(R.string.perf_info_error_description),
                actionLabel = stringResource(R.string.perf_refresh_info),
                onAction = viewModel::refreshInfo,
            )
        }
        BenchmarkCard(
            state = state,
            onStart = viewModel::startBenchmark,
            onCancel = viewModel::cancelBenchmark,
        )
        if (state.infoError == null) {
            OutlinedButton(
                onClick = viewModel::refreshInfo,
                enabled = !state.isInfoLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(stringResource(R.string.perf_refresh_info))
            }
        }
    }
}

@Composable
private fun CpuInfoCard(info: PerformanceInfo) {
    InfoCard(
        title = stringResource(R.string.perf_cpu_title),
        confidence = info.cpuConfidence,
    ) {
        InfoRow(stringResource(R.string.perf_cpu_model), performanceValue(info.cpuModel))
        InfoRow(stringResource(R.string.perf_cpu_architecture), performanceValue(info.cpuArchitecture))
        InfoRow(stringResource(R.string.perf_cpu_cores), info.cpuCores.toString())
        HorizontalDivider(
            color = Neutral600,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        Text(
            text = stringResource(R.string.perf_cpu_clock_speeds),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        info.cpuFrequencies.forEach { CoreFrequencyRow(it) }
    }
}

@Composable
private fun RamInfoCard(info: PerformanceInfo) {
    val context = LocalContext.current
    InfoCard(
        title = stringResource(R.string.perf_ram_title),
        confidence = info.ramConfidence,
    ) {
        InfoRow(
            stringResource(R.string.perf_ram_total),
            info.totalRamBytes?.let { Formatter.formatFileSize(context, it) } ?: performanceUnavailable(),
        )
        InfoRow(
            stringResource(R.string.perf_ram_available),
            info.availableRamBytes?.let { Formatter.formatFileSize(context, it) } ?: performanceUnavailable(),
        )
    }
}

@Composable
private fun GpuInfoCard(info: PerformanceInfo) {
    InfoCard(
        title = stringResource(R.string.perf_gpu_title),
        confidence = info.gpuConfidence,
    ) {
        InfoRow(stringResource(R.string.perf_gpu_gles_version), performanceValue(info.glEsVersion))
        InfoRow(stringResource(R.string.perf_gpu_renderer), performanceValue(info.glRenderer))
        InfoRow(stringResource(R.string.perf_gpu_vendor), performanceValue(info.glVendor))
        InfoRow(
            stringResource(R.string.perf_gpu_vulkan_feature),
            if (info.vulkanFeatureDeclared) stringResource(R.string.status_yes) else stringResource(R.string.status_no),
        )
    }
}

@Composable
private fun BenchmarkCard(
    state: PerformanceInfoState,
    onStart: () -> Unit,
    onCancel: () -> Unit,
) {
    InfoCard(title = stringResource(R.string.perf_benchmark_title)) {
        Text(
            text = stringResource(R.string.perf_benchmark_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        when (state.benchmarkPhase) {
            BenchmarkPhase.RUNNING -> {
                CircularProgressIndicator()
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text(stringResource(R.string.perf_benchmark_cancel))
                }
            }
            BenchmarkPhase.COMPLETED -> {
                state.benchmarkResult?.let { BenchmarkResultRows(it) }
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text(stringResource(R.string.perf_benchmark_run_again))
                }
            }
            BenchmarkPhase.CANCELLED,
            BenchmarkPhase.ERROR,
            -> {
                Text(
                    text =
                        stringResource(
                            if (state.benchmarkPhase == BenchmarkPhase.CANCELLED) {
                                R.string.perf_benchmark_cancelled
                            } else if (state.benchmarkError == "benchmark_timeout") {
                                R.string.perf_benchmark_timeout
                            } else {
                                R.string.perf_benchmark_error
                            },
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text(stringResource(R.string.perf_benchmark_start))
                }
            }
            BenchmarkPhase.IDLE ->
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.perf_benchmark_start))
                }
        }
    }
}

@Composable
private fun BenchmarkResultRows(result: PerformanceBenchmarkResult) {
    val numberFormat = NumberFormat.getIntegerInstance()
    InfoRow(
        stringResource(R.string.perf_benchmark_cpu_rate),
        stringResource(R.string.perf_benchmark_cpu_rate_value, numberFormat.format(result.cpuOperationsPerSecond)),
    )
    InfoRow(
        stringResource(R.string.perf_benchmark_memory_rate),
        result.memoryMebibytesPerSecond?.let {
            stringResource(R.string.perf_benchmark_memory_rate_value, it)
        } ?: performanceUnavailable(),
    )
    InfoRow(
        stringResource(R.string.perf_benchmark_workload),
        stringResource(R.string.perf_benchmark_workload_value, result.memoryBytesProcessed / 1_048_576),
    )
    InfoRow(
        stringResource(R.string.perf_benchmark_duration),
        stringResource(R.string.perf_benchmark_duration_value, result.durationMillis),
    )
    InfoRow(stringResource(R.string.perf_benchmark_thermal_before), thermalStatusLabel(result.thermalBefore))
    InfoRow(stringResource(R.string.perf_benchmark_thermal_after), thermalStatusLabel(result.thermalAfter))
    InfoRow(
        stringResource(R.string.device_captured_at),
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(result.capturedAt),
    )
    if (result.error != null) {
        Text(
            text = stringResource(R.string.perf_benchmark_memory_unavailable),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun CoreFrequencyRow(freq: CpuCoreFrequency) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.perf_cpu_core_label, freq.coreIndex),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text =
                stringResource(
                    R.string.perf_cpu_freq_range,
                    frequencyValue(freq.currentMhz),
                    frequencyValue(freq.minMhz),
                    frequencyValue(freq.maxMhz),
                ),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun frequencyValue(value: Long?): String =
    value?.let { stringResource(R.string.perf_cpu_frequency_value, it) } ?: performanceUnavailable()

@Composable
private fun performanceValue(value: String): String =
    if (value == PerformanceInfo.UNAVAILABLE) performanceUnavailable() else value

@Composable
private fun performanceUnavailable(): String = stringResource(R.string.device_value_unavailable)

@Composable
private fun thermalStatusLabel(status: ThermalStatusCode): String =
    stringResource(
        when (status) {
            ThermalStatusCode.NONE -> R.string.perf_thermal_none
            ThermalStatusCode.LIGHT -> R.string.perf_thermal_light
            ThermalStatusCode.MODERATE -> R.string.perf_thermal_moderate
            ThermalStatusCode.SEVERE -> R.string.perf_thermal_severe
            ThermalStatusCode.CRITICAL -> R.string.perf_thermal_critical
            ThermalStatusCode.EMERGENCY -> R.string.perf_thermal_emergency
            ThermalStatusCode.SHUTDOWN -> R.string.perf_thermal_shutdown
            ThermalStatusCode.UNAVAILABLE -> R.string.device_value_unavailable
        },
    )
