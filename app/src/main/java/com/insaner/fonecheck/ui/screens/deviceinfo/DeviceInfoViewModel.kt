package com.insaner.fonecheck.ui.screens.deviceinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.di.IoDispatcher
import com.insaner.fonecheck.domain.model.DeviceInfo
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

data class DeviceInfoState(
    val info: DeviceInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DeviceInfoViewModel
    @Inject
    constructor(
        private val deviceInfoProvider: DeviceInfoProvider,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _state = MutableStateFlow(DeviceInfoState(isLoading = true))
        val state: StateFlow<DeviceInfoState> = _state.asStateFlow()
        private var refreshJob: Job? = null

        init {
            refresh()
        }

        fun refresh() {
            refreshJob?.cancel()
            _state.value = _state.value.copy(isLoading = true, error = null)
            refreshJob =
                viewModelScope.launch {
                    try {
                        val info = withContext(ioDispatcher) { deviceInfoProvider.capture() }
                        _state.value = DeviceInfoState(info = info)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        _state.value =
                            _state.value.copy(
                                isLoading = false,
                                error = CAPTURE_ERROR,
                            )
                    }
                }
        }

        private companion object {
            const val CAPTURE_ERROR = "device_capture_failed"
        }
    }
