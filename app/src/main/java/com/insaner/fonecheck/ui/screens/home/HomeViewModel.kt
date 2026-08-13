package com.insaner.fonecheck.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.data.repository.ReportReadFailure
import com.insaner.fonecheck.data.repository.ReportRepository
import com.insaner.fonecheck.data.repository.SavedReportSummary
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.ReportKind
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LatestFullCheckState {
    data object Loading : LatestFullCheckState

    data object Empty : LatestFullCheckState

    data class Available(
        val report: DiagnosticReport,
    ) : LatestFullCheckState

    data class Unavailable(
        val reason: ReportReadFailure,
    ) : LatestFullCheckState

    data object Error : LatestFullCheckState
}

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val reportRepository: ReportRepository,
    ) : ViewModel() {
        private val _latestFullCheck = MutableStateFlow<LatestFullCheckState>(LatestFullCheckState.Loading)
        val latestFullCheck: StateFlow<LatestFullCheckState> = _latestFullCheck.asStateFlow()

        private var reportsJob: Job? = null

        init {
            observeLatestFullCheck()
        }

        fun retry() {
            observeLatestFullCheck()
        }

        private fun observeLatestFullCheck() {
            reportsJob?.cancel()
            _latestFullCheck.value = LatestFullCheckState.Loading
            reportsJob =
                viewModelScope.launch {
                    try {
                        reportRepository.observeSummaries().collectLatest { summaries ->
                            _latestFullCheck.value = loadLatestFullCheck(summaries)
                        }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        _latestFullCheck.value = LatestFullCheckState.Error
                    }
                }
        }

        private suspend fun loadLatestFullCheck(summaries: List<SavedReportSummary>): LatestFullCheckState {
            val latest =
                summaries.firstOrNull(SavedReportSummary::isFullCheckCandidate)
                    ?: return LatestFullCheckState.Empty
            latest.unavailableReason?.let { return LatestFullCheckState.Unavailable(it) }

            return when (val loaded = reportRepository.getById(latest.stableId)) {
                is ReportLoadResult.Available ->
                    if (loaded.report.kind == ReportKind.FULL_CHECK) {
                        LatestFullCheckState.Available(loaded.report)
                    } else {
                        LatestFullCheckState.Error
                    }
                ReportLoadResult.NotFound -> LatestFullCheckState.Empty
                is ReportLoadResult.Unavailable -> LatestFullCheckState.Unavailable(loaded.reason)
            }
        }
    }

private fun SavedReportSummary.isFullCheckCandidate(): Boolean =
    kind == ReportKind.FULL_CHECK || (kind == null && categoryId == null)
