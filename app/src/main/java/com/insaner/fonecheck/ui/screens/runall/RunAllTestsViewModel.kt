package com.insaner.fonecheck.ui.screens.runall

import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

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
)

data class RunAllTestsState(
    val stage: RunAllStage = RunAllStage.PREFLIGHT,
    val permissions: RunAllPermissions = RunAllPermissions(),
    val selections: RunAllSelections = RunAllSelections(),
    val hardware: RunAllHardwareProfile = RunAllHardwareProfile(),
    val plan: RunAllPlan = RunAllPlan.EMPTY,
    val manualChecks: ManualCheckResults = ManualCheckResults(),
    val displayColorIndex: Int = 0,
    val report: DiagnosticReport? = null,
) {
    val progress: RunAllProgress?
        get() =
            if (stage in plan.interactiveStages) {
                plan.progressFor(stage)
            } else {
                null
            }
}

@HiltViewModel
class RunAllTestsViewModel
    @Inject
    constructor(
        private val clock: EpochMillisClock,
        private val idProvider: IdProvider,
    ) : ViewModel() {
        private val _state = MutableStateFlow(RunAllTestsState())
        val state: StateFlow<RunAllTestsState> = _state
        private val reportStartedAt = Instant.ofEpochMilli(clock.currentTimeMillis())

        fun updateSelections(selections: RunAllSelections) {
            if (_state.value.stage != RunAllStage.PREFLIGHT) return
            _state.value = _state.value.copy(selections = selections)
        }

        fun onPreflightAccepted(
            selections: RunAllSelections,
            hardware: RunAllHardwareProfile,
        ) {
            _state.value =
                _state.value.copy(
                    stage = RunAllStage.PERMISSIONS,
                    selections = selections,
                    hardware = hardware,
                )
        }

        fun onPermissionsResolved(permissions: RunAllPermissions) {
            val plan =
                RunAllStagePlanner.plan(
                    hardware = _state.value.hardware,
                    permissions = permissions,
                    selections = _state.value.selections,
                )
            _state.value =
                _state.value.copy(
                    permissions = permissions,
                    plan = plan,
                    stage = plan.stages.firstOrNull() ?: RunAllStage.RESULTS,
                )
        }

        fun onAutomaticChecksComplete() {
            advance()
        }

        fun nextDisplayColor(lastColorIndex: Int) {
            val current = _state.value.displayColorIndex
            if (current < lastColorIndex) {
                _state.value = _state.value.copy(displayColorIndex = current + 1)
            }
        }

        fun recordDisplay(result: Boolean?) {
            recordManualCheck { it.copy(display = result) }
        }

        fun recordSpeaker(result: Boolean?) {
            recordManualCheck { it.copy(speaker = result) }
        }

        fun recordCamera(result: Boolean?) {
            recordManualCheck { it.copy(camera = result) }
        }

        fun recordSensors(result: Boolean?) {
            recordManualCheck { it.copy(sensors = result) }
        }

        fun recordVibration(result: Boolean?) {
            recordManualCheck { it.copy(vibration = result) }
        }

        fun recordButtons(result: Boolean?) {
            recordManualCheck { it.copy(buttons = result) }
        }

        fun recordBiometrics(result: Boolean?) {
            recordManualCheck { it.copy(biometrics = result) }
        }

        fun completeReport(
            device: ReportDeviceContext,
            app: ReportAppContext,
            snapshots: List<DiagnosticCategorySnapshot>,
        ) {
            if (_state.value.report != null) return

            val report =
                ReportAssembler.assemble(
                    ReportAssemblyRequest(
                        stableId = idProvider.newId(),
                        kind = ReportKind.FULL_CHECK,
                        startedAt = reportStartedAt,
                        completedAt = Instant.ofEpochMilli(clock.currentTimeMillis()),
                        device = device,
                        app = app,
                        snapshots = snapshots,
                    ),
                )
            _state.value = _state.value.copy(report = report)
        }

        private fun recordManualCheck(
            update: (ManualCheckResults) -> ManualCheckResults,
        ) {
            _state.value =
                _state.value.copy(
                    manualChecks = update(_state.value.manualChecks),
                )
            advance()
        }

        private fun advance() {
            val currentIndex = _state.value.plan.stages.indexOf(_state.value.stage)
            if (currentIndex < 0) return
            val nextStage = _state.value.plan.stages.getOrNull(currentIndex + 1) ?: RunAllStage.RESULTS
            _state.value = _state.value.copy(stage = nextStage)
        }

    }
