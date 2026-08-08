package com.insaner.fonecheck.ui.screens.comparison

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.data.repository.ReportReadFailure
import com.insaner.fonecheck.data.repository.ReportRepository
import com.insaner.fonecheck.di.IoDispatcher
import com.insaner.fonecheck.domain.comparison.ReportComparison
import com.insaner.fonecheck.domain.comparison.ReportComparisonEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface ReportComparisonState {
    data object Loading : ReportComparisonState

    data class Content(
        val comparison: ReportComparison,
    ) : ReportComparisonState

    data object NotFound : ReportComparisonState

    data class Unavailable(
        val reasons: Set<ReportReadFailure>,
    ) : ReportComparisonState

    data object Error : ReportComparisonState
}

@HiltViewModel
class ReportComparisonViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val reportRepository: ReportRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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
                _state.value = ReportComparisonState.NotFound
                return
            }
            loadJob =
                viewModelScope.launch {
                    try {
                        val result =
                            withContext(ioDispatcher) {
                                reportRepository.getForComparison(firstReportId, secondReportId)
                            }
                        val first = result.first
                        val second = result.second
                        _state.value =
                            when {
                                first is ReportLoadResult.NotFound || second is ReportLoadResult.NotFound ->
                                    ReportComparisonState.NotFound

                                first is ReportLoadResult.Unavailable || second is ReportLoadResult.Unavailable ->
                                    ReportComparisonState.Unavailable(
                                        listOfNotNull(
                                            (first as? ReportLoadResult.Unavailable)?.reason,
                                            (second as? ReportLoadResult.Unavailable)?.reason,
                                        ).toSet(),
                                    )

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
