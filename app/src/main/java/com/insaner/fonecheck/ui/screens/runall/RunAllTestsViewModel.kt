package com.insaner.fonecheck.ui.screens.runall

import androidx.lifecycle.ViewModel
import com.insaner.fonecheck.domain.model.CategoryTestResult
import com.insaner.fonecheck.domain.model.DiagnosticCategorySnapshot
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportAssembler
import com.insaner.fonecheck.domain.model.ReportAssemblyRequest
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.TestSession
import com.insaner.fonecheck.domain.model.TestStatus
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

enum class RunAllStage {
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
    val stage: RunAllStage = RunAllStage.PERMISSIONS,
    val permissions: RunAllPermissions = RunAllPermissions(),
    val manualChecks: ManualCheckResults = ManualCheckResults(),
    val displayColorIndex: Int = 0,
    val session: TestSession? = null,
    val report: DiagnosticReport? = null,
)

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

        fun onPermissionsResolved(permissions: RunAllPermissions) {
            _state.value =
                _state.value.copy(
                    permissions = permissions,
                    stage = RunAllStage.AUTOMATIC,
                )
        }

        fun onAutomaticChecksComplete() {
            moveTo(RunAllStage.DISPLAY)
        }

        fun nextDisplayColor(lastColorIndex: Int) {
            val current = _state.value.displayColorIndex
            if (current < lastColorIndex) {
                _state.value = _state.value.copy(displayColorIndex = current + 1)
            }
        }

        fun recordDisplay(result: Boolean?) {
            recordManualCheck(RunAllStage.AUDIO) { it.copy(display = result) }
        }

        fun recordSpeaker(result: Boolean?) {
            recordManualCheck(RunAllStage.CAMERA) { it.copy(speaker = result) }
        }

        fun recordCamera(result: Boolean?) {
            recordManualCheck(RunAllStage.SENSORS) { it.copy(camera = result) }
        }

        fun recordSensors(result: Boolean?) {
            recordManualCheck(RunAllStage.VIBRATION) { it.copy(sensors = result) }
        }

        fun recordVibration(result: Boolean?) {
            recordManualCheck(RunAllStage.BUTTONS) { it.copy(vibration = result) }
        }

        fun recordButtons(result: Boolean?) {
            recordManualCheck(RunAllStage.BIOMETRICS) { it.copy(buttons = result) }
        }

        fun recordBiometrics(result: Boolean?) {
            recordManualCheck(RunAllStage.RESULTS) { it.copy(biometrics = result) }
        }

        fun completeSession(
            deviceInfo: DeviceInfo,
            categories: List<CategoryTestResult>,
        ) {
            if (_state.value.session != null) return

            _state.value =
                _state.value.copy(
                    session =
                        TestSession(
                            id = idProvider.newId(),
                            timestamp = clock.currentTimeMillis(),
                            deviceInfo = deviceInfo,
                            categories = categories,
                            overallScore = calculateOverallScore(categories),
                        ),
                )
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
            nextStage: RunAllStage,
            update: (ManualCheckResults) -> ManualCheckResults,
        ) {
            _state.value =
                _state.value.copy(
                    manualChecks = update(_state.value.manualChecks),
                    stage = nextStage,
                )
        }

        private fun moveTo(stage: RunAllStage) {
            _state.value = _state.value.copy(stage = stage)
        }

        private fun calculateOverallScore(categories: List<CategoryTestResult>): Int {
            val scores =
                categories.mapNotNull { category ->
                    when (category.status) {
                        TestStatus.Pass, is TestStatus.Info -> 100
                        is TestStatus.Warning -> 65
                        is TestStatus.Fail -> 0
                        TestStatus.NotAvailable, TestStatus.NotTested -> null
                    }
                }
            return if (scores.isEmpty()) 0 else scores.average().toInt()
        }
    }
