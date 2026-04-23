package com.vylexai.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vylexai.app.domain.device.DeviceReport
import com.vylexai.app.domain.device.DeviceScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DeviceScanState {
    data object Scanning : DeviceScanState
    data class Ready(val report: DeviceReport) : DeviceScanState
    data class Error(val message: String) : DeviceScanState
}

@HiltViewModel
class DeviceScanViewModel @Inject constructor(
    private val scanner: DeviceScanner
) : ViewModel() {

    private val _state = MutableStateFlow<DeviceScanState>(DeviceScanState.Scanning)
    val state: StateFlow<DeviceScanState> = _state.asStateFlow()

    init {
        rescan()
    }

    fun rescan() {
        _state.value = DeviceScanState.Scanning
        viewModelScope.launch {
            _state.value = runCatching { DeviceScanState.Ready(scanner.scan()) }
                .getOrElse { DeviceScanState.Error(it.message ?: "scan_failed") }
        }
    }
}
