package com.vylexai.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vylexai.app.data.worker.WorkerController
import com.vylexai.app.data.worker.WorkerSnapshot
import com.vylexai.app.data.worker.WorkerStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProviderWorkerViewModel @Inject constructor(
    private val controller: WorkerController,
    store: WorkerStore
) : ViewModel() {

    val state: StateFlow<WorkerSnapshot> = store.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS),
        initialValue = WorkerSnapshot(
            enabled = false,
            tasksCompletedToday = 0,
            bsaiEarnedToday = 0.0,
            lastStartedAt = 0L,
            lastHeartbeatAt = 0L,
            lastError = null
        )
    )

    fun setActive(active: Boolean) {
        viewModelScope.launch { if (active) controller.start() else controller.stop() }
    }

    private companion object {
        const val SUBSCRIBE_TIMEOUT_MS = 5_000L
    }
}
