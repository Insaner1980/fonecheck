package com.insaner.fonecheck.ui.screens.storage

import android.text.format.Formatter
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.HeadlineReadout
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StorageTestScreen(
    modifier: Modifier = Modifier,
    viewModel: StorageTestViewModel = hiltViewModel(),
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
        if (state.isInfoLoading && state.info == null) {
            Column {
                IndeterminateRule()
                Note(
                    text = stringResource(R.string.storage_loading),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }

        state.info?.let { info ->
            StorageOverviewSection(info)
            StorageVolumesSection(info.appAccessibleVolumes)
        }

        state.infoError?.let {
            Column {
                Note(
                    text = stringResource(R.string.storage_info_error),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                )
                SecondaryButton(
                    label = stringResource(R.string.storage_refresh),
                    onClick = viewModel::refreshInfo,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        StorageBenchmarkSection(
            state = state,
            onStart = viewModel::startBenchmark,
            onCancel = viewModel::cancelBenchmark,
            onSkip = viewModel::skipBenchmark,
        )

        StorageSection(label = stringResource(R.string.storage_limitations_title)) {
            Note(stringResource(R.string.storage_limitations_description))
            HairlineRule()
        }

        if (state.infoError == null) {
            SecondaryButton(
                label = stringResource(R.string.storage_refresh),
                onClick = viewModel::refreshInfo,
                enabled = !state.isInfoLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StorageOverviewSection(info: StorageInfo) {
    val context = LocalContext.current
    val usagePercent = info.usagePercent
    val total = Formatter.formatFileSize(context, info.totalBytes)
    val used = Formatter.formatFileSize(context, info.usedBytes)
    val available = Formatter.formatFileSize(context, info.availableBytes)

    StorageSection(
        label = stringResource(R.string.storage_overview_title),
        trailing = stringResource(R.string.device_captured_at, formatMeasurementTime(info.capturedAt)),
    ) {
        if (usagePercent != null) {
            HeadlineReadout(
                value = numberFormat().format(usagePercent),
                unit = stringResource(R.string.storage_percent_value, "").trim(),
                rawValues = stringResource(R.string.storage_usage_context, used, available, total),
                modifier = Modifier.padding(vertical = FonecheckTheme.spacing.md),
            )
        } else {
            DataRow(stringResource(R.string.storage_total), total)
            DataRow(stringResource(R.string.storage_used), used)
            DataRow(stringResource(R.string.storage_available), available)
            DataRow(stringResource(R.string.storage_usage), null)
        }
        DataRow(
            label = stringResource(R.string.storage_internal_access),
            value = stringResource(R.string.storage_accessible).takeIf { info.internalStorageAccessible },
            showDivider = false,
        )
        Note(stringResource(R.string.confidence_high))
        HairlineRule()
    }
}

@Composable
private fun StorageVolumesSection(volumes: List<AppStorageVolumeInfo>) {
    val context = LocalContext.current
    StorageSection(label = stringResource(R.string.storage_volumes_title)) {
        if (volumes.isEmpty()) {
            Note(stringResource(R.string.storage_no_shared_volumes))
            HairlineRule()
        } else {
            volumes.forEachIndexed { index, volume ->
                DataRow(
                    label =
                        if (volume.isPrimary) {
                            stringResource(R.string.storage_volume_primary)
                        } else {
                            stringResource(R.string.storage_volume_number, index + 1)
                        },
                    value = storageStateLabel(volume.stateCode),
                )
                DataRow(
                    label = stringResource(R.string.storage_removable),
                    value = stringResource(if (volume.isRemovable) R.string.status_yes else R.string.status_no),
                )
                DataRow(
                    label = stringResource(R.string.storage_total),
                    value = volume.totalBytes?.let { Formatter.formatFileSize(context, it) },
                )
                DataRow(
                    label = stringResource(R.string.storage_available),
                    value = volume.availableBytes?.let { Formatter.formatFileSize(context, it) },
                )
            }
        }
    }
}

@Composable
private fun StorageBenchmarkSection(
    state: StorageTestState,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onSkip: () -> Unit,
) {
    StorageSection(
        label = stringResource(R.string.storage_benchmark_title),
        trailing =
            state.benchmarkResult?.let {
                stringResource(R.string.device_captured_at, formatMeasurementTime(it.capturedAt))
            },
    ) {
        Note(stringResource(R.string.storage_benchmark_description))
        when (state.benchmarkPhase) {
            StorageBenchmarkPhase.RUNNING -> {
                Column(
                    modifier = Modifier.padding(vertical = FonecheckTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
                ) {
                    StatusText(
                        text = stringResource(R.string.storage_benchmark_running),
                        tone = SemanticTone.NEUTRAL,
                    )
                    IndeterminateRule()
                }
                SecondaryButton(
                    label = stringResource(R.string.storage_benchmark_cancel),
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                )
                SecondaryButton(
                    label = stringResource(R.string.storage_benchmark_skip),
                    onClick = onSkip,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = FonecheckTheme.spacing.sm),
                )
            }
            StorageBenchmarkPhase.COMPLETED -> {
                state.benchmarkResult?.let { StorageBenchmarkRows(it) }
                BenchmarkStartButton(onStart, R.string.storage_benchmark_run_again)
            }
            StorageBenchmarkPhase.IDLE -> {
                BenchmarkStartButton(onStart, R.string.storage_benchmark_start)
                SecondaryButton(
                    label = stringResource(R.string.storage_benchmark_skip),
                    onClick = onSkip,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = FonecheckTheme.spacing.sm),
                )
            }
            StorageBenchmarkPhase.NOT_RUN,
            StorageBenchmarkPhase.SKIPPED,
            StorageBenchmarkPhase.CANCELLED,
            StorageBenchmarkPhase.ERROR,
            -> {
                Note(storageBenchmarkMessage(state))
                state.benchmarkResult?.let { StorageBenchmarkConditions(it) }
                BenchmarkStartButton(onStart, R.string.storage_benchmark_start)
            }
        }
    }
}

@Composable
private fun StorageBenchmarkRows(result: StorageBenchmarkResult) {
    val context = LocalContext.current
    DataRow(
        label = stringResource(R.string.storage_benchmark_write_rate),
        value =
            result.writeMebibytesPerSecond?.let {
                stringResource(R.string.storage_rate_value, it)
            },
    )
    DataRow(
        label = stringResource(R.string.storage_benchmark_read_rate),
        value =
            result.readMebibytesPerSecond?.let {
                stringResource(R.string.storage_rate_value, it)
            },
    )
    DataRow(
        stringResource(R.string.storage_benchmark_data_size),
        Formatter.formatFileSize(context, result.dataSizeBytes),
    )
    DataRow(
        stringResource(R.string.storage_benchmark_buffer_size),
        Formatter.formatFileSize(context, result.bufferSizeBytes.toLong()),
    )
    DataRow(
        stringResource(R.string.storage_benchmark_duration),
        stringResource(R.string.storage_duration_value, result.durationMillis),
    )
    DataRow(
        stringResource(R.string.storage_benchmark_location),
        stringResource(R.string.storage_app_cache),
    )
    DataRow(
        label = stringResource(R.string.storage_benchmark_cleanup),
        value =
            stringResource(
                if (result.cleanupSucceeded) {
                    R.string.storage_cleanup_complete
                } else {
                    R.string.storage_cleanup_failed
                },
            ),
        tone = if (result.cleanupSucceeded) SemanticTone.PASS else SemanticTone.ATTENTION,
        showDivider = false,
    )
    Note(stringResource(R.string.storage_benchmark_conditions))
    HairlineRule()
}

@Composable
private fun StorageBenchmarkConditions(result: StorageBenchmarkResult) {
    val context = LocalContext.current
    DataRow(
        stringResource(R.string.storage_benchmark_required_space),
        Formatter.formatFileSize(context, result.dataSizeBytes),
    )
    DataRow(
        stringResource(R.string.storage_benchmark_available_before),
        Formatter.formatFileSize(context, result.availableBeforeBytes.coerceAtLeast(0L)),
    )
    if (!result.cleanupSucceeded) {
        DataRow(
            label = stringResource(R.string.storage_benchmark_cleanup),
            value = stringResource(R.string.storage_cleanup_failed),
            tone = SemanticTone.ATTENTION,
        )
    }
}

@Composable
private fun BenchmarkStartButton(
    onStart: () -> Unit,
    label: Int,
) {
    PrimaryButton(
        label = stringResource(label),
        onClick = onStart,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = FonecheckTheme.spacing.sm),
    )
}

@Composable
private fun StorageSection(
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
private fun storageBenchmarkMessage(state: StorageTestState): String =
    stringResource(
        when {
            state.benchmarkPhase == StorageBenchmarkPhase.SKIPPED -> R.string.storage_benchmark_skipped
            state.benchmarkPhase == StorageBenchmarkPhase.CANCELLED -> R.string.storage_benchmark_cancelled
            state.benchmarkError == StorageBenchmarkErrorCode.INSUFFICIENT_SPACE ->
                R.string.storage_benchmark_insufficient_space
            state.benchmarkError == StorageBenchmarkErrorCode.DATA_MISMATCH ->
                R.string.storage_benchmark_data_mismatch
            state.benchmarkError == StorageBenchmarkErrorCode.CLEANUP_FAILED ->
                R.string.storage_benchmark_cleanup_error
            else -> R.string.storage_benchmark_error
        },
    )

@Composable
private fun storageStateLabel(stateCode: String): String =
    stringResource(
        when (stateCode) {
            "mounted" -> R.string.storage_state_mounted
            "mounted_ro" -> R.string.storage_state_read_only
            else -> R.string.storage_state_unavailable
        },
    )

private fun numberFormat(): NumberFormat =
    NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = 1
        minimumFractionDigits = 0
    }

private val measurementTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm", Locale.ROOT)

private fun formatMeasurementTime(
    value: Instant,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = measurementTimeFormatter.withZone(zoneId).format(value)
