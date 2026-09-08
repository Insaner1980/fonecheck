package com.insaner.fonecheck.ui.screens.runall

import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.ui.screens.audio.AudioTestState
import com.insaner.fonecheck.ui.screens.deviceinfo.DeviceInfoViewModel
import com.insaner.fonecheck.ui.screens.performance.BenchmarkPhase
import com.insaner.fonecheck.ui.screens.performance.PerformanceInfoViewModel
import com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyViewModel
import com.insaner.fonecheck.ui.screens.storage.StorageTestViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val AUTOMATIC_MICROPHONE_DURATION_MS = 1_500L
private const val AUTOMATIC_MICROPHONE_TIMEOUT_MS = 3_000L
private const val AUTOMATIC_STATE_POLL_INTERVAL_MS = 100L
private const val DEVICE_INFO_TIMEOUT_MS = 3_000L
private const val PERFORMANCE_TIMEOUT_MS = 7_000L

/**
 * Runs on the caller's main-thread scope. The state machine owns claims and deadlines;
 * the resource owner cancels both this scope and independently owned category work.
 * Audio/connectivity callbacks are the narrow seam for their Android-bound operations.
 */
@Suppress("LongParameterList", "kotlin:S107", "CyclomaticComplexMethod", "kotlin:S3776")
internal suspend fun runAutomaticChecks(
    token: Long,
    sessionViewModel: RunAllTestsViewModel,
    resourceOwner: RunAllResourceOwner,
    deviceViewModel: DeviceInfoViewModel,
    performanceViewModel: PerformanceInfoViewModel,
    simViewModel: SimTelephonyViewModel,
    storageViewModel: StorageTestViewModel,
    audioState: () -> AudioTestState,
    updateHeadphones: () -> Unit,
    startRecording: (Long) -> Unit,
    cancelRecording: () -> Unit,
    refreshConnectivity: () -> Unit,
) = coroutineScope {
    val entry = sessionViewModel.state.value
    if (entry.stage != RunAllStage.AUTOMATIC || !sessionViewModel.claimStage(token)) return@coroutineScope
    val execution = coroutineContext[Job]!!
    resourceOwner.beginAutomatic(execution)
    val stageWatcher =
        launch(start = CoroutineStart.UNDISPATCHED) {
            sessionViewModel.state.first { it.stageToken != token }
            execution.cancel()
        }
    try {
        runAutomaticInfoChecks(
            token = token,
            entry = entry,
            sessionViewModel = sessionViewModel,
            deviceViewModel = deviceViewModel,
            performanceViewModel = performanceViewModel,
            simViewModel = simViewModel,
        )
        val retestCategory = entry.targetCategory
        if (retestCategory == null || retestCategory == DiagnosticCategoryId.STORAGE) {
            val issue =
                runAutomaticStorageCheck(storageViewModel, entry.selections.includeStorageBenchmark)
            issue?.let { sessionViewModel.reportAutomaticIssue(token, DiagnosticCategoryId.STORAGE, it) }
        }
        if (retestCategory == null || retestCategory == DiagnosticCategoryId.AUDIO) {
            var microphoneIssue: RunAllStageOutcome? = null
            try {
                updateHeadphones()
                if (entry.selections.includeMicrophone && entry.permissions.microphone) {
                    try {
                        startRecording(AUTOMATIC_MICROPHONE_DURATION_MS)
                        val completed =
                            withTimeoutOrNull(AUTOMATIC_MICROPHONE_TIMEOUT_MS) {
                                while (audioState().isRecording) {
                                    delay(AUTOMATIC_STATE_POLL_INTERVAL_MS)
                                }
                            } != null
                        microphoneIssue =
                            when {
                                !completed -> RunAllStageOutcome.TIMED_OUT
                                !audioState().hasRecordedAudio -> RunAllStageOutcome.ERROR
                                else -> null
                            }
                    } finally {
                        cancelRecording()
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                microphoneIssue = RunAllStageOutcome.ERROR
                cancelRecording()
            }
            microphoneIssue?.let {
                sessionViewModel.reportAutomaticIssue(
                    token,
                    DiagnosticCategoryId.AUDIO,
                    it,
                )
            }
        }
        if (retestCategory == null || retestCategory == DiagnosticCategoryId.CONNECTIVITY) {
            try {
                refreshConnectivity()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                sessionViewModel.reportAutomaticIssue(
                    token,
                    DiagnosticCategoryId.CONNECTIVITY,
                    RunAllStageOutcome.ERROR,
                )
            }
        }

        coroutineContext.ensureActive()
    } finally {
        stageWatcher.cancel()
        resourceOwner.endAutomatic(execution)
    }
    coroutineContext.ensureActive()
    sessionViewModel.onAutomaticChecksComplete(token)
}

private suspend fun runAutomaticInfoChecks(
    token: Long,
    entry: RunAllTestsState,
    sessionViewModel: RunAllTestsViewModel,
    deviceViewModel: DeviceInfoViewModel,
    performanceViewModel: PerformanceInfoViewModel,
    simViewModel: SimTelephonyViewModel,
) {
    // Start the independent prerequisite reads together before waiting in the existing order.
    if (entry.targetCategory.includes(DiagnosticCategoryId.DEVICE)) {
        deviceViewModel.refresh()
    }
    if (entry.targetCategory.includes(DiagnosticCategoryId.PERFORMANCE)) {
        performanceViewModel.refreshInfo()
    }
    if (entry.targetCategory.includes(DiagnosticCategoryId.SIM)) simViewModel.refresh()
    val retestCategory = entry.targetCategory
    if (retestCategory.includes(DiagnosticCategoryId.DEVICE)) {
        val result =
            withTimeoutOrNull(DEVICE_INFO_TIMEOUT_MS) {
                deviceViewModel.state.first { !it.isLoading }
            }
        when {
            result == null -> {
                deviceViewModel.cancelCapture()
                sessionViewModel.reportAutomaticIssue(
                    token,
                    DiagnosticCategoryId.DEVICE,
                    RunAllStageOutcome.TIMED_OUT,
                )
            }
            result.error != null ->
                sessionViewModel.reportAutomaticIssue(
                    token,
                    DiagnosticCategoryId.DEVICE,
                    RunAllStageOutcome.ERROR,
                )
        }
    }
    if (retestCategory.includes(DiagnosticCategoryId.SIM)) {
        val result =
            withTimeoutOrNull(DEVICE_INFO_TIMEOUT_MS) {
                simViewModel.state.first { !it.isLoading }
            }
        when {
            result == null -> {
                simViewModel.cancelCapture()
                sessionViewModel.reportAutomaticIssue(
                    token,
                    DiagnosticCategoryId.SIM,
                    RunAllStageOutcome.TIMED_OUT,
                )
            }
            result.error != null ->
                sessionViewModel.reportAutomaticIssue(
                    token,
                    DiagnosticCategoryId.SIM,
                    RunAllStageOutcome.ERROR,
                )
        }
    }
    if (retestCategory.includes(DiagnosticCategoryId.PERFORMANCE)) {
        val result =
            withTimeoutOrNull(PERFORMANCE_TIMEOUT_MS) {
                performanceViewModel.state.first { !it.isInfoLoading }
                performanceViewModel.startBenchmark()
                performanceViewModel.state.first { it.benchmarkPhase != BenchmarkPhase.RUNNING }
            }
        val issue =
            when {
                result == null -> {
                    performanceViewModel.cancelInfoCapture()
                    performanceViewModel.cancelBenchmark()
                    RunAllStageOutcome.TIMED_OUT
                }
                result.benchmarkError == "benchmark_timeout" ->
                    RunAllStageOutcome.TIMED_OUT
                result.infoError != null || result.benchmarkError != null -> RunAllStageOutcome.ERROR
                else -> null
            }
        issue?.let { sessionViewModel.reportAutomaticIssue(token, DiagnosticCategoryId.PERFORMANCE, it) }
    }
}

private fun DiagnosticCategoryId?.includes(category: DiagnosticCategoryId): Boolean =
    this == null || this == category
