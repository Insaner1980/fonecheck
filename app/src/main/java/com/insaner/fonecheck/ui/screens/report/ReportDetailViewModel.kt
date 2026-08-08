package com.insaner.fonecheck.ui.screens.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.data.repository.ReportReadFailure
import com.insaner.fonecheck.data.repository.ReportRepository
import com.insaner.fonecheck.di.IoDispatcher
import com.insaner.fonecheck.domain.model.DiagnosticReport
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ReportDetailState {
    data object Loading : ReportDetailState

    data class Content(
        val report: DiagnosticReport,
    ) : ReportDetailState

    data object NotFound : ReportDetailState

    data class Unavailable(
        val reason: ReportReadFailure,
    ) : ReportDetailState

    data object Error : ReportDetailState
}

@HiltViewModel
class ReportDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val reportRepository: ReportRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val reportId = savedStateHandle.get<String>(REPORT_ID_ARGUMENT).orEmpty()
        private val _state = MutableStateFlow<ReportDetailState>(ReportDetailState.Loading)
        val state: StateFlow<ReportDetailState> = _state.asStateFlow()
        private var loadJob: Job? = null

        init {
            load()
        }

        fun retry() {
            load()
        }

        private fun load() {
            loadJob?.cancel()
            _state.value = ReportDetailState.Loading
            if (reportId.isBlank()) {
                _state.value = ReportDetailState.NotFound
                return
            }
            loadJob =
                viewModelScope.launch {
                    try {
                        _state.value =
                            when (val result = withContext(ioDispatcher) { reportRepository.getById(reportId) }) {
                                is ReportLoadResult.Available -> ReportDetailState.Content(result.report)
                                ReportLoadResult.NotFound -> ReportDetailState.NotFound
                                is ReportLoadResult.Unavailable -> ReportDetailState.Unavailable(result.reason)
                            }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        _state.value = ReportDetailState.Error
                    }
                }
        }

        private companion object {
            const val REPORT_ID_ARGUMENT = "reportId"
        }
    }
