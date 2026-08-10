package com.insaner.fonecheck.ui.screens.export

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.data.repository.ReportReadFailure
import com.insaner.fonecheck.data.repository.ReportRepository
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.export.ExportedReport
import com.insaner.fonecheck.export.ReportExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReportExportState {
    data object Loading : ReportExportState

    data class Ready(
        val report: DiagnosticReport,
        val isGenerating: Boolean = false,
        val error: String? = null,
        val shareRequest: ExportedReport? = null,
    ) : ReportExportState

    data object NotFound : ReportExportState

    data class Unavailable(
        val reason: ReportReadFailure,
    ) : ReportExportState

    data object Error : ReportExportState
}

@HiltViewModel
class ReportExportViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val reportRepository: ReportRepository,
        private val reportExporter: ReportExporter,
    ) : ViewModel() {
        private val reportId = savedStateHandle.get<String>(REPORT_ID_ARGUMENT).orEmpty()
        private val _state = MutableStateFlow<ReportExportState>(ReportExportState.Loading)
        val state: StateFlow<ReportExportState> = _state.asStateFlow()
        private var loadJob: Job? = null
        private var exportJob: Job? = null

        init {
            load()
        }

        fun retryLoad() {
            load()
        }

        fun exportPdf() {
            export(PDF_EXPORT_FAILED, reportExporter::exportPdf)
        }

        fun exportJson() {
            export(JSON_EXPORT_FAILED, reportExporter::exportJson)
        }

        private fun export(
            errorCode: String,
            operation: suspend (DiagnosticReport) -> ExportedReport,
        ) {
            val ready = _state.value as? ReportExportState.Ready ?: return
            if (ready.isGenerating) return
            _state.value = ready.copy(isGenerating = true, error = null, shareRequest = null)
            exportJob =
                viewModelScope.launch {
                    try {
                        val exported = operation(ready.report)
                        _state.value = ready.copy(shareRequest = exported)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        _state.value = ready.copy(error = errorCode)
                    }
                }
        }

        fun consumeShareRequest() {
            val ready = _state.value as? ReportExportState.Ready ?: return
            _state.value = ready.copy(shareRequest = null)
        }

        private fun load() {
            loadJob?.cancel()
            exportJob?.cancel()
            _state.value = ReportExportState.Loading
            if (reportId.isBlank()) {
                _state.value = ReportExportState.NotFound
                return
            }
            loadJob =
                viewModelScope.launch {
                    try {
                        _state.value =
                            when (val result = reportRepository.getById(reportId)) {
                                is ReportLoadResult.Available -> ReportExportState.Ready(result.report)
                                ReportLoadResult.NotFound -> ReportExportState.NotFound
                                is ReportLoadResult.Unavailable -> ReportExportState.Unavailable(result.reason)
                            }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        _state.value = ReportExportState.Error
                    }
                }
        }

        private companion object {
            const val REPORT_ID_ARGUMENT = "reportId"
            const val PDF_EXPORT_FAILED = "pdf_export_failed"
            const val JSON_EXPORT_FAILED = "json_export_failed"
        }
    }
