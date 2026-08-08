package com.insaner.fonecheck.ui.screens.storage

import android.text.format.Formatter
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.ui.components.InfoCard
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.TestScreenContent
import java.text.NumberFormat

@Composable
fun StorageTestScreen(
    modifier: Modifier = Modifier,
    viewModel: StorageTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TestScreenContent(modifier = modifier) {
        if (state.isInfoLoading && state.info == null) {
            item {
                ScreenStateCard(
                    type = ScreenStateType.LOADING,
                    message = stringResource(R.string.storage_loading),
                )
            }
        }
        state.info?.let { info ->
            item { StorageOverviewCard(info) }
            item { StorageVolumesCard(info.appAccessibleVolumes) }
        }
        state.infoError?.let {
            item {
                ScreenStateCard(
                    type = ScreenStateType.ERROR,
                    message = stringResource(R.string.storage_info_error),
                    actionLabel = stringResource(R.string.storage_refresh),
                    onAction = viewModel::refreshInfo,
                )
            }
        }
        item {
            StorageBenchmarkCard(
                state = state,
                onStart = viewModel::startBenchmark,
                onCancel = viewModel::cancelBenchmark,
                onSkip = viewModel::skipBenchmark,
            )
        }
        item {
            InfoCard(title = stringResource(R.string.storage_limitations_title)) {
                Text(
                    text = stringResource(R.string.storage_limitations_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (state.infoError == null) {
            item {
                OutlinedButton(
                    onClick = viewModel::refreshInfo,
                    enabled = !state.isInfoLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(stringResource(R.string.storage_refresh))
                }
            }
        }
    }
}

@Composable
private fun StorageOverviewCard(info: StorageInfo) {
    val context = LocalContext.current
    InfoCard(
        title = stringResource(R.string.storage_overview_title),
        confidence = Confidence.HIGH,
    ) {
        InfoRow(stringResource(R.string.storage_total), Formatter.formatFileSize(context, info.totalBytes))
        InfoRow(stringResource(R.string.storage_used), Formatter.formatFileSize(context, info.usedBytes))
        InfoRow(stringResource(R.string.storage_available), Formatter.formatFileSize(context, info.availableBytes))
        InfoRow(
            stringResource(R.string.storage_usage),
            info.usagePercent?.let {
                stringResource(R.string.storage_percent_value, numberFormat().format(it))
            } ?: stringResource(R.string.device_value_unavailable),
        )
        InfoRow(
            stringResource(R.string.storage_internal_access),
            stringResource(
                if (info.internalStorageAccessible) {
                    R.string.storage_accessible
                } else {
                    R.string.device_value_unavailable
                },
            ),
        )
    }
}

@Composable
private fun StorageVolumesCard(volumes: List<AppStorageVolumeInfo>) {
    val context = LocalContext.current
    InfoCard(title = stringResource(R.string.storage_volumes_title)) {
        if (volumes.isEmpty()) {
            Text(
                text = stringResource(R.string.storage_no_shared_volumes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        volumes.forEachIndexed { index, volume ->
            Text(
                text =
                    if (volume.isPrimary) {
                        stringResource(R.string.storage_volume_primary)
                    } else {
                        stringResource(R.string.storage_volume_number, index + 1)
                    },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = if (index == 0) 0.dp else 12.dp),
            )
            InfoRow(stringResource(R.string.storage_mounted_state), storageStateLabel(volume.stateCode))
            InfoRow(
                stringResource(R.string.storage_removable),
                stringResource(if (volume.isRemovable) R.string.status_yes else R.string.status_no),
            )
            volume.totalBytes?.let {
                InfoRow(stringResource(R.string.storage_total), Formatter.formatFileSize(context, it))
            }
            volume.availableBytes?.let {
                InfoRow(stringResource(R.string.storage_available), Formatter.formatFileSize(context, it))
            }
        }
    }
}

@Composable
private fun StorageBenchmarkCard(
    state: StorageTestState,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onSkip: () -> Unit,
) {
    InfoCard(title = stringResource(R.string.storage_benchmark_title)) {
        Text(
            text = stringResource(R.string.storage_benchmark_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        when (state.benchmarkPhase) {
            StorageBenchmarkPhase.RUNNING -> {
                CircularProgressIndicator()
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text(stringResource(R.string.storage_benchmark_cancel))
                }
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.storage_benchmark_skip))
                }
            }
            StorageBenchmarkPhase.COMPLETED -> {
                state.benchmarkResult?.let { StorageBenchmarkRows(it) }
                BenchmarkStartButton(onStart, R.string.storage_benchmark_run_again)
            }
            StorageBenchmarkPhase.IDLE -> {
                BenchmarkStartButton(onStart, R.string.storage_benchmark_start)
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.storage_benchmark_skip))
                }
            }
            StorageBenchmarkPhase.NOT_RUN,
            StorageBenchmarkPhase.SKIPPED,
            StorageBenchmarkPhase.CANCELLED,
            StorageBenchmarkPhase.ERROR,
            -> {
                Text(
                    text = storageBenchmarkMessage(state),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                state.benchmarkResult?.let { StorageBenchmarkConditions(it) }
                BenchmarkStartButton(onStart, R.string.storage_benchmark_start)
            }
        }
    }
}

@Composable
private fun StorageBenchmarkRows(result: StorageBenchmarkResult) {
    val context = LocalContext.current
    InfoRow(
        stringResource(R.string.storage_benchmark_write_rate),
        stringResource(R.string.storage_rate_value, result.writeMebibytesPerSecond ?: 0.0),
    )
    InfoRow(
        stringResource(R.string.storage_benchmark_read_rate),
        stringResource(R.string.storage_rate_value, result.readMebibytesPerSecond ?: 0.0),
    )
    InfoRow(
        stringResource(R.string.storage_benchmark_data_size),
        Formatter.formatFileSize(context, result.dataSizeBytes),
    )
    InfoRow(
        stringResource(R.string.storage_benchmark_buffer_size),
        Formatter.formatFileSize(context, result.bufferSizeBytes.toLong()),
    )
    InfoRow(
        stringResource(R.string.storage_benchmark_duration),
        stringResource(R.string.storage_duration_value, result.durationMillis),
    )
    InfoRow(stringResource(R.string.storage_benchmark_location), stringResource(R.string.storage_app_cache))
    InfoRow(
        stringResource(R.string.storage_benchmark_cleanup),
        stringResource(
            if (result.cleanupSucceeded) R.string.storage_cleanup_complete else R.string.storage_cleanup_failed,
        ),
    )
    Text(
        text = stringResource(R.string.storage_benchmark_conditions),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun StorageBenchmarkConditions(result: StorageBenchmarkResult) {
    val context = LocalContext.current
    InfoRow(
        stringResource(R.string.storage_benchmark_required_space),
        Formatter.formatFileSize(context, result.dataSizeBytes),
    )
    InfoRow(
        stringResource(R.string.storage_benchmark_available_before),
        Formatter.formatFileSize(context, result.availableBeforeBytes.coerceAtLeast(0L)),
    )
    if (!result.cleanupSucceeded) {
        InfoRow(stringResource(R.string.storage_benchmark_cleanup), stringResource(R.string.storage_cleanup_failed))
    }
}

@Composable
private fun BenchmarkStartButton(
    onStart: () -> Unit,
    label: Int,
) {
    Button(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(stringResource(label))
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
