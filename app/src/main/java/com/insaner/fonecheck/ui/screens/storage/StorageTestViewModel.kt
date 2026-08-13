package com.insaner.fonecheck.ui.screens.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.di.IoDispatcher
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

enum class StorageBenchmarkPhase {
    IDLE,
    RUNNING,
    COMPLETED,
    NOT_RUN,
    SKIPPED,
    CANCELLED,
    ERROR,
}

data class StorageTestState(
    val info: StorageInfo? = null,
    val isInfoLoading: Boolean = true,
    val infoError: String? = null,
    val benchmarkPhase: StorageBenchmarkPhase = StorageBenchmarkPhase.IDLE,
    val benchmarkResult: StorageBenchmarkResult? = null,
    val benchmarkError: StorageBenchmarkErrorCode? = null,
)

@HiltViewModel
class StorageTestViewModel
    @Inject
    constructor(
        private val infoProvider: StorageInfoProvider,
        private val benchmarkRunner: StorageBenchmarkRunner,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _state = MutableStateFlow(StorageTestState())
        val state: StateFlow<StorageTestState> = _state.asStateFlow()

        private var infoJob: Job? = null
        private var benchmarkJob: Job? = null
        private var skipRequested = false

        init {
            refreshInfo()
        }

        fun refreshInfo() {
            infoJob?.cancel()
            _state.value = _state.value.copy(isInfoLoading = true, infoError = null)
            infoJob =
                viewModelScope.launch {
                    val info =
                        try {
                            withContext(ioDispatcher) { infoProvider.capture() }
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: Exception) {
                            _state.value = _state.value.copy(isInfoLoading = false, infoError = INFO_CAPTURE_FAILED)
                            return@launch
                        }
                    _state.value = _state.value.copy(info = info, isInfoLoading = false)
                }
        }

        // The runner performs blocking file I/O and intentionally inherits this background context.
        @Suppress("kotlin:S3776", "kotlin:S6311")
        fun startBenchmark() {
            if (_state.value.benchmarkPhase == StorageBenchmarkPhase.RUNNING) return
            skipRequested = false
            _state.value =
                _state.value.copy(
                    benchmarkPhase = StorageBenchmarkPhase.RUNNING,
                    benchmarkResult = null,
                    benchmarkError = null,
                )
            benchmarkJob =
                viewModelScope.launch {
                    try {
                        val result =
                            withTimeout(BENCHMARK_TIMEOUT_MILLIS) {
                                withContext(ioDispatcher) { benchmarkRunner.run() }
                            }
                        _state.value =
                            _state.value.copy(
                                benchmarkPhase = phaseFor(result),
                                benchmarkResult = result,
                                benchmarkError = result.error,
                            )
                        refreshInfo()
                    } catch (_: TimeoutCancellationException) {
                        if (_state.value.benchmarkPhase == StorageBenchmarkPhase.RUNNING) {
                            _state.value =
                                _state.value.copy(
                                    benchmarkPhase = StorageBenchmarkPhase.ERROR,
                                    benchmarkError = StorageBenchmarkErrorCode.IO_ERROR,
                                )
                        }
                    } catch (_: CancellationException) {
                        if (_state.value.benchmarkPhase == StorageBenchmarkPhase.RUNNING) {
                            _state.value =
                                _state.value.copy(
                                    benchmarkPhase =
                                        if (skipRequested) {
                                            StorageBenchmarkPhase.SKIPPED
                                        } else {
                                            StorageBenchmarkPhase.CANCELLED
                                        },
                                )
                        }
                    } catch (_: Exception) {
                        _state.value =
                            _state.value.copy(
                                benchmarkPhase = StorageBenchmarkPhase.ERROR,
                                benchmarkError = StorageBenchmarkErrorCode.IO_ERROR,
                            )
                    }
                }
        }

        fun cancelBenchmark() {
            if (_state.value.benchmarkPhase != StorageBenchmarkPhase.RUNNING) return
            skipRequested = false
            benchmarkJob?.cancel()
        }

        fun skipBenchmark() {
            if (_state.value.benchmarkPhase == StorageBenchmarkPhase.RUNNING) {
                skipRequested = true
                benchmarkJob?.cancel()
            } else {
                _state.value =
                    _state.value.copy(
                        benchmarkPhase = StorageBenchmarkPhase.SKIPPED,
                        benchmarkResult = null,
                        benchmarkError = null,
                    )
            }
        }

        override fun onCleared() {
            benchmarkJob?.cancel()
            infoJob?.cancel()
        }

        private fun phaseFor(result: StorageBenchmarkResult): StorageBenchmarkPhase =
            when (result.error) {
                null -> StorageBenchmarkPhase.COMPLETED
                StorageBenchmarkErrorCode.INSUFFICIENT_SPACE -> StorageBenchmarkPhase.NOT_RUN
                StorageBenchmarkErrorCode.IO_ERROR,
                StorageBenchmarkErrorCode.DATA_MISMATCH,
                StorageBenchmarkErrorCode.CLEANUP_FAILED,
                -> StorageBenchmarkPhase.ERROR
            }

        private companion object {
            const val BENCHMARK_TIMEOUT_MILLIS = 45_000L
            const val INFO_CAPTURE_FAILED = "storage_info_failed"
        }
    }
