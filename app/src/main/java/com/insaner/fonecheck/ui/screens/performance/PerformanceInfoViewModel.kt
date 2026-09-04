package com.insaner.fonecheck.ui.screens.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.di.IoDispatcher
import com.insaner.fonecheck.domain.model.PerformanceBenchmarkResult
import com.insaner.fonecheck.domain.model.PerformanceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

enum class BenchmarkPhase {
    IDLE,
    RUNNING,
    COMPLETED,
    CANCELLED,
    ERROR,
}

data class PerformanceInfoState(
    val info: PerformanceInfo? = null,
    val isInfoLoading: Boolean = false,
    val infoError: String? = null,
    val benchmarkPhase: BenchmarkPhase = BenchmarkPhase.IDLE,
    val benchmarkResult: PerformanceBenchmarkResult? = null,
    val benchmarkError: String? = null,
)

@HiltViewModel
class PerformanceInfoViewModel
    @Inject
    constructor(
        private val performanceInfoProvider: PerformanceInfoProvider,
        private val benchmarkRunner: PerformanceBenchmarkRunner,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _state = MutableStateFlow(PerformanceInfoState(isInfoLoading = true))
        val state: StateFlow<PerformanceInfoState> = _state.asStateFlow()
        private var infoJob: Job? = null
        private var benchmarkJob: Job? = null

        init {
            refreshInfo()
        }

        fun refreshInfo() {
            infoJob?.cancel()
            _state.value = _state.value.copy(isInfoLoading = true, infoError = null)
            infoJob =
                viewModelScope.launch {
                    try {
                        val info = withContext(ioDispatcher) { performanceInfoProvider.capture() }
                        _state.value = _state.value.copy(info = info, isInfoLoading = false)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        _state.value = _state.value.copy(isInfoLoading = false, infoError = INFO_ERROR)
                    }
                }
        }

        fun cancelInfoCapture() {
            infoJob?.cancel()
            infoJob = null
        }

        @Suppress("kotlin:S6311") // The runner performs CPU work and intentionally inherits this background context.
        fun startBenchmark() {
            if (_state.value.benchmarkPhase == BenchmarkPhase.RUNNING) return
            _state.value =
                _state.value.copy(
                    benchmarkPhase = BenchmarkPhase.RUNNING,
                    benchmarkResult = null,
                    benchmarkError = null,
                )
            benchmarkJob =
                viewModelScope.launch {
                    try {
                        val result =
                            withTimeout(BENCHMARK_TIMEOUT_MS) {
                                withContext(ioDispatcher) { benchmarkRunner.run() }
                            }
                        _state.value =
                            _state.value.copy(
                                benchmarkPhase = BenchmarkPhase.COMPLETED,
                                benchmarkResult = result,
                            )
                    } catch (_: TimeoutCancellationException) {
                        _state.value =
                            _state.value.copy(
                                benchmarkPhase = BenchmarkPhase.ERROR,
                                benchmarkError = BENCHMARK_TIMEOUT,
                            )
                    } catch (_: CancellationException) {
                        if (_state.value.benchmarkPhase == BenchmarkPhase.RUNNING) {
                            _state.value = _state.value.copy(benchmarkPhase = BenchmarkPhase.CANCELLED)
                        }
                    } catch (_: Exception) {
                        _state.value =
                            _state.value.copy(
                                benchmarkPhase = BenchmarkPhase.ERROR,
                                benchmarkError = BENCHMARK_ERROR,
                            )
                    }
                }
        }

        fun cancelBenchmark() {
            if (_state.value.benchmarkPhase != BenchmarkPhase.RUNNING) return
            _state.value = _state.value.copy(benchmarkPhase = BenchmarkPhase.CANCELLED)
            benchmarkJob?.cancel()
        }

        private companion object {
            const val BENCHMARK_TIMEOUT_MS = 5_000L
            const val INFO_ERROR = "performance_info_failed"
            const val BENCHMARK_TIMEOUT = "benchmark_timeout"
            const val BENCHMARK_ERROR = "benchmark_failed"
        }
    }
