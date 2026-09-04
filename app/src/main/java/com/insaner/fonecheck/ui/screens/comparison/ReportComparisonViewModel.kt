package com.insaner.fonecheck.ui.screens.comparison

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.data.repository.ReportReadFailure
import com.insaner.fonecheck.data.repository.ReportRepository
import com.insaner.fonecheck.domain.comparison.ReportComparison
import com.insaner.fonecheck.domain.comparison.ReportComparisonEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReportComparisonState {
    data object Loading : ReportComparisonState

    data class Content(
        val comparison: ReportComparison,
    ) : ReportComparisonState

    data class Issues(
        val first: ComparisonReportIssue?,
        val second: ComparisonReportIssue?,
    ) : ReportComparisonState

    data object Error : ReportComparisonState
}

enum class ComparisonReportIssue {
    NOT_FOUND,
    UNSUPPORTED_SCHEMA_VERSION,
    CORRUPT_DATA,
}

@HiltViewModel
class ReportComparisonViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val reportRepository: ReportRepository,
    ) : ViewModel() {
        private val firstReportId = savedStateHandle.get<String>(FIRST_REPORT_ID_ARGUMENT).orEmpty()
        private val secondReportId = savedStateHandle.get<String>(SECOND_REPORT_ID_ARGUMENT).orEmpty()
        private val _state = MutableStateFlow<ReportComparisonState>(ReportComparisonState.Loading)
        val state: StateFlow<ReportComparisonState> = _state.asStateFlow()
        private var loadJob: Job? = null

        init {
            load()
        }

        fun retry() {
            load()
        }

        private fun load() {
            loadJob?.cancel()
            _state.value = ReportComparisonState.Loading
            if (firstReportId.isBlank() || secondReportId.isBlank()) {
                _state.value =
                    ReportComparisonState.Issues(
                        first = ComparisonReportIssue.NOT_FOUND.takeIf { firstReportId.isBlank() },
                        second = ComparisonReportIssue.NOT_FOUND.takeIf { secondReportId.isBlank() },
                    )
                return
            }
            loadJob =
                viewModelScope.launch {
                    try {
                        val result = reportRepository.getForComparison(firstReportId, secondReportId)
                        val first = result.first
                        val second = result.second
                        val firstIssue = first.comparisonIssue()
                        val secondIssue = second.comparisonIssue()
                        _state.value =
                            when {
                                firstIssue != null || secondIssue != null ->
                                    ReportComparisonState.Issues(firstIssue, secondIssue)

                                first is ReportLoadResult.Available && second is ReportLoadResult.Available ->
                                    ReportComparisonState.Content(
                                        ReportComparisonEngine.compare(first.report, second.report),
                                    )

                                else -> ReportComparisonState.Error
                            }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        _state.value = ReportComparisonState.Error
                    }
                }
        }

        private companion object {
            const val FIRST_REPORT_ID_ARGUMENT = "firstReportId"
            const val SECOND_REPORT_ID_ARGUMENT = "secondReportId"
        }
    }

private fun ReportLoadResult.comparisonIssue(): ComparisonReportIssue? =
    when (this) {
        is ReportLoadResult.Available -> null
        ReportLoadResult.NotFound -> ComparisonReportIssue.NOT_FOUND
        is ReportLoadResult.Unavailable ->
            when (reason) {
                ReportReadFailure.UNSUPPORTED_SCHEMA_VERSION -> ComparisonReportIssue.UNSUPPORTED_SCHEMA_VERSION
                ReportReadFailure.CORRUPT_DATA -> ComparisonReportIssue.CORRUPT_DATA
            }
    }
