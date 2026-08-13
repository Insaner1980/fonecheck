package com.insaner.fonecheck.ui.screens.simtelephony

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.di.IoDispatcher
import com.insaner.fonecheck.domain.model.SimTelephonyInfo
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

data class SimTelephonyState(
    val info: SimTelephonyInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SimTelephonyViewModel
    @Inject
    constructor(
        private val provider: SimTelephonyProvider,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _state = MutableStateFlow(SimTelephonyState(isLoading = true))
        val state: StateFlow<SimTelephonyState> = _state.asStateFlow()
        private var refreshJob: Job? = null

        init {
            beginRefresh()
        }

        fun refresh() {
            beginRefresh()
        }

        private fun beginRefresh() {
            refreshJob?.cancel()
            _state.value = _state.value.copy(isLoading = true, error = null)
            refreshJob =
                viewModelScope.launch {
                    val info =
                        try {
                            withContext(ioDispatcher) { provider.capture() }
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: Exception) {
                            _state.value = _state.value.copy(isLoading = false, error = CAPTURE_ERROR)
                            return@launch
                        }
                    _state.value = SimTelephonyState(info = info)
                }
        }

        private companion object {
            const val CAPTURE_ERROR = "sim_capture_failed"
        }
    }
