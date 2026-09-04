package com.insaner.fonecheck.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.data.repository.ReportRepository
import com.insaner.fonecheck.data.repository.SavedReportSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryState(
    val reports: List<SavedReportSummary> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val deletingReportIds: Set<String> = emptySet(),
)

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val reportRepository: ReportRepository,
    ) : ViewModel() {
        private val _state = MutableStateFlow(HistoryState())
        val state: StateFlow<HistoryState> = _state.asStateFlow()
        private var observeJob: Job? = null

        init {
            observeReports()
        }

        fun retry() {
            observeReports()
        }

        fun delete(reportId: String) {
            if (reportId in _state.value.deletingReportIds) return
            _state.update {
                it.copy(
                    error = null,
                    deletingReportIds = it.deletingReportIds + reportId,
                )
            }
            viewModelScope.launch {
                try {
                    reportRepository.delete(reportId)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    _state.update { it.copy(error = DELETE_ERROR) }
                } finally {
                    _state.update { it.copy(deletingReportIds = it.deletingReportIds - reportId) }
                }
            }
        }

        private fun observeReports() {
            observeJob?.cancel()
            _state.update { it.copy(isLoading = true, error = null) }
            observeJob =
                viewModelScope.launch {
                    try {
                        reportRepository.observeSummaries().collect { reports ->
                            _state.update {
                                it.copy(
                                    reports = reports.sortedWith(REPORT_ORDER),
                                    isLoading = false,
                                    error = null,
                                )
                            }
                        }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = LOAD_ERROR,
                            )
                        }
                    }
                }
        }

        private companion object {
            const val LOAD_ERROR = "history_load_failed"
            const val DELETE_ERROR = "history_delete_failed"
            val REPORT_ORDER =
                compareByDescending<SavedReportSummary>(SavedReportSummary::completedAt)
                    .thenByDescending(SavedReportSummary::stableId)
        }
    }
