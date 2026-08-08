package com.insaner.fonecheck.ui.screens.runall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.data.repository.ReportRepository
import com.insaner.fonecheck.domain.model.DiagnosticCategorySnapshot
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportAssembler
import com.insaner.fonecheck.domain.model.ReportAssemblyRequest
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class RunAllStage {
    PREFLIGHT,
    PERMISSIONS,
    AUTOMATIC,
    DISPLAY,
    AUDIO,
    CAMERA,
    SENSORS,
    VIBRATION,
    BUTTONS,
    BIOMETRICS,
    RESULTS,
}

enum class RunAllRunStatus {
    NOT_STARTED,
    RUNNING,
    COMPLETED,
}

enum class RunAllStageOutcome {
    COMPLETED,
    PASSED,
    FAILED,
    SKIPPED,
    UNAVAILABLE,
    TIMED_OUT,
    ERROR,
}

enum class RunAllInterruptionReason {
    USER_CANCEL,
    BACKGROUND,
    CONFIGURATION_CHANGE,
    SCREEN_DISPOSED,
}

enum class ReportSaveStatus {
    IDLE,
    SAVING,
    SAVED,
    FAILED,
}

data class RunAllPermissions(
    val microphone: Boolean = false,
    val camera: Boolean = false,
    val location: Boolean = false,
    val phone: Boolean = false,
    val bluetooth: Boolean = false,
)

data class ManualCheckResults(
    val display: Boolean? = null,
    val speaker: Boolean? = null,
    val camera: Boolean? = null,
    val sensors: Boolean? = null,
    val vibration: Boolean? = null,
    val buttons: Boolean? = null,
    val biometrics: Boolean? = null,
    val outcomes: Map<RunAllStage, RunAllStageOutcome> = emptyMap(),
)

data class RunAllTestsState(
    val stage: RunAllStage = RunAllStage.PREFLIGHT,
    val stageToken: Long = 0L,
    val runStatus: RunAllRunStatus = RunAllRunStatus.NOT_STARTED,
    val lastInterruption: RunAllInterruptionReason? = null,
    val permissions: RunAllPermissions = RunAllPermissions(),
    val selections: RunAllSelections = RunAllSelections(),
    val hardware: RunAllHardwareProfile = RunAllHardwareProfile(),
    val plan: RunAllPlan = RunAllPlan.EMPTY,
    val manualChecks: ManualCheckResults = ManualCheckResults(),
    val displayColorIndex: Int = 0,
    val cameraIds: List<String> = emptyList(),
    val cameraIndex: Int = 0,
    val stageIssue: RunAllStageOutcome? = null,
    val report: DiagnosticReport? = null,
    val saveStatus: ReportSaveStatus = ReportSaveStatus.IDLE,
) {
    val progress: RunAllProgress?
        get() =
            if (stage in plan.interactiveStages) {
                plan.progressFor(stage)
            } else {
                null
            }

    val stageOutcomes: Map<RunAllStage, RunAllStageOutcome>
        get() = manualChecks.outcomes

    val currentCameraId: String?
        get() = cameraIds.getOrNull(cameraIndex)
}

