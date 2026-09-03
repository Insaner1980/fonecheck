package com.insaner.fonecheck.ui.screens.storage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.ui.TopBarAction
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.HairlineRule
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.RegisterRefreshTopBarAction
import com.insaner.fonecheck.ui.components.ScreenLoadingNote
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.WindowBar
import com.insaner.fonecheck.ui.components.WindowLabel
import com.insaner.fonecheck.ui.components.WindowReading
import com.insaner.fonecheck.ui.components.formatCaptureTimestamp
import com.insaner.fonecheck.ui.format.uiFileSize
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone

@Composable
fun StorageTestScreen(
    modifier: Modifier = Modifier,
    onTopBarActionChange: (TopBarAction?) -> Unit = {},
    viewModel: StorageTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // CPD-OFF
    // Storage and performance intentionally share the standard information-screen loading shell.
    RegisterRefreshTopBarAction(
        contentDescriptionResId = R.string.storage_refresh,
        enabled = !state.isInfoLoading,
        onRefresh = viewModel::refreshInfo,
        onTopBarActionChange = onTopBarActionChange,
    )

    TestScreenContent(
        modifier = modifier,
        liveStateUpdatedAtEpochMillis = state.info?.capturedAt?.toEpochMilli(),
    ) {
        if (state.isInfoLoading && state.info == null) {
            item { ScreenLoadingNote(message = stringResource(R.string.storage_loading)) }
        }
        // CPD-ON

        state.info?.let { info ->
            item { StorageOverviewSection(info) }
            item { StorageAccessSection(info) }
            if (
                info.appAccessibleVolumes.isEmpty() ||
                info.appAccessibleVolumes.any { it.isRemovable || !it.isPrimary }
            ) {
                item { StorageVolumesSection(info.appAccessibleVolumes) }
            }
        }

        state.infoError?.let {
            item {
                Note(
                    text = stringResource(R.string.storage_info_error),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                )
            }
        }

        item {
            StorageBenchmarkSection(
                state = state,
                onStart = viewModel::startBenchmark,
                onCancel = viewModel::cancelBenchmark,
                onSkip = viewModel::skipBenchmark,
            )
        }

        item {
            StorageSection(label = stringResource(R.string.storage_limitations_title)) {
                Note(stringResource(R.string.storage_limitations_description))
                HairlineRule()
            }
        }
    }
}

@Composable
internal fun StorageOverviewSection(info: StorageInfo) {
    val usagePercent = info.usagePercent
    val total = uiFileSize(info.totalBytes)
    val used = uiFileSize(info.usedBytes)
    val available = uiFileSize(info.availableBytes)

    StorageSection(label = stringResource(R.string.storage_overview_title)) {
        if (usagePercent != null) {
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            // A share of a fixed capacity: bounded, so the window carries a bar as well as a figure.
            ReadoutWindow {
                WindowLabel(text = stringResource(R.string.storage_usage))
                Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
                WindowReading(
                    value = uiNumber(usagePercent, maximumFractionDigits = 1),
                    unit = stringResource(R.string.storage_usage_unit),
                )
                Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
                WindowBar(percentage = usagePercent.toInt())
            }
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            DataRow(stringResource(R.string.storage_used), used)
            DataRow(stringResource(R.string.storage_available), available)
            DataRow(stringResource(R.string.storage_total), total)
        } else {
            DataRow(stringResource(R.string.storage_total), total)
            DataRow(stringResource(R.string.storage_used), used)
            DataRow(stringResource(R.string.storage_available), available)
            DataRow(stringResource(R.string.storage_usage), null)
        }
    }
}

