package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkErrorCode
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkPhase
import com.insaner.fonecheck.ui.screens.storage.StorageTestViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

private const val STORAGE_TIMEOUT_MS = 45_000L

internal suspend fun runAutomaticStorageCheck(
    storageViewModel: StorageTestViewModel,
    includeBenchmark: Boolean,
): RunAllStageOutcome? {
    val result =
        withTimeoutOrNull(STORAGE_TIMEOUT_MS) {
            storageViewModel.refreshInfo()
            storageViewModel.state.first { !it.isInfoLoading }
            if (includeBenchmark) {
                storageViewModel.startBenchmark()
                storageViewModel.state.first {
                    it.benchmarkPhase != StorageBenchmarkPhase.RUNNING
                }
            } else {
                storageViewModel.skipBenchmark()
                storageViewModel.state.first {
                    it.benchmarkPhase != StorageBenchmarkPhase.RUNNING
                }
            }
        }
    val issue =
        when {
            result == null || result.benchmarkError == StorageBenchmarkErrorCode.TIMEOUT ->
                RunAllStageOutcome.TIMED_OUT
            result.infoError != null -> RunAllStageOutcome.ERROR
            result.benchmarkPhase == StorageBenchmarkPhase.ERROR -> RunAllStageOutcome.ERROR
            else -> null
        }
    if (result == null) {
        storageViewModel.cancelInfoCapture()
        storageViewModel.cancelBenchmark()
    }
    return issue
}