@HiltViewModel
class RunAllTestsViewModel
    @Inject
    constructor(
        private val clock: EpochMillisClock,
        private val idProvider: IdProvider,
        private val reportRepository: ReportRepository,
    ) : ViewModel() {
        private val _state = MutableStateFlow(RunAllTestsState())
        val state: StateFlow<RunAllTestsState> = _state

        private var nextStageToken = 0L
        private var claimedStageToken: Long? = null
        private var stageTimeoutJob: Job? = null
        private var reportSaveJob: Job? = null
        private var reportStartedAt: Instant? = null

        fun updateSelections(selections: RunAllSelections) {
            if (_state.value.stage != RunAllStage.PREFLIGHT ||
                _state.value.runStatus != RunAllRunStatus.NOT_STARTED
            ) {
                return
            }
            _state.value = _state.value.copy(selections = selections)
        }

        fun onPreflightAccepted(
            selections: RunAllSelections,
            hardware: RunAllHardwareProfile,
        ) {
            if (_state.value.stage != RunAllStage.PREFLIGHT ||
                _state.value.runStatus != RunAllRunStatus.NOT_STARTED
            ) {
                return
            }
            reportStartedAt = Instant.ofEpochMilli(clock.currentTimeMillis())
            enterStage(
                RunAllStage.PERMISSIONS,
                _state.value.copy(
                    selections = selections,
                    hardware = hardware,
                    runStatus = RunAllRunStatus.RUNNING,
                    lastInterruption = null,
                ),
            )
        }

        fun onPermissionsResolved(permissions: RunAllPermissions) {
            val current = _state.value
            if (current.runStatus != RunAllRunStatus.RUNNING || current.stage != RunAllStage.PERMISSIONS) return
            val plan =
                RunAllStagePlanner.plan(
                    hardware = current.hardware,
                    permissions = permissions,
                    selections = current.selections,
                )
            enterStage(
                plan.stages.firstOrNull() ?: RunAllStage.RESULTS,
                current.copy(
                    permissions = permissions,
                    plan = plan,
                ),
            )
        }

        fun claimStage(token: Long): Boolean {
            val current = _state.value
            if (!isCurrentStage(token) ||
                current.stage == RunAllStage.PREFLIGHT ||
                current.stage == RunAllStage.PERMISSIONS ||
                current.stage == RunAllStage.RESULTS ||
                claimedStageToken == token
            ) {
                return false
            }
            claimedStageToken = token
            scheduleTimeout(token, current.stage)
            return true
        }

        fun onAutomaticChecksComplete(token: Long) {
            finishStage(token, RunAllStage.AUTOMATIC, RunAllStageOutcome.COMPLETED)
        }

        fun nextDisplayColor(
            token: Long,
            lastColorIndex: Int,
        ) {
            if (!isCurrentStage(token, RunAllStage.DISPLAY)) return
            val current = _state.value.displayColorIndex
            if (current < lastColorIndex) {
                _state.value = _state.value.copy(displayColorIndex = current + 1)
            }
        }

        fun recordDisplay(
            token: Long,
            result: Boolean?,
            outcome: RunAllStageOutcome = outcomeFor(result),
        ) {
            finishStage(token, RunAllStage.DISPLAY, outcome, result)
        }

        fun recordSpeaker(
            token: Long,
            result: Boolean?,
            outcome: RunAllStageOutcome = outcomeFor(result),
        ) {
            finishStage(token, RunAllStage.AUDIO, outcome, result)
        }

        fun recordCamera(
            token: Long,
            result: Boolean?,
            outcome: RunAllStageOutcome = outcomeFor(result),
        ) {
            finishStage(token, RunAllStage.CAMERA, outcome, result)
        }

        fun prepareCameraStage(
            token: Long,
            cameraIds: List<String>,
        ): Boolean {
            if (!isCurrentStage(token, RunAllStage.CAMERA) || claimedStageToken != token) return false
            val distinctIds = cameraIds.distinct()
            if (distinctIds.isEmpty()) {
                markStageUnavailable(token)
                return false
            }
            if (_state.value.cameraIds.isEmpty()) {
                _state.value =
                    _state.value.copy(
                        cameraIds = distinctIds,
                        cameraIndex = 0,
                    )
            }
            return _state.value.currentCameraId != null
        }

        fun recordCameraCapture(token: Long) {
            val current = _state.value
            if (!isCurrentStage(token, RunAllStage.CAMERA) || claimedStageToken != token) return
            val hasNextCamera = current.cameraIndex + 1 < current.cameraIds.size
            if (hasNextCamera) {
                enterStage(
                    RunAllStage.CAMERA,
                    current.copy(
                        cameraIndex = current.cameraIndex + 1,
                        stageIssue = null,
                    ),
                )
            } else {
                finishStage(token, RunAllStage.CAMERA, RunAllStageOutcome.PASSED, true)
            }
        }

        fun reportStageIssue(
            token: Long,
            issue: RunAllStageOutcome,
        ) {
            if (!isCurrentStage(token) || claimedStageToken != token || _state.value.stageIssue != null) return
            require(issue == RunAllStageOutcome.TIMED_OUT || issue == RunAllStageOutcome.ERROR)
            cancelStageTimeout()
            _state.value = _state.value.copy(stageIssue = issue)
        }

        fun recordSensors(
            token: Long,
            result: Boolean?,
            outcome: RunAllStageOutcome = outcomeFor(result),
        ) {
            finishStage(token, RunAllStage.SENSORS, outcome, result)
        }

        fun recordVibration(
            token: Long,
            result: Boolean?,
            outcome: RunAllStageOutcome = outcomeFor(result),
        ) {
            finishStage(token, RunAllStage.VIBRATION, outcome, result)
        }

        fun recordButtons(
            token: Long,
            result: Boolean?,
            outcome: RunAllStageOutcome = outcomeFor(result),
        ) {
            finishStage(token, RunAllStage.BUTTONS, outcome, result)
        }

        fun recordBiometrics(
            token: Long,
            result: Boolean?,
            outcome: RunAllStageOutcome = outcomeFor(result),
        ) {
            finishStage(token, RunAllStage.BIOMETRICS, outcome, result)
        }

        fun skipStage(token: Long) {
            val stage = _state.value.stage
            if (stage !in _state.value.plan.interactiveStages) return
            finishStage(token, stage, RunAllStageOutcome.SKIPPED, null)
        }

        fun markStageUnavailable(token: Long) {
            val stage = _state.value.stage
            finishStage(token, stage, RunAllStageOutcome.UNAVAILABLE, null)
        }

        fun retryStage(token: Long): Boolean {
            val current = _state.value
            if (!isCurrentStage(token) || current.stage !in current.plan.interactiveStages) return false
            val retryState =
                if (current.stage == RunAllStage.DISPLAY) {
                    current.copy(displayColorIndex = 0, stageIssue = null)
                } else {
                    current.copy(stageIssue = null)
                }
            enterStage(current.stage, retryState)
            return true
        }

        fun interruptRun(reason: RunAllInterruptionReason): Boolean {
            if (_state.value.runStatus != RunAllRunStatus.RUNNING) return false
            cancelStageTimeout()
            claimedStageToken = null
            reportStartedAt = null
            _state.value =
                RunAllTestsState(
                    stageToken = newToken(),
                    lastInterruption = reason,
                )
            return true
        }

        fun completeReport(
            token: Long,
            device: ReportDeviceContext,
            app: ReportAppContext,
            snapshots: List<DiagnosticCategorySnapshot>,
        ) {
            val current = _state.value
            if (!isCurrentStage(token, RunAllStage.RESULTS) || current.report != null) return
            val startedAt = reportStartedAt ?: return

            val report =
                ReportAssembler.assemble(
                    ReportAssemblyRequest(
                        stableId = idProvider.newId(),
                        kind = ReportKind.FULL_CHECK,
                        startedAt = startedAt,
                        completedAt = Instant.ofEpochMilli(clock.currentTimeMillis()),
                        device = device,
                        app = app,
                        snapshots = snapshots,
                    ),
                )
            _state.value =
                current.copy(
                    report = report,
                    runStatus = RunAllRunStatus.COMPLETED,
                    saveStatus = ReportSaveStatus.SAVING,
                )
            persistFrozenReport(report)
        }

        fun retryReportSave() {
            val current = _state.value
            val report = current.report ?: return
            if (current.saveStatus != ReportSaveStatus.FAILED) return
            _state.value = current.copy(saveStatus = ReportSaveStatus.SAVING)
            persistFrozenReport(report)
        }

        private fun finishStage(
            token: Long,
            expectedStage: RunAllStage,
            outcome: RunAllStageOutcome,
            result: Boolean? = null,
        ) {
            if (!isCurrentStage(token, expectedStage) || claimedStageToken != token) return
            cancelStageTimeout()
            claimedStageToken = null
            val current = _state.value
            val updatedManual =
                updateManualResult(
                    current.manualChecks.copy(
                        outcomes = current.manualChecks.outcomes + (expectedStage to outcome),
                    ),
                    expectedStage,
                    result,
                )
            val currentIndex = current.plan.stages.indexOf(expectedStage)
            if (currentIndex < 0) return
            val nextStage = current.plan.stages.getOrNull(currentIndex + 1) ?: RunAllStage.RESULTS
            enterStage(
                nextStage,
                current.copy(manualChecks = updatedManual),
            )
        }

        private fun updateManualResult(
            manual: ManualCheckResults,
            stage: RunAllStage,
            result: Boolean?,
        ): ManualCheckResults =
            when (stage) {
                RunAllStage.DISPLAY -> manual.copy(display = result)
                RunAllStage.AUDIO -> manual.copy(speaker = result)
                RunAllStage.CAMERA -> manual.copy(camera = result)
                RunAllStage.SENSORS -> manual.copy(sensors = result)
                RunAllStage.VIBRATION -> manual.copy(vibration = result)
                RunAllStage.BUTTONS -> manual.copy(buttons = result)
                RunAllStage.BIOMETRICS -> manual.copy(biometrics = result)
                else -> manual
            }

        private fun scheduleTimeout(
            token: Long,
            stage: RunAllStage,
        ) {
            val timeout =
                when (stage) {
                    RunAllStage.AUTOMATIC -> AUTOMATIC_TIMEOUT_MS
                    RunAllStage.DISPLAY -> DISPLAY_TIMEOUT_MS
                    RunAllStage.CAMERA -> CAMERA_TIMEOUT_MS
                    else -> null
                } ?: return
            stageTimeoutJob =
                viewModelScope.launch {
                    delay(timeout)
                    if (stage == RunAllStage.CAMERA) {
                        reportStageIssue(token, RunAllStageOutcome.TIMED_OUT)
                    } else {
                        finishStage(token, stage, RunAllStageOutcome.TIMED_OUT)
                    }
                }
        }

        private fun enterStage(
            stage: RunAllStage,
            baseState: RunAllTestsState = _state.value,
        ) {
            cancelStageTimeout()
            claimedStageToken = null
            _state.value =
                baseState.copy(
                    stage = stage,
                    stageToken = newToken(),
                    stageIssue = null,
                )
        }

        private fun isCurrentStage(
            token: Long,
            expectedStage: RunAllStage = _state.value.stage,
        ): Boolean {
            val current = _state.value
            return current.runStatus == RunAllRunStatus.RUNNING &&
                current.stageToken == token &&
                current.stage == expectedStage
        }

        private fun cancelStageTimeout() {
            stageTimeoutJob?.cancel()
            stageTimeoutJob = null
        }

        private fun persistFrozenReport(report: DiagnosticReport) {
            if (reportSaveJob?.isActive == true) return
            reportSaveJob =
                viewModelScope.launch {
                    val saved =
                        try {
                            reportRepository.insert(report)
                            true
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: Exception) {
                            val existing = runCatching { reportRepository.getById(report.stableId) }.getOrNull()
                            existing is ReportLoadResult.Available && existing.report == report
                        }
                    if (_state.value.report?.stableId == report.stableId) {
                        _state.value =
                            _state.value.copy(
                                saveStatus =
                                    if (saved) {
                                        ReportSaveStatus.SAVED
                                    } else {
                                        ReportSaveStatus.FAILED
                                    },
                            )
                    }
                }
        }

        private fun newToken(): Long = ++nextStageToken

        override fun onCleared() {
            cancelStageTimeout()
            reportSaveJob?.cancel()
            super.onCleared()
        }

        companion object {
            const val AUTOMATIC_TIMEOUT_MS = 70_000L
            const val DISPLAY_TIMEOUT_MS = 30_000L
            const val CAMERA_TIMEOUT_MS = 12_000L

            private fun outcomeFor(result: Boolean?): RunAllStageOutcome =
                when (result) {
                    true -> RunAllStageOutcome.PASSED
                    false -> RunAllStageOutcome.FAILED
                    null -> RunAllStageOutcome.SKIPPED
                }
        }
    }