@Composable
internal fun StorageAccessSection(info: StorageInfo) {
    val primaryVolume = info.appAccessibleVolumes.firstOrNull { it.isPrimary }

    StorageSection(label = stringResource(R.string.storage_access_title)) {
        DataRow(
            label = stringResource(R.string.storage_private_access),
            value = stringResource(R.string.storage_accessible).takeIf { info.internalStorageAccessible },
            confidence = Confidence.HIGH,
        )
        DataRow(
            label = stringResource(R.string.storage_volume_primary),
            value = primaryVolume?.let { storageStateLabel(it.stateCode) },
        )
        DataRow(
            label = stringResource(R.string.storage_removable_storage),
            value =
                primaryVolume?.let {
                    stringResource(if (it.isRemovable) R.string.status_yes else R.string.status_no)
                },
        )
    }
}

@Composable
internal fun StorageVolumesSection(volumes: List<AppStorageVolumeInfo>) {
    if (volumes.isEmpty()) {
        StorageSection(label = stringResource(R.string.storage_volumes_title)) {
            Note(stringResource(R.string.storage_no_shared_volumes))
            HairlineRule()
        }
        return
    }

    volumes.forEachIndexed { index, volume ->
        if (volume.isRemovable || !volume.isPrimary) {
            StorageSection(
                label =
                    if (volume.isPrimary) {
                        stringResource(R.string.storage_volume_primary)
                    } else {
                        stringResource(R.string.storage_volume_number, index + 1)
                    },
            ) {
                DataRow(
                    label = stringResource(R.string.storage_mounted_state),
                    value = storageStateLabel(volume.stateCode),
                )
                DataRow(
                    label = stringResource(R.string.storage_removable_storage),
                    value = stringResource(if (volume.isRemovable) R.string.status_yes else R.string.status_no),
                )
                DataRow(
                    label = stringResource(R.string.storage_total),
                    value = volume.totalBytes?.let { uiFileSize(it) },
                )
                DataRow(
                    label = stringResource(R.string.storage_available),
                    value = volume.availableBytes?.let { uiFileSize(it) },
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
    StorageSection(label = stringResource(R.string.storage_benchmark_title)) {
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
                if (state.benchmarkPhase == StorageBenchmarkPhase.ERROR) {
                    StatusText(
                        text = stringResource(R.string.state_error_title),
                        tone = SemanticTone.FAIL,
                    )
                }
                Note(storageBenchmarkMessage(state))
                state.benchmarkResult?.let { StorageBenchmarkConditions(it) }
                BenchmarkStartButton(onStart, R.string.storage_benchmark_start)
            }
        }
    }
}

@Composable
private fun StorageBenchmarkRows(result: StorageBenchmarkResult) {
    val locale = LocalLocale.current.platformLocale
    DataRow(
        label = stringResource(R.string.storage_benchmark_write_rate),
        value =
            result.writeMebibytesPerSecond?.let {
                stringResource(R.string.storage_rate_value, uiNumber(it, 1, 1))
            },
    )
    DataRow(
        label = stringResource(R.string.storage_benchmark_read_rate),
        value =
            result.readMebibytesPerSecond?.let {
                stringResource(R.string.storage_rate_value, uiNumber(it, 1, 1))
            },
    )
    DataRow(
        stringResource(R.string.storage_benchmark_data_size),
        uiFileSize(result.dataSizeBytes),
    )
    DataRow(
        stringResource(R.string.storage_benchmark_buffer_size),
        uiFileSize(result.bufferSizeBytes.toLong()),
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
        stringResource(R.string.storage_benchmark_captured),
        formatCaptureTimestamp(result.capturedAt, locale),
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
        showDivider = false,
    )
    Note(stringResource(R.string.storage_benchmark_conditions))
    HairlineRule()
}

@Composable
private fun StorageBenchmarkConditions(result: StorageBenchmarkResult) {
    DataRow(
        stringResource(R.string.storage_benchmark_required_space),
        uiFileSize(result.dataSizeBytes),
    )
    DataRow(
        stringResource(R.string.storage_benchmark_available_before),
        uiFileSize(result.availableBeforeBytes.coerceAtLeast(0L)),
    )
    if (!result.cleanupSucceeded) {
        DataRow(
            label = stringResource(R.string.storage_benchmark_cleanup),
            value = stringResource(R.string.storage_cleanup_failed),
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
